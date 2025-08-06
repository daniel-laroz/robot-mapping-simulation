package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final List<LandMark> landmarks;
    private final List<Pose> Poses;

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    public FusionSlam() {
	    this.landmarks = new LinkedList<>();
		this.Poses = new LinkedList<>();
	}

	public static FusionSlam getInstance() {
		return FusionSlamHolder.instance;
	}

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public LandMark getLandmarkById(String id) {
        for (LandMark landMark : getLandmarks()) {
            if (landMark.getId().equals(id)) {
                return landMark;
            }
        }
        return null;
    }

    public List<Pose> getPoses() {
        return Poses;
    }
}
