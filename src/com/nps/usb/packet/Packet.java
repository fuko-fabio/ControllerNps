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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import com.nps.usb.microcontroller.Command;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class Packet {

    Command command;
    short size;
    byte[] data;

    public Packet(Command command, short size, byte[] data) {
        super();
        this.command = command;
        this.size = size;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public short getSize() {
        return size;
    }

    public void setSize(short size) {
        this.size = size;
    }

    public byte[] toByteArray() {
        byte[] sizeBytes = bytesFromShort(this.size);
        byte[] packet = this.data;
        packet[0] = this.command.value();
        packet[4] = sizeBytes[0];
        packet[5] = sizeBytes[1];
        return packet;
    }

    public static short shortFromBytes(byte firstByte, byte secondByte) {
        byte[] byteArray = new byte[2];

        byteArray[0] = firstByte;
        byteArray[1] = secondByte;
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        return shortBuffer.get();
    }

    public static byte[] bytesFromShort(short value) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }

    public static ReceivedPacket parse(byte[] buffer) {
        ReceivedPacket data = new ReceivedPacket();
        data.setAnalogOne(Packet.shortFromBytes(buffer[20], buffer[21]));
        data.setAnalogTwo(Packet.shortFromBytes(buffer[22], buffer[23]));
        data.setAnalogThree(Packet.shortFromBytes(buffer[24], buffer[25]));
        data.setAnalogFour(Packet.shortFromBytes(buffer[26], buffer[27]));

        short digitalInputs = Packet.shortFromBytes(buffer[16], buffer[17]);
        data.setDigitalOne((digitalInputs & 0x01) == 1);
        data.setDigitalTwo(((digitalInputs >> 1) & 0x01) == 1);
        data.setDigitalThree(((digitalInputs >> 2) & 0x01) == 1);
        data.setDigitalFour(((digitalInputs >> 3) & 0x01) == 1);
        return data;
    }
}
