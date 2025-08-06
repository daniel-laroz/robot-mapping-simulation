package bgu.spl.mics.application;

import bgu.spl.mics.application.Configuration.CameraConfig;
import bgu.spl.mics.application.Configuration.LidarConfigItem;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.FusionSlam;

import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.application.services.FusionSlamService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 */
public class GurionRockRunner {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <configPath>");
            return;
        }

        String configPath = args[0];
        try {
            // 1) Parse config
            Configuration config = ConfigurationParser.parseConfigurationFile(configPath);

            // 2) Parse the camera, lidar, pose data
            ConfigurationParser.ParsedData data = ConfigurationParser.parseAllData(config, configPath);
            Map<String, List<StampedDetectedObjects>> cameraData = data.getCameraData();
            // List<StampedCloudPoints> lidarData                 = data.getLidarData();
            List<Pose> poseData                                = data.getPoseData();

            // 3) Build Cameras
            List<Camera> cameras = new ArrayList<>();
            for (CameraConfig cc : config.getCameras().getCamerasConfigurations()) {
                String key = cc.getCameraKey();
                List<StampedDetectedObjects> detections = cameraData.getOrDefault(key, new ArrayList<>());
                Camera cam = new Camera(cc.getId(), cc.getFrequency(), detections);
                cameras.add(cam);
            }

            // 4) Build LiDarTrackerWorkers
            List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
            for (LidarConfigItem liConf : config.getLiDarWorkers().getLidarConfigurations()) {
                LiDarWorkerTracker lw = new LiDarWorkerTracker(liConf.getId(), liConf.getFrequency());
                lidarWorkers.add(lw);
            }

            // 5) Build the LiDarDataBase (if you want a single global DB)
            // Pass the same file path from config? 
            // Actually, we already have 'lidarData'. We can do:
            Path configDir = Paths.get(configPath).getParent();
            String lidarPath = configDir.resolve(config.getLiDarWorkers().getLidarsDataPath()).toString();
            LiDarDataBase db = LiDarDataBase.getInstance(lidarPath); 
            // Now db.getCloudPoints() is the same as 'lidarData', presumably

            // 6) Build a GPSIMU with the pose data
            GPSIMU gpsimu = new GPSIMU(poseData);

            // 7) Print all created data (before starting threads)
            System.out.println("=== PRINTING CREATED DATA ===");

            System.out.println("TickTime: " + config.getTickTime() + ", Duration: " + config.getDuration());

            System.out.println("\nCreated " + cameras.size() + " Cameras:");
            for (Camera c : cameras) {
                System.out.println("  Camera id=" + c.getId() + ", freq=" + c.getFrequency()
                                   + ", #detectionEntries=" + c.getDetections().size());
            }

            System.out.println("\nCreated " + lidarWorkers.size() + " LiDar Workers:");
            for (LiDarWorkerTracker lw : lidarWorkers) {
                System.out.println("  LiDar id=" + lw.getId() + ", freq=" + lw.getFrequency());
            }

            System.out.println("\nLiDarDataBase has " + db.getCloudPoints().size() + " stampedCloudPoints entries.");

            System.out.println("\nCreated GPSIMU with #poses=" + poseData.size());

            // 8) Create MicroServices
            List<CameraService> cameraServices = new ArrayList<>();
            for (Camera cam : cameras) {
                cameraServices.add(new CameraService(cam));
            }

            // LiDarWorkerService: pass the worker + the db or parse logic
            List<LiDarService> lidarServices = new ArrayList<>();
            for (LiDarWorkerTracker lw : lidarWorkers) {
                LiDarService ls = new LiDarService(lw); 
                // If your LiDarWorkerService constructor expects a Map<id,List<StampedCloudPoints>> 
                // you can build that from db.getCloudPoints().
                // Or pass 'db' directly if your constructor allows it.
                lidarServices.add(ls);
            }

            // PoseService
            PoseService poseService = new PoseService(gpsimu);

            // TimeService
            TimeService timeService = new TimeService(config.getTickTime(), config.getDuration());

            // (Optionally) FusionSlamService
            FusionSlamService fusionService = new FusionSlamService(FusionSlam.getInstance(), new File(configPath).getParent());

            // 9) Start MicroServices in threads
            int numServices = cameraServices.size() + lidarServices.size() + 1 + 1; // Cameras + LiDars + PoseService + FusionSlamService
            Latch.getInstance(numServices);

            List<Thread> threads = new ArrayList<>();

            Thread pose = new Thread(poseService);
            threads.add(pose);
            pose.start();

            for (CameraService cs : cameraServices) {
                Thread t = new Thread(cs);
                threads.add(t);
                t.start();
            }

            for (LiDarService ls : lidarServices) {
                Thread t = new Thread(ls);
                threads.add(t);
                t.start();
            }

            Thread fusion = new Thread(fusionService);
            threads.add(fusion);
            fusion.start();

            Latch.getInstance(0).geLatch().await();
            new Thread(timeService).start();

            System.out.println("\nAll microservices started. Simulation is running...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
