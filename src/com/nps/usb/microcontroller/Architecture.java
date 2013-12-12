package com.nps.usb.microcontroller;


/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public enum Architecture {
    SRSR_STANDARD_PRIORITY("Standard Priority Sequence SRSR"),
    SSRR_STANDARD_PRIORITY("Standard Priority Sequence SSRR"),
    SRSR_HI_PRIORITY_JAVA("Java Hi Priority Sequence SRSR"),
    SSRR_HI_PRIORITY_JAVA("Java Hi Priority Sequence SSRR"),
    SRSR_HI_PRIORITY_ANDROID("Android Hi Priority Sequence SRSR"),
    SSRR_HI_PRIORITY_ANDROID("Android Hi Priority Sequence SSRR"),
    SWRWSWRW_EVENT_DRIVEN("SWR Event Driven Sequence SRSR"),
    SWSWRWRW_EVENT_DRIVEN("SWR Event Driven Sequence SSRR"),
    SRWWSRWW_EVENT_DRIVEN("SRW Event Driven Sequence SRSR"),
    SSRRWWWW_EVENT_DRIVEN("SRW Event Driven Sequence SSRR");

    private String name;

    Architecture(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static Architecture fromName(String name) {
        Architecture[] arhitectures = Architecture.values();
        for (int i = 0; i < arhitectures.length; i++) {
            if (arhitectures[i].toString().equalsIgnoreCase(name))
                return arhitectures[i];
        }
        return Architecture.SRSR_STANDARD_PRIORITY;
    }
}
