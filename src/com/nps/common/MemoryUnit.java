/*******************************************************************************
 * Copyright 2014 Norbert Pabian.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 ******************************************************************************/
package com.nps.common;


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
