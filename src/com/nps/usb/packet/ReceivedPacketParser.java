package com.nps.usb.packet;

public class ReceivedPacketParser {

    public static ReceivedPacketData parse(byte[] buffer) {
        ReceivedPacketData data = new ReceivedPacketData();
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