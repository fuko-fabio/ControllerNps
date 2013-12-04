package com.nps.usb.microcontroller;


/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public enum Arhitecture {
    SEQUENCE_SRSR("Sequence SRSR"),
    SEQUENCE_SSRR("Sequence SSRR"),
    PARALLEL_JTO("Java one thread"),
    PARALLEL_ATO("Android one thread"),
    PARALLEL_JTT("Java two threads"),
    PARALLEL_AJT("Android two threads");

    private String name;

    Arhitecture(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static Arhitecture fromName(String name) {
        Arhitecture[] arhitectures = Arhitecture.values();
        for (int i = 0; i < arhitectures.length; i++) {
            if (arhitectures[i].toString().equalsIgnoreCase(name))
                return arhitectures[i];
        }
        return Arhitecture.SEQUENCE_SRSR;
    }
}
