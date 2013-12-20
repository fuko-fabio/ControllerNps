package com.nps.architecture;

public enum Hub {
    NORMAL("St0_"),
    FAST("Mt1_");

    private String descriptor;

    Hub(String descriptor) {
        this.descriptor = descriptor;
    }

    public String descriptor() {
        return descriptor;
    }
}
