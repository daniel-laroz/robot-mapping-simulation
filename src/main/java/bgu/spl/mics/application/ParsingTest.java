package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.Pose;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A standalone test to check if parsing works correctly.
 */
public class ParsingTest {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ParsingTest <path_to_configuration_file>");
            return;
        }

        String configPath = args[0];

        try {
            // 1) Parse the configuration file
            Configuration config = ConfigurationParser.parseConfigurationFile(configPath);
            System.out.println("Configuration parsed successfully!");
            System.out.println("TickTime: " + config.getTickTime());
            System.out.println("Duration: " + config.getDuration());

            // Number of LiDar worker configs
            int numLidarConfigs = config.getLiDarWorkers().getLidarConfigurations().size();
            System.out.println("Number of LiDar worker configs: " + numLidarConfigs);
            

            // 2) Use parseAllData to get cameraData, lidarData, poseData
            ConfigurationParser.ParsedData parsedData = ConfigurationParser.parseAllData(config, configPath);

            // 2a) Print camera data
            Map<String, List<StampedDetectedObjects>> camData = parsedData.getCameraData();
            System.out.println("\nCamera data parsed. # of camera keys: " + camData.size());
            camData.forEach((key, detections) -> {
                System.out.println("[CameraKey=" + key + "] -> # of detections: " + detections.size());
                for (StampedDetectedObjects sdo : detections) {
                    System.out.println("  time=" + sdo.getTime() + ", objects=" + sdo.getDetectedObjects().size());
                }
            });

            // 2b) Print LiDAR data
            List<StampedCloudPoints> lidarList = parsedData.getLidarData();
            System.out.println("\nLiDAR data parsed. # of entries: " + lidarList.size());
            for (StampedCloudPoints scp : lidarList) {
                System.out.println(" time=" + scp.getTime() + ", id=" + scp.getId()
                                   + ", #points=" + scp.getCloudPoints().size());
            }

            // 2c) Print pose data
            List<Pose> poseList = parsedData.getPoseData();
            System.out.println("\nPose data parsed. # of poses: " + poseList.size());
            for (Pose p : poseList) {
                System.out.println(" time=" + p.getTime() + ", x=" + p.getX()
                                   + ", y=" + p.getY() + ", yaw=" + p.getYaw());
            }

        } catch (IOException e) {
            System.err.println("Error parsing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
