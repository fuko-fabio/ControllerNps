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
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class UsbGate {

    protected UsbDevice mUsbDevice;
    private static int TIMEOUT = 500;
    private boolean forceClaim = true;
    private boolean connected = false;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpointIn;
    private UsbEndpoint mUsbEndpointOut;
    private UsbDeviceConnection mUsbConnection;
    private UsbManager mUsbManager;
    private UsbRequest sendUsbRequest;
    private UsbRequest receiveUsbRequest;
    private boolean isAsyncInitialized;

    
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
        sendUsbRequest = new UsbRequest();
        receiveUsbRequest = new UsbRequest();
        isAsyncInitialized = false;
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
     * Returns the name of the device. In the standard implementation, this is the path of the device file for the device in the usbfs file system.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return mUsbDevice.getDeviceName();
    }

    /**
     * Send synchronous data via USB
     * 
     * @param packet generated packet to sent
     * @return length of data transferred (or zero) for success, or negative value for failure 
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public int send(Packet packet) throws IllegalAccessException {
        return send(packet.toByteArray());
    }

    /**
     * Send synchronous data via USB
     * 
     * @param buffer data to send
     * @return length of data transferred (or zero) for success, or negative value for failure 
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public int send(byte[] buffer) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, buffer.length, TIMEOUT);
    }

    /**
     * Send synchronous data via USB with specified timeout
     * 
     * @param buffer data to send
     * @param timeout
     * @return length of data transferred (or zero) for success, or negative value for failure 
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public int send(byte[] buffer, int timeout) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, buffer.length, timeout);
    }

    /**
     * Read synchronous data from USB
     * 
     * @param buffer received data
     * @return length of data transferred (or zero) for success, or negative value for failure 
     * @throws IllegalAccessException  if USB interface cannot be claimed
     */
    public long receive(byte[] buffer) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        return  mUsbConnection.bulkTransfer(mUsbEndpointIn, buffer, buffer.length, TIMEOUT);
    }

    /**
     * Read synchronous data from USB with specified timeout
     * 
     * @param buffer received data
     * @param timeout
     * @return length of data transferred (or zero) for success, or negative value for failure 
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public long receive(byte[] buffer, int timeout) throws IllegalAccessException {
        if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
            throw new IllegalAccessException("USB interface cannot be claimed");
        return  mUsbConnection.bulkTransfer(mUsbEndpointIn, buffer, buffer.length, timeout);
    }

    /**
     * Initializes the sern/read request so it can read or write data on the given endpoint.
     * 
     * @throws IllegalAccessException if USB interface cannot be claimed or usb request cannot be initialized
     */
    public void initAsyncUsbRequests() throws IllegalAccessException {
        if(!isAsyncInitialized) {
            if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
                throw new IllegalAccessException("USB interface cannot be claimed ");
            if (!sendUsbRequest.initialize(mUsbConnection, mUsbEndpointOut))
                throw new IllegalAccessException("USB request cannot be opened");
            if (!receiveUsbRequest.initialize(mUsbConnection, mUsbEndpointIn))
                throw new IllegalAccessException("USB request cannot be opened");
            isAsyncInitialized = true;
        }
    }

    /**
     * Send asynchronous data via USB
     * Initialize async usb requests before: initAsyncUsbRequests()
     * 
     * @param buffer data to send
     * @return true if the queueing operation succeeded 
     */
    public boolean sendAsync(byte[] buffer) {
        return sendUsbRequest.queue(ByteBuffer.wrap(buffer), buffer.length);
    }

    /**
     * Read asynchronous data from USB
     * Initialize async usb requests before: initAsyncUsbRequests()
     * 
     * @param buffer received data
     * @return true if the queueing operation succeeded 
     */
    public boolean receiveAsync(byte[] buffer) {
        return receiveUsbRequest.queue(ByteBuffer.wrap(buffer), buffer.length);
    }

    /**
     * Waits for the result of a queue(ByteBuffer, int) operation Note that this
     * may return requests queued on multiple UsbEndpoints. When multiple
     * endpoints are in use, getEndpoint() and getClientData() can be useful in
     * determining how to process the result of this function.
     * 
     * @return a completed USB request, or null if an error occurred
     */
    public UsbRequest asyncRequestWait() {
        return mUsbConnection.requestWait();
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
        sendUsbRequest.cancel();
        sendUsbRequest.close();
        receiveUsbRequest.cancel();
        receiveUsbRequest.close();
        mUsbConnection.close();
        return true;
    }
}
