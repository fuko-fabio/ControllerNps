package com.nps.micro.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nps.architecture.Sequence;
import com.nps.micro.R;
import com.nps.micro.model.TestsViewModel;
import com.nps.storage.Storage;
import com.nps.test.Scenario;
import com.nps.test.ScenariosGenerator;

public class TestsSectionFragment extends BaseSectionFragment {

    private TestsFragmentListener listener;
    private final TestsViewModel model = new TestsViewModel();

    private TextView status;
    private Button runButton;
    private EditText repeatsInput;
    private Button repeatsButton;
    private EditText outSizeInput;
    private Button outSizeButton;
    private EditText inSizeInput;
    private Button inSizeButton;
    private CheckBox normalPriorityCheckBox;
    private CheckBox hiPriorityJavaCheckBox;
    private CheckBox hiPriorityAndroidCheckBox;
    private CheckBox saveLogsCheckBox;
    private CheckBox saveStreamCheckBox;
    private EditText simulateEditText;
    private CheckBox extendedDevicesCombination;
    private CheckBox fastHub;
    private String[] sequencesArray;
    private TextView sequenceText;
    private Button arhitectureButton;
    private TextView deviceText;
    private Button deviceButton;
    private List<String> availableMicrocontrollers  = new ArrayList<String>();
    private List<String> selectedMicrocontrollers = new ArrayList<String>();
    private List<Sequence> selectedSequences = new ArrayList<Sequence>();
    private RadioGroup radioStorageGroup;

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
                                // TODO Auto-generated method stub

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

        status = (TextView) rootView.findViewById(R.id.statusText);
        
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
                    model.setRepeats(Integer.valueOf(s.toString()));
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

        saveLogsCheckBox = (CheckBox) rootView.findViewById(R.id.saveLogsCheckBox);
        saveLogsCheckBox.setSelected(model.isSaveLogs());
        saveLogsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setSaveLogs(isChecked);
            }});
        
        saveStreamCheckBox = (CheckBox) rootView.findViewById(R.id.saveStreamDataCheckbox);
        saveStreamCheckBox.setSelected(model.isSaveStreams());
        saveStreamCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setSaveStreams(isChecked);
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
        normalPriorityCheckBox.setSelected(model.isNormalThreadPriority());
        normalPriorityCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setNormalThreadPriority(isChecked);
            }});
        
        hiPriorityAndroidCheckBox = (CheckBox) rootView.findViewById(R.id.hiAndroidPriorityCheckbox);
        hiPriorityAndroidCheckBox.setSelected(model.isHiAndroidThreadPriority());
        hiPriorityAndroidCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setHiAndroidThreadPriority(isChecked);
            }});
        
        hiPriorityJavaCheckBox = (CheckBox) rootView.findViewById(R.id.hiJavaPriorityCheckbox);
        hiPriorityJavaCheckBox.setSelected(model.isHiJavaThreadPriority());
        hiPriorityJavaCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setHiJavaThreadPriority(isChecked);
            }});
        
        extendedDevicesCombination = (CheckBox) rootView.findViewById(R.id.extendedDevicesCheckBox);
        extendedDevicesCombination.setSelected(model.isExtendedDevicesCombination());
        extendedDevicesCombination.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setExtendedDevicesCombination(isChecked);
            }});

        fastHub = (CheckBox) rootView.findViewById(R.id.fastHubCheckBox);
        fastHub.setSelected(model.isFastHub());
        fastHub.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setFastHub(isChecked);
            }});

        radioStorageGroup = (RadioGroup) rootView.findViewById(R.id.storageRadioGroup);
        
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

        createSequenceChooser(rootView);
        createDeviceChooser(rootView, runButton);
        return rootView;
    }

    private void createSequenceChooser(View rootView) {
        sequencesArray = getResources().getStringArray(R.array.sequence_array);
        selectedSequences.addAll(Arrays.asList(model.getSequences()));
        sequenceText = (TextView) rootView.findViewById(R.id.selectedSequenceText);
        StringBuilder builder = new StringBuilder();
        for (Sequence item : selectedSequences) {
            builder.append(item.toString()).append('\n');
        }
        sequenceText.setText(builder.toString());
        runButton.setEnabled(true);
        sequenceText.setText(builder.toString());
        arhitectureButton = (Button) rootView.findViewById(R.id.selectSequenceButton);
        arhitectureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] checkedItems = new boolean[sequencesArray.length];
                for(int i = 0; i < sequencesArray.length; i++){
                    if(selectedSequences.contains(Sequence.fromString(sequencesArray[i]))) {
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
                                        Sequence selectedArchitecture = Sequence.fromString(sequencesArray[which]);
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
                                if (selectedSequences.isEmpty()) {
                                    sequenceText.setText(getResources().getString(R.string.none));
                                    runButton.setEnabled(false);
                                } else {
                                    StringBuilder builder = new StringBuilder();
                                    for (Sequence item : selectedSequences) {
                                        builder.append(item.toString()).append('\n');
                                    }
                                    sequenceText.setText(builder.toString());
                                    runButton.setEnabled(true);
                                }
                                model.setSequences(selectedSequences.toArray(new Sequence[selectedSequences.size()]));
                            }
                        }).create().show();
            }
        });
    }

    private void createDeviceChooser(View rootView, final Button runButton) {
        deviceText = (TextView) rootView.findViewById(R.id.selectedDeviceText);
        StringBuilder builder = new StringBuilder();
        for (String item : selectedMicrocontrollers) {
            builder.append(item).append('\n');
        }
        deviceText.setText(builder.toString());
        deviceButton = (Button) rootView.findViewById(R.id.selectDeviceButton);
        deviceButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean[] checkedItems = new boolean[availableMicrocontrollers.size()];
                for(int i = 0; i < availableMicrocontrollers.size(); i++){
                    if(selectedMicrocontrollers.contains(availableMicrocontrollers.get(i))) {
                        checkedItems[i] = true;
                    } else {
                        checkedItems[i] = false;
                    }
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.device)
                        .setMultiChoiceItems(availableMicrocontrollers.toArray(new String[availableMicrocontrollers.size()]), checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        String item = availableMicrocontrollers.get(which);
                                        if (isChecked) {
                                            selectedMicrocontrollers.add(item);
                                        } else if (selectedMicrocontrollers.contains(item)) {
                                            selectedMicrocontrollers.remove(item);
                                        }
                                    }
                                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (selectedMicrocontrollers.isEmpty()) {
                                    deviceText.setText(getResources().getString(R.string.none));
                                    runButton.setEnabled(false);
                                } else {
                                    StringBuilder builder = new StringBuilder();
                                    for (String item : selectedMicrocontrollers) {
                                        builder.append(item).append('\n');
                                    }
                                    deviceText.setText(builder.toString());
                                    runButton.setEnabled(true);
                                }
                                model.setDevices(selectedMicrocontrollers.toArray(new String[selectedMicrocontrollers.size()]));
                            }
                        }).create().show();
            }});
    }

    public void setAvailableMicrocontrollers(List<String> availableMicrocontrollers) {
        this.availableMicrocontrollers = availableMicrocontrollers;
        model.setDevices(availableMicrocontrollers.toArray(new String[availableMicrocontrollers.size()]));
        StringBuilder builder = new StringBuilder();
        for (String item : availableMicrocontrollers) {
            builder.append(item).append('\n');
        }
        deviceText.setText(builder.toString());
        if (availableMicrocontrollers.isEmpty()) {
            runButton.setEnabled(true);
        }
        if(selectedMicrocontrollers.isEmpty()) {
            selectedMicrocontrollers.addAll(this.availableMicrocontrollers);
        }
    }

    public void setStatus(String string) {
        if (status != null) {
            status.setText(string);
        }
    }
}
