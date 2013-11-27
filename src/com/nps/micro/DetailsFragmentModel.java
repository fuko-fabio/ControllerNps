package com.nps.micro;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class DetailsFragmentModel {

    int numberOfRepeats;
    short packetOutSize;
    short packetInSize;
    String device;
    Arhitecture arhitecture;
    boolean saveLogs;

    public DetailsFragmentModel() {
        super();
        numberOfRepeats = 1;
        packetOutSize = 16;
        packetInSize = 48;
        device = "All"; // TODO FIXME
        arhitecture = Arhitecture.SEQUENCE;
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
