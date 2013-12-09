package com.nps.micro.model;

import com.nps.usb.microcontroller.Arhitecture;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class DetailsViewModel {

    private int repeats;
    private int streamOutSize;
    private int[] streamInSize;
    private String device;
    private Arhitecture[] arhitectures;
    private boolean saveLogs;

    public DetailsViewModel() {
        super();
        repeats = 1;
        streamOutSize = 16;
        streamInSize = new int[]{48};
        device = null;
        arhitectures = new Arhitecture[]{Arhitecture.SRSR_STANDARD_PRIORITY};
        saveLogs = false;
    }

    public int getRepeats() {
        return repeats;
    }
    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }
    public int getStreamOutSize() {
        return streamOutSize;
    }
    public void setStreamOutSize(short streamOutSize) {
        this.streamOutSize = streamOutSize;
    }
    public int[] getStreamInSize() {
        return streamInSize;
    }
    public void setStreamInSize(int[] streamInSize) {
        this.streamInSize = streamInSize;
    }
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
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
