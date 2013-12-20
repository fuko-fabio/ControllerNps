package com.nps.test;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Process;
import android.util.Log;

import com.nps.architecture.Sequence.Group;
import com.nps.architecture.ThreadPriority;
import com.nps.micro.UsbService;
import com.nps.storage.ExternalStorage;
import com.nps.storage.ExternalStorageException;
import com.nps.storage.TestResults;
import com.nps.usb.microcontroller.Microcontroller;
import com.nps.usb.microcontroller.MicrocontrollerException;
import com.nps.usb.microcontroller.Packet;

public class ScenarioThread extends Thread {

    private static final String TAG = "ScenarioThread";

    private final UsbService service;
    private final ExternalStorage externalStorage;
    private final Scenario scenario;
    private final Microcontroller[] microcontrollers;

    private static final int STREAM_QUEUE_SIZE = 10;
    private static final int STREAM_BUFFER_RANGE = 64000;
    private static final int STREAM_BUFFER_SIZE = STREAM_BUFFER_RANGE * 3;
    private BlockingQueue<byte[]> streamQueue;
    private ByteBuffer streamBuffer;
    private StreamWriterThread streamWriterThread;
    
    private static final int FAKE_ARRAY_SIZE = 3000; // ~0.3ms
    private double fakeDataArray0[] = new double[FAKE_ARRAY_SIZE];
    private double fakeDataArray1[] = new double[FAKE_ARRAY_SIZE];

    public ScenarioThread (UsbService service, Microcontroller[] microcontrollers, Scenario scenario, ExternalStorage externalStorage) {
        this.service = service;
        this.microcontrollers = microcontrollers;
        this.scenario = scenario;
        this.externalStorage = externalStorage;

        if(scenario.getThreadPriority() == ThreadPriority.JAVA_BASED_HIGH){
            this.setPriority(Thread.MAX_PRIORITY);
        }

        if(scenario.isSaveStreamData()) {
            streamQueue = new ArrayBlockingQueue<byte[]>(STREAM_QUEUE_SIZE);
            streamBuffer = ByteBuffer.allocateDirect(STREAM_BUFFER_SIZE);
        }
        if(scenario.isSimulateComputations()) {
            for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
                fakeDataArray0[i] = Math.random();
                fakeDataArray1[i] = Math.random();
            }
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "Starting test: Sequence " + scenario.getSequence().name() + " priority: " + scenario.getThreadPriority().name());
        if(scenario.getThreadPriority() == ThreadPriority.ANDROID_BASED_HIGH){
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        }
        final int repeats = scenario.getRepeats();
        final boolean saveStreamData = scenario.isSaveStreamData();
        final boolean simulateComputations = scenario.isSimulateComputations();
        if(saveStreamData) {
            streamWriterThread = new StreamWriterThread(streamQueue, externalStorage);
            streamWriterThread.start();
        }
        final short[] streamInSizes = scenario.getStreamInSizes();
        final int streamInSizesLenght = streamInSizes.length;
        try {
            for (int i = 0; i < streamInSizes.length; i++) {
                service.showTestRunningNotification(streamInSizesLenght, i, scenario.getSequence(), scenario.getThreadPriority(), streamInSizes[i]);
                System.gc();
                switchMicrocontrollersToStreamMode(microcontrollers, scenario.getStreamOutSize(), (short) streamInSizes[i]);
                TestResults testResults = new TestResults(scenario.getStreamOutSize(),
                                                          streamInSizes[i],
                                                          repeats,
                                                          scenario.getSequence(),
                                                          scenario.getThreadPriority(),
                                                          (short)scenario.getDevices().length);

                if (scenario.getSequence().isInGroup(Group.SYNC)) {
                    execSyncScenarioLoop(microcontrollers, repeats, testResults, saveStreamData, simulateComputations);
                } else if(scenario.getSequence().isInGroup(Group.ASYNC)) {
                    execASyncScenarioLoop(microcontrollers, repeats, testResults, saveStreamData, simulateComputations);
                }

                switchMicrocontrollersToCommandMode(microcontrollers);
                System.gc();
                if (scenario.isSaveSpeedLogs()) {
                    saveTestResults(testResults);
                }
            }
        } catch (MicrocontrollerException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        }
        if(saveStreamData) {
            streamWriterThread.finalize();
        }
        Log.d(TAG, "Test done: Java thread sequence " + scenario.getSequence().name() + " priority: " + scenario.getThreadPriority().name());
    }

    private void switchMicrocontrollersToStreamMode(Microcontroller[] selectedMicrocontrollers,
            short streamOutSize, short streamInSize) throws MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.setStreamParameters(streamOutSize, streamInSize);
            micro.switchToStreamMode();
        }
    }

    private void switchMicrocontrollersToCommandMode(Microcontroller[] selectedMicrocontrollers)
            throws MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.switchToCommandMode();
        }
    }

    private void execSyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
            int repeats, TestResults testResults, boolean saveStreamData,
            boolean simulateComputations) throws MicrocontrollerException {
        switch (scenario.getSequence()) {
        case SRSR:
            execSyncSeqSRSR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case SSRR:
            execSyncSeqSSRR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        default:
            Log.e(TAG, "Unknown SYNC test sequence");
            break;
        }
    }

    private void execASyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
            int repeats, TestResults testResults, boolean saveStreamData,
            boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        switch (scenario.getSequence()) {
        case SRSR_wwww:
            execASyncSeqSRSRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case SRww_SRww:
            execASyncSeqSRwwSRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case SSRR_wwww:
            execASyncSeqSSRRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case SSww_RRww:
            execASyncSeqSSwwRRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case Sw_Rw_Sw_Rw:
            execASyncSeqSwRwSwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        case Sw_Sw_Rw_Rw:
            execASyncSeqSwSwRwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations);
            break;
        default:
            Log.e(TAG, "Unknown ASYNC test sequence");
            break;
        }
    }

    private void execASyncSeqSwRwSwRw(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.asyncRequestWait();
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSRwwSRww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSwSwRwRw(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.asyncRequestWait();
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSSRRwwww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSRSRwwww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execASyncSeqSSwwRRww(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.asyncRequestWait();
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execSyncSeqSRSR(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void execSyncSeqSSRR(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData();
                }
                if(saveStreamData) {
                    writeStreamToQueue(micro.getLastSentStreamPacket(),
                                          micro.getLastReceivedStreamPacket());
                }
            }
            byte[] packet = selectedMicrocontrollers[0].getLastReceivedStreamPacket();

            testResults.addDuration(i,
                                    Packet.shortFromBytes(packet[0], packet[1]),
                                    System.nanoTime() - before,
                                    Packet.shortFromBytes(packet[4], packet[5]));
        }
    }

    private void saveTestResults(TestResults measuredData) {
        try {
            externalStorage.save(measuredData);
        } catch (ExternalStorageException e) {
            Log.w(TAG, "Couldn't save file with speed logs cause: " + e.getMessage());
        }
    }

    private synchronized void writeStreamToQueue(byte[] lastSentStreamPacket, byte[] lastReceivedStreamPacket) {
        streamBuffer.put(lastReceivedStreamPacket);
        streamBuffer.put(lastReceivedStreamPacket);
        if(streamBuffer.position() >= STREAM_BUFFER_RANGE) {
            byte[] streamToSave = new byte[STREAM_BUFFER_RANGE];
            try {
                byte[] tmp = new byte[streamBuffer.position() - STREAM_BUFFER_RANGE];
                streamBuffer.rewind();
                streamBuffer.get(streamToSave, 0, STREAM_BUFFER_RANGE);
                streamBuffer.get(tmp, 0, tmp.length);
                streamBuffer.clear();
                streamBuffer.put(tmp);
            } catch (Exception e) {
                Log.e(TAG, "Problem with stream buffer: " + e.getMessage());
            }
            try {
                streamQueue.put(streamToSave);
            } catch (InterruptedException e) {
                Log.d(TAG, "Couldn't send data to write cause: " + e.getMessage());
            }
        }
    }

    private double calculateFakeData() {
        long before = System.nanoTime();
        double sum = 0;
        for (int i = 0; i < FAKE_ARRAY_SIZE; i++) {
            sum =+ fakeDataArray0[i] * fakeDataArray1[i];
        }
        //Log.d(TAG, "Symulating compitation takes: " + String.valueOf(((double)(System.nanoTime() - before)) / 1000000) + " ms");
        return sum;
    }
}
