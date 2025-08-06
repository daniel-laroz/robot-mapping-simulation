package bgu.spl.mics.application.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.LastFrames;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private final LinkedList<TrackedObjectsEvent> trackedEventQueue;
    private final String directoryPath;
    private boolean needsToTerminate;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, String directoryPath) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        this.trackedEventQueue = new LinkedList<>();
        this.directoryPath = directoryPath;
        this.needsToTerminate = false;
        // TODO Implement this
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");
        
        subscribeBroadcast(TickBroadcast.class, tick -> {
            while (!trackedEventQueue.isEmpty() && trackedEventQueue.peek().getTrackedObjects().get(0).getTime() <= fusionSlam.getPoses().size()) {
                TrackedObjectsEvent toe = trackedEventQueue.poll();
                List<TrackedObject> trackedObjects = toe.getTrackedObjects();
                Pose trackTimePose = fusionSlam.getPoses().get(trackedObjects.get(0).getTime() - 1);

                float xRobot = trackTimePose.getX();
                float yRobot = trackTimePose.getY();
                double yawRad = Math.toRadians(trackTimePose.getYaw());
                double sinYaw = Math.sin(yawRad);
                double cosYaw = Math.cos(yawRad);

                for (TrackedObject obj : trackedObjects) {
                    updateLandMark(obj, xRobot, yRobot, sinYaw, cosYaw);
                }
                complete(toe, true);
            }
            if (needsToTerminate) {
                writeOutputFile(false, null, null);
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
                Thread.currentThread().interrupt();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSenderClass() == TimeService.class) {
                writeOutputFile(false, null, null);
                Thread.currentThread().interrupt();
            }

            else if (terminated.getSenderClass() == LiDarService.class) {
                if (trackedEventQueue.isEmpty()) {
                    writeOutputFile(false, null, null);
                    sendBroadcast(new TerminatedBroadcast(this.getClass()));
                    Thread.currentThread().interrupt();
                }
                else {
                    needsToTerminate = true;
                }
            }
        });
        
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            writeOutputFile(true, crashed.getErrorDescription(), crashed.getfaultySensor());
            Thread.currentThread().interrupt();
        });

        subscribeEvent(TrackedObjectsEvent.class, toe -> {
            trackedEventQueue.add(toe);
        });

        subscribeEvent(PoseEvent.class, pe -> {
            fusionSlam.getPoses().add(pe.getPose());
        });

    }

    public LandMark updateLandMark(TrackedObject obj, float xRobot, float yRobot, double sinYaw, double cosYaw) {
        List<CloudPoint> global = new ArrayList<>();
        for (CloudPoint point : obj.getCoordinates()) {
            double xObj = cosYaw * point.getX() - sinYaw * point.getY() + xRobot;
            double yObj = sinYaw * point.getX() + cosYaw * point.getY() + yRobot;
            global.add(new CloudPoint(xObj, yObj));
        }    

        LandMark landMark = fusionSlam.getLandmarkById(obj.getId());
        if (landMark == null) {
            fusionSlam.getLandmarks().add(new LandMark(obj.getId(), obj.getDescription(), global));
            StatisticalFolder.getInstance().incrementLandmarks();
        }

        else {
            List<CloudPoint> coordinates = landMark.getCoordinates();
            int shorter = coordinates.size();
            if (coordinates.size() > global.size()) {
                shorter = global.size();
            }
            for (int i = 0; i < shorter; ++i) {
                CloudPoint oldCoordinate = coordinates.get(i);
                CloudPoint newCoordinate = global.get(i);
                oldCoordinate.setX((oldCoordinate.getX() + newCoordinate.getX()) / 2);
                oldCoordinate.setY((oldCoordinate.getY() + newCoordinate.getY()) / 2);
            }
            for (int j = shorter; j < global.size(); ++j) {
                coordinates.add(global.get(j));
            }
        }
        return landMark;
    }

    private void writeOutputFile(boolean isError, String errorMessage, String faultySensor) {
        LinkedHashMap<String, Object> output = new LinkedHashMap<>();

        StatisticalFolder stats = StatisticalFolder.getInstance();
        LinkedHashMap<String, Object> landMarks = new LinkedHashMap<>();

        for (LandMark landmark : fusionSlam.getLandmarks()) {
            Map<String, Object> landmarkData = new LinkedHashMap<>();
            landmarkData.put("id", landmark.getId());
            landmarkData.put("description", landmark.getDescription());
            landmarkData.put("coordinates", landmark.getCoordinates());
            landMarks.put(landmark.getId(), landmarkData);
        }

        if (isError) {
            output.put("error", errorMessage);
            output.put("faultySensor", faultySensor);

            // Add LastFrames data if needed
            LastFrames lastFrames = LastFrames.getInstance();
            output.put("lastCamerasFrame", lastFrames.getLastCameraFrames());
            output.put("lastLiDarWorkerTrackersFrame", lastFrames.getLastLiDarWorkerTrackerFrames());

            Object[] poses = this.fusionSlam.getPoses().toArray();
            output.put("poses", poses);

            LinkedHashMap<String, Object> statistics = new LinkedHashMap<>();


            statistics.put("systemRuntime", stats.getSystemRuntime());
            statistics.put("numDetectedObjects", stats.getNumDetectedObjects());
            statistics.put("numTrackedObjects", stats.getNumTrackedObjects());
            statistics.put("numLandmarks", stats.getNumLandmarks());
            statistics.put("landMarks", landMarks);
            output.put("statistics", statistics);

        } else {
            // Add world map
            output.put("systemRuntime", stats.getSystemRuntime());
            output.put("numDetectedObjects", stats.getNumDetectedObjects());
            output.put("numTrackedObjects", stats.getNumTrackedObjects());
            output.put("numLandmarks", stats.getNumLandmarks());
            output.put("landMarks", landMarks);
        }

        // Write JSON to output file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fileName = "output_file.json";
        if (isError) {
            fileName = "error_output.json";
        }
        File outputFile = new File(this.directoryPath, fileName);
        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(output, writer);
        } catch (JsonIOException | IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }

    }
}
