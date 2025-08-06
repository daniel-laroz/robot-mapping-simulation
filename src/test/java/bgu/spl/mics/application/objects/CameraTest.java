package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Camera class, focusing on the function that
 * supplies new data (addDetectedObject).
 */
class CameraTest {

    private Camera camera;

    /**
     * Precondition:
     *  - The camera is created with an empty list of detections.
     */
    @BeforeEach
    void setUp() {
        camera = new Camera(1, 2, new ArrayList<>());
    }

    /**
     * Test #1: Add a single DetectedObject for time=2, 
     * ensuring it creates a new StampedDetectedObjects entry.
     * 
     * Postcondition:
     *  - The camera has exactly one stamped detection with time=2,
     *  - That detection has exactly one object: "Wall_1", "Wall".
     * 
     * Invariant:
     *  - The rest of the camera’s data remains intact (which is empty in this case).
     */
    @Test
    void testAddDetectedObject_CreateNewTime() {
        DetectedObject obj = new DetectedObject("Wall_1", "Wall");
        camera.addDetectedObject(2, obj);

        assertEquals(1, camera.getDetections().size(), 
            "Expected exactly one StampedDetectedObjects after adding");
        StampedDetectedObjects sdo = camera.getDetections().get(0);
        assertEquals(2, sdo.getTime(), 
            "The time of the stamped detection should be 2");
        assertEquals(1, sdo.getDetectedObjects().size(), 
            "Expected exactly one DetectedObject in time=2 entry");

        DetectedObject actual = sdo.getDetectedObjects().get(0);
        assertEquals("Wall_1", actual.getId(), 
            "DetectedObject ID should match");
        assertEquals("Wall", actual.getDescription(), 
            "DetectedObject description should match");
    }

    /**
     * Test #2: Add two different objects at the same time=4,
     * ensuring the second object merges into the same StampedDetectedObjects 
     * rather than creating a new one.
     * 
     * Postcondition:
     *  - The camera’s detection list has exactly ONE StampedDetectedObjects with time=4,
     *  - That entry has exactly TWO detected objects.
     * Invariant:
     *  - No other times are created in the detection list.
     */
    @Test
    void testAddDetectedObject_SameTime() {
        DetectedObject obj1 = new DetectedObject("Wall_2", "Wall");
        DetectedObject obj2 = new DetectedObject("Chair_1", "Chair Base");

        camera.addDetectedObject(4, obj1);
        camera.addDetectedObject(4, obj2);

        assertEquals(1, camera.getDetections().size(),
            "Expected exactly one StampedDetectedObjects after adding objects for time=4");
        StampedDetectedObjects sdo = camera.getDetections().get(0);
        assertEquals(4, sdo.getTime(),
            "The time of the stamped detection should be 4");
        assertEquals(2, sdo.getDetectedObjects().size(),
            "Should contain exactly 2 detected objects for time=4");
        
        // Check they are the objects we added
        DetectedObject actual1 = sdo.getDetectedObjects().get(0);
        DetectedObject actual2 = sdo.getDetectedObjects().get(1);

        assertEquals("Wall_2", actual1.getId());
        assertEquals("Chair_1", actual2.getId());
    }
}
