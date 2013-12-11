package com.nps.micro.model;

import com.nps.usb.microcontroller.Arhitecture;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class DetailsViewModel {

    private int repeats;
    private short streamOutSize;
    private short[] streamInSize;
    private String[] devices;
    private Arhitecture[] arhitectures;
    private boolean saveLogs;

    public DetailsViewModel() {
        super();
        repeats = 1;
        streamOutSize = 16;
        streamInSize = new short[]{48};
        devices = null;
        arhitectures = new Arhitecture[]{Arhitecture.SRSR_STANDARD_PRIORITY};
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
    public Arhitecture[] getArhitectures() {
        return arhitectures;
    }
    public void setArhitectures(Arhitecture[] arhitectures) {
        this.arhitectures = arhitectures;
    }
    public boolean isSaveLogs() {
        return saveLogs;
    }
    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }
}
