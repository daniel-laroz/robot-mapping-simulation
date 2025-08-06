package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    // TODO: Define fields and methods for statistics tracking.
    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;

    private static class SingletonHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder() {
        // Private constructor prevents instantiation from outside.
        systemRuntime = new AtomicInteger();
        numDetectedObjects = new AtomicInteger();
        numTrackedObjects = new AtomicInteger();
        numLandmarks = new AtomicInteger();
    }
    public static StatisticalFolder getInstance() {
        return SingletonHolder.instance;
    }

    // ================ GETTERS ================


    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    /**
     * @return the total number of objects detected by cameras.
     */
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    /**
     * @return the total number of objects tracked by LiDAR.
     */
    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    /**
     * @return the total number of unique landmarks in the map.
     */
    public int getNumLandmarks() {
        return numLandmarks.get();
    }



    // ================ COUNTER UPDATES ================

    public void addRunTime() {
        systemRuntime.incrementAndGet();;
    }


    public void addDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    public void addTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    public void incrementLandmarks() {
        numLandmarks.incrementAndGet();;
    }


}