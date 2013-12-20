package com.nps.architecture;

public enum ThreadPriority {
    NORMAL("Normal", "N___"),
    JAVA_BASED_HIGH("Java High", "N___"),
    ANDROID_BASED_HIGH("Android High", "N___");

    private String descriptor;
    private String name;

    ThreadPriority(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public String descriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return name;
    }
}
