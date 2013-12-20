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
import com.nps.test.Scenario;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class ExternalStorage {

    private static final String PREFIX = "data";
    private static final String EXTENSION = ".m";
    private final String logsCatalog;
    private Context context;
    private Scenario scenario;

    public ExternalStorage(Context context, Scenario scenario) {
        this.context = context;
        this.logsCatalog = this.context.getString(R.string.logs_catalog);
        this.scenario = scenario;
    }

    public File getRootTreeDir() {
        File dir = new File(ExternalStorageUtils.getSdCardPath() + logsCatalog + File.separator +
                            getDate() + File.separator + scenario.getSequence().group().name() +
                            '_' + scenario.getDevices().length + 'D' +
                            File.separator + scenario.getSequence() + File.separator +
                            scenario.getThreadPriority() + File.separator);
        dir.mkdirs();
        return dir;
    }

    public File getRootFlatDir() {
        File dir = new File(ExternalStorageUtils.getSdCardPath() + logsCatalog + File.separator +
                            getDate() + File.separator + scenario.getSequence().group().descriptor() +
                            'D' + scenario.getDevices().length + '_' + scenario.getHub().descriptor() +
                            scenario.getSequence().descriptor() + scenario.getThreadPriority().descriptor() +
                            File.separator);
        dir.mkdirs();
        return dir;
    }

    public void save(TestResults data) throws ExternalStorageException {
        if (ExternalStorageUtils.isAvailable() && ExternalStorageUtils.isWritable()) {
            File logsFile = new File(getRootFlatDir() + File.separator + logsFilename(data) + EXTENSION);
            try {
                logsFile.createNewFile();
                FileOutputStream logsFileOutputStream = new FileOutputStream(logsFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(logsFileOutputStream);
                myOutWriter.append(data.toMatlabFileFormat());
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
        return new SimpleDateFormat("yyyyMM_dd").format(new Date());
    }

    private String logsFilename(TestResults md) {
        return PREFIX + '_' + md.getStreamOutSize() + '_' + md.getStreamInSize() + "_x" + md.getRepeats();
    }
}
