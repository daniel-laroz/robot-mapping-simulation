package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.LinkedList;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new LinkedList<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS geStatus() {
        return status;
    }

    public void setStatus(STATUS newStatus) {
        this.status = newStatus;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void setLastTrackedObjects(List<TrackedObject> newTrackedObjects ) {
        this.lastTrackedObjects = newTrackedObjects;
    }
}
