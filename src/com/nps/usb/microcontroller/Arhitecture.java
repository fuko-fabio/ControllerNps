package com.nps.usb.microcontroller;


/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public enum Arhitecture {
    SRSR_STANDARD_PRIORITY("Standard Priority Sequence SRSR"),
    SSRR_STANDARD_PRIORITY("Standard Priority Sequence SSRR"),
    SRSR_HI_PRIORITY_JAVA("Java Hi Priority Sequence SRSR"),
    SSRR_HI_PRIORITY_JAVA("Java Hi Priority Sequence SSRR"),
    SRSR_HI_PRIORITY_ANDROID("Android Hi Priority Sequence SRSR"),
    SSRR_HI_PRIORITY_ANDROID("Android Hi Priority Sequence SSRR");

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
        return Arhitecture.SRSR_STANDARD_PRIORITY;
    }
}
