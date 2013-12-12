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

import com.nps.micro.model.TestsViewModel;
import com.nps.storage.ExternalFile;
import com.nps.storage.ExternalStorageException;
import com.nps.storage.TestResults;
import com.nps.usb.UsbGateException;
import com.nps.usb.microcontroller.Architecture;
import com.nps.usb.microcontroller.AsyncSequence;
import com.nps.usb.microcontroller.Microcontroller;
import com.nps.usb.microcontroller.MicrocontrollerException;
import com.nps.usb.microcontroller.Packet;
import com.nps.usb.microcontroller.Priority;
import com.nps.usb.microcontroller.Sequence;

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
    private Microcontroller[] selectedMicrocontrollers;

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
        filterSelectedMicrocontrollers(model);
        for (Architecture arhitecture : model.getArchitectures()) {
            switch (arhitecture) {
            case SRSR_STANDARD_PRIORITY:
                testSyncThread(model, Sequence.SRSR, Priority.NORMAL, arhitecture);
                break;
            case SSRR_STANDARD_PRIORITY:
                testSyncThread(model, Sequence.SSRR, Priority.NORMAL, arhitecture);
                break;
            case SRSR_HI_PRIORITY_ANDROID:
                testSyncThread(model, Sequence.SRSR, Priority.ANDROID_BASED_HIGH, arhitecture);
                break;
            case SRSR_HI_PRIORITY_JAVA:
                testSyncThread(model, Sequence.SRSR, Priority.JAVA_BASED_HIGH, arhitecture);
                break;
            case SSRR_HI_PRIORITY_ANDROID:
                testSyncThread(model, Sequence.SSRR, Priority.ANDROID_BASED_HIGH, arhitecture);
                break;
            case SSRR_HI_PRIORITY_JAVA:
                testSyncThread(model, Sequence.SSRR, Priority.JAVA_BASED_HIGH, arhitecture);
                break;
            case SWRWSWRW_EVENT_DRIVEN:
                testAsyncThread(model, arhitecture, AsyncSequence.SWRWSWRW);
                break;
            case SRWWSRWW_EVENT_DRIVEN:
                testAsyncThread(model, arhitecture, AsyncSequence.SRWWSRWW);
                break;
            case SWSWRWRW_EVENT_DRIVEN:
                testAsyncThread(model, arhitecture, AsyncSequence.SWSWRWRW);
                break;
            case SSRRWWWW_EVENT_DRIVEN:
                testAsyncThread(model, arhitecture, AsyncSequence.SSRRWWWW);
                break;
            }
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

    private void filterSelectedMicrocontrollers(TestsViewModel model) {
        if (model.getDevices() == null) {
            model.setDevices(microcontrollersNames.toArray(new String[microcontrollersNames.size()]));
        }
        List<String> selectedDevices = Arrays.asList(model.getDevices());
        selectedMicrocontrollers = new Microcontroller[selectedDevices.size()];
        int index = 0;
        for (Microcontroller microcontroller : availableMicrocontrollers) {
            if (selectedDevices.contains(microcontroller.getDeviceName())) {
                selectedMicrocontrollers[index] = microcontroller;
                index++;
            }
        }

    }

    private void testSyncThread(final TestsViewModel model, final Sequence sequence, final Priority priority, final Architecture arhitecture) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting test: Sequence " + sequence.name() + " priority: " + priority.name());
                if(priority == Priority.ANDROID_BASED_HIGH){
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                }
                final int repeats = model.getRepeats();
                try {
                    for (short setreamInSize : model.getStreamInSizes()) {
                        System.gc();
                        switchMicrocontrollersToStreamMode((short) model.getStreamOutSize(), (short) setreamInSize);
                        TestResults testResults = new TestResults(model.getStreamOutSize(), setreamInSize, repeats, arhitecture, (short)model.getDevices().length);
                        switch (sequence){
                        case SRSR:
                            execStandardSeqSendReadSend(repeats, testResults);
                            break;
                        case SSRR:
                            execStandardSeqSendSendRread(repeats, testResults);
                            break;
                        }
                        switchMicrocontrollersToCommandMode();
                        System.gc();
                        if (model.isSaveLogs()) {
                            saveTestResults(testResults);
                        }
                    }
                } catch (MicrocontrollerException e) {
                    Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
                }
                Log.d(TAG, "Test done: Java thread sequence " + sequence.name() + " priority: " + priority.name());
            }
        });
        if(priority == Priority.JAVA_BASED_HIGH){
            thread.setPriority(Thread.MAX_PRIORITY);
        }
        futures.add(executorService.submit(thread));
    }

    private void testAsyncThread(final TestsViewModel model, final Architecture arhitecture, final AsyncSequence sequence) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting test: Sequence " + sequence.name() + " event driven");
                final int repeats = model.getRepeats();
                try {
                    for (short setreamInSize : model.getStreamInSizes()) {
                        System.gc();
                        switchMicrocontrollersToStreamMode((short) model.getStreamOutSize(), (short) setreamInSize);
                        TestResults testResults = new TestResults(model.getStreamOutSize(), setreamInSize, repeats, arhitecture, (short)model.getDevices().length);
                        switch (sequence){
                        case SRSRWWWW:
                            break;
                        case SRWWSRWW:
                            execQueueSendReadWaitSeqSendRreadSend(repeats, testResults);
                            break;
                        case SSRRWWWW:
                            execQueueSendReadWaitSeqSendSendRread(repeats, testResults);
                            break;
                        case SSWWRRWW:
                            break;
                        case SWRWSWRW:
                            execQueueSendWaitReadSeqSendRreadSend(repeats, testResults);
                            break;
                        case SWSWRWRW:
                            execQueueSendWaitSeqSendSendRread(repeats, testResults);
                            break;
                        }
                        switchMicrocontrollersToCommandMode();
                        System.gc();
                        if (model.isSaveLogs()) {
                            saveTestResults(testResults);
                        }
                    }
                } catch (MicrocontrollerException e) {
                    Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
                } catch (IllegalAccessException e ) {
                    Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
                }
                Log.d(TAG, "Test done: Sequence " + sequence.name() +  " event driven");
            }
        });
        futures.add(executorService.submit(thread));
    }

    private void execQueueSendWaitReadSeqSendRreadSend(int repeats, TestResults testResults) throws MicrocontrollerException, IllegalAccessException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.asyncRequestWait();
                micro.receiveAsyncStreamPacket();
                micro.asyncRequestWait();
            }
            updateTestResults(i, System.nanoTime() - before, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void execQueueSendReadWaitSeqSendRreadSend(int repeats, TestResults testResults) throws MicrocontrollerException, IllegalAccessException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                micro.asyncRequestWait();
                micro.asyncRequestWait();
            }
            updateTestResults(i, System.nanoTime() - before, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void execQueueSendWaitSeqSendSendRread(int repeats, TestResults testResults) throws MicrocontrollerException, IllegalAccessException {
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
                micro.asyncRequestWait();
            }
            updateTestResults(i, System.nanoTime() - before, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void execQueueSendReadWaitSeqSendSendRread(int repeats, TestResults testResults) throws MicrocontrollerException, IllegalAccessException {
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
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                micro.asyncRequestWait();
            }
            updateTestResults(i, System.nanoTime() - before, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void execStandardSeqSendReadSend(final int repeats, TestResults testResults) throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
                micro.receiveStreamPacket();
            }
            updateTestResults(i, System.nanoTime() - before, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void execStandardSeqSendSendRread(final int repeats, TestResults testResults) throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            long duration = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveStreamPacket();
            }
            updateTestResults(i, System.nanoTime() - duration, selectedMicrocontrollers[0].getLastReceivedStreamPacket(), testResults);
        }
    }

    private void updateTestResults(final int index, final long duration, final byte[] packet, TestResults testResults){
        testResults.addDuration(index, Packet.shortFromBytes(packet[0], packet[1]),
                duration,
                Packet.shortFromBytes(packet[4], packet[5]));
    }

    private void saveTestResults(TestResults measuredData) {
        ExternalFile extFile = new ExternalFile(getApplicationContext());
        try {
            extFile.save(measuredData);
        } catch (ExternalStorageException e) {
            Log.w(TAG, "Couldn't save file with speed logs cause: " + e.getMessage());
        }
    }

    private void switchMicrocontrollersToStreamMode(short streamOutSize, short streamInSize) throws MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.setStreamParameters(streamOutSize, streamInSize);
            micro.switchToStreamMode();
        }
    }

    private void switchMicrocontrollersToCommandMode() throws MicrocontrollerException {
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
