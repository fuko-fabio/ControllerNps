package com.nps.storage;

import java.io.File;

import android.os.Environment;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
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