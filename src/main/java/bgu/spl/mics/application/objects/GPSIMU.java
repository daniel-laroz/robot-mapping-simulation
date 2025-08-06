package bgu.spl.mics.application.objects;

import java.util.List;


// ** HOW SHOULD WE READ THE JSON FILE? I ASSUMED THAT AS A WHOLE - GPSIMU GETS A QUEUE

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private STATUS status;    // e.g., Up, Down, Error
    private int currentTick;        // optional if you want to store it
    private List<Pose> poseList; // all the poses from the JSON

    public GPSIMU(List<Pose> poseList) {
        this.poseList = poseList;
        this.status = STATUS.UP;  // default
        this.currentTick = 0;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    /**
     * Add a Pose to the queue (useful if you parse your JSON into a list
     * and then add them here).
     */
    public void addPose(Pose p) {
        this.poseList.add(p);
    }
}