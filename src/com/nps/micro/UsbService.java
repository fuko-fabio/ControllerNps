/*******************************************************************************
 * Copyright 2014 Norbert Pabian.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 ******************************************************************************/
package com.nps.micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.nps.test.StatusThread;
import com.nps.usb.UsbGateException;
import com.nps.usb.microcontroller.Microcontroller;
import com.nps.usb.microcontroller.MicrocontrollerException;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class UsbService extends Service {

    public class Status {
        private String text;
        private boolean busy;

        public Status(String text, boolean busy) {
            this.text = text;
            this.busy = busy;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isBusy() {
            return busy;
        }

        public void setBusy(boolean busy) {
            this.busy = busy;
        }
    }

    private static final String TAG = "UsbService";
    
    private NotificationManager notificationManager;
    private int NOTIFICATION = R.string.local_service_notification;

    private ExecutorService executorService;
    @SuppressWarnings("rawtypes")
    private final List<Future> futures = new ArrayList<Future>();
    private StatusThread statusThread;

    private static boolean isRunning = false;

    private UsbManager usbManager;
    private List<UsbDevice> devices = new ArrayList<UsbDevice>();
    private Map<String, Microcontroller> availableMicrocontrollers = new HashMap<String, Microcontroller>();

     // Keeps track of all current registered clients.
    static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_STATUS = 4;

    private static final String KILL_ACTION = "com.nps.micro.killService";

    static final String MSG_STATUS_CONTENT = "status";
    static final String MSG_STATUS_BUSY = "statusBusy";

    private Status status;


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

    @SuppressWarnings("rawtypes")
    public List<Future> getFutures() {
        return futures;
    }

    public List<String> getAvailableMicrocontrollers() {
        return new ArrayList<String>(availableMicrocontrollers.keySet());
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service...");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        executorService = Executors.newSingleThreadExecutor();
        statusThread = new StatusThread(this);
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
        status = new Status(getString(R.string.ready), false);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeMicrocontrollers();
        statusThread.finalize();
        executorService.shutdown();
        for( @SuppressWarnings("rawtypes") Future future : futures) {
            if(!future.isDone()) {
                future.cancel(false);
            }
        }
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Log.e(TAG, "Couldn't finalize tasks cause: " + e.getMessage());
        }
        notificationManager.cancel(NOTIFICATION);
        isRunning = false;
        super.onDestroy();
    }

    private void closeMicrocontrollers() {
        for( Entry<String, Microcontroller> entry : availableMicrocontrollers.entrySet()) {
            entry.getValue().closeConnection();
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
        status = new Status(getText(R.string.local_service_testing).toString() +
                            ' ' + scenario.getSequence().toString() +
                            ' ' + scenario.getThreadPriority().toString() +
                            " In packet: " + scenario.getStreamInSize() +
                            " Out packet: " + scenario.getStreamOutSize() +
                            " On " + scenario.getDevices().length + " devices",
                            true);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent killIntent = new Intent();  
        killIntent.setAction(KILL_ACTION);
        PendingIntent pendingIntentKill = PendingIntent.getBroadcast(getApplicationContext(), 12345, killIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                                            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentText(status.getText())
                                            .setContentTitle(getText(R.string.local_service_notification))
                                            .addAction(R.drawable.ic_launcher, getString(R.string.kill), pendingIntentKill).build();

        notificationManager.notify(NOTIFICATION, notification);
        sendStatusMessage();
    }

    /**
     * Show a notification service is finished testing microcontrollers.
     */
    public void showTestDoneNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                                            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentText(getText(R.string.local_service_test_done))
                                            .setContentTitle(getText(R.string.local_service_notification)).build();
        notificationManager.notify(NOTIFICATION, notification);
        status = new Status(getString(R.string.ready), false);
        sendStatusMessage();
    }

    private void initMicrocontrollers() {
        for (int i=0; i<devices.size();i++) {
            Microcontroller micro;
            try {
                micro = new Microcontroller(usbManager, devices.get(i));
                availableMicrocontrollers.put(micro.getDeviceName(), micro);
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
        if(!scenarios.isEmpty()){
            for(Scenario scenario : scenarios) {
                testScenarioThread(scenario);
            }
            if(statusThread.isAlive()){
                statusThread.wakeUp();
            } else {
                statusThread.start();
            }
        }
    }

    private void testScenarioThread(final Scenario scenario) {
        ScenarioThread thread = new ScenarioThread(this,
                                           getSelectedMicrocontrollersForScenario(scenario),
                                           scenario,
                                           new ExternalStorage(getApplicationContext(), scenario));
        futures.add(executorService.submit(thread));
    }

    private Microcontroller[] getSelectedMicrocontrollersForScenario(Scenario scenario) {
        Microcontroller[] selectedMicrocontrollers = new Microcontroller[scenario.getDevices().length];
        int index = 0;
        for ( String deviceName : scenario.getDevices()) {
            selectedMicrocontrollers[index] = availableMicrocontrollers.get(deviceName);
            index++;
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

    private void sendStatusMessage() {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle b = new Bundle();
                b.putString(MSG_STATUS_CONTENT, status.getText());
                b.putBoolean(MSG_STATUS_BUSY, status.isBusy());
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
    
    public Status getStatus() {
        return this.status;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public void pingDevice(String deviceName) {
        Microcontroller micro = availableMicrocontrollers.get(deviceName);
        if(micro != null) {
            try {
                micro.getStreamParameters();
            } catch (MicrocontrollerException e) {
                Log.e(TAG, "Couldn't ping device: " + deviceName + " cause: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "Couldn't ping device: " + deviceName + " Device not exists.");
        }
    }

    public byte[] getLastReceivedPacket(String deviceName) {
        Microcontroller micro = availableMicrocontrollers.get(deviceName);
        if(micro != null) {
            return micro.getLastReceivedStreamPacket();
        }
        return null;
    }
}
