/*******************************************************************************
 * Copyright 2014 Norbert Pabian.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 ******************************************************************************/
package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class ReceivedPacket {

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
