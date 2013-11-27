package com.nps.micro;

import java.util.HashMap;
import java.util.Iterator;

import com.nps.usb.DeviceIds;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class MainActivity extends FragmentActivity {

    private static final String ACTION_USB_PERMISSION = "com.nps.micro.USB_PERMISSION";
    private static final String TAG = "MainActivity";
    
    private DetailsFragmentModel model = new DetailsFragmentModel();
    private UsbService microUsbService;

    private Intent usbServiceIntent;
    private Messenger messengerService;
    private boolean isDeviceAvailable = false;
    private boolean isBoundToService;
    // private DeviceIds deviceIds = new DeviceIds("Olimex sam3", 24857, 1003);
    private DeviceIds deviceIds = new DeviceIds("Olimex sam7", 24870, 1003);

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
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            startUsbService(device);
                        }
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

            messengerService = new Messenger(((UsbService.LocalBinder) service).getMessenger());
            try {
                Message msg = Message.obtain(null, UsbService.MSG_REGISTER_CLIENT);
                msg.replyTo = messengerService;
                messengerService.send(msg);
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

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

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
        
      EditText repeatsInput = (EditText) findViewById(R.id.repeatsInput);
      repeatsInput.setText(String.valueOf(model.getNumberOfRepeats()));
      repeatsInput.addTextChangedListener(new TextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
              model.setNumberOfRepeats(Integer.valueOf(s.toString()));
          }
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }
          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
          }});

      EditText outSizeInput = (EditText) findViewById(R.id.packetOutSizeInput);
      outSizeInput.setText(String.valueOf(model.getPacketOutSize()));
      outSizeInput.addTextChangedListener(new TextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
              model.setPacketOutSize(Short.valueOf(s.toString()));
          }
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }
          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
          }});

      EditText inSizeInput = (EditText) findViewById(R.id.packetInSizeInput);
      inSizeInput.setText(String.valueOf(model.getPacketInSize()));
      inSizeInput.addTextChangedListener(new TextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
              model.setPacketInSize(Short.valueOf(s.toString()));
          }
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }
          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
          }});

      CheckBox saveLogsCheckBox = (CheckBox) findViewById(R.id.saveLogsCheckBox);
      saveLogsCheckBox.setSelected(model.isSaveLogs());
      saveLogsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              model.setSaveLogs(isChecked);
          }});
      
      Button runButton = (Button) findViewById(R.id.runButton);
      runButton.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
              model.isSaveLogs();
          }});
    }

    private void initUsbService() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        initUsbDevice(usbManager);
    }

    private void startUsbService(UsbDevice device) {
        isDeviceAvailable = true;
        Log.d(TAG, "Setup USB service.");
        usbServiceIntent = new Intent(this, UsbService.class);
        usbServiceIntent.putExtra("device", device);
        Log.d(TAG, "Starting nps usb service...");
        startService(usbServiceIntent);
    }

    private boolean isExpectedDevice(UsbDevice device) {
        if (device == null) {
            return false;
        }
        return device.getProductId() == deviceIds.getProductId()
                && device.getVendorId() == deviceIds.getVendorId();
    }

    private void initUsbDevice(UsbManager usbManager) {

        UsbDevice device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice dev = deviceIterator.next();
                if (isExpectedDevice(dev)) {
                    device = dev;
                    break;
                }
            }
            if (device != null) {
                if (!usbManager.hasPermission(device)) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    registerReceiver(usbPermissionBroadcastReceiver, filter);
                    usbManager.requestPermission(device, permissionIntent);
                } else {
                    startUsbService(device);
                }
            } else {
                Log.d(TAG, "Cannot find target USB device");
                Dialogs.getUsbDeviceNotFoundDialog(this).show();
            }
        } else {
            startUsbService(device);
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

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
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

        private Fragment[] fragments = new Fragment[] { new HomeSectionFragment(),
                new DetailsSectionFragment(), new GraphSectionFragment() };

        private CharSequence[] titles = new CharSequence[] { getString(R.string.title_home),
                getString(R.string.title_details), getString(R.string.title_graph) };

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
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
    }

    public static class HomeSectionFragment extends BaseSectionFragment {

        public HomeSectionFragment() {
            this.layout = R.layout.home;
        }
    }

    public static class DetailsSectionFragment extends BaseSectionFragment {
        public DetailsSectionFragment() {
            this.layout = R.layout.details;
        }
    }

    public static class GraphSectionFragment extends BaseSectionFragment {

        public GraphSectionFragment() {
            this.layout = R.layout.graph;
        }
    }

    public static class BaseSectionFragment extends Fragment {

        protected int layout;

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View newView = inflater.inflate(layout, null);
            ViewGroup rootView = (ViewGroup) getView();
            rootView.removeAllViews();
            rootView.addView(newView);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(layout, container, false);
            return rootView;
        }
    }
}
