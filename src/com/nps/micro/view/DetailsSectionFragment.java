package com.nps.micro.view;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.nps.micro.model.DetailsViewModel;
import com.nps.usb.microcontroller.Arhitecture;

public class DetailsSectionFragment extends BaseSectionFragment {

    private DetailsFragmentListener listener;
    private DetailsViewModel model;

    public DetailsSectionFragment() {
        this.layout = R.layout.details;
        this.model = new DetailsViewModel();
    }

    public void setListener( DetailsFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(layout, container, false);

        final EditText repeatsInput = (EditText) rootView.findViewById(R.id.repeatsInput);
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

        Button repeatsButton = (Button) rootView.findViewById(R.id.setRepeatsButton);
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

        final EditText outSizeInput = (EditText) rootView.findViewById(R.id.packetOutSizeInput);
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

        Button outSizeButton = (Button) rootView.findViewById(R.id.setOutSizeButton);
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

        final EditText inSizeInput = (EditText) rootView.findViewById(R.id.packetInSizeInput);
        inSizeInput.setText(String.valueOf(model.getStreamInSize()[0]));
        inSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String[] vals = s.toString().split(" ");
                List<Integer> intVals = new ArrayList<Integer>();
                for (String val : vals) {
                    try {
                        intVals.add(Integer.valueOf(val));
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                    }
                }
                int[] array = new int[intVals.size()];
                for(int i = 0; i < intVals.size(); i++) array[i] = intVals.get(i);
                model.setStreamInSize(array);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }});

        Button inSizeButton = (Button) rootView.findViewById(R.id.setInSizeButton);
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

        final TextView arhitectureText = (TextView) rootView.findViewById(R.id.selectedArhitectureText);
        String defaultArhitecture = getResources().getStringArray(R.array.arhitecture_array)[0];
        arhitectureText.setText(defaultArhitecture);
        model.setArhitectures(new Arhitecture[]{Arhitecture.SRSR_STANDARD_PRIORITY});
        Button arhitectureButton = (Button) rootView.findViewById(R.id.selectArhitectureButton);
        arhitectureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> selectedItems = new ArrayList<String>();
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.arhitecture)
                        .setMultiChoiceItems(R.array.arhitecture_array, null,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        String item = getResources().getStringArray(
                                                R.array.arhitecture_array)[which];
                                        if (isChecked) {
                                            selectedItems.add(item);
                                        } else if (selectedItems.contains(item)) {
                                            selectedItems.remove(item);
                                        }
                                    }
                                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                arhitectureText.setText(selectedItems.toString());
                                List<Arhitecture> architecturesList = new ArrayList<Arhitecture>();
                                for (String item : selectedItems) {
                                    architecturesList.add(Arhitecture.fromName(item));
                                }
                                model.setArhitectures(architecturesList
                                        .toArray(new Arhitecture[architecturesList.size()]));
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // TODO Devices list
        final TextView deviceText = (TextView) rootView.findViewById(R.id.selectedDeviceText);
        deviceText.setText("All");
        Button deviceButton = (Button) rootView.findViewById(R.id.selectDeviceButton);
        deviceButton.setEnabled(false);
        deviceButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.arhitecture)
                       .setItems(R.array.arhitecture_array, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               deviceText.setText(getResources().getStringArray(R.array.arhitecture_array)[which]);
                               dialog.dismiss();
                       }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});

        CheckBox saveLogsCheckBox = (CheckBox) rootView.findViewById(R.id.saveLogsCheckBox);
        saveLogsCheckBox.setSelected(model.isSaveLogs());
        saveLogsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setSaveLogs(isChecked);
            }});

        Button runButton = (Button) rootView.findViewById(R.id.runButton);
        runButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRunUsbTest(model);
                }
            }});

        return rootView;
    }
}
