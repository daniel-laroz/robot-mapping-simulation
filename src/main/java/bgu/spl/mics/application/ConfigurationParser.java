package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedCloudPoints;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class ConfigurationParser {

    

    public static Configuration parseConfigurationFile(String filePath) throws IOException, JsonIOException, JsonSyntaxException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Configuration.class);
        }
    }

    /**
     * Parses camera data JSON into a map of camera keys to their detections.
     *
     * @param filePath the path to the camera data JSON file
     * @return a map where the key is the camera key, and the value is a list of detections
     * @throws IOException if the file cannot be read
     */
    public static Map<String, List<StampedDetectedObjects>> parseCameraData(String filePath) throws IOException, JsonIOException, JsonSyntaxException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }

    public static List<StampedCloudPoints> parseLidarData(String filePath) throws IOException, JsonIOException, JsonSyntaxException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            // Use a List type, since the JSON is an array
            Type type = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            return gson.fromJson(reader, type);
        }
    }
    

public static List<Pose> parsePoseData(String filePath) throws IOException, JsonIOException, JsonSyntaxException {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(filePath)) {
        Type type = new TypeToken<List<Pose>>() {}.getType();
        return gson.fromJson(reader, type);
    }
}


    // ✅ New method to parse all data using paths from the configuration
    public static ParsedData parseAllData(Configuration config, String configFilePath) throws IOException {
        Path configDir = Paths.get(configFilePath).getParent();
    
        String cameraPath = configDir.resolve(config.getCameras().getCameraDataPath()).toString();
        String lidarPath = configDir.resolve(config.getLiDarWorkers().getLidarsDataPath()).toString();
        String posePath = configDir.resolve(config.getPoseJsonFile()).toString();
    
        if (!new File(cameraPath).exists()) throw new IOException("Camera data file not found: " + cameraPath);
        if (!new File(lidarPath).exists()) throw new IOException("LiDAR data file not found: " + lidarPath);
        if (!new File(posePath).exists()) throw new IOException("Pose data file not found: " + posePath);
    
        Map<String, List<StampedDetectedObjects>> cameraData = parseCameraData(cameraPath);
        List<StampedCloudPoints> lidarData = parseLidarData(lidarPath);
        List<Pose> poseData = parsePoseData(posePath);
    
        return new ParsedData(cameraData, lidarData, poseData);
    }
    
        // Inner class to hold all parsed data
        public static class ParsedData {
            private final Map<String, List<StampedDetectedObjects>> cameraData;
            private final List<StampedCloudPoints> lidarData;
            private final List<Pose> poseData;
    
            public ParsedData(Map<String, List<StampedDetectedObjects>> cameraData, List<StampedCloudPoints> lidarData, List<Pose> poseData) {
                this.cameraData = cameraData;
                this.lidarData = lidarData;
                this.poseData = poseData;
            }
    
            public Map<String, List<StampedDetectedObjects>> getCameraData() {
                return cameraData;
            }
    
            public List<StampedCloudPoints> getLidarData() {
                return lidarData;
            }
    
            public List<Pose> getPoseData() {
                return poseData;
            }
        }
}
