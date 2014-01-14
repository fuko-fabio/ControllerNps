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
package com.nps.micro.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.nps.micro.R;
import com.nps.usb.packet.Packet;
import com.nps.usb.packet.ReceivedPacket;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class GraphSectionFragment extends BaseSectionFragment {

    private static TimeSeries analogOneSeries;
    private static TimeSeries analogTwoSeries;
    private static TimeSeries analogThreeSeries;
    private static TimeSeries analogFourSeries;
    private static XYMultipleSeriesDataset dataset;
    private static XYMultipleSeriesRenderer renderer;
    private static XYSeriesRenderer analogOneSeriesRenedrer;
    private static XYSeriesRenderer analogTwoSeriesRenedrer;
    private static XYSeriesRenderer analogThreeSeriesRenedrer;
    private static XYSeriesRenderer analogFourSeriesRenedrer;
    private static GraphicalView graphicalView;
    private static GraphUpdaterThread graphUpdaterThread;

    private ToggleButton enableButton;
    private Spinner devicesSpinner;
    private boolean enabled = false;
    private boolean autoEnableGraph = false;

    private int currentDeviceIndex = 0;
    private ArrayList<String> devicesList = new ArrayList<String>();
    private ArrayAdapter<String> devicesListAdapter;

    private GraphFragmentListener listener;

    public GraphSectionFragment() {
        this.layout = R.layout.graph;

        dataset = new XYMultipleSeriesDataset();

        renderer = new XYMultipleSeriesRenderer();
        renderer.setAxesColor(Color.CYAN);
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(15);
        renderer.setFitLegend(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setPanEnabled(false, false);
        renderer.setMargins( new int []{20, 20, 10, 0});
        renderer.setZoomButtonsVisible(false);
        renderer.setBarSpacing(5);
        renderer.setShowGrid(true);

        analogOneSeriesRenedrer = new XYSeriesRenderer();
        analogOneSeriesRenedrer.setColor(Color.RED);
        renderer.addSeriesRenderer(analogOneSeriesRenedrer);
        
        analogTwoSeriesRenedrer = new XYSeriesRenderer();
        analogTwoSeriesRenedrer.setColor(Color.BLUE);
        renderer.addSeriesRenderer(analogTwoSeriesRenedrer);
        
        analogThreeSeriesRenedrer = new XYSeriesRenderer();
        analogThreeSeriesRenedrer.setColor(Color.GREEN);
        renderer.addSeriesRenderer(analogThreeSeriesRenedrer);
        
        analogFourSeriesRenedrer = new XYSeriesRenderer();
        analogFourSeriesRenedrer.setColor(Color.YELLOW);
        renderer.addSeriesRenderer(analogFourSeriesRenedrer);

        analogOneSeries = new TimeSeries("Analog One");
        analogTwoSeries = new TimeSeries("Analog Two");
        analogThreeSeries = new TimeSeries("Analog Threr");
        analogFourSeries = new TimeSeries("Analog Four");
        
        dataset.addSeries(analogOneSeries);
        dataset.addSeries(analogTwoSeries);
        dataset.addSeries(analogThreeSeries);
        dataset.addSeries(analogFourSeries);

        graphUpdaterThread = new GraphUpdaterThread(analogOneSeries,
                analogTwoSeries, analogThreeSeries, analogFourSeries);
    }

    @Override
    public void onDestroy() {
        graphUpdaterThread.finalize();
        super.onDestroy();
    }

    public void setOnGraphFragmentListener(GraphFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);
        LinearLayout parentLayout = (LinearLayout) rootView.findViewById(R.id.graphLayout);

        renderer.setChartTitle(getString(R.string.analog_inputs));
        renderer.setXTitle(getString(R.string.time));
        renderer.setYTitle(getString(R.string.value));
        graphicalView = ChartFactory.getTimeChartView(getActivity(), dataset, renderer, "Test");

        graphicalView.refreshDrawableState();
        graphicalView.repaint();
        parentLayout.addView(graphicalView);

        graphUpdaterThread.setGraphicalView(graphicalView);
        if(!graphUpdaterThread.isAlive()){
            graphUpdaterThread.start();
        }

        enableButton = (ToggleButton) rootView.findViewById(R.id.enableGraphToggle);
        enableButton.setChecked(enabled);
        enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enabled = isChecked;
                graphUpdaterThread.pause(!isChecked);
            }
        });

        devicesSpinner = (Spinner) rootView.findViewById(R.id.devicesSpinner);
        devicesListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.text_view, this.devicesList);
        devicesSpinner.setAdapter(devicesListAdapter);
        devicesSpinner.setSelection(currentDeviceIndex);
        devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                currentDeviceIndex = pos;
                graphUpdaterThread.forDevice(devicesList.get(pos));
                renderer.setChartTitle(getString(R.string.analog_inputs) + ' ' + devicesList.get(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        updateStatus(rootView);
        return rootView;
    }

    public void setAvailableDevices(List<String> devicesList) {
        this.devicesList.clear();
        this.devicesList.addAll(devicesList);
        devicesListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.text_view, this.devicesList);
        devicesSpinner.setAdapter(devicesListAdapter);
    }

    public void setListener(GraphFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void updateStatus(String status, boolean busy) {
        super.updateStatus(status, busy);
        if(autoEnableGraph) {
            enabled = busy;
            graphUpdaterThread.pause(!busy);
            enableButton.setChecked(busy);
        } else if (!busy) {
            enabled = false;
            graphUpdaterThread.pause(true);
            enableButton.setChecked(false);
        }
    }

    public void setAutoEnableGraph(boolean autoEnableGraph) {
        this.autoEnableGraph = autoEnableGraph;
    }

    private class GraphUpdaterThread extends Thread {

        private static final int MAX_ITEMS = 200;
        private boolean pause = true;
        private boolean run = true;

        private final TimeSeries analogOneSeries;
        private final TimeSeries analogTwoSeries;
        private final TimeSeries analogThreeSeries;
        private final TimeSeries analogFourSeries;
        private GraphicalView graphicalView;

        private String currentDeviceName;

        public GraphUpdaterThread(TimeSeries analogOneSeries,
                TimeSeries analogTwoSeries, TimeSeries analogThreeSeries, TimeSeries analogFourSeries) {
            this.analogOneSeries = analogOneSeries;
            this.analogTwoSeries = analogTwoSeries;
            this.analogThreeSeries = analogThreeSeries;
            this.analogFourSeries = analogFourSeries;
        }

        public void run() {
            while (run) {
                if (!pause) {
                    if (analogOneSeries.getItemCount() > MAX_ITEMS) {
                        analogOneSeries.remove(0);
                        analogTwoSeries.remove(0);
                        analogThreeSeries.remove(0);
                        analogFourSeries.remove(0);
                    }

                    byte[] packet = listener.getLastReceivedPacket(currentDeviceName);
                    if(packet != null) {
                        ReceivedPacket data = Packet.parse(packet);
                        analogOneSeries.add(new Date(), data.getAnalogOne());
                        analogTwoSeries.add(new Date(), data.getAnalogTwo());
                        analogThreeSeries.add(new Date(), data.getAnalogThree());
                        analogFourSeries.add(new Date(), data.getAnalogFour());
                        graphicalView.repaint();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void pause(boolean pause) {
            if(!pause) {
                analogOneSeries.clear();
                analogTwoSeries.clear();
                analogThreeSeries.clear();
                analogFourSeries.clear();
            }
            this.pause = pause;
        }

        public void finalize() {
            this.run = false;
        }

        public void forDevice(String deviceName) {
            this.currentDeviceName = deviceName;
        }

        public void setGraphicalView(GraphicalView view) {
            this.graphicalView = view;
        }
    }
}
