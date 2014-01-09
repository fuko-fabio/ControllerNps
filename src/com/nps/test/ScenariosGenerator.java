package com.nps.test;

import java.util.ArrayList;
import java.util.List;

import com.nps.architecture.Hub;
import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;
import com.nps.micro.model.TestsViewModel;

public class ScenariosGenerator {

    private TestsViewModel model;

    public ScenariosGenerator(TestsViewModel model){
        this.model = model;
    }

    public List<Scenario> generate() {
        List<Scenario> scenarios = new ArrayList<Scenario>();
        for (Sequence sequence : model.getSequences()) {
            generateScenariosForThreadPriorities(scenarios, sequence);
        }
        return scenarios;
    }

    private void generateScenariosForThreadPriorities(List<Scenario> scenarios, Sequence sequence) {
        List<ThreadPriority> priorities = new ArrayList<ThreadPriority>();
        if (model.isNormalThreadPriority()) {
            priorities.add(ThreadPriority.NORMAL);
        }
        if (model.isHiJavaThreadPriority()) {
            priorities.add(ThreadPriority.JAVA_BASED_HIGH);
        }
        if (model.isHiAndroidThreadPriority()) {
            priorities.add(ThreadPriority.ANDROID_BASED_HIGH);
        }
        for (ThreadPriority priority : priorities) {
            generateScenariosForDevices(scenarios, sequence, priority);
        }
    }

    private void generateScenariosForDevices(List<Scenario> scenarios, Sequence sequence,
            ThreadPriority priority) {
        if (model.isExtendedDevicesCombination()) {
            String[] devices = model.getDevices();
            for(int i = 0; i < devices.length; i++) {
                String[] testDevices = new String[i + 1];
                for(int j = 0; j <= i; j++) {
                    testDevices[j] = devices[j];
                }
                generateScenariosForStreamInSizes(scenarios, sequence, priority, testDevices);
            }
        } else {
            generateScenariosForStreamInSizes(scenarios, sequence, priority, model.getDevices());
        }
    }

    private void generateScenariosForStreamInSizes(List<Scenario> scenarios, Sequence sequence, ThreadPriority priority, String[] devices) {
        for(short streamInSize : model.getStreamInSizes()) {
            scenarios.add(buildScenario(sequence, priority, devices, streamInSize));
        }
    }
    
    private Scenario buildScenario(Sequence sequence, ThreadPriority priority, String[] devices, short streamInSize) {
        return new ScenarioBuilder().withRepeats(model.getRepeats())
                .withSequence(sequence)
                .withStreamInSize(streamInSize)
                .withStreamOutSize(model.getStreamOutSize())
                .withThreadPriority(priority)
                .withDevices(clearPostfixes(devices))
                .withStreamBufferSize(model.getStreamBufferSize() * model.getStreamBufferUnit().getMultiplier())
                .withHub(model.isFastHub() ? Hub.FAST : Hub.NORMAL)
                .isSaveSpeedLogs(model.isSaveLogs())
                .isSaveStreamData(model.isSaveStreams())
                .withSimulateComputations(model.getSimulateComputations())
                .withStorageType(model.getStorageType()).build();
    }

    private String[] clearPostfixes(String[] devices) {
        for( String device : devices) {
            device.replaceAll("'", "");
        }
        return devices;
    }
}
