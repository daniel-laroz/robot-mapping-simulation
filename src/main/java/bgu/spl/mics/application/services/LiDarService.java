package bgu.spl.mics.application.services;

import bgu.spl.mics.LastFrames;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import java.util.List;
import java.util.LinkedList;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private final LiDarWorkerTracker LiDarWorkerTracker;
    private final LinkedList<DetectObjectsEvent> detectEventQueue;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDarWorkerTracker" + String.valueOf(LiDarWorkerTracker.getId()));
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.detectEventQueue = new LinkedList<>();
        // TODO Implement this
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSenderClass() == TimeService.class) {
                Thread.currentThread().interrupt();
            }
        });
        
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            Thread.currentThread().interrupt();
        });



        subscribeBroadcast(TickBroadcast.class, tick -> {
            if (StatisticalFolder.getInstance().getNumTrackedObjects() >= LiDarDataBase.getInstance("").getCloudPoints().size()) {
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
                LiDarWorkerTracker.setStatus(STATUS.DOWN);
                Thread.currentThread().interrupt();
                return;
            }

            while (!detectEventQueue.isEmpty() && tick.getCurrentTick() >= detectEventQueue.peek().getStampedObjects().getTime() + LiDarWorkerTracker.getFrequency()) {
                DetectObjectsEvent doe = detectEventQueue.poll();
                StampedDetectedObjects stampedObjects = doe.getStampedObjects();
                int detectTime = stampedObjects.getTime();
                List<TrackedObject> toSend = new LinkedList<>();

                for (DetectedObject obj : stampedObjects.getDetectedObjects()) {
                    StampedCloudPoints objPoints = LiDarDataBase.getInstance("").getStampedCloudPoints(obj.getId(), detectTime);
                    if (objPoints.getId().equals("ERROR")) {
                            sendBroadcast(new CrashedBroadcast(getName(), this.getName() + " disconnected"));
                            LiDarWorkerTracker.setStatus(STATUS.ERROR);
                            Thread.currentThread().interrupt();
                            return;
                    }

                    List<List<Double>> coordinates = objPoints.getCloudPoints(); //filePath
                    List<CloudPoint> cloudPoints = new LinkedList<>();

                    for (List<Double> coordinate : coordinates) {
                        cloudPoints.add(new CloudPoint(coordinate.get(0), coordinate.get(1)));
                    }

                    toSend.add(new TrackedObject(obj.getId(), detectTime, obj.getDescription(), cloudPoints));
                }

                this.LiDarWorkerTracker.setLastTrackedObjects(toSend);
                LastFrames.getInstance().setLastLiDarWorkerTrackerFrame(getName(), this.LiDarWorkerTracker.getLastTrackedObjects());
                sendEvent(new TrackedObjectsEvent(toSend));
                // ------------------ Update statistical folder ----------------
                StatisticalFolder.getInstance().addTrackedObjects(toSend.size());
                complete(doe, true);
            }
        });

        subscribeEvent(DetectObjectsEvent.class, doe -> {
            detectEventQueue.add(doe);
        });
    }
}
