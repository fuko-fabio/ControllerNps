package com.nps.micro;

import java.util.ArrayList;
import java.util.List;

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
import android.os.RemoteException;
import android.util.Log;

import com.nps.micro.model.DetailsViewModel;
import com.nps.storage.ExternalFile;
import com.nps.storage.ExternalStorageException;
import com.nps.storage.TestResults;
import com.nps.usb.UsbGateException;
import com.nps.usb.microcontroller.Microcontroller;
import com.nps.usb.microcontroller.MicrocontrollerException;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class UsbService extends Service {

    private static final String TAG = "UsbService";
    
    private NotificationManager notificationManager;
    private int NOTIFICATION = R.string.local_service_notification;

    private static boolean isRunning = false;

    private UsbManager usbManager;
    private List<UsbDevice> devices = new ArrayList<UsbDevice>();
    private List<Microcontroller> microcontrollers = new ArrayList<Microcontroller>();

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

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service...");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

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
        // Cancel the persistent notification.
        notificationManager.cancel(NOTIFICATION);

        isRunning = false;
    }

    private void closeMicrocontrollers() {
        for( Microcontroller micro : microcontrollers) {
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

    public void initMicrocontrollers() {
        for (UsbDevice device : devices) {
            Microcontroller micro;
            try {
                micro = new Microcontroller(usbManager, device);
                microcontrollers.add(micro);
            } catch (UsbGateException e) {
                Log.d(TAG, "Cannot open USB connection cause: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Cannot open USB connection cause: " + e.getMessage());
            }
        }
        if (microcontrollers.isEmpty()) {
            //TODO Send msg and close app
        }
    }

    public void testCommunication(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
        int repeats = model.getRepeats();
        for (int setreamInSize : model.getStreamInSize()) {
            switchMicrocontrollersToStreamMode((short) model.getStreamOutSize(), (short) setreamInSize);
            TestResults testResults = new TestResults(model.getStreamOutSize(), setreamInSize, repeats, model.getArhitecture());
            switch (model.getArhitecture()) {
            case PARALLEL_ATO:
                testParallelAndroidOneThread(repeats, testResults);
                break;
            case PARALLEL_AJT:
                testParallelAndroidTwoThread(repeats, testResults);
                break;
            case PARALLEL_JTO:
                testParallelJavaOneThread(repeats, testResults);
                break;
            case PARALLEL_JTT:
                testParallelJavaTwoThread(repeats, testResults);
                break;
            case SEQUENCE_SRSR:
                testSequenceSendReadSendRead(repeats, testResults);
                break;
            case SEQUENCE_SSRR:
                testSequenceSendSendReadRead(repeats, testResults);
                break;
            }
            switchMicrocontrollersToCommandMode();
            if (model.isSaveLogs()) {
                saveTestResults(testResults);
            }
        }
    }

    private void testParallelAndroidOneThread(int repeats, TestResults testResults) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelAndroidTwoThread(int repeats, TestResults testResults) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelJavaOneThread(int repeats, TestResults testResults) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelJavaTwoThread(int repeats, TestResults testResults) {
        // TODO Auto-generated method stub
        
    }

    private void testSequenceSendReadSendRead(int repeats, TestResults testResults) throws MicrocontrollerException {
        Log.d(TAG, "Starting test: Sequence SRSR");
        for (int i = 0; i < repeats; i++) {
            long duration = System.nanoTime();
            for (Microcontroller micro : microcontrollers) {
                micro.sendStreamPacket(null);
                micro.receiveStreamPacket();
            }
            testResults.addDuration(i, System.nanoTime() - duration, 0, 0);
        }
        Log.d(TAG, "Sequence SRSR test done");
    }

    private void testSequenceSendSendReadRead(int repeats, TestResults testResults) throws MicrocontrollerException {
        Log.d(TAG, "Starting test: Sequence SSRR");
        for (int i = 0; i < repeats; i++) {
            long duration = System.nanoTime();
            for (Microcontroller micro : microcontrollers) {
                micro.sendStreamPacket(null);
            }
            for (Microcontroller micro : microcontrollers) {
                micro.receiveStreamPacket();
            }
            testResults.addDuration(i, System.nanoTime() - duration, 0, 0);
        }
        Log.d(TAG, "Sequence SSRR test done");
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
        for (Microcontroller micro : microcontrollers) {
            micro.setStreamParameters(streamOutSize, streamInSize);
            micro.switchToStreamMode();
        }
    }

    private void switchMicrocontrollersToCommandMode() throws MicrocontrollerException {
        for (Microcontroller micro : microcontrollers) {
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