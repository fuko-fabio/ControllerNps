package com.nps.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.nps.storage.ExternalStorage;

public class StreamWriterThread extends Thread {

    private static final String TAG = "StreamWriterThread";

    private static final String STREAM_FILENAME = "stream.b";

    private final ExternalStorage externalStorage;
    private final BlockingQueue<byte[]> streamQueue;
    private volatile boolean isRunning;

    public StreamWriterThread(BlockingQueue<byte[]> streamQueue, ExternalStorage externalStorage) {
        this.streamQueue = streamQueue;
        this.externalStorage = externalStorage;
        this.isRunning = true;
    }

    @Override
    public void run() {
        Log.d(TAG, "Starting writing stream to file");
        FileOutputStream fos = null;
        try {
            File file = new File(externalStorage.getRootFlatDir() + File.separator + STREAM_FILENAME);
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't create file cause: " + e.getMessage());
            return;
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        while (isRunning) {
            try {
                byte[] streamData = streamQueue.take();
                Log.d(TAG, "Writing " + streamData.length + "bytes to file...");
                bos.write(streamData);
            } catch (InterruptedException e) {
                Log.e(TAG, "Couldn't get data to write from queue cause: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write stream to file cause: " + e.getMessage());
                isRunning = false;
            }
        }
        try {
            bos.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close file cause: " + e.getMessage());
        }
        Log.i(TAG, "Writing stream to file done");
    }

    public void finalize() {
        isRunning = false;
    }
}
