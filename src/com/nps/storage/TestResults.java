package com.nps.storage;

import com.nps.usb.microcontroller.Arhitecture;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class TestResults {

    private final int streamOutSize;
    private final int streamInSize;
    private final int repeats;
    private final Arhitecture arhitecture;
    private long[][] durations;

    public TestResults(int streamOutSize, int streamInSize, int repeats, Arhitecture arhitecture) {
        this.streamOutSize = streamOutSize;
        this.streamInSize = streamInSize;
        this.repeats = repeats;
        this.durations = new long[repeats][4];
        this.arhitecture = arhitecture;
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

    public Arhitecture getArhitecture() {
        return arhitecture;
    }

    public void addDuration(final int currentIndex, final int hardwareIndex, final long duration, final int hardwareDuration, final int reserved) {
        this.durations[currentIndex][0] = hardwareIndex;
        this.durations[currentIndex][1] = duration;
        this.durations[currentIndex][2] = hardwareDuration;
        this.durations[currentIndex][3] = reserved;
    }

    public String toMatlabFileFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("dane.rozmiarWysylanegoPakietu = " + streamOutSize + ";\n")
               .append("dane.rozmiarOdbieranegoPakietu = " + streamInSize + ";\n")
               .append("dane.wartosci = [\n");
        double durationSum = 0;
        double time;
        for (long[] d : durations) {
            time = (double)d[1]/1000000;
            durationSum += time;
            builder.append(d[0] + ", " +
                           time + ", " +
                           d[2] + ", " +
                           d[3] + ";\n");
        }
        builder.append("];\n")
               .append("dane.ileRazy = " + repeats + ";\n")
               .append("dane.sumDT = " + durationSum + ";\n")
               .append("dane.sredniaDT = " + durationSum/repeats + ";\n")
               .append("dane.nazwa = '" + streamOutSize + "B/" + streamInSize + "B/x" + repeats + "';\n");
        return builder.toString();
    }
}
