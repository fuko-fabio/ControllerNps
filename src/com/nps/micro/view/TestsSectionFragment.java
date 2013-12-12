package com.nps.micro.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import android.widget.TextView;

import com.nps.micro.R;
import com.nps.micro.model.TestsViewModel;
import com.nps.usb.microcontroller.Architecture;

public class TestsSectionFragment extends BaseSectionFragment {

    private TestsFragmentListener listener;
    private final TestsViewModel model = new TestsViewModel();

    private Button runButton;
    private EditText repeatsInput;
    private Button repeatsButton;
    private EditText outSizeInput;
    private Button outSizeButton;
    private EditText inSizeInput;
    private Button inSizeButton;
    private CheckBox saveLogsCheckBox;
    private String[] arhitecturesArray;
    private TextView architectureText;
    private Button arhitectureButton;
    private TextView deviceText;
    private Button deviceButton;
    private List<String> availableMicrocontrollers  = new ArrayList<String>();
    private List<String> selectedMicrocontrollers = new ArrayList<String>();
    private List<Architecture> selectedArchitectures = new ArrayList<Architecture>();

    public TestsSectionFragment() {
        this.layout = R.layout.tests;
    }

    public void setListener( TestsFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);

        runButton = (Button) rootView.findViewById(R.id.runButton);
        runButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRunUsbTest(model);
                }
            }});

        repeatsInput = (EditText) rootView.findViewById(R.id.repeatsInput);
        repeatsInput.setText(String.valueOf(model.getRepeats()));
        repeatsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                model.setRepeats(Integer.valueOf(s.toString()));
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
                model.setStreamOutSize(Short.valueOf(s.toString()));
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

        createArchitectureChooser(rootView);
        createDeviceChooser(rootView, runButton);
        return rootView;
    }

    private void createArchitectureChooser(View rootView) {
        arhitecturesArray = getResources().getStringArray(R.array.architecture_array);
        selectedArchitectures.addAll(Arrays.asList(model.getArchitectures()));
        architectureText = (TextView) rootView.findViewById(R.id.selectedArchitectureText);
        StringBuilder builder = new StringBuilder();
        for (Architecture item : selectedArchitectures) {
            builder.append(item.toString()).append('\n');
        }
        architectureText.setText(builder.toString());
        runButton.setEnabled(true);
        architectureText.setText(builder.toString());
        arhitectureButton = (Button) rootView.findViewById(R.id.selectArhitectureButton);
        arhitectureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] checkedItems = new boolean[arhitecturesArray.length];
                for(int i = 0; i < arhitecturesArray.length; i++){
                    if(selectedArchitectures.contains(Architecture.fromName(arhitecturesArray[i]))) {
                        checkedItems[i] = true;
                    } else {
                        checkedItems[i] = false;
                    }
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.arhitecture)
                        .setMultiChoiceItems(arhitecturesArray, checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        Architecture selectedArchitecture = Architecture.fromName(arhitecturesArray[which]);
                                        if (isChecked) {
                                            selectedArchitectures.add(selectedArchitecture);
                                        } else if (selectedArchitectures.contains(selectedArchitecture)) {
                                            selectedArchitectures.remove(selectedArchitecture);
                                        }
                                    }
                                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (selectedArchitectures.isEmpty()) {
                                    architectureText.setText(getResources().getString(R.string.none));
                                    runButton.setEnabled(false);
                                } else {
                                    StringBuilder builder = new StringBuilder();
                                    for (Architecture item : selectedArchitectures) {
                                        builder.append(item.toString()).append('\n');
                                    }
                                    architectureText.setText(builder.toString());
                                    runButton.setEnabled(true);
                                }
                                model.setArchitectures(convertArchitecturesObjects(selectedArchitectures));
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
        selectedMicrocontrollers.addAll(this.availableMicrocontrollers);
    }

    private Architecture[] convertArchitecturesObjects(List<Architecture> architectures){
        return architectures.toArray(new Architecture[architectures.size()]);
    }
}
