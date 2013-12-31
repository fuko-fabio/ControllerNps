package com.nps.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.nps.micro.R;
import com.nps.test.Scenario;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class ExternalStorage {

    private static final String M_PREFIX = "data";
    private static final String BIN_PREFIX = "bin";
    private static final String M_EXTENSION = ".m";
    private static final String BIN_EXTENSION = ".bin";
    //private static final String DATE_FORMAT = "yyyyMM_ddhhmm";
    private static final String DATE_FORMAT = "yyyyMM_dd";
    private final String logsCatalog;
    private final String binsCatalog;
    private final Context context;
    private final Scenario scenario;
    private final File externalDir;
    private final String testDate;

    public ExternalStorage(Context context, Scenario scenario) {
        this.context = context;
        this.logsCatalog = this.context.getString(R.string.logs_catalog);
        this.binsCatalog = this.context.getString(R.string.bins_catalog);
        this.scenario = scenario;
        this.externalDir = new File(Storage.getStoragePath(scenario.getStorageType()));
        this.testDate = new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    public void save(TestResults data) throws ExternalStorageException {
        if (Storage.isAvailable() && Storage.isWritable()) {
            File logsFile = new File(mFilePath());
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

    public String binFilePath() {
        File file = new File(externalDir + File.separator + binsCatalog + File.separator + testDirName());
        file.mkdirs();
        return file.getAbsolutePath() + File.separator + baseFilename(BIN_PREFIX) + BIN_EXTENSION;
    }

    private String mFilePath() {
        File file = new File(externalDir + File.separator + logsCatalog + File.separator + testDirName());
        file.mkdirs();
        return file.getAbsolutePath() + File.separator + baseFilename(M_PREFIX) + M_EXTENSION;
    }
    
    private String baseFilename(String prefix) {
        return prefix + '_' + scenario.getStreamOutSize() + '_' + scenario.getStreamInSize() + "_x" + scenario.getRepeats();
    }
    
    private String testDirName() {
        return testDate + File.separator + scenario.getSequence().group().descriptor() +
        'D' + scenario.getDevices().length + '_' + scenario.getHub().descriptor() +
        scenario.getSequence().descriptor() + scenario.getThreadPriority().descriptor() + 'C' +
        scenario.getSimulateComputations() + "__" + File.separator;
    }
}
