package com.nps.micro.model;

import com.nps.usb.microcontroller.Architecture;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class TestsViewModel {

    private int repeats;
    private short streamOutSize;
    private short[] streamInSize;
    private String[] devices;
    private Architecture[] architectures;
    private boolean saveLogs;

    public TestsViewModel() {
        super();
        repeats = 1;
        streamOutSize = 16;
        streamInSize = new short[]{48};
        devices = null;
        architectures = new Architecture[]{Architecture.SRSR_STANDARD_PRIORITY};
        saveLogs = false;
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
    public Architecture[] getArchitectures() {
        return architectures;
    }
    public void setArchitectures(Architecture[] arhitectures) {
        this.architectures = arhitectures;
    }
    public boolean isSaveLogs() {
        return saveLogs;
    }
    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }
}
