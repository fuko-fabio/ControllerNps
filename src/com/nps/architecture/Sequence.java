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
package com.nps.architecture;

public enum Sequence {

    SRSR (Group.SYNC, "Sync SRSR", "Sy1_"),
    SSRR (Group.SYNC, "Sync SSRR", "Sy2_"),

    SSRR_wwww (Group.ASYNC, "Async SSRR_wwww", "As4_"),
    SRSR_wwww (Group.ASYNC, "Async SRSR_wwww", "As5_"),
    SSww_RRww (Group.ASYNC, "Async SSww_RRww", "As6_"),
    SRww_SRww (Group.ASYNC, "Async SRww_SRww", "As2_"),
    Sw_Sw_Rw_Rw (Group.ASYNC, "Async Sw_Sw_Rw_Rw", "As3_"),
    Sw_Rw_Sw_Rw (Group.ASYNC, "Async Sw_Rw_Sw_Rw", "As1_");

    private Group group;
    private String name;
    private String descriptor;

    Sequence(Group group, String name, String descriptor) {
        this.group = group;
        this.name = name;
        this.descriptor = descriptor;
    }

    public boolean isInGroup(Group group) {
        return this.group == group;
    }

    public Group group() {
        return this.group;
    }

    public enum Group {
        SYNC("S"),
        ASYNC("A");

        private String descriptor;

        Group(String descriptor) {
            this.descriptor = descriptor;
        }

        public String descriptor() {
            return descriptor;
        }
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
    
    public String descriptor() {
        return descriptor;
    }
}
