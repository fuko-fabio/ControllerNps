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
package com.nps.storage;

import java.io.File;

import android.os.Environment;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
public class Storage {

    public enum Type {
        INTERNAL, EXTERNAL
    }

    private static final String SD_CARD_DIR = "sdcard";

    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static String getSdCardPath() {
        File storage= Environment.getExternalStorageDirectory();
        File parent = storage.getParentFile();
        if (parent != null) {
            File sdDir =  new File(parent.toURI().resolve(SD_CARD_DIR));
            if (sdDir.isDirectory()) {
                return sdDir.getAbsolutePath() + File.separator;
            }
        }
        return storage.getAbsolutePath() + File.separator;
    }

    public static String getStoragePath(Type type) {
        switch (type) {
        case EXTERNAL:
            return getSdCardPath();
        case INTERNAL:
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        default:
           return null;
        }
    }

    /**
     * @return True if the external storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }
}
