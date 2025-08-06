package bgu.spl.mics.application.services;

import bgu.spl.mics.LastFrames;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import java.util.List;
import java.util.Iterator;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    // private int mbt;
    private final Camera camera;
    private int curr;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + String.valueOf(camera.getId()));
        this.camera = camera;
        this.curr = 0;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");
        
        subscribeBroadcast(TickBroadcast.class, tick -> {
            List<StampedDetectedObjects> detections = camera.getDetections();
            if (curr >= detections.size()) {
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
                camera.setStatus(STATUS.DOWN);
                Thread.currentThread().interrupt();
                return;
            }

            int currentTick = tick.getCurrentTick();           
            StampedDetectedObjects next = detections.get(curr);
            int detectionTime = next.getTime();  // from the JSON
            int sendTime = detectionTime + camera.getFrequency();

            // If the current simulation tick matches detectionTime+frequency, we send it
            if (currentTick >= sendTime) {
                Iterator<DetectedObject> it = next.getDetectedObjects().iterator();

                while (it.hasNext()) {
                    DetectedObject nextObj = it.next();
                    if (nextObj.getId().equals("ERROR")) {
                        sendBroadcast(new CrashedBroadcast(getName(), nextObj.getDescription()));
                        camera.setStatus(STATUS.ERROR);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                LastFrames.getInstance().setLastCameraFrame(getName(), next.getDetectedObjects(), next.getTime());
                sendEvent(new DetectObjectsEvent(camera.getId(), next));
                // ------------------ Update statistical folder ----------------
                int count = next.getDetectedObjects().size();
                StatisticalFolder.getInstance().addDetectedObjects(count);

                ++curr;
            }   
        });


        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSenderClass() == TimeService.class) {
                Thread.currentThread().interrupt();
            }
        });
        
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            // If a sensor detects an error (e.g., cable disconnection indicated in the JSON files),
            // it interrupts all other sensors, causing the system to stop.
            // Affected MicroServices terminate and write an output file detailing the system's state
            // before the error and indicating which sensor(s) caused the error.
            Thread.currentThread().interrupt();
        });
        
    }
}