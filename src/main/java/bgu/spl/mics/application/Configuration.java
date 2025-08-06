package bgu.spl.mics.application;

import java.util.List;

public class Configuration {
    private CamerasConfig Cameras;
    private LidarConfig LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    // Nested class for Cameras
    public static class CamerasConfig {
        private List<CameraConfig> CamerasConfigurations;
        private String camera_datas_path;

        public List<CameraConfig> getCamerasConfigurations() {
            return CamerasConfigurations;
        }

        public String getCameraDataPath() {
            return camera_datas_path;
        }
    }

    // Nested class for individual Camera configuration
    public static class CameraConfig {
        private int id;
        private int frequency;
        private String camera_key;

        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }

        public String getCameraKey() {
            return camera_key;
        }
    }

    // Nested class for LiDAR configurations
    public static class LidarConfig {
        private List<LidarConfigItem> LidarConfigurations;
        private String lidars_data_path;

        public List<LidarConfigItem> getLidarConfigurations() {
            return LidarConfigurations;
        }

        public String getLidarsDataPath() {
            return lidars_data_path;
        }
    }

    // Nested class for individual LiDAR configuration
    public static class LidarConfigItem {
        private int id;
        private int frequency;

        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    public CamerasConfig getCameras() {
        return Cameras;
    }

    public LidarConfig getLiDarWorkers() {
        return LiDarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }
}