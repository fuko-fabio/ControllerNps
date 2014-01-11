package com.nps.usb.packet;


public class ReceivedPacketData {

    private boolean digitalOne;
    private boolean digitalTwo;
    private boolean digitalThree;
    private boolean digitalFour;

    private short analogOne;
    private short analogTwo;
    private short analogThree;
    private short analogFour;

    public boolean getDigitalOne() {
        return digitalOne;
    }

    public void setDigitalOne(boolean digitalOne) {
        this.digitalOne = digitalOne;
    }

    public boolean getDigitalTwo() {
        return digitalTwo;
    }

    public void setDigitalTwo(boolean digitalTwo) {
        this.digitalTwo = digitalTwo;
    }

    public boolean getDigitalThree() {
        return digitalThree;
    }

    public void setDigitalThree(boolean digitalThree) {
        this.digitalThree = digitalThree;
    }

    public boolean getDigitalFour() {
        return digitalFour;
    }

    public void setDigitalFour(boolean digitalFour) {
        this.digitalFour = digitalFour;
    }

    public short getAnalogOne() {
        return analogOne;
    }

    public void setAnalogOne(short analogOne) {
        this.analogOne = analogOne;
    }

    public short getAnalogTwo() {
        return analogTwo;
    }

    public void setAnalogTwo(short analogTwo) {
        this.analogTwo = analogTwo;
    }

    public short getAnalogThree() {
        return analogThree;
    }

    public void setAnalogThree(short analogThree) {
        this.analogThree = analogThree;
    }

    public short getAnalogFour() {
        return analogFour;
    }

    public void setAnalogFour(short analogFour) {
        this.analogFour = analogFour;
    }

}
