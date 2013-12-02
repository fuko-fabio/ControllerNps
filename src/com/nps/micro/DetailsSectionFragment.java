package com.nps.micro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

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

        //TODO Arhitecture values
        final TextView arhitectureText = (TextView) rootView.findViewById(R.id.selectedArhitectureText);
        arhitectureText.setText(getResources().getStringArray(R.array.arhitecture_array)[0]);
        Button arhitectureButton = (Button) rootView.findViewById(R.id.selectArhitectureButton);
        arhitectureButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.arhitecture)
                       .setItems(R.array.arhitecture_array, new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                               arhitectureText.setText(getResources().getStringArray(R.array.arhitecture_array)[which]);
                               dialog.dismiss();
                       }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});

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
