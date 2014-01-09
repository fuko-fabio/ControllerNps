package com.nps.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.util.Log;

import com.nps.micro.UsbService;

public class StatusThread extends Thread {

    private static final String TAG = "StatusThread";

    private UsbService service;

    private boolean run;

    private boolean done;

    public StatusThread(UsbService service) {
        this.service = service;
        this.run = true;
        this.done = false;
    }

    @Override
    public void run() {
        while(run) {
            if(!done){
                while(!emptyFutures()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Couldn't wait for threads cause: " + e.getMessage());
                    }
                }
                Log.d(TAG, "All threads done");
                service.showTestDoneNotification();
                done = true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        }
    }

    public void finalize() {
        run = false;
    }

    public void wakeUp() {
        done = false;
    }

    @SuppressWarnings("rawtypes")
    private boolean emptyFutures() {
        List<Future> toRemove = new ArrayList<Future>();
        List<Future> futures = service.getFutures();
        for( Future future : futures) {
            try {
                if(future.get() == null ) {
                    toRemove.add(future);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.d(TAG, "Couldn't wait for threads cause: " + e.getMessage());
            }
        }
        futures.removeAll(toRemove);
        Log.d(TAG, "Scenarios to end: " + futures.size());
        return futures.isEmpty();
    }
}
