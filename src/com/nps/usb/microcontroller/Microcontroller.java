package com.nps.usb.microcontroller;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;
import com.nps.usb.packet.Packet;
import com.nps.usb.packet.PacketBuilder;
import com.nps.usb.packet.PacketSize;

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

    /**
     * Reads current microcontroller packets size
     *  
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public void getStreamParameters() throws MicrocontrollerException {
        isCommandMode();
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

    /**
     * Sets microcontroller packets in/out size
     * 
     * @param streamOutSize output packet size
     * @param streamInSize input packet size
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public void setStreamParameters(short streamOutSize, short streamInSize)
            throws MicrocontrollerException {
        isCommandMode();
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

    /**
     * Sends packet to microcontroller
     * 
     * @param packet packet to send, can be null then default packet will be sent
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public void sendStreamPacket(Packet packet) throws MicrocontrollerException {
        isStreamMode();
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

    /**
     * Receives packet from microcontroller
     * 
     * @return received packet
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public byte[] receiveStreamPacket() throws MicrocontrollerException {
        isStreamMode();
        try {
            this.usbGate.receive(testStreamInBuffer);
        } catch (IllegalAccessException e) {
            throw new MicrocontrollerException("Cannot read stream packet cause: " + e.getMessage());
        }
        return testStreamInBuffer;
    }

    /**
     * Asynchronously send packet to microcontroller 
     *
     * @param packet packet to send, can be null then default packet will be sent
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public void sendAsyncStreamPacket(Packet packet) throws MicrocontrollerException {
        isStreamMode();
        if (packet == null) {
            this.usbGate.sendAsync(testStreamOutBuffer);
        } else {
            this.usbGate.sendAsync(packet.toByteArray());
        }
    }

    /**
     * Asynchronously receive packet from microcontroller 
     *
     * @throws MicrocontrollerException if operation cannot be executed
     */
    public void receiveAsyncStreamPacket() throws MicrocontrollerException {
        isStreamMode();
        this.usbGate.receiveAsync(testStreamInBuffer);
    }

    /**
     * Initialize async communication with microcontroller
     * 
     * @throws IllegalAccessException
     */
    public void initAsyncCommunication() throws IllegalAccessException {
        this.usbGate.initAsyncUsbRequests();
    }

    /**
     * Returns last sent buffer to microcontroller
     * 
     * @return last sent buffer
     */
    public byte[] getLastSentStreamPacket() {
        return testStreamOutBuffer;
    }

    /**
     * Returns last received buffer from microcontroller
     * 
     * @return last received buffer
     */
    public byte[] getLastReceivedStreamPacket() {
        return testStreamInBuffer;
    }

    /**
     * Avaiting for one async send/read request
     * 
     * @return a complete UsbRequest object or null if error occurred
     */
    public UsbRequest asyncRequestWait() {
        return this.usbGate.asyncRequestWait();
    }

    /**
     * Switch communication to command mode
     * 
     * @throws MicrocontrollerException
     * @throws UsbGateException
     */
    public void switchToCommandMode() throws MicrocontrollerException {
        isStreamMode();
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

    /**
     * Returns current microcontroller packet size object
     * 
     * @return PacketSize object with current settings
     */
    public PacketSize getCurrentPacketSize() {
        return packetSize;
    }

    /**
     * Returns device name based on linux catalog manes
     * 
     * @return device name
     */
    public String getDeviceName() {
        return usbGate.getDeviceName();
    }

    private void updatePacketSize(byte[] inputBuffer) {
        packetSize.setStreamOutSize(Packet.shortFromBytes(inputBuffer[16], inputBuffer[17]));
        packetSize.setStreamInSize(Packet.shortFromBytes(inputBuffer[18], inputBuffer[19]));
    }

    private void updateTestBuffers() {
        testStreamInBuffer = new byte[packetSize.getStreamInSize()];
        testStreamOutBuffer = new PacketBuilder(packetSize.getStreamOutSize())
                                  .withCommand(Command.SEND_STREAM_PACKET)
                                  .withLastByte((byte) 'a')
                                  .build().toByteArray();
    }

    private void isStreamMode() throws MicrocontrollerException {
        if (mode != Mode.STREAM) {
            throw new MicrocontrollerException(
                    "Cannot eval method. Current communication mode is 'COMMAND'");
        }
    }

    private void isCommandMode() throws MicrocontrollerException {
        if (mode != Mode.COMMAND) {
            throw new MicrocontrollerException(
                    "Cannot eval method. Current communication mode is 'STREAM'");
        }
    }
}
