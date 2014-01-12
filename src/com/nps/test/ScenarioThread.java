package com.nps.test;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import com.nps.usb.packet.Packet;

public class ScenarioThread extends Thread {

    private static final String TAG = "ScenarioThread";

    private final UsbService service;
    private final ExternalStorage externalStorage;
    private final Scenario scenario;
    private final Microcontroller[] microcontrollers;

    private static final int QUEUE_SIZE = 20 * 1024 * 1024; // 20MB
    private final int streamBufferSize;
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
        this.streamBufferSize = scenario.getStreamBufferSize();
        if(scenario.getThreadPriority() == ThreadPriority.JAVA_BASED_HIGH){
            this.setPriority(Thread.MAX_PRIORITY);
        }

        if(scenario.isSaveStreamData()) {
            int queueSize = QUEUE_SIZE / scenario.getStreamBufferSize();
            streamQueue = new ArrayBlockingQueue<byte[]>(queueSize);
            streamBuffer = ByteBuffer.allocateDirect(streamBufferSize *3);
        }
        if(scenario.getSimulateComputations() > 0) {
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
        final boolean simulateComputations = scenario.getSimulateComputations() > 0;
        final short computationsMultiplier = scenario.getSimulateComputations();
        final TestResults testResults = new TestResults(scenario);
        if(saveStreamData) {
            streamWriterThread = new StreamWriterThread(streamQueue, externalStorage);
            streamWriterThread.start();
        }
        service.showTestRunningNotification(scenario);
        System.gc();
        try {
            switchMicrocontrollersToStreamMode(microcontrollers, scenario.getStreamOutSize(), (short) scenario.getStreamInSize());
        } catch (MicrocontrollerException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        }
        try {
            if (scenario.getSequence().isInGroup(Group.SYNC)) {
                execSyncScenarioLoop(microcontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            } else if(scenario.getSequence().isInGroup(Group.ASYNC)) {
                execASyncScenarioLoop(microcontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            }
            if (scenario.isSaveSpeedLogs()) {
                saveTestResults(testResults);
            }
            if(saveStreamData) {
                try {
                    int size = streamBuffer.position();
                    byte[] tmp = new byte[size];
                    streamBuffer.rewind();
                    streamBuffer.get(tmp, 0, size);
                    streamBuffer.clear();
                    streamQueue.put(tmp);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Couldn't send data to write cause: " + e.getMessage());
                }
                streamWriterThread.finalize();
            }
        } catch (MicrocontrollerException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        }
        try {
            switchMicrocontrollersToCommandMode(microcontrollers);
        } catch (MicrocontrollerException e) {
            Log.e(TAG, "Couldn't execute test cause: " + e.getMessage());
        }
        Log.d(TAG, "Test done: Java thread sequence " + scenario.getSequence().name() + " priority: " + scenario.getThreadPriority().name());
    }

    private void switchMicrocontrollersToStreamMode(Microcontroller[] selectedMicrocontrollers,
            short streamOutSize, short streamInSize) throws MicrocontrollerException {
        for (Microcontroller micro : getUniqueMicrocontrollers(selectedMicrocontrollers)) {
            micro.setStreamParameters(streamOutSize, streamInSize);
            micro.switchToStreamMode();
        }
    }

    private Collection<Microcontroller> getUniqueMicrocontrollers(Microcontroller[] microcontrollers) {
        Map<String, Microcontroller> map = new HashMap<String, Microcontroller>();
        for(Microcontroller microcontroller : microcontrollers) {
            map.put(microcontroller.getDeviceName(), microcontroller);
        }
        return map.values();
    }

    private void switchMicrocontrollersToCommandMode(Microcontroller[] selectedMicrocontrollers)
            throws MicrocontrollerException {
        for (Microcontroller micro : getUniqueMicrocontrollers(selectedMicrocontrollers)) {
            micro.switchToCommandMode();
        }
    }

    private void execSyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
            int repeats, TestResults testResults, boolean saveStreamData,
            boolean simulateComputations,  short computationsMultiplier) throws MicrocontrollerException {
        switch (scenario.getSequence()) {
        case SRSR:
            execSyncSeqSRSR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case SSRR:
            execSyncSeqSSRR(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        default:
            Log.e(TAG, "Unknown SYNC test sequence");
            break;
        }
    }

    private void execASyncScenarioLoop(Microcontroller[] selectedMicrocontrollers,
            int repeats, TestResults testResults, boolean saveStreamData,
            boolean simulateComputations,  short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
        switch (scenario.getSequence()) {
        case SRSR_wwww:
            execASyncSeqSRSRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case SRww_SRww:
            execASyncSeqSRwwSRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case SSRR_wwww:
            execASyncSeqSSRRwwww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case SSww_RRww:
            execASyncSeqSSwwRRww(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case Sw_Rw_Sw_Rw:
            execASyncSeqSwRwSwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        case Sw_Sw_Rw_Rw:
            execASyncSeqSwSwRwRw(selectedMicrocontrollers, repeats, testResults, saveStreamData, simulateComputations, computationsMultiplier);
            break;
        default:
            Log.e(TAG, "Unknown ASYNC test sequence");
            break;
        }
    }

    private void execASyncSeqSwRwSwRw(Microcontroller[] selectedMicrocontrollers, final int repeats, TestResults testResults,
            final boolean saveStreamData, final boolean simulateComputations, short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
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
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations, short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
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
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
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
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
        for (Microcontroller micro : selectedMicrocontrollers) {
            micro.initAsyncCommunication();
        }
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendAsyncStreamPacket(null);
                micro.receiveAsyncStreamPacket();
                if (simulateComputations) {
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier) throws IllegalAccessException, MicrocontrollerException {
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
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            final long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData(computationsMultiplier);
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
            final boolean saveStreamData, final boolean simulateComputations,  short computationsMultiplier)
            throws MicrocontrollerException {
        for (int i = 0; i < repeats; i++) {
            long before = System.nanoTime();
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.sendStreamPacket(null);
            }
            for (Microcontroller micro : selectedMicrocontrollers) {
                micro.receiveStreamPacket();
                if (simulateComputations) {
                    calculateFakeData(computationsMultiplier);
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
        streamBuffer.put(lastSentStreamPacket);
        streamBuffer.put(lastReceivedStreamPacket);
        if(streamBuffer.position() >= streamBufferSize) {
            byte[] streamToSave = new byte[streamBufferSize];
            try {
                byte[] tmp = new byte[streamBuffer.position() - streamBufferSize];
                streamBuffer.rewind();
                streamBuffer.get(streamToSave, 0, streamBufferSize);
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

    private double calculateFakeData(short computationsMultiplier) {
        double sum = 0;
        for (int i = 0; i < computationsMultiplier; i++) {
            for (int j = 0; j < FAKE_ARRAY_SIZE; j++) {
                sum = +fakeDataArray0[j] * fakeDataArray1[j];
            }
        }
        return sum;
    }
}
