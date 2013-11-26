package com.nps.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.nps.micro.R;

public class SpeedLogger {

	private final String logsCatalog;
	private Context context;
	
	public SpeedLogger(Context context) {
		this.context = context;
		logsCatalog = this.context.getString(R.string.logs_catalog);
	}

	public void saveLogs(String logs, String device) throws ExternalStorageException {
		if(ExternalStorage.isAvailable() && ExternalStorage.isWritable()) {
			File logsFile = new File(logsFilePath(device));
			try {
				logsFile.createNewFile();
				FileOutputStream logsFileOutputStream = new FileOutputStream(logsFile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(logsFileOutputStream);
				myOutWriter.append(logs);
				myOutWriter.close();
				logsFileOutputStream.close();
			} catch (IOException e) {
				throw new ExternalStorageException("Couldn't save speed logs cause: " + e.getMessage());
			}
		} else {
			throw new ExternalStorageException("Couldn't save speed logs. SD card is not available or not writable.");
		}
	}

	private String logsFilePath(String device) {
		String date = new SimpleDateFormat("yyyyy_mm_dd_hh:mm:ss").format(new Date());
		return ExternalStorage.getSdCardPath() + logsCatalog  + '/' + device + date;
	}
}
