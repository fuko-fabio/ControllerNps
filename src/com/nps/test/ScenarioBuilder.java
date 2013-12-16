package com.nps.test;

import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;

public class ScenarioBuilder {

    private int repeats;
    private short streamOutSize;
    private short[] streamInSize;
    private String[] devices;
    private ThreadPriority threadPriority;
    private Sequence sequence;
    private boolean saveSpeedLogs;
    private boolean saveStreamData;
    private boolean simulateComputations;

    public ScenarioBuilder withRepeats(int repeats) {
        this.repeats = repeats;
        return this;
    }

    public ScenarioBuilder withStreamOutSize(short streamOutSize) {
        this.streamOutSize = streamOutSize;
        return this;
    }

    public ScenarioBuilder withStreamInSize(short[] streamInSize) {
        this.streamInSize = streamInSize;
        return this;
    }

    public ScenarioBuilder withDevices(String[] devices) {
        this.devices = devices;
        return this;
    }

    public ScenarioBuilder withThreadPriority(ThreadPriority threadPriority) {
        this.threadPriority = threadPriority;
        return this;
    }

    public ScenarioBuilder withSequence(Sequence sequence) {
        this.sequence = sequence;
        return this;
    }

    public ScenarioBuilder isSaveSpeedLogs(boolean saveSpeedLogs) {
        this.saveSpeedLogs = saveSpeedLogs;
        return this;
    }

    public ScenarioBuilder isSaveStreamData(boolean saveStreamData) {
        this.saveStreamData = saveStreamData;
        return this;
    }

    public ScenarioBuilder isSimulateComputations(boolean simulateComputations) {
        this.simulateComputations = simulateComputations;
        return this;
    }

    public Scenario build() {
        Scenario scenario = new Scenario();
        scenario.setDevices(devices);
        scenario.setRepeats(repeats);
        scenario.setSaveSpeedLogs(saveSpeedLogs);
        scenario.setSaveStreamData(saveStreamData);
        scenario.setSequence(sequence);
        scenario.setSimulateComputations(simulateComputations);
        scenario.setStreamInSizes(streamInSize);
        scenario.setStreamOutSize(streamOutSize);
        scenario.setThreadPriority(threadPriority);
        return scenario;
    }
}
