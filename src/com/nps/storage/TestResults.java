package com.nps.storage;

import com.nps.architecture.Sequence;
import com.nps.architecture.ThreadPriority;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class TestResults {

    private final short streamOutSize;
    private final short streamInSize;
    private final int repeats;
    private final short numberOfDevices;
    private final Sequence sequence;
    private final ThreadPriority threadPriority;
    private final short[] index;
    private final long[] duration;
    private final short[] hwDuration;

    public TestResults(short streamOutSize, short streamInSize, int repeats, Sequence sequence,
            ThreadPriority threadPriority, short numberOfDevices) {
        this.streamOutSize = streamOutSize;
        this.streamInSize = streamInSize;
        this.repeats = repeats;
        this.index = new short[repeats];
        this.duration = new long[repeats];
        this.hwDuration = new short[repeats];
        this.sequence = sequence;
        this.threadPriority = threadPriority;
        this.numberOfDevices = numberOfDevices;
    }

    public int getStreamOutSize() {
        return streamOutSize;
    }

    public int getStreamInSize() {
        return streamInSize;
    }

    public int getRepeats() {
        return repeats;
    }

    public void addDuration(final int currentIndex, final short hardwareIndex, final long duration, final short hardwareDuration) {
        this.index[currentIndex] = hardwareIndex;
        this.duration[currentIndex] = duration;
        this.hwDuration[currentIndex] = hardwareDuration;
    }

    public String toMatlabFileFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("dane.rozmiarWysylanegoPakietu = " + streamOutSize + ";\n")
               .append("dane.rozmiarOdbieranegoPakietu = " + streamInSize + ";\n")
               .append("dane.wartosci = [\n");
        double durationSum = 0;
        double time;
        for(int i = 0; i<repeats;i++){
            time = (double)duration[i]/1000000;
            durationSum += time;
            builder.append(index[i] + ", " +
                           time + ", " +
                           hwDuration[i] + ", " +
                           0 + ";\n");
        }
        builder.append("];\n")
               .append("dane.ileRazy = " + repeats + ";\n")
               .append("dane.sumDT = " + durationSum + ";\n")
               .append("dane.sredniaDT = " + durationSum/repeats + ";\n")
               .append("dane.nazwa = '" + streamOutSize + "B/" + streamInSize + "B/x" + repeats + "';\n")
               .append("dane.sekwencja = " + sequence.name() + ";\n")
               .append("dane.priorytetWatku = " + threadPriority.name() + ";\n")
               .append("dane.iloscKart = " + numberOfDevices + ";\n");
        return builder.toString();
    }

    public short getNumberOfDevices() {
        return numberOfDevices;
    }

    public ThreadPriority getThreadPriority() {
        return threadPriority;
    }

    public Sequence getSequence() {
        return sequence;
    }
}
