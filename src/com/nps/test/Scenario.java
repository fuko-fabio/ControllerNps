package com.nps.test;

import com.nps.architecture.Hub;
import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;
import com.nps.storage.Storage;

public class Scenario {

    private int repeats;
    private short streamOutSize;
    private short streamInSize;
    private String[] devices;
    private ThreadPriority threadPriority;
    private Sequence sequence;
    private Hub hub;
    private boolean saveSpeedLogs;
    private boolean saveStreamData;
    private short simulateComputations;
    private Storage.Type storageType;
    private int streamBufferSize;
    private int streamQueueSize;

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public short getStreamOutSize() {
        return streamOutSize;
    }

    public void setStreamOutSize(short streamOutSize) {
        this.streamOutSize = streamOutSize;
    }

    public short getStreamInSize() {
        return streamInSize;
    }

    public void setStreamInSize(short streamInSize) {
        this.streamInSize = streamInSize;
    }

    public ThreadPriority getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(ThreadPriority threadPriority) {
        this.threadPriority = threadPriority;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public String[] getDevices() {
        return devices;
    }

    public void setDevices(String[] devices) {
        this.devices = devices;
    }

    public boolean isSaveSpeedLogs() {
        return saveSpeedLogs;
    }

    public void setSaveSpeedLogs(boolean saveSpeedLogs) {
        this.saveSpeedLogs = saveSpeedLogs;
    }

    public boolean isSaveStreamData() {
        return saveStreamData;
    }

    public void setSaveStreamData(boolean saveStreamData) {
        this.saveStreamData = saveStreamData;
    }

    public short getSimulateComputations() {
        return simulateComputations;
    }

    public void setSimulateComputations(short simulateComputations) {
        this.simulateComputations = simulateComputations;
    }

    public Hub getHub() {
        return hub;
    }

    public void setHub(Hub hub) {
        this.hub = hub;
    }

    public Storage.Type getStorageType() {
        return storageType;
    }

    public void setStorageType(Storage.Type storageType) {
        this.storageType = storageType;
    }

    @Override
    public String toString() {
        return sequence.name() + ' ' +
               threadPriority.name() + ' ' +
               "devices: " + devices.length + ' ' + 
               "stream In size: " + streamInSize + ' ' + 
               "repeats: " + repeats;
    }

    public int getStreamBufferSize() {
        return streamBufferSize;
    }

    public void setStreamBufferSize(int streamBufferSize) {
        this.streamBufferSize = streamBufferSize;
    }

    public int getStreamQueueSize() {
        return streamQueueSize;
    }

    public void setStreamQueueSize(int streamQueueSize) {
        this.streamQueueSize = streamQueueSize;
    }
}
