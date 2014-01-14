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
package com.nps.test;

import com.nps.architecture.Hub;
import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;
import com.nps.storage.Storage;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class ScenarioBuilder {

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

    public ScenarioBuilder withRepeats(int repeats) {
        this.repeats = repeats;
        return this;
    }

    public ScenarioBuilder withStreamOutSize(short streamOutSize) {
        this.streamOutSize = streamOutSize;
        return this;
    }

    public ScenarioBuilder withStreamInSize(short streamInSize) {
        this.streamInSize = streamInSize;
        return this;
    }

    public ScenarioBuilder withDevices(String[] devices) {
        this.devices = devices;
        return this;
    }

    public ScenarioBuilder withThreadPriority(ThreadPriority threadPriority) {
        this.threadPriority = threadPriority;
        return this;
    }

    public ScenarioBuilder withSequence(Sequence sequence) {
        this.sequence = sequence;
        return this;
    }

    public ScenarioBuilder isSaveSpeedLogs(boolean saveSpeedLogs) {
        this.saveSpeedLogs = saveSpeedLogs;
        return this;
    }

    public ScenarioBuilder isSaveStreamData(boolean saveStreamData) {
        this.saveStreamData = saveStreamData;
        return this;
    }

    public ScenarioBuilder withSimulateComputations(short simulateComputations) {
        this.simulateComputations = simulateComputations;
        return this;
    }

    public ScenarioBuilder withHub(Hub hub) {
        this.hub = hub;
        return this;
    }

    public ScenarioBuilder withStorageType(Storage.Type storageType) {
        this.storageType = storageType;
        return this;
    }
    
    public ScenarioBuilder withStreamBufferSize(int streamBufferSize) {
        this.streamBufferSize = streamBufferSize;
        return this;
    }

    public ScenarioBuilder withStreamQueueSize(int streamQueueSize) {
        this.streamQueueSize = streamQueueSize;
        return this;
    }

    public Scenario build() {
        Scenario scenario = new Scenario();
        scenario.setDevices(devices);
        scenario.setRepeats(repeats);
        scenario.setSaveSpeedLogs(saveSpeedLogs);
        scenario.setSaveStreamData(saveStreamData);
        scenario.setSequence(sequence);
        scenario.setSimulateComputations(simulateComputations);
        scenario.setStreamInSize(streamInSize);
        scenario.setStreamOutSize(streamOutSize);
        scenario.setThreadPriority(threadPriority);
        scenario.setHub(hub);
        scenario.setStreamBufferSize(streamBufferSize);
        scenario.setStorageType(storageType);
        scenario.setStreamQueueSize(streamQueueSize);
        return scenario;
    }
}
