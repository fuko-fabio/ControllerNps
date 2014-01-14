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
package com.nps.usb.microcontroller;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public enum Command {

    UNKNOWN              ((byte) 0),
    SEND_STREAM_PACKET   ((byte) 1),
    RESET_PACKETS        ((byte) 2),
    GET_STREAM_PARAMETERS((byte) 3),
    SWITCH_TO_STREAM     ((byte) 4),
    SET_STREAM_PARAMETERS((byte) 5);

    private byte value = 0;

    Command(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }

    public static Command fromValue(byte id) {
        Command[] PacketCommands = Command.values();
        for (int i = 0; i < PacketCommands.length; i++) {
            if (PacketCommands[i].value() == id)
                return PacketCommands[i];
        }
        return Command.UNKNOWN;
    }

}
