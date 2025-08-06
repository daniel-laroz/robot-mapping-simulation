package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

// ** IM NOT SURE IF I CAN GET A ERROR THROUGH POSE_DATA.JSON

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;
    private int curr;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        curr = 0;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        System.out.println("GPSIMU " + getName() + " started");
        
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick();
            List<Pose> poseList = gpsimu.getPoseList();

            // If no next Pose, we're presumably done
            if (curr >= poseList.size()) {
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
                gpsimu.setStatus(STATUS.DOWN);
                Thread.currentThread().interrupt();
                return;
            }


            // If we do have a Pose, check if the time is <= currentTick
            Pose next = poseList.get(curr);
            int poseTime = next.getTime();
            if (currentTick >= poseTime) {
                // Send a PoseEvent to whomever is subscribing (e.g. FusionSLAM)
                sendEvent(new PoseEvent(next));
                ++curr;
            }
            // else: The time hasn't come yet, do nothing

        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            // If something else crashed, we terminate too
            Thread.currentThread().interrupt();
        });

        // (Optional) If you also want to gracefully stop on TerminatedBroadcast from others:
        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSenderClass() == TimeService.class || terminated.getSenderClass() == FusionSlamService.class) {
                Thread.currentThread().interrupt();
            }
        });
    } 
}