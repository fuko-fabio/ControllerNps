package com.nps.micro.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DynamicListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.StableArrayAdapter;

import com.nps.architecture.MemoryUnit;
import com.nps.micro.R;
import com.nps.micro.model.ViewModel;
import com.nps.storage.Storage;
import com.nps.test.Scenario;
import com.nps.test.ScenariosGenerator;

public class TestsSectionFragment extends BaseSectionFragment {

    private TestsFragmentListener listener;
    private final ViewModel model = new ViewModel();

    private EditText repeatsInput;
    private Button repeatsButton;
    private EditText outSizeInput;
    private Button outSizeButton;
    private EditText inSizeInput;
    private Button inSizeButton;

    private CheckBox normalPriorityCheckBox;
    private CheckBox hiPriorityJavaCheckBox;
    private CheckBox hiPriorityAndroidCheckBox;

    private StableArrayAdapter selectedSequencesAdapter;
    private DynamicListView selectedSequencesListView;
    private Button selectSequenceButton;

    private ArrayAdapter<String> availableDevicesAdapter;
    private ListView availableDevicesListView;
    private StableArrayAdapter selectedDevicesAdapter;
    private DynamicListView selectedDevicesListView;
    private CheckBox extendedDevicesCombination;

    private CheckBox saveLogsCheckBox;
    private CheckBox saveStreamCheckBox;
    private RadioGroup radioStorageGroup;
    private RadioButton externalStorageRadio;
    private RadioButton internalStorageRadio;
    private EditText streamBufferSizeInput;
    private Spinner memoryUnitSpinner;
    private RadioGroup radioQueueGroup;
    private RadioButton autoStreamQueueSizeRadio;
    private RadioButton manualStreamQueueSizeRadio;
    private EditText streamQueueSize;

    private EditText simulateEditText;
    private CheckBox fastHub;
    private CheckBox autoEnableGraphCheckBox;

    private Button runButton;

    private List<String> devicesList = new ArrayList<String>();
    private final List<String> selectedDevices = new ArrayList<String>();
    private final List<String> selectedSequences = new ArrayList<String>();

    public TestsSectionFragment() {
        this.layout = R.layout.tests;
    }

    public void setListener( TestsFragmentListener listener) {
        this.listener = listener;
    }

    private void prepareScenarios() {
        ScenariosGenerator scenariosGeneratior = new ScenariosGenerator(model);
        final List<Scenario> scenarios = scenariosGeneratior.generate();
        List<CharSequence> items = new ArrayList<CharSequence>();
        for (Scenario scenario : scenarios) {
            items.add(scenario.toString());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.test_scenatios_title)
                .setItems(items.toArray(new CharSequence[items.size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                .setPositiveButton(R.string.run, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onRunUsbTest(scenarios);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);

        runButton = (Button) rootView.findViewById(R.id.runButton);
        runButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                prepareScenarios();
            }
        });

        repeatsInput = (EditText) rootView.findViewById(R.id.repeatsInput);
        repeatsInput.setText(String.valueOf(model.getRepeats()));
        repeatsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if(!val.isEmpty() && StringUtils.isNumeric(val)) {
                    model.setRepeats(Integer.valueOf(s.toString()));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});

        repeatsButton = (Button) rootView.findViewById(R.id.setRepeatsButton);
        repeatsButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.repeats)
                       .setItems(R.array.repeats_array, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               repeatsInput.setText(getResources().getStringArray(R.array.repeats_array)[which]);
                               dialog.dismiss();
                       }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});

        outSizeInput = (EditText) rootView.findViewById(R.id.packetOutSizeInput);
        outSizeInput.setText(String.valueOf(model.getStreamOutSize()));
        outSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if(!val.isEmpty() && StringUtils.isNumeric(val)) {
                    model.setStreamOutSize(Short.valueOf(s.toString()));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});

        outSizeButton = (Button) rootView.findViewById(R.id.setOutSizeButton);
        outSizeButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.out_size)
                       .setItems(R.array.out_size_array, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               outSizeInput.setText(getResources().getStringArray(R.array.out_size_array)[which]);
                               dialog.dismiss();
                       }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});

        inSizeInput = (EditText) rootView.findViewById(R.id.packetInSizeInput);
        inSizeInput.setText(String.valueOf(model.getStreamInSizes()[0]));
        inSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String[] vals = s.toString().split(" ");
                List<Short> intVals = new ArrayList<Short>();
                for (String val : vals) {
                    try {
                        intVals.add(Short.valueOf(val));
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                    }
                }
                short[] array = new short[intVals.size()];
                for(int i = 0; i < intVals.size(); i++) array[i] = intVals.get(i);
                model.setStreamInSize(array);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});

        inSizeButton = (Button) rootView.findViewById(R.id.setInSizeButton);
        inSizeButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.in_size)
                       .setItems(R.array.in_size_array, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               inSizeInput.setText(getResources().getStringArray(R.array.in_size_array)[which]);
                               dialog.dismiss();
                       }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});

        radioStorageGroup = (RadioGroup) rootView.findViewById(R.id.storageRadioGroup);
        externalStorageRadio = (RadioButton) rootView.findViewById(R.id.externalStorageRadio);
        internalStorageRadio = (RadioButton) rootView.findViewById(R.id.internalStorageRadio);
        switch (model.getStorageType()) {
        case EXTERNAL:
            externalStorageRadio.setChecked(true);
            break;
        case INTERNAL:
            internalStorageRadio.setChecked(true);
            break;
        }
        externalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
        internalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
        
        radioStorageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                case R.id.internalStorageRadio:
                    model.setStorageType(Storage.Type.INTERNAL);
                    break;
                case R.id.externalStorageRadio:
                    model.setStorageType(Storage.Type.EXTERNAL);
                    break;
                }
            }
        });

        saveLogsCheckBox = (CheckBox) rootView.findViewById(R.id.saveLogsCheckBox);
        saveLogsCheckBox.setChecked(model.isSaveLogs());
        saveLogsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setSaveLogs(isChecked);
                externalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
                internalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
                
            }});
        

        streamBufferSizeInput = (EditText) rootView.findViewById(R.id.bufferSizeEditText);
        streamBufferSizeInput.setText(String.valueOf(model.getStreamBufferSize()));
        streamBufferSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if(!val.isEmpty() && StringUtils.isNumeric(val)) {
                    model.setStreamBufferSize(Integer.valueOf(s.toString()));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});

        memoryUnitSpinner = (Spinner) rootView.findViewById(R.id.memoryUnitSpinner);
        memoryUnitSpinner.setSelection(model.getStreamBufferUnit().getIndex());
        memoryUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                model.setStreamBufferUnit(MemoryUnit.fromIndex(pos));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        saveStreamCheckBox = (CheckBox) rootView.findViewById(R.id.saveStreamDataCheckbox);
        saveStreamCheckBox.setChecked(model.isSaveStreams());
        streamBufferSizeInput.setEnabled(saveStreamCheckBox.isChecked());
        memoryUnitSpinner.setEnabled(saveStreamCheckBox.isChecked());
        streamQueueSize = (EditText) rootView.findViewById(R.id.streamQueueSize);
        streamQueueSize.setText(String.valueOf(model.getStreamQueueSize()));
        streamQueueSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if(!val.isEmpty() && StringUtils.isNumeric(val)) {
                    model.setStreamQueueSize(Integer.valueOf(s.toString()));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});
        streamQueueSize.setEnabled(model.isSaveStreams() && !model.isAutoStreamQueueSize());
        radioQueueGroup = (RadioGroup) rootView.findViewById(R.id.queueRadioGroup);
        autoStreamQueueSizeRadio = (RadioButton) rootView.findViewById(R.id.autoBufferRadio);
        manualStreamQueueSizeRadio = (RadioButton) rootView.findViewById(R.id.manualBufferRadio);
        if (model.isAutoStreamQueueSize()) {
            autoStreamQueueSizeRadio.setChecked(true);
        } else {
            manualStreamQueueSizeRadio.setChecked(true);
        }
        autoStreamQueueSizeRadio.setEnabled(model.isSaveStreams());
        manualStreamQueueSizeRadio.setEnabled(model.isSaveStreams());
        radioQueueGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                case R.id.autoBufferRadio:
                    model.setAutoStreamQueueSize(true);
                    streamQueueSize.setEnabled(false);
                    break;
                case R.id.manualBufferRadio:
                    model.setAutoStreamQueueSize(false);
                    streamQueueSize.setEnabled(true);
                    break;
                }
            }
        });

        saveStreamCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setSaveStreams(isChecked);
                externalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
                internalStorageRadio.setEnabled(model.isSaveStreams() || model.isSaveLogs());
                streamBufferSizeInput.setEnabled(isChecked);
                memoryUnitSpinner.setEnabled(isChecked);
                autoStreamQueueSizeRadio.setEnabled(model.isSaveStreams());
                manualStreamQueueSizeRadio.setEnabled(model.isSaveStreams());
                streamQueueSize.setEnabled(model.isSaveStreams() && !model.isAutoStreamQueueSize());
            }});

        simulateEditText = (EditText) rootView.findViewById(R.id.simulateComputationsEditText);
        simulateEditText.setText(String.valueOf(model.getSimulateComputations()));
        simulateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.setSimulateComputations(Short.valueOf(s.toString()));
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        normalPriorityCheckBox = (CheckBox) rootView.findViewById(R.id.normalPriorityCheckbox);
        normalPriorityCheckBox.setChecked(model.isNormalThreadPriority());
        normalPriorityCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setNormalThreadPriority(isChecked);
            }});
        
        hiPriorityAndroidCheckBox = (CheckBox) rootView.findViewById(R.id.hiAndroidPriorityCheckbox);
        hiPriorityAndroidCheckBox.setChecked(model.isHiAndroidThreadPriority());
        hiPriorityAndroidCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setHiAndroidThreadPriority(isChecked);
            }});
        
        hiPriorityJavaCheckBox = (CheckBox) rootView.findViewById(R.id.hiJavaPriorityCheckbox);
        hiPriorityJavaCheckBox.setChecked(model.isHiJavaThreadPriority());
        hiPriorityJavaCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setHiJavaThreadPriority(isChecked);
            }});
        
        extendedDevicesCombination = (CheckBox) rootView.findViewById(R.id.extendedDevicesCheckBox);
        extendedDevicesCombination.setChecked(model.isExtendedDevicesCombination());
        extendedDevicesCombination.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setExtendedDevicesCombination(isChecked);
            }});

        fastHub = (CheckBox) rootView.findViewById(R.id.fastHubCheckBox);
        fastHub.setChecked(model.isFastHub());
        fastHub.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setFastHub(isChecked);
            }});

        autoEnableGraphCheckBox = (CheckBox) rootView.findViewById(R.id.autoEnableGraphCheckBox);
        autoEnableGraphCheckBox.setChecked(model.isAutoEnableGraph());
        autoEnableGraphCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setAutoEnableGraph(isChecked);
            }});

        createSequenceChooser(rootView);
        createDeviceChooser(rootView, runButton);
        updateStatus(rootView);
        return rootView;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 1;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }  

    private void createSequenceChooser(View rootView) {
        final String[] sequencesArray = getResources().getStringArray(R.array.sequence_array);
        selectedSequences.clear();
        selectedSequences.addAll(model.getSequencesAsStrings());
        runButton.setEnabled(model.getSequences().length > 0);
        selectedSequencesListView = (DynamicListView) rootView.findViewById(R.id.selectedSequencesListView);
        selectedSequencesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        selectedSequencesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedSequences);
        selectedSequencesListView.setListItems(selectedSequences);
        selectedSequencesListView.setAdapter(selectedSequencesAdapter);
        setListViewHeightBasedOnChildren(selectedSequencesListView);
        selectedSequencesListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }

                v.onTouchEvent(event);
                return true;
            }
        });

        selectSequenceButton = (Button) rootView.findViewById(R.id.selectSequenceButton);
        selectSequenceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] checkedItems = new boolean[sequencesArray.length];
                for(int i = 0; i < sequencesArray.length; i++){
                    if(selectedSequences.contains(sequencesArray[i])) {
                        checkedItems[i] = true;
                    } else {
                        checkedItems[i] = false;
                    }
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.arhitecture)
                        .setMultiChoiceItems(sequencesArray, checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        String selectedArchitecture = sequencesArray[which];
                                        if (isChecked) {
                                            selectedSequences.add(selectedArchitecture);
                                        } else if (selectedSequences.contains(selectedArchitecture)) {
                                            selectedSequences.remove(selectedArchitecture);
                                        }
                                    }
                                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                runButton.setEnabled(!selectedSequences.isEmpty());
                                model.setSequences(selectedSequences);
                                selectedSequencesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedSequences);
                                selectedSequencesListView.setAdapter(selectedSequencesAdapter);
                                setListViewHeightBasedOnChildren(selectedSequencesListView);
                            }
                        }).create().show();
            }
        });
    }

    private void createDeviceChooser(View rootView, final Button runButton) {
        availableDevicesListView = (ListView) rootView.findViewById(R.id.availableDevicesListView);
        availableDevicesAdapter = new ArrayAdapter<String>(getActivity(), R.layout.text_view, devicesList);
        availableDevicesListView.setAdapter(availableDevicesAdapter);
        availableDevicesListView.setTextFilterEnabled(true);
        availableDevicesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                selectedDevices.add(ensureUniqueItem(item));
                selectedDevicesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedDevices);
                selectedDevicesListView.setAdapter(selectedDevicesAdapter);
                setListViewHeightBasedOnChildren(selectedDevicesListView);
                updateModelSelectedDevices();
            }

            private String ensureUniqueItem(String item) {
                if(selectedDevices.contains(item)){
                    return ensureUniqueItem(item + "'");
                } else {
                    return item;
                }
            }
        });
        availableDevicesListView.setOnItemLongClickListener( new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                view.setBackgroundColor(Color.CYAN);
                final String item = (String) parent.getItemAtPosition(position);
                final String msg = getResources().getString(R.string.ping_device_info);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.ping_device_title)
                        .setMessage(String.format(msg, item))
                        .setPositiveButton(R.string.ping, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                view.setBackgroundColor(Color.BLACK);
                                if (listener != null) {
                                    listener.onPingDevice(item);
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                view.setBackgroundColor(Color.BLACK);
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                return true;
            }
        });
        setListViewHeightBasedOnChildren(availableDevicesListView);

        selectedDevicesListView = (DynamicListView) rootView.findViewById(R.id.selectedDevicesListView);
        selectedDevicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        selectedDevicesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedDevices);
        selectedDevicesListView.setListItems(selectedDevices);
        selectedDevicesListView.setAdapter(selectedDevicesAdapter);
        selectedDevicesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                selectedDevices.remove(item);
                selectedDevicesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedDevices);
                selectedDevicesListView.setAdapter(selectedDevicesAdapter);
                setListViewHeightBasedOnChildren(selectedDevicesListView);
                updateModelSelectedDevices();
            }
        });
        selectedDevicesListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }
                v.onTouchEvent(event);
                return true;
            }
        });
        setListViewHeightBasedOnChildren(selectedDevicesListView);
    }

    private void updateModelSelectedDevices() {
        List<String> selected = new ArrayList<String>();
        for (String name : selectedDevices) {
            selected.add(name.replaceAll("'", ""));
        }
        model.setDevices(selected.toArray(new String[selected.size()]));
        runButton.setEnabled(selectedDevices.size() != 0);
        extendedDevicesCombination.setEnabled(selectedDevices.size() > 1);
    }

    public void setAvailableDevices(List<String> availableDevices) {
        this.devicesList = availableDevices;
        availableDevicesAdapter = new ArrayAdapter<String>(getActivity(), R.layout.text_view, devicesList);
        availableDevicesListView.setAdapter(availableDevicesAdapter);
        availableDevicesListView.setTextFilterEnabled(true);
        setListViewHeightBasedOnChildren(availableDevicesListView);

        if (selectedDevices.isEmpty() ) {
            selectedDevices.addAll(devicesList);
            updateModelSelectedDevices();
        }

        selectedDevicesAdapter = new StableArrayAdapter(getActivity(), R.layout.text_view, selectedDevices);
        selectedDevicesListView.setListItems(selectedDevices);
        selectedDevicesListView.setAdapter(selectedDevicesAdapter);
        setListViewHeightBasedOnChildren(selectedDevicesListView);
    }
    
    public boolean isAutoEnableGraph() {
        return model.isAutoEnableGraph();
    }
}
