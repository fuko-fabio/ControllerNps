package com.nps.architecture;


public enum MemoryUnit {
    B(1, 0),
    KB(1024, 1),
    MB(1024 * 1024, 2);

    private int multiplier;
    private int index;

    MemoryUnit(int multiplier, int index) {
        this.multiplier = multiplier;
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }

    public int getMultiplier(){
        return multiplier;
    }

    public static MemoryUnit fromIndex(int index) {
        MemoryUnit[] unit = MemoryUnit.values();
        for (int i = 0; i < unit.length; i++) {
            if (unit[i].index == index)
                return unit[i];
        }
        return B;
    }
}
