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

import com.nps.usb.microcontroller.Command;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class PacketBuilder {

    Command command;
    short size;
    byte[] packetData;

    public PacketBuilder(short packetSize) {
        super();
        this.size = packetSize;
        this.packetData = new byte[this.size];
    }

    public PacketBuilder withCommand(Command command) {
        this.command = command;
        return this;
    }

    public PacketBuilder withByte(int position, byte data) {
        this.packetData[position] = data;
        return this;
    }

    public PacketBuilder withLastByte(byte data) {
        this.packetData[size - 1] = data;
        return this;
    }

    public Packet build() {
        return new Packet(command, size, packetData);
    }

}
