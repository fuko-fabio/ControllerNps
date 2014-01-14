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
public class PacketSize {
    private static final short defaultStreamInSize = 48;
    private static final short defaultStreamOutSize = 16;
    private short customStreamInSize;
    private short customStreamOutSize;

    /**
     * Default constructor stream in = default input stream size 16 stream out =
     * default output stream size 48
     */
    public PacketSize() {
        customStreamInSize = defaultStreamInSize;
        customStreamOutSize = defaultStreamOutSize;
    }

    /**
     * @param streamInSize
     *            size of input stream
     * @param streamOutSize
     *            size of output stream
     */
    public PacketSize(short streamInSize, short streamOutSize) {
        customStreamInSize = streamInSize;
        customStreamOutSize = streamOutSize;
    }

    /**
     * @return size of input stream
     */
    public short getStreamInSize() {
        return customStreamInSize;
    }

    /**
     * @param size
     *            size of input stream
     */
    public void setStreamInSize(short size) {
        this.customStreamInSize = size;
    }

    /**
     * @return size of output stream
     */
    public short getStreamOutSize() {
        return customStreamOutSize;
    }

    /**
     * @param size
     *            size of output stream
     */
    public void setStreamOutSize(short size) {
        this.customStreamOutSize = size;
    }

    /**
     * @return default size of input stream
     */
    public short getDefaultStreamInSize() {
        return defaultStreamInSize;
    }

    /**
     * @return default size of output stream
     */
    public short getDefaultStreamOutSize() {
        return defaultStreamOutSize;
    }
}
