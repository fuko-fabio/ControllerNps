package com.nps.micro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nps.storage.ExternalStorage;
import com.nps.test.Scenario;
import com.nps.test.ScenarioThread;
import com.nps.usb.UsbGateException;
import com.nps.usb.microcontroller.Microcontroller;

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
    static final int MSG_STATUS = 4;

    private static final String KILL_ACTION = "com.nps.micro.killService";

    static final String MSG_STATUS_CONTENT = "status";

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

    public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(KILL_ACTION.equals(action)) {
                Log.v("shuffTest","Pressed YES");
            }
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
        List<String> devicesNames = new ArrayList<String>();
        int numberOfDevices = intent.getIntExtra(MainActivity.PACKAGE + '.' + "numberOfDevices", 0);
        for (int i = 0; i < numberOfDevices; i++) {
            devicesNames.add(intent.getStringExtra(MainActivity.PACKAGE + '.' + "device" + i));
        }
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice dev = deviceIterator.next();
            if (devicesNames.contains(dev.getDeviceName())) {
                devices.add(dev);
            }
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
    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                                            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentText(getText(R.string.local_service_running))
                                            .setContentTitle(getText(R.string.local_service_notification)).build();
        notificationManager.notify(NOTIFICATION, notification);
    }

    /**
     * Show a notification service is testing microcontrollers.
     */
    public void showTestRunningNotification(Scenario scenario) {
        String contentText = getText(R.string.local_service_testing).toString() +
                             ' ' + scenario.getSequence().toString() +
                             ' ' + scenario.getThreadPriority().toString() +
                             "packet In size: " + scenario.getStreamInSize() +
                             " on " + scenario.getDevices().length + " devices";
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        Intent killIntent = new Intent();  
        killIntent.setAction(KILL_ACTION);
        PendingIntent pendingIntentKill = PendingIntent.getBroadcast(this, 12345, killIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                                            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentText(contentText)
                                            .setContentTitle(getText(R.string.local_service_notification))
                                            .addAction(R.drawable.ic_launcher, getString(R.string.kill), pendingIntentKill).build();

        notificationManager.notify(NOTIFICATION, notification);
        sendStatusMessage(contentText);
    }

    /**
     * Show a notification service is finished testing microcontrollers.
     */
    private void showTestDoneNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                                            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentText(getText(R.string.local_service_test_done))
                                            .setContentTitle(getText(R.string.local_service_notification)).build();
        notificationManager.notify(NOTIFICATION, notification);
        sendStatusMessage(getString(R.string.ready));
    }

    private void initMicrocontrollers() {
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
        if (availableMicrocontrollers.isEmpty()) {
            //TODO Send msg and close app
            this.stopSelf();
        }
    }
    
    public void testCommunication(List<Scenario> scenarios) {
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
                    } catch (ExecutionException e) {
                        Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
                    }
                }
                futures.removeAll(toRemove);
                Log.d(TAG, "Scenarios to end: " + futures.size());
                return futures.isEmpty();
            }
        });
        thread.start();
    }

    private void testScenarioThread(final Scenario scenario) {
        ScenarioThread thread = new ScenarioThread(this,
                                           getSelectedMicrocontrollersForScenario(scenario),
                                           scenario,
                                           new ExternalStorage(getApplicationContext(), scenario));
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
            default:
                super.handleMessage(msg);
            }
        }
    }

    private void sendStatusMessage(String status) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle b = new Bundle();
                b.putString(MSG_STATUS_CONTENT, status);
                Message message = Message.obtain(null, MSG_STATUS);
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
