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
package com.nps.scenario;

import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;
import com.nps.common.Hub;
import com.nps.storage.Storage;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class Scenario {

    private int repeats;
    private short streamOutSize;
    private short streamInSize;
    private String[] devices;
    private ThreadPriority threadPriority;
    private Sequence sequence;
    private Hub hub;
    private boolean saveSpeedLogs;
    private boolean saveStreamData;
    private short simulateComputations;
    private Storage.Type storageType;
    private int streamBufferSize;
    private int streamQueueSize;

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public short getStreamOutSize() {
        return streamOutSize;
    }

    public void setStreamOutSize(short streamOutSize) {
        this.streamOutSize = streamOutSize;
    }

    public short getStreamInSize() {
        return streamInSize;
    }

    public void setStreamInSize(short streamInSize) {
        this.streamInSize = streamInSize;
    }

    public ThreadPriority getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(ThreadPriority threadPriority) {
        this.threadPriority = threadPriority;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public String[] getDevices() {
        return devices;
    }

    public void setDevices(String[] devices) {
        this.devices = devices;
    }

    public boolean isSaveSpeedLogs() {
        return saveSpeedLogs;
    }

    public void setSaveSpeedLogs(boolean saveSpeedLogs) {
        this.saveSpeedLogs = saveSpeedLogs;
    }

    public boolean isSaveStreamData() {
        return saveStreamData;
    }

    public void setSaveStreamData(boolean saveStreamData) {
        this.saveStreamData = saveStreamData;
    }

    public short getSimulateComputations() {
        return simulateComputations;
    }

    public void setSimulateComputations(short simulateComputations) {
        this.simulateComputations = simulateComputations;
    }

    public Hub getHub() {
        return hub;
    }

    public void setHub(Hub hub) {
        this.hub = hub;
    }

    public Storage.Type getStorageType() {
        return storageType;
    }

    public void setStorageType(Storage.Type storageType) {
        this.storageType = storageType;
    }

    @Override
    public String toString() {
        return sequence.name() + ' ' +
               threadPriority.name() + ' ' +
               "devices: " + devices.length + ' ' + 
               "stream In size: " + streamInSize + ' ' + 
               "repeats: " + repeats;
    }

    public int getStreamBufferSize() {
        return streamBufferSize;
    }

    public void setStreamBufferSize(int streamBufferSize) {
        this.streamBufferSize = streamBufferSize;
    }

    public int getStreamQueueSize() {
        return streamQueueSize;
    }

    public void setStreamQueueSize(int streamQueueSize) {
        this.streamQueueSize = streamQueueSize;
    }
}
