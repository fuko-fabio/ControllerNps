package com.nps.micro.model;

import com.nps.architecture.Sequence;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class TestsViewModel {

    private int repeats;
    private short streamOutSize;
    private short[] streamInSize;
    private String[] devices;
    private Sequence[] sequences;
    private boolean saveLogs;
    private boolean saveStreams;
    private boolean normalThreadPriority;
    private boolean hiJavaThreadPriority;
    private boolean hiAndroidThreadPriority;
    private boolean simulateComputations;
    private boolean extendedDevicesCombination;

    public TestsViewModel() {
        super();
        repeats = 1;
        streamOutSize = 16;
        streamInSize = new short[] { 48 };
        devices = null;
        sequences = new Sequence[] {Sequence.SRSR};
        normalThreadPriority = true;
        hiJavaThreadPriority = false;
        hiAndroidThreadPriority = false;
        saveLogs = false;
        saveStreams = false;
        extendedDevicesCombination = false;
    }

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

    public short[] getStreamInSizes() {
        return streamInSize;
    }

    public void setStreamInSize(short[] streamInSize) {
        this.streamInSize = streamInSize;
    }

    public String[] getDevices() {
        return devices;
    }

    public void setDevices(String[] devices) {
        this.devices = devices;
    }

    public boolean isSaveLogs() {
        return saveLogs;
    }

    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }

    public boolean isHiJavaThreadPriority() {
        return hiJavaThreadPriority;
    }

    public void setHiJavaThreadPriority(boolean hiJavaThreadPriority) {
        this.hiJavaThreadPriority = hiJavaThreadPriority;
    }

    public boolean isHiAndroidThreadPriority() {
        return hiAndroidThreadPriority;
    }

    public void setHiAndroidThreadPriority(boolean hiAndroidThreadPriority) {
        this.hiAndroidThreadPriority = hiAndroidThreadPriority;
    }

    public boolean isSimulateComputations() {
        return simulateComputations;
    }

    public void setSimulateComputations(boolean simulateComputations) {
        this.simulateComputations = simulateComputations;
    }

    public boolean isSaveStreams() {
        return saveStreams;
    }

    public void setSaveStreams(boolean saveStreams) {
        this.saveStreams = saveStreams;
    }

    public Sequence[] getSequences() {
        return sequences;
    }

    public void setSequences(Sequence[] sequences) {
        this.sequences = sequences;
    }

    public boolean isNormalThreadPriority() {
        return normalThreadPriority;
    }

    public void setNormalThreadPriority(boolean normalThreadPriority) {
        this.normalThreadPriority = normalThreadPriority;
    }

    public boolean isExtendedDevicesCombination() {
        return extendedDevicesCombination;
    }

    public void setExtendedDevicesCombination(boolean extendedDevicesCombination) {
        this.extendedDevicesCombination = extendedDevicesCombination;
    }
}
