package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;



public class Camera {
    private final int id;
    private final int frequency;   // How many ticks to wait before sending the event
    private STATUS status;
    private final List<StampedDetectedObjects> detecedObjectsList;
    
    // Optional pointer to the next index of detections to send:
    private int nextDetectionIndex = 0;

    public Camera(int id, int frequency, List<StampedDetectedObjects> detections) {
        this.id = id;
        this.frequency = frequency;
        this.detecedObjectsList = detections;
        this.status = STATUS.UP;  // for example
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<StampedDetectedObjects> getDetections() {
        return detecedObjectsList;
    }

    public int getNextDetectionIndex() {
        return nextDetectionIndex;
    }

        public void addDetectedObject(int time, DetectedObject obj) {
        // Find if there's an existing StampedDetectedObjects with the same time
        for (StampedDetectedObjects stamped : detecedObjectsList) {
            if (stamped.getTime() == time) {
                stamped.getDetectedObjects().add(obj);
                return;
            }
        }
        // Otherwise, create a new entry
        List<DetectedObject> newList = new ArrayList<>();
        newList.add(obj);
        detecedObjectsList.add(new StampedDetectedObjects(time, newList));
    }

}