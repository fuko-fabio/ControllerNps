package com.nps.storage;

import com.nps.test.Scenario;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class TestResults {

    private final Scenario scenario;
    private final short[] index;
    private final long[] duration;
    private final short[] hwDuration;

    public TestResults(Scenario scenario) {
        this.scenario = scenario;
        this.index = new short[scenario.getRepeats()];
        this.duration = new long[scenario.getRepeats()];
        this.hwDuration = new short[scenario.getRepeats()];
    }

    public void addDuration(final int currentIndex, final short hardwareIndex, final long duration, final short hardwareDuration) {
        this.index[currentIndex] = hardwareIndex;
        this.duration[currentIndex] = duration;
        this.hwDuration[currentIndex] = hardwareDuration;
    }

    public String toMatlabFileFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("dane.rozmiarWysylanegoPakietu = " + scenario.getStreamOutSize() + ";\n")
               .append("dane.rozmiarOdbieranegoPakietu = " + scenario.getStreamInSize() + ";\n")
               .append("dane.wartosci = [\n");
        double durationSum = 0;
        double time;
        for (int i = 0; i < scenario.getRepeats(); i++) {
            time = (double)duration[i]/1000000;
            durationSum += time;
            builder.append(index[i] + ", " +
                           time + ", " +
                           hwDuration[i] + ", " +
                           0 + ";\n");
        }
        builder.append("];\n")
               .append("dane.ileRazy = " + scenario.getRepeats() + ";\n")
               .append("dane.sumDT = " + durationSum + ";\n")
               .append("dane.sredniaDT = " + durationSum/scenario.getRepeats() + ";\n")
               .append("dane.nazwa = '" + scenario.getStreamOutSize() + "B/" + scenario.getStreamInSize() + "B/x" + scenario.getRepeats() + "';\n")
               .append("dane.sekwencja = '" + scenario.getSequence().name() + "';\n")
               .append("dane.priorytetWatku = '" + scenario.getThreadPriority().name() + "';\n")
               .append("dane.iloscKart = " + scenario.getDevices().length + ";\n");
        return builder.toString();
    }
}
