package com.nps.architecture;

public enum Sequence {

    SRSR (Group.SYNC, "Sync SRSR"),
    SSRR (Group.SYNC, "Sync SSRR"),

    SSRR_wwww (Group.ASYNC, "Async SSRR_wwww"),
    SRSR_wwww (Group.ASYNC, "Async SRSR_wwww"),
    SSww_RRww (Group.ASYNC, "Async SSww_RRww"),
    SRww_SRww (Group.ASYNC, "Async SRww_SRww"),
    Sw_Sw_Rw_Rw (Group.ASYNC, "Async Sw_Sw_Rw_Rw"),
    Sw_Rw_Sw_Rw (Group.ASYNC, "Async Sw_Rw_Sw_Rw");

    private Group group;
    private String name;

    Sequence(Group group, String name) {
        this.group = group;
        this.name = name;
    }

    public boolean isInGroup(Group group) {
        return this.group == group;
    }

    public Group group() {
        return this.group;
    }

    public enum Group {
        SYNC,
        ASYNC;
    }

    public String toString() {
        return this.name;
    }

    public static Sequence fromString(String name) {
        Sequence[] sequence = Sequence.values();
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i].toString().equalsIgnoreCase(name))
                return sequence[i];
        }
        return SRSR;
    }
}
