package com.nps.micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

import com.nps.micro.UsbService.Status;
import com.nps.micro.view.AboutSectionFragment;
import com.nps.micro.view.BaseSectionFragment;
import com.nps.micro.view.Dialogs;
import com.nps.micro.view.GraphFragmentListener;
import com.nps.micro.view.GraphSectionFragment;
import com.nps.micro.view.TestsFragmentListener;
import com.nps.micro.view.TestsSectionFragment;
import com.nps.test.Scenario;
import com.nps.usb.DeviceIds;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class MainActivity extends FragmentActivity {

    public static final String PACKAGE = "com.nps.micro";
    private static final String ACTION_USB_PERMISSION = PACKAGE + ".USB_PERMISSION";
    private static final String TAG = "MainActivity";

    private UsbService microUsbService;

    private Intent usbServiceIntent;
    private Messenger messengerService;
    private boolean isDeviceAvailable = false;
    private boolean isBoundToService;
    // private DeviceIds deviceIds = new DeviceIds("Olimex sam3", 24857, 1003);
    private DeviceIds deviceIds = new DeviceIds("Olimex sam7", 24870, 1003);

    List<UsbDevice> devices = new ArrayList<UsbDevice>();
    List<UsbDevice> devicesWithoutPermisions = new ArrayList<UsbDevice>();
    List<UsbDevice> devicesWithPermisions = new ArrayList<UsbDevice>();

    private final Messenger messenger = new Messenger(new IncomingHandler());

    private BroadcastReceiver usbDisconnectedBroadcastReceiver;

    private class UsbDisconnectedBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "UsbBroadcastReceiver";
        private final Activity activity;

        public UsbDisconnectedBroadcastReceiver(Activity activity) {
            this.activity = activity;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (isExpectedDevice(device)) {
                    Log.d(TAG, "USB device disconnected: " + device);
                    stopUsbService();
                    Dialogs.getDeviceDisconnectedDialog(activity).show();
                }
            }
        }
    };

    private final BroadcastReceiver usbPermissionBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        devicesWithoutPermisions.remove(0);
                        devicesWithPermisions.add(device);
                        requestPermisionForDevice();
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private ServiceConnection usbServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            microUsbService = ((UsbService.LocalBinder) service).getService();
            mSectionsPagerAdapter.updateAvailabeDevices(microUsbService.getAvailableMicrocontrollers());
            messengerService = new Messenger(((UsbService.LocalBinder) service).getMessenger());
            try {
                Message msg = Message.obtain(null, UsbService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                messengerService.send(msg);
                
                Status status = microUsbService.getStatus();
                mSectionsPagerAdapter.updateStatus(status.getText(), status.isBusy());
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            messengerService = null;
            microUsbService = null;
        }
    };

    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UsbService.MSG_STATUS:
                Bundle b = msg.getData();
                mSectionsPagerAdapter.updateStatus(b.getString(UsbService.MSG_STATUS_CONTENT), b.getBoolean(UsbService.MSG_STATUS_BUSY));
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private UsbManager usbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        usbDisconnectedBroadcastReceiver = new UsbDisconnectedBroadcastReceiver(this);
        registerReceiver(usbDisconnectedBroadcastReceiver, new IntentFilter(
                UsbManager.ACTION_USB_DEVICE_DETACHED));
        initUsbService();
    }

    private void initUsbService() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        initUsbDevices();
    }

    private void startUsbService() {
        isDeviceAvailable = true;
        Log.d(TAG, "Setup USB service.");
        usbServiceIntent = new Intent(this, UsbService.class);
        usbServiceIntent.putExtra(PACKAGE + '.' + "numberOfDevices", devices.size());
        for (int i = 0; i < devicesWithPermisions.size(); i++) {
            usbServiceIntent.putExtra(PACKAGE + '.' + "device" + i, devicesWithPermisions.get(i).getDeviceName());
        }
        Log.d(TAG, "Starting nps usb service...");
        startService(usbServiceIntent);
    }

    private boolean isExpectedDevice(UsbDevice device) {
        if (device == null) {
            return false;
        }
        if (device.getProductId() == deviceIds.getProductId()
                && device.getVendorId() == deviceIds.getVendorId()) {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                if (device.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initUsbDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice dev = deviceIterator.next();
            if (isExpectedDevice(dev)) {
                devices.add(dev);
            }
        }
        if (!devices.isEmpty()) {
            devicesWithoutPermisions = new ArrayList<UsbDevice>(devices);
            requestPermisionForDevice();
        } else {
            Log.d(TAG, "Cannot find target USB device");
            Dialogs.getUsbDeviceNotFoundDialog(this).show();
        }
    }

    private void requestPermisionForDevice() {
        if (devicesWithoutPermisions.isEmpty()) {
            startUsbService();
            bindToUsbService();
            return;
        }
        UsbDevice dev = devicesWithoutPermisions.get(0);
        if (usbManager.hasPermission(dev)) {
            devicesWithPermisions.add(devicesWithoutPermisions.get(0));
            devicesWithoutPermisions.remove(0);
            requestPermisionForDevice();
        } else {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(usbPermissionBroadcastReceiver, filter);
            usbManager.requestPermission(dev, permissionIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindToUsbService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindToUsbService();
        if(getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindFromUsbService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindFromUsbService();
    }

    @Override
    protected void onDestroy() {
        stopUsbService();
        if (usbDisconnectedBroadcastReceiver != null) {
            try {
                unregisterReceiver(usbDisconnectedBroadcastReceiver);
            } catch (Exception e) {
                Log.d(TAG, "Cannot unregister receiver: " + e.getMessage());
            }
        }
        super.onDestroy();
    }

    private void stopUsbService() {
        if (UsbService.isRunning() && usbServiceIntent != null) {
            stopService(usbServiceIntent);
        }
        if (usbPermissionBroadcastReceiver != null) {
            try {
                unregisterReceiver(usbPermissionBroadcastReceiver);
            } catch (Exception e) {
                Log.d(TAG, "Cannot unregister receiver: " + e.getMessage());
            }
        }
    }

    private void unbindFromUsbService() {
        if (isBoundToService) {
            Log.d(TAG, "Unbinding nps usb service...");
            // If we have received the service, and hence registered with it,
            // then now is the time to unregister.
            if (messengerService != null) {
                try {
                    Message msg = Message.obtain(null, UsbService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messengerService;
                    messengerService.send(msg);
                } catch (RemoteException e) {
                    Log.d(TAG, "Cannot unregister client from service: " + e.getMessage());
                    // There is nothing special we need to do if the service has
                    // crashed.
                }
            }
            // Detach our existing connection.
            unbindService(usbServiceConnection);
            isBoundToService = false;
        }
    }

    private void bindToUsbService() {
        if (!isBoundToService && isDeviceAvailable) {
            Log.d(TAG, "Binding nps usb service...");
            isBoundToService = bindService(new Intent(this, UsbService.class),
                    usbServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private BaseSectionFragment[] fragments;

        private CharSequence[] titles = new CharSequence[] { getString(R.string.title_tests),
                getString(R.string.title_graph), getString(R.string.title_about) };

        private TestsSectionFragment testsFragment;
        private GraphSectionFragment graphFragment;
        private AboutSectionFragment aboutFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            graphFragment = new GraphSectionFragment();
            graphFragment.setOnGraphFragmentListener(new GraphFragmentListener() {
                @Override
                public byte[] getLastReceivedPacket(String deviceName) {
                    if (microUsbService != null) {
                        return microUsbService.getLastReceivedPacket(deviceName);
                    }
                    return null;
                }
            });
            aboutFragment = new AboutSectionFragment();
            testsFragment = new TestsSectionFragment();
            testsFragment.setListener(new TestsFragmentListener() {

                @Override
                public void onRunUsbTest(List<Scenario> scenarios) {
                    if (microUsbService != null) {
                        microUsbService.testCommunication(scenarios);
                        graphFragment.setAutoEnableGraph(testsFragment.isAutoEnableGraph());
                    }
                }

                @Override
                public void onPingDevice(String deviceName) {
                    if (microUsbService != null) {
                        microUsbService.pingDevice(deviceName);
                    }
                }
            });
            fragments = new BaseSectionFragment[] { testsFragment, graphFragment, aboutFragment };
        }

        public void updateStatus(String status, boolean busy) {
            for (BaseSectionFragment fragment : fragments) {
                fragment.updateStatus(status, busy);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        public void updateAvailabeDevices(List<String> devices) {
            testsFragment.setAvailableDevices(devices);
            graphFragment.setAvailableDevices(devices);
        }
    }
}
