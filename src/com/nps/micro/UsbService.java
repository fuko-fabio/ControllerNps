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

import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;
import com.nps.usb.packet.MeasurementsData;
import com.nps.usb.packet.Microcontroller;
import com.nps.usb.packet.MicrocontrollerException;
import com.nps.usb.packet.Packet;

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
        try {
            for (UsbDevice device : devices) {
                microcontrollers.add(new Microcontroller(usbManager, device));
            }
        } catch (UsbGateException e) {
            Log.d(TAG, "Cannot open USB connection cause: " + e.getMessage());
            sendMessageToUI(MSG_ERROR_OPEN_USB_GATE, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Cannot open USB connection cause: " + e.getMessage());
            sendMessageToUI(MSG_ERROR_OPEN_USB_GATE, e.getMessage());
        }
    }

    public void testCommunication(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
        switchMicrocontrollersToStreamMode(model);
        switch (model.getArhitecture()) {
        case PARALLEL_ATO:
            testParallelAndroidOneThread(model);
            break;
        case PARALLEL_AJT:
            testParallelAndroidTwoThread(model);
            break;
        case PARALLEL_JTO:
            testParallelJavaOneThread(model);
            break;
        case PARALLEL_JTT:
            testParallelJavaTwoThread(model);
            break;
        case SEQUENCE_SRSR:
            testSequenceSendReadSendRead(model);
            break;
        case SEQUENCE_SSRR:
            testSequenceSendSendReadRead(model);
            break;
        }
        switchMicrocontrollersToCommandMode(model);
    }

    private void testParallelAndroidOneThread(DetailsViewModel model) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelAndroidTwoThread(DetailsViewModel model) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelJavaOneThread(DetailsViewModel model) {
        // TODO Auto-generated method stub
        
    }

    private void testParallelJavaTwoThread(DetailsViewModel model) {
        // TODO Auto-generated method stub
        
    }

    private void testSequenceSendReadSendRead(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
        Log.d(TAG, "Starting test: Sequence SRSR");
        for (int i = 0; i < model.getNumberOfRepeats(); i++) {
            int index = 0;
            for (Microcontroller micro : microcontrollers) {
                long duration = micro.sendStreamPacket();
                duration = duration + micro.receiveStreamPacket();
                byte[] rd = micro.getLastReceivedData();
                short hardwareDuration = Packet.shortFromBytes(rd[0], rd[1]);
                index++;
            }
        }
        if (model.isSaveLogs()) {
            saveMeasurmentsResults();
        }
        Log.d(TAG, "Sequence SRSR test done");
    }

    private void testSequenceSendSendReadRead(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
        Log.d(TAG, "Starting test: Sequence SSRR");
        for (int i = 0; i < model.getNumberOfRepeats(); i++) {
            List<Long> tmpDurations = new ArrayList<Long>();
            for (Microcontroller micro : microcontrollers) {
                long duration = micro.sendStreamPacket();
                tmpDurations.add(duration);
            }
            int index = 0;
            for (Microcontroller micro : microcontrollers) {
                long duration = tmpDurations.get(index) + micro.receiveStreamPacket();
                byte[] rd = micro.getLastReceivedData();
                short hardwareDuration = Packet.shortFromBytes(rd[0], rd[1]);
                index++;
            }
        }
        if (model.isSaveLogs()) {
            saveMeasurmentsResults();
        }
        Log.d(TAG, "Sequence SSRR test done");
    }

    private void saveMeasurmentsResults() {
        // TODO Auto-generated method stub
        
    }

    private void switchMicrocontrollersToStreamMode(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
        for (Microcontroller micro : microcontrollers) {
            micro.setStreamParameters(model.getPacketOutSize(), model.getPacketInSize());
            micro.switchToStreamMode();
        }
    }

    private void switchMicrocontrollersToCommandMode(DetailsViewModel model) throws MicrocontrollerException, UsbGateException {
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
