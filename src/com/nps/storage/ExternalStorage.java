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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

import com.nps.micro.R;
import com.nps.scenario.Scenario;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
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

    @SuppressLint("SimpleDateFormat")
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
        return file.getAbsolutePath() + File.separator + BIN_PREFIX + baseFilename() + BIN_EXTENSION;
    }

    private String mFilePath() {
        File file = new File(externalDir + File.separator + logsCatalog + File.separator + testDirName());
        file.mkdirs();
        return file.getAbsolutePath() + File.separator + M_PREFIX + baseFilename() + M_EXTENSION;
    }
    
    private String baseFilename() {
        return "_" + scenario.getStreamOutSize() + "_" + scenario.getStreamInSize() +
               "_x" + scenario.getRepeats() + "_d" + scenario.getDevices().length;
    }
    
    private String testDirName() {
        return testDate + File.separator + scenario.getSequence().group().descriptor() +
        "D" + scenario.getDevices().length + "_" + scenario.getHub().descriptor() +
        scenario.getSequence().descriptor() + scenario.getThreadPriority().descriptor() + "C" +
        scenario.getSimulateComputations() + "__" + File.separator;
    }
}
