package com.nps.usb.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Norbert Pabian www.npsoftware.pl
 */
public class MeasurementsData {

    int streamOutSize;
    int streamInSize;
    int repeats;
    String description;
    List<Long> duration = new ArrayList<Long>();
    List<Long> hardwareDuration = new ArrayList<Long>();

    public String toMatlabFileFormat() {
        // TODO Implement me
        return duration.toString();
    }

    public void setStreamOutSize(int streamOutSize) {
        this.streamOutSize = streamOutSize;
    }

    public void setStreamInSize(int streamInSize) {
        this.streamInSize = streamInSize;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addDuration(Long duration) {
        this.duration.add(duration);
    }

    public void addHardwareDuration(Long duration) {
        this.hardwareDuration.add(duration);
    }

    public void addDurations(long duration, long hardwareDuration) {
        this.duration.add(duration);
        this.hardwareDuration.add(hardwareDuration);
    }
}
