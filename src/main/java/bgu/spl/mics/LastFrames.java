package bgu.spl.mics;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class LastFrames {

	private final ConcurrentHashMap<String, LinkedHashMap<String ,Object>> lastCamerasFrame;
	private final ConcurrentHashMap<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    
	private static class SingletonHolder {
		private static LastFrames instance = new LastFrames();
	}
	
	private LastFrames() {
	    lastCamerasFrame = new ConcurrentHashMap<>();
		lastLiDarWorkerTrackersFrame = new ConcurrentHashMap<>();
	}

	public static LastFrames getInstance() {
		return SingletonHolder.instance;
	}

	public void setLastCameraFrame(String name, List<DetectedObject> detectedObjects, int time) {
		LinkedHashMap<String, Object> detectedObjectsMap = new LinkedHashMap<>();
		detectedObjectsMap.put("time", time);
		detectedObjectsMap.put("detectedObjects", detectedObjects);
		lastCamerasFrame.put(name, detectedObjectsMap);
	}

	public void setLastLiDarWorkerTrackerFrame(String name, List<TrackedObject> trackedObject) {
		lastLiDarWorkerTrackersFrame.put(name, trackedObject);
	}

	public ConcurrentHashMap<String, LinkedHashMap<String ,Object>> getLastCameraFrames() {
        return lastCamerasFrame;
    }

	public ConcurrentHashMap<String, List<TrackedObject>> getLastLiDarWorkerTrackerFrames() {
        return lastLiDarWorkerTrackersFrame;
    }
}
