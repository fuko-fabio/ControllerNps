package com.nps.micro;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class DetailsViewModel {

    private int numberOfRepeats;
    private short packetOutSize;
    private short packetInSize;
    private String device;
    private Arhitecture arhitecture;
    private boolean saveLogs;

    public DetailsViewModel() {
        super();
        numberOfRepeats = 1;
        packetOutSize = 16;
        packetInSize = 48;
        device = null;
        arhitecture = Arhitecture.SEQUENCE_SRSR;
        saveLogs = true;
    }

    public int getNumberOfRepeats() {
        return numberOfRepeats;
    }
    public void setNumberOfRepeats(int numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
    }
    public short getPacketOutSize() {
        return packetOutSize;
    }
    public void setPacketOutSize(short packetOutSize) {
        this.packetOutSize = packetOutSize;
    }
    public short getPacketInSize() {
        return packetInSize;
    }
    public void setPacketInSize(short packetInSize) {
        this.packetInSize = packetInSize;
    }
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public Arhitecture getArhitecture() {
        return arhitecture;
    }
    public void setArhitecture(Arhitecture arhitecture) {
        this.arhitecture = arhitecture;
    }
    public boolean isSaveLogs() {
        return saveLogs;
    }
    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }
}
