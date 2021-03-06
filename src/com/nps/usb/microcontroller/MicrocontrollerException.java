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
package com.nps.usb.microcontroller;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class MicrocontrollerException extends Exception {

    private static final long serialVersionUID = -8485014422033067650L;

    public MicrocontrollerException() {
        super("PacketTransferException: An unknown error occurred!");
    }

    public MicrocontrollerException(String error) {
        super(error);
    }
}
