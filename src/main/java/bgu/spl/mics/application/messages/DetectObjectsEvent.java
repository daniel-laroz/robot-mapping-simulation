package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * An event that cameras send to LiDAR workers.
 * Receives a "StampedDetectedObjects" so the LiDAR can do further processing.
 */
public class DetectObjectsEvent implements Event<Boolean> {
    
    private final StampedDetectedObjects stampedObjects;
    private final int cameraId;

    public DetectObjectsEvent(int cameraId, StampedDetectedObjects stampedObjects) {
        this.cameraId = cameraId;
        this.stampedObjects = stampedObjects;
    }


    public StampedDetectedObjects getStampedObjects() {
        return stampedObjects;
    }

    public int getCameraId() {
        return cameraId;
    }
}