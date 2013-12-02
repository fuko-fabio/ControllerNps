package com.nps.usb;

import java.nio.ByteBuffer;

import com.nps.usb.packet.Packet;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class UsbGate {

    protected UsbDevice mUsbDevice;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    private boolean connected = false;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpointIn;
    private UsbEndpoint mUsbEndpointOut;
    private UsbDeviceConnection mUsbConnection;
    private UsbManager mUsbManager;
    private UsbRequest mUsbRequest;

    /**
     * @param device USB device object
     * @throws IllegalArgumentException
     * @throws UsbGateException
     */
    public UsbGate(UsbManager usbManager, UsbDevice device) throws IllegalArgumentException,
            UsbGateException {
        if (device == null) {
            throw new IllegalArgumentException("Device not found!");
        }
        if (usbManager == null) {
            throw new IllegalArgumentException("USB manager not found!");
        }
        mUsbManager = usbManager;

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            if (device.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
                mUsbInterface = device.getInterface(i);
            }
        }
        if (mUsbInterface == null) {
            throw new UsbGateException("CDC Usb interface not found!");
        }

        for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = mUsbInterface.getEndpoint(i);
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    mUsbEndpointIn = endpoint;
                } else {
                    mUsbEndpointOut = endpoint;
                }
            }
        }
        if (mUsbEndpointIn == null || mUsbEndpointOut == null) {
            throw new UsbGateException("Endpoints not found!");
        }
        mUsbDevice = device;
    }

    /**
     * Create connection with USB device
     * 
     * @throws UsbGateException if connection cannot be established
     */
    public void createConnection() throws UsbGateException {
        mUsbConnection = mUsbManager.openDevice(mUsbDevice);
        if (mUsbConnection != null)
            connected = true;
        else
            throw new UsbGateException("Cannot get connection");
    }

    /**
     * @return true if connection with usb device is opened
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Send synchronous data via USB
     * 
     * @param packet generated packet to sent
     * @return true for success or false for failure
     * @throws IllegalAccessException if USB interface cannot be claimed
     * @throws UsbGateException if data cannot be send via USB interface
     */
    public long send(Packet packet) throws IllegalAccessException, UsbGateException {
        return send(packet.toByteArray());
    }

    /**
     * Send synchronous data via USB
     * 
     * @param buffer data to send
     * @return how long time spend on send data in nanoseconds
     * @throws IllegalAccessException if USB interface cannot be claimed
     * @throws UsbGateException if data cannot be send via USB interface
     */
    public long send(byte[] buffer) throws IllegalAccessException, UsbGateException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        long duration = System.nanoTime();
        int result = mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, buffer.length, TIMEOUT);
        duration = System.nanoTime() - duration;
        if (result < 0 ) {
            throw new UsbGateException("Couldn't send data via USB interface.");
        }
        return duration;
    }

    /**
     * Send synchronous data via USB with specified timeout
     * 
     * @param buffer data to send
     * @param timeout
     * @return how long time spend on send data in nanoseconds
     * @throws IllegalAccessException if USB interface cannot be claimed
     * @throws UsbGateException if data cannot be send via USB interface
     */
    public long send(byte[] buffer, int timeout) throws IllegalAccessException, UsbGateException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        long duration = System.nanoTime();
        int result = mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, buffer.length, timeout);
        duration = System.nanoTime() - duration;
        if (result < 0 ) {
            throw new UsbGateException("Couldn't send data via USB interface.");
        }
        return duration;
    }

    /**
     * Read synchronous data from USB
     * 
     * @param buffer received data
     * @return how long time spend on read data in nanoseconds
     * @throws IllegalAccessException  if USB interface cannot be claimed
     * @throws UsbGateException if data cannot be read from USB interface
     */
    public long receive(byte[] buffer) throws IllegalAccessException, UsbGateException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        long duration = System.nanoTime();
        int result =  mUsbConnection.bulkTransfer(mUsbEndpointIn, buffer, buffer.length, TIMEOUT);
        duration = System.nanoTime() - duration;
        if (result < 0 ) {
            throw new UsbGateException("Couldn't read data from USB interface.");
        }
        return duration;
    }

    /**
     * Read synchronous data from USB with specified timeout
     * 
     * @param buffer received data
     * @param timeout
     * @return how long time spend on read data in nanoseconds
     * @throws IllegalAccessException if USB interface cannot be claimed
     * @throws UsbGateException if data cannot be read from USB interface
     */
    public long receive(byte[] buffer, int timeout) throws IllegalAccessException, UsbGateException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        long duration = System.nanoTime();
        int result =  mUsbConnection.bulkTransfer(mUsbEndpointIn, buffer, buffer.length, timeout);
        duration = System.nanoTime() - duration;
        if (result < 0 ) {
            throw new UsbGateException("Couldn't read data from USB interface.");
        }
        return duration;
    }

    /**
     * Send asynchronous data via USB
     * 
     * @param buffer data to send
     * @return true if the operation succeeded
     * @throws IllegalAccessException if the USB request was not opened or if USB interface cannot be claimed
     */
    public boolean sendAsync(byte[] buffer) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed ");
        if (!mUsbRequest.initialize(mUsbConnection, mUsbEndpointOut))
            throw new IllegalAccessException("USB request cannot be opened");

        boolean sent = mUsbRequest.queue(ByteBuffer.wrap(buffer), buffer.length);

        mUsbRequest.cancel();
        mUsbRequest.close();
        return sent;
    }

    /**
     * Read asynchronous data from USB
     * 
     * @param buffer received data
     * @return true if the operation succeeded
     * @throws IllegalAccessException if the USB request was not opened or if USB interface cannot be claimed
     */
    public boolean receiveAsync(byte[] buffer) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed ");
        if (!mUsbRequest.initialize(mUsbConnection, mUsbEndpointIn))
            throw new IllegalAccessException("USB request cannot be opened");

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
        boolean recived = mUsbRequest.queue(byteBuffer, buffer.length);
        buffer = byteBuffer.array();

        mUsbRequest.cancel();
        mUsbRequest.close();
        return recived;
    }

    /**
     * Close USB connection and release resources
     * 
     * @return true if the operation succeeded
     * @throws IllegalArgumentException if USB connection or interface not exist
     */
    public boolean close() throws IllegalArgumentException {
        if (mUsbConnection == null)
            throw new IllegalArgumentException("USB connection not foud");
        if (mUsbInterface == null)
            throw new IllegalArgumentException("USB interface not foud");

        if (!mUsbConnection.releaseInterface(mUsbInterface))
            return false;

        mUsbConnection.close();
        return true;
    }
}
