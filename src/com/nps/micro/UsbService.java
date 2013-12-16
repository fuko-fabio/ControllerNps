package com.nps.micro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.nps.architecture.ThreadPriority;
import com.nps.architecture.Sequence.Group;
import com.nps.micro.model.TestsViewModel;
import com.nps.storage.ExternalFile;
import com.nps.storage.ExternalStorageException;
import com.nps.storage.TestResults;
import com.nps.test.Scenario;
import com.nps.test.ScenariosGenerator;
import com.nps.usb.UsbGateException;
import com.nps.usb.microcontroller.Microcontroller;
import com.nps.usb.microcontroller.MicrocontrollerException;
import com.nps.usb.microcontroller.Packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class UsbService extends Service {

    private static final String TAG = "UsbService";
    
    private NotificationManager notificationManager;
    private int NOTIFICATION = R.string.local_service_notification;

    private ExecutorService executorService;
    @SuppressWarnings("rawtypes")
    private List<Future> futures = new ArrayList<Future>();

    private static boolean isRunning = false;

    private UsbManager usbManager;
    private List<UsbDevice> devices = new ArrayList<UsbDevice>();
    private List<Microcontroller> availableMicrocontrollers = new ArrayList<Microcontroller>();
    private List<String> microcontrollersNames = new ArrayList<String>();

     // Keeps track of all current registered clients.
    static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;

    static final int MSG_ERROR_CREATE_USB_GATE = 5;
    static final int MSG_ERROR_OPEN_USB_GATE = 6;
    static final int MSG_ERROR_SWITCH_TO_STREAM = 7;

    // Target we publish for clients to send messages to IncomingHandler.
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }

        public IBinder getMessenger() {
            return mMessenger.getBinder();
        }
    }

    public List<String> getAvailableMicrocontrollers() {
        return microcontrollersNames;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service...");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        executorService = Executors.newSingleThreadExecutor();
        showNotification();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        int numberOfDevices = intent.getIntExtra("numberOfDevices", 0);
        for (int i = 0; i < numberOfDevices; i++) {
            devices.add((UsbDevice) intent.getParcelableExtra("device" + i));
        }
        initMicrocontrollers();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeMicrocontrollers();
        notificationManager.cancel(NOTIFICATION);
        executorService.shutdown();
        isRunning = false;
    }

    private void closeMicrocontrollers() {
        for( Microcontroller micro : availableMicrocontrollers) {
            micro.closeConnection();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
    private void showNotification() {
        Notification notification = new Notification(R.drawable.ic_launcher,
                getText(R.string.local_service_started), System.currentTimeMillis());

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, getText(R.string.local_service_notification),
                getText(R.string.local_service_running), contentIntent);

        notificationManager.notify(NOTIFICATION, notification);
    }

    /**
     * Show a notification service is testing microcontrollers.
     */
    @SuppressWarnings("deprecation")
    private void showTestStartedNotification() {
        Notification notification = new Notification(R.drawable.ic_launcher,
                getText(R.string.local_service_test_started), System.currentTimeMillis());

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, getText(R.string.local_service_notification),
                getText(R.string.local_service_testing), contentIntent);

        notificationManager.notify(NOTIFICATION, notification);
    }

    /**
     * Show a notification service is finished testing microcontrollers.
     */
    @SuppressWarnings("deprecation")
    private void showTestDoneNotification() {
        Notification notification = new Notification(R.drawable.ic_launcher,
                getText(R.string.local_service_test_done), System.currentTimeMillis());

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, getText(R.string.local_service_notification),
                getText(R.string.local_service_running), contentIntent);

        notificationManager.notify(NOTIFICATION, notification);
    }

    public void initMicrocontrollers() {
        for (int i=0; i<devices.size();i++) {
            Microcontroller micro;
            try {
                micro = new Microcontroller(usbManager, devices.get(i));
                availableMicrocontrollers.add(micro);
                microcontrollersNames.add(micro.getDeviceName());
            } catch (UsbGateException e) {
                Log.e(TAG, "Cannot open USB connection cause: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Cannot open USB connection cause: " + e.getMessage());
            }
        }
        //if (microcontrollers.isEmpty()) {
            //TODO Send msg and close app
        //}
    }
    
    public void testCommunication(TestsViewModel model) throws MicrocontrollerException, UsbGateException {
        showTestStartedNotification();
        ScenariosGenerator scenariosGeneratior = new ScenariosGenerator(model);
        List<Scenario> scenarios = scenariosGeneratior.generate();
        for(Scenario scenario : scenarios) {
            testScenarioThread(scenario);
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!emptyFutures()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
                    }
                }
                Log.d(TAG, "All threads done");
                showTestDoneNotification();
            }
            private boolean emptyFutures() {
                @SuppressWarnings("rawtypes")
                List<Future> toRemove = new ArrayList<Future>();
                for( @SuppressWarnings("rawtypes") Future future : futures) {
                    try {
                        if(future.get() == null ) {
                            toRemove.add(future);
                        }
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
                        e.getMessage();
                    } catch (ExecutionException e) {
                        Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
                    }
                }
                futures.removeAll(toRemove);
                return futures.isEmpty();
            }
        });
        thread.start();
    }

    private void testScenarioThread(final Scenario scenario) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting test: Sequence " + scenario.getSequence().name() + " priority: " + scenario.getThreadPriority().name());
                if(scenario.getThreadPriority() == ThreadPriority.ANDROID_BASED_HIGH){
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                }
                final int repeats = scenario.getRepeats();
                final boolean saveStreamData = scenario.isSaveStreamData();
                final boolean simulateComputations = scenario.isSimulateComputations();
                final Microcontroller[] selectedMicrocontrollers = getSelectedMicrocontrollersForScenario(scenario);
                try {
                    for (short setreamInSize : scenario.getStreamInSizes()) {
                        System.gc();
                        switchMicrocontrollersToStreamMode(selectedMicrocontrollers, scenario.getStreamOutSize(), (short) setreamInSize);
                        TestResults testResults = new TestResults(scenario.getStreamOutSize(),
                                                                  setreamInSize,
                                                                  repeats,
                                                                  scenario.getSequence(),
                                                                  scenario.getThreadPriority(),
                                                                  (short)scenario.getDevices().length);

                        if (scenario.getSequence().isInGroup(Group.SYNC)) {
                            execSyncScenarioLoop(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                        } else if(scenario.getSequence().isInGroup(Group.ASYNC)) {
                            execASyncScenarioLoop(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                        }

                        switchMicrocontrollersToCommandMode(selectedMicrocontrollers);
                        System.gc();
                        if (scenario.isSaveSpeedLogs()) {
                            saveTestResults(testResults);
                        }
                    }
                } catch (MicrocontrollerException e) {
                    Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
                }
                Log.d(TAG, "Test done: Java thread sequence " + scenario.getSequence().name() + " priority: " + scenario.getThreadPriority().name());
            }

            private void execSyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
                    int repeats, TestResults testResults, boolean saveStreamData,
                    boolean simulateComputations) throws MicrocontrollerException {
                switch (scenario.getSequence()) {
                case SRSR:
                    execSyncSeqSRSR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case SSRR:
                    execSyncSeqSSRR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                default:
                    Log.e(TAG, "Unknown SYNC test sequence");
                    break;
                }
            }

            private void execASyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
                    int repeats, TestResults testResults, boolean saveStreamData,
                    boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
                switch (scenario.getSequence()) {
                case SRSR_wwww:
                    execASyncSeqSRSRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case SRww_SRww:
                    execASyncSeqSRwwSRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case SSRR_wwww:
                    execASyncSeqSSRRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case SSww_RRww:
                    execASyncSeqSSwwRRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case Sw_Rw_Sw_Rw:
                    execASyncSeqSwRwSwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                case Sw_Sw_Rw_Rw:
                    execASyncSeqSwSwRwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
                    break;
                default:
                    Log.e(TAG, "Unknown ASYNC test sequence");
                    break;
                }
            }

        });
        if(scenario.getThreadPriority() == ThreadPriority.JAVA_BASED_HIGH){
            thread.setPriority(Thread.MAX_PRIORITY);
        }
        futures.add(executorService.submit(thread));
    }

    private Microcontroller[] getSelectedMicrocontrollersForScenario(Scenario scenario) {
        List<String> selectedDevices = Arrays.asList(scenario.getDevices());
        Microcontroller[] selectedMicrocontrollers = new Microcontroller[selectedDevices.size()];
        int index = 0;
        for (Microcontroller microcontroller : availableMicrocontrollers) {
            if (selectedDevices.contains(microcontroller.getDeviceName())) {
                selectedMicrocontrollers[index] = microcontroller;
                index++;
            }
        }
        return selectedMicrocontrollers;
    }

    private void execASyncSeqSwRwSwRw(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.asyncRequestWait();
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSRwwSRww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSwSwRwRw(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.asyncRequestWait();
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSSRRwwww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSRSRwwww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSSwwRRww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execSyncSeqSRSR(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execSyncSeqSSRR(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                if(saveStreamData) {
                    writeStreamDataToFile(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void calculateFakeData() {
        // TODO Auto-generated method stub
        
    }

    private void writeStreamDataToFile(byte[] lastSentStreamPacket, byte[] lastReceivedStreamPacket) {
        // TODO Auto-generated method stub
        
    }

    private void saveTestResults(TestResults measuredData) {
        ExternalFile extFile = new ExternalFile(getApplicationContext());
        try {
            extFile.save(measuredData);
        } catch (ExternalStorageException e) {
            Log.w(TAG, "Couldn't save file with speed logs cause: " + e.getMessage());
        }
    }

    private void switchMicrocontrollersToStreamMode(Microcontroller[] selectedMicrocontrollers,
            short streamOutSize, short streamInSize) throws MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.setStreamParameters(streamOutSize, streamInSize);
            micro.switchToStreamMode();
        }
    }

    private void switchMicrocontrollersToCommandMode(Microcontroller[] selectedMicrocontrollers)
            throws MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.switchToCommandMode();
        }
    }

    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SET_INT_VALUE:

                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToUI(int what, String msg) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                // Send data as a String
                Bundle b = new Bundle();
                b.putString("msg", msg);
                Message message = Message.obtain(null, MSG_SET_STRING_VALUE);
                message.setData(b);
                mClients.get(i).send(message);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.remove(i);
            }
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
