package com.nps.usb.microcontroller;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class Microcontroller {

    private static final String TAG = "Microcontroller";

    private Mode mode = Mode.COMMAND;
    private PacketSize packetSize = new PacketSize();
    private UsbGate usbGate;

    private byte[] testStreamInBuffer;
    private byte[] testStreamOutBuffer;

    public Microcontroller(UsbManager usbManager, UsbDevice device)
            throws IllegalArgumentException, UsbGateException {
        usbGate = new UsbGate(usbManager, device);
        Log.d(TAG, "USB gate created succesfully for device: " + device);
        usbGate.createConnection();
        Log.d(TAG, "USB connection oppened succesfully for device: " + device);
    }

    public void closeConnection() {
        usbGate.close();
    }

    public void getStreamParameters() throws MicrocontrollerException {
        if (mode == Mode.STREAM) {
            throw new MicrocontrollerException(
                    "Cannot read stream parameters. Current communication mode is 'STREAM'");
        }
        Packet packet = new PacketBuilder(packetSize.getDefaultStreamOutSize())
                .withCommand(Command.GET_STREAM_PARAMETERS).withLastByte((byte) 'a').build();
        byte[] receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
        try {
            this.usbGate.send(packet);
            this.usbGate.receive(receivedBuffer);
            updatePacketSize(receivedBuffer);
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot read stream parameters cause: "
                    + e.getMessage());
        }
    }

    public void setStreamParameters(short streamOutSize, short streamInSize)
            throws MicrocontrollerException {
        if (mode == Mode.STREAM) {
            throw new MicrocontrollerException(
                    "Cannot set stream parameters. Current communication mode is 'STREAM'");
        }
        byte[] streamOutSizeBytes = Packet.bytesFromShort(streamOutSize);
        byte[] streamInSizeBytes = Packet.bytesFromShort(streamInSize);
        Packet packet = new PacketBuilder(packetSize.getDefaultStreamOutSize())
                .withCommand(Command.SET_STREAM_PARAMETERS).withByte(8, streamOutSizeBytes[0])
                .withByte(9, streamOutSizeBytes[1]).withByte(10, streamInSizeBytes[0])
                .withByte(11, streamInSizeBytes[1]).build();
        byte[] receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
        try {
            this.usbGate.send(packet);
            this.usbGate.receive(receivedBuffer);
            updatePacketSize(receivedBuffer);
            updateTestBuffers();
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot set stream parameters cause: "
                    + e.getMessage());
        }
    }

    private void updateTestBuffers() {
        testStreamInBuffer = new byte[packetSize.getStreamInSize()];
        testStreamOutBuffer = new PacketBuilder(packetSize.getStreamOutSize())
                                  .withCommand(Command.SEND_STREAM_PACKET)
                                  .withLastByte((byte) 'a')
                                  .build().toByteArray();
    }

    public void sendStreamPacket(Packet packet) throws MicrocontrollerException {
        if (mode == Mode.COMMAND) {
            throw new MicrocontrollerException(
                    "Cannot seant stream packet. Current communication mode is 'COMMAND'");
        }
        try {
            if (packet == null) {
                this.usbGate.send(testStreamOutBuffer);
            } else {
                this.usbGate.send(packet);
            }
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot send stream packet cause: " + e.getMessage());
        }
    }

    public byte[] receiveStreamPacket() throws MicrocontrollerException {
        if (mode == Mode.COMMAND) {
            throw new MicrocontrollerException(
                    "Cannot read stream packet. Current communication mode is 'COMMAND'");
        }
        try {
            this.usbGate.receive(testStreamInBuffer);
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot read stream packet cause: " + e.getMessage());
        }
        return testStreamInBuffer;
    }

    public byte[] getLastReceivedStreamPacket() {
        return testStreamInBuffer;
    }

    /**
     * Switch communication to command mode
     * 
     * @throws MicrocontrollerException
     * @throws UsbGateException
     */
    public void switchToCommandMode() throws MicrocontrollerException {
        if (mode == Mode.COMMAND) {
            return;
        }
        byte[] streamOutSizeBytes = Packet.bytesFromShort(packetSize.getDefaultStreamOutSize());
        byte[] streamInSizeBytes = Packet.bytesFromShort(packetSize.getDefaultStreamInSize());
        Packet packet = new PacketBuilder(packetSize.getStreamOutSize())
                .withCommand(Command.RESET_PACKETS).withByte(8, streamOutSizeBytes[0])
                .withByte(9, streamOutSizeBytes[1]).withByte(10, streamInSizeBytes[0])
                .withByte(11, streamInSizeBytes[1]).withLastByte((byte) 'a').build();
        byte[] receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
        try {
            this.usbGate.send(packet);
            this.usbGate.receive(receivedBuffer);
            updatePacketSize(receivedBuffer);
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot switch to command mode cause: "
                    + e.getMessage());
        }
        mode = Mode.COMMAND;
    }

    /**
     * Switch communication to stream mode
     * 
     * @throws MicrocontrollerException
     * @throws UsbGateException
     */
    public void switchToStreamMode() throws MicrocontrollerException {
        if (mode == Mode.STREAM) {
            return;
        }
        Packet packet = new PacketBuilder(packetSize.getDefaultStreamOutSize())
                .withCommand(Command.SWITCH_TO_STREAM).withLastByte((byte) 'a').build();
        byte[] receivedBuffer = new byte[packetSize.getStreamInSize()];
        try {
            this.usbGate.send(packet);
            this.usbGate.receive(receivedBuffer);
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot switch to stream mode: " + e.getMessage());
        }
        mode = Mode.STREAM;
    }

    /**
     * Current communication mode
     * 
     * @return communication mode
     */
    public Mode getMode() {
        return mode;
    }

    public PacketSize getCurrentPacketSize() {
        return packetSize;
    }

    public String getDeviceName() {
        return usbGate.getDeviceName();
    }

    private void updatePacketSize(byte[] inputBuffer) {
        packetSize.setStreamOutSize(Packet.shortFromBytes(inputBuffer[16], inputBuffer[17]));
        packetSize.setStreamInSize(Packet.shortFromBytes(inputBuffer[18], inputBuffer[19]));
    }
}
