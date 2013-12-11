package com.nps.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

import com.nps.micro.R;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class ExternalFile {

    private static final String PREFIX = "data";
    private static final String EXTENSION = ".m";
    private final String logsCatalog;
    private Context context;

    public ExternalFile(Context context) {
        this.context = context;
        logsCatalog = this.context.getString(R.string.logs_catalog);
    }

    public void save(TestResults measuredData) throws ExternalStorageException {
        if (ExternalStorage.isAvailable() && ExternalStorage.isWritable()) {
            File dir = new File(ExternalStorage.getSdCardPath() + logsCatalog + File.separator
                    + getDate() + "_D" + measuredData.getNumberOfDevices() + File.separator
                    + measuredData.getArhitecture().name());
            dir.mkdirs();
            File logsFile = new File(dir + File.separator + logsFilename(measuredData) + EXTENSION);
            try {
                logsFile.createNewFile();
                FileOutputStream logsFileOutputStream = new FileOutputStream(logsFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(logsFileOutputStream);
                myOutWriter.append(measuredData.toMatlabFileFormat());
                myOutWriter.close();
                logsFileOutputStream.close();
            } catch (IOException e) {
                throw new ExternalStorageException("Couldn't save speed logs cause: "
                        + e.getMessage());
            }
        } else {
            throw new ExternalStorageException(
                    "Couldn't save speed logs. SD card is not available or not writable.");
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getDate() {
        return new SimpleDateFormat("yyyy_MM_dd").format(new Date());
    }

    private String logsFilename(TestResults md) {
        return PREFIX + '_' + md.getStreamOutSize() + '_' + md.getStreamInSize() + "_x" + md.getRepeats();
    }
}
