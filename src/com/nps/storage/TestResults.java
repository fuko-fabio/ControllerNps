package com.nps.storage;

import java.util.ArrayList;
import java.util.List;

import com.nps.usb.microcontroller.Arhitecture;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class TestResults {

    private int streamOutSize;
    private int streamInSize;
    private int repeats;
    private Arhitecture arhitecture;
    private List<DurationFrame> durations;

    public class DurationFrame {
        int index;
        long duration;
        int hardwareDuration;
        int reserved;
        
        public DurationFrame(int index, long duration, int hardwareDuration, int reserved) {
            this.index = index;
            this.duration = duration;
            this.hardwareDuration =hardwareDuration;
            this.reserved = reserved;
        }
    }

    public TestResults(int streamOutSize, int streamInSize, int repeats, Arhitecture arhitecture) {
        this.streamOutSize = streamOutSize;
        this.streamInSize = streamInSize;
        this.repeats = repeats;
        this.durations = new ArrayList<DurationFrame>(this.repeats);
        this.arhitecture = arhitecture;
    }

    public int getStreamOutSize() {
        return streamOutSize;
    }

    public void setStreamOutSize(int streamOutSize) {
        this.streamOutSize = streamOutSize;
    }

    public int getStreamInSize() {
        return streamInSize;
    }

    public void setStreamInSize(int streamInSize) {
        this.streamInSize = streamInSize;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public Arhitecture getArhitecture() {
        return arhitecture;
    }

    public void setArhitecture(Arhitecture arhitecture) {
        this.arhitecture = arhitecture;
    }

    public void addDuration(int index, long duration, int hardwareDuration, int reserved) {
        this.durations.add(new DurationFrame(index, duration, hardwareDuration, reserved));
    }

    public String toMatlabFileFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("dane.rozmiarWysylanegoPakietu = " + streamOutSize + ';')
               .append("dane.rozmiarOdbieranegoPakietu = " + streamInSize + ';')
               .append("dane.wartosci = [");
        for (DurationFrame d : durations) {
            builder.append(d.index + ", " +
                           d.duration + ", " +
                           d.hardwareDuration + ", " +
                           d.reserved + ';');
        }
        builder.append("];");
        return builder.toString();
    }
}
