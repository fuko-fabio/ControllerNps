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
package com.nps.usb;

import android.hardware.usb.UsbDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class DeviceId implements Parcelable {
	
    protected String name;
    protected int productId;
    protected int vendorId;

    public String getName() {
        return name;
    }

    public int getProductId() {
        return productId;
    }

    public int getVendorId() {
        return vendorId;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean equals(UsbDevice device) {
        return device.getProductId() == productId
               && device.getVendorId() == vendorId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(productId);
        dest.writeInt(vendorId);
    }

    public static final Parcelable.Creator<DeviceId> CREATOR = new Parcelable.Creator<DeviceId>() {
        public DeviceId createFromParcel(Parcel in) {
            return new DeviceId(in);
        }

        public DeviceId[] newArray(int size) {
            return new DeviceId[size];
        }
    };

    public DeviceId(String name, int productId, int vendorId) {
        this.name = name;
        this.productId = productId;
        this.vendorId = vendorId;
    }
    
    private DeviceId(Parcel in) {
        this.name = in.readString();
        this.productId = in.readInt();
        this.vendorId = in.readInt();
    }
}
