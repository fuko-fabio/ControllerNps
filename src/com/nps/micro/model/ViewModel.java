package com.nps.micro.model;

import java.util.ArrayList;
import java.util.List;

import com.nps.architecture.MemoryUnit;
import com.nps.architecture.Sequence;
import com.nps.storage.Storage;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class ViewModel {

    private int repeats;
    private short streamOutSize;
    private short[] streamInSize;
    private String[] devices;
    private Sequence[] sequences;
    private short simulateComputations;
    private boolean saveLogs;
    private boolean saveStreams;
    private boolean normalThreadPriority;
    private boolean hiJavaThreadPriority;
    private boolean hiAndroidThreadPriority;
    private boolean extendedDevicesCombination;
    private boolean fastHub;
    private Storage.Type storageType;
    private int streamBufferSize;
    private MemoryUnit streamBufferUnit;
    private boolean autoEnableGraph;
    private boolean autoStreamQueueSize;
    private int streamQueueSize;

    public ViewModel() {
        super();
        repeats = 10;
        streamOutSize = 16;
        streamInSize = new short[] { 48 };
        devices = new String[]{};
        sequences = new Sequence[] {Sequence.SSRR_wwww};
        normalThreadPriority = false;
        hiJavaThreadPriority = false;
        hiAndroidThreadPriority = true;
        saveLogs = false;
        saveStreams = false;
        extendedDevicesCombination = false;
        simulateComputations = 0;
        fastHub = false;
        storageType = Storage.Type.EXTERNAL;
        streamBufferSize = 500;
        streamBufferUnit = MemoryUnit.KB;
        autoEnableGraph = false;
        autoStreamQueueSize = true;
        streamQueueSize = 10;
    }

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

    public short[] getStreamInSizes() {
        return streamInSize;
    }

    public void setStreamInSize(short[] streamInSize) {
        this.streamInSize = streamInSize;
    }

    public String[] getDevices() {
        return devices;
    }

    public void setDevices(String[] devices) {
        this.devices = devices;
    }

    public boolean isSaveLogs() {
        return saveLogs;
    }

    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }

    public boolean isHiJavaThreadPriority() {
        return hiJavaThreadPriority;
    }

    public void setHiJavaThreadPriority(boolean hiJavaThreadPriority) {
        this.hiJavaThreadPriority = hiJavaThreadPriority;
    }

    public boolean isHiAndroidThreadPriority() {
        return hiAndroidThreadPriority;
    }

    public void setHiAndroidThreadPriority(boolean hiAndroidThreadPriority) {
        this.hiAndroidThreadPriority = hiAndroidThreadPriority;
    }

    public short getSimulateComputations() {
        return simulateComputations;
    }

    public void setSimulateComputations(short simulateComputations) {
        this.simulateComputations = simulateComputations;
    }

    public boolean isSaveStreams() {
        return saveStreams;
    }

    public void setSaveStreams(boolean saveStreams) {
        this.saveStreams = saveStreams;
    }

    public Sequence[] getSequences() {
        return sequences;
    }

    public List<String> getSequencesAsStrings() {
        List<String> sequencesStrings = new ArrayList<String>();
        for (Sequence sequence : sequences) {
            sequencesStrings.add(sequence.toString());
        }
        return sequencesStrings;
    }

    public void setSequences(Sequence[] sequences) {
        this.sequences = sequences;
    }

    public void setSequences(List<String> sequences) {
        List<Sequence> sequencesObjects = new ArrayList<Sequence>();
        for (String sequence : sequences) {
            sequencesObjects.add(Sequence.fromString(sequence));
        }
        this.sequences = sequencesObjects.toArray(new Sequence[sequencesObjects.size()]);
    }

    public boolean isNormalThreadPriority() {
        return normalThreadPriority;
    }

    public void setNormalThreadPriority(boolean normalThreadPriority) {
        this.normalThreadPriority = normalThreadPriority;
    }

    public boolean isExtendedDevicesCombination() {
        return extendedDevicesCombination;
    }

    public void setExtendedDevicesCombination(boolean extendedDevicesCombination) {
        this.extendedDevicesCombination = extendedDevicesCombination;
    }

    public boolean isFastHub() {
        return fastHub;
    }

    public void setFastHub(boolean fastHub) {
        this.fastHub = fastHub;
    }

    public Storage.Type getStorageType() {
        return storageType;
    }

    public void setStorageType(Storage.Type storageType) {
        this.storageType = storageType;
    }

    public int getStreamBufferSize() {
        return streamBufferSize;
    }

    public void setStreamBufferSize(int streamBufferSize) {
        this.streamBufferSize = streamBufferSize;
    }

    public MemoryUnit getStreamBufferUnit() {
        return streamBufferUnit;
    }

    public void setStreamBufferUnit(MemoryUnit streamBufferUnit) {
        this.streamBufferUnit = streamBufferUnit;
    }

    public boolean isAutoEnableGraph() {
        return autoEnableGraph;
    }

    public void setAutoEnableGraph(boolean autoEnableGraph) {
        this.autoEnableGraph = autoEnableGraph;
    }

    public boolean isAutoStreamQueueSize() {
        return autoStreamQueueSize;
    }

    public void setAutoStreamQueueSize(boolean autoStreamQueueSize) {
        this.autoStreamQueueSize = autoStreamQueueSize;
    }

    public int getStreamQueueSize() {
        return streamQueueSize;
    }

    public void setStreamQueueSize(int streamQueueSize) {
        this.streamQueueSize = streamQueueSize;
    }
}
