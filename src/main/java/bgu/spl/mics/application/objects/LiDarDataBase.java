package bgu.spl.mics.application.objects;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import bgu.spl.mics.ListParser;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;
    private static volatile LiDarDataBase instance = null;

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

    private LiDarDataBase(String filePath) {
        try {
            Type listType = new TypeToken<List<StampedCloudPoints>>(){}.getType();
            List<StampedCloudPoints> potentialCloudPoints = ListParser.parse(filePath, listType);
            if (potentialCloudPoints != null) {
                this.cloudPoints = potentialCloudPoints;
            }
            else {
                // System.out.println("Failed to parse lidar_data file.");
                cloudPoints = new ArrayList<>();
            }
        }
        catch (IOException | JsonIOException | JsonSyntaxException e) {
            this.cloudPoints = new ArrayList<>();
        }
    }

    public static LiDarDataBase getInstance(String filePath){
        if (instance == null) {
            synchronized(LiDarDataBase.class) {
                if (instance == null) {
                    LiDarDataBase x = new LiDarDataBase(filePath);
                    instance = x;
                }
            }
        }
        return instance;
    }

    public StampedCloudPoints getStampedCloudPoints(String id, int time) {
        for (StampedCloudPoints stp : cloudPoints) {
            if (stp.getTime() == time && stp.getId().equals(id)) {
                return stp;
            }
        }
        return null;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}