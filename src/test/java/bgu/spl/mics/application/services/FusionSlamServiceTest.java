package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.StatisticalFolder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the updateLandMark(...) method of FusionSlamService.
 * We check scenarios where a new landmark is created
 * and where an existing landmark is updated/merged.
 */
class FusionSlamServiceTest {

    private FusionSlam fusion;
    private FusionSlamService fusionService;

    @BeforeEach
    void setUp() {
        // PRECONDITION: a fresh FusionSlam with no landmarks
        // You might need to reset the singleton if your code supports it.
        fusion = new FusionSlam();
        // We'll pass an empty directoryPath, as we won't test file output here
        fusionService = new FusionSlamService(fusion, "");
        // Because updateLandMark(...) is private by default, 
        // you either make it package-private or call it via reflection or a public wrapper.
        // For demonstration, assume we changed it to package-private so we can call it here.
    }

    /**
     * Test #1: When the landmark does not yet exist, updateLandMark(...) should create a new
     * landmark with the transformed global coordinates.
     * 
     * Precondition:
     *  - No existing landmark with ID="Obj_1"
     * Postcondition:
     *  - A new LandMark with ID="Obj_1" is added to FusionSlam, 
     *    with the correct # of points and transformed coordinates.
     *  - The StatisticalFolder increments the landmark count by 1.
     */
    @Test
    void testUpdateLandMark_createsNewLandmark() {
        // Suppose the robot is at x=2, y=3, yaw=90 deg => sinYaw=1, cosYaw=0
        float xRobot = 2.0f;
        float yRobot = 3.0f;
        double yaw = 90.0;
        double sinYaw = Math.sin(Math.toRadians(yaw)); // =1
        double cosYaw = Math.cos(Math.toRadians(yaw)); // =0

        // A TrackedObject with local coordinates
        List<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(1.0, 0.0)); // local(1,0)
        localPoints.add(new CloudPoint(2.0, -1.0)); // local(2, -1)

        TrackedObject obj = new TrackedObject("Obj_1", 5, "TestDescription", localPoints);

        // The method we're testing:
        LandMark result = fusionService.updateLandMark(obj, xRobot, yRobot, sinYaw, cosYaw);

        // POSTCONDITION: This is a newly created landmark => result should be null 
        // *before* creation, but let's confirm how your code returns it:
        // Actually, your code returns "landMark" at the end. 
        // If it was created brand new, "landMark" is 'null' until you do the creation, 
        // but the method returns the old reference. 
        // So the final line "return landMark" might return null if it didn't exist previously 
        // (unless you want to store it in 'landMark' after creation).
        // We'll just check if the new landmark is in the fusion slam list:

        LandMark newlyCreated = fusion.getLandmarkById("Obj_1");
        assertNotNull(newlyCreated, "Should have created a new landmark with id=Obj_1");
        assertEquals("TestDescription", newlyCreated.getDescription());

        // Check the global coordinates: 
        // local(1,0) -> yaw=90 => rotated => (0,1), plus robot(2,3) => (2,4)
        // local(2,-1) -> rotate => local(2,-1)->(1,2?), let's do the math:
        //  x' = cosYaw*x - sinYaw*y + xRobot = 0*2 -1*( -1 ) + 2= 2+1+2= not correct? let's do carefully
        // Actually: x' = cos(90)*2 - sin(90)*(-1) + 2 = 0*2 -1*(-1)+2= 1+2=3
        // y' = sin(90)*2 + cos(90)*(-1) + 3 = 1*2 + 0*(-1)+3=2+3=5
        // So final => (3,5)

        List<CloudPoint> coords = newlyCreated.getCoordinates();
        assertEquals(2, coords.size(), "We have 2 global points now");
        
        // first point => (2,4)
        assertEquals(2.0, coords.get(0).getX(), 0.001);
        assertEquals(4.0, coords.get(0).getY(), 0.001);

        // second point => (3,5)
        assertEquals(3.0, coords.get(1).getX(), 0.001);
        assertEquals(5.0, coords.get(1).getY(), 0.001);

        // Also check StatisticalFolder updated
        assertEquals(1, StatisticalFolder.getInstance().getNumLandmarks(),
            "Should have incremented the landmark count by 1");
    }

    /**
     * Test #2: If the landmark already exists, updateLandMark merges the new global points
     * by averaging overlapping points and appending any extra points.
     * 
     * Precondition:
     *  - A LandMark with ID="Obj_2" already in fusionSlam with some coords
     * Postcondition:
     *  - The old coords are merged with newly transformed ones (shorter / longer logic).
     * Invariant:
     *  - The rest of the slam data is unchanged.
     */
    @Test
    void testUpdateLandMark_mergesExistingLandmark() {
        // Create an existing landmark in fusionSlam
        List<CloudPoint> existingCoords = new ArrayList<>();
        existingCoords.add(new CloudPoint(10.0, 10.0)); 
        existingCoords.add(new CloudPoint(12.0, 14.0));
        LandMark existing = new LandMark("Obj_2", "OldDesc", existingCoords);
        fusion.getLandmarks().add(existing);

        // Suppose the robot is x=0, y=0, yaw=0 => sinYaw=0, cosYaw=1 => no rotation
        float xRobot = 0f;
        float yRobot = 0f;
        double sinYaw = 0.0;
        double cosYaw = 1.0;

        // The new TrackedObject with 3 points => 2 overlap, 1 extra
        List<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(11.0, 10.0)); // merges with old(10,10) ??? see logic
        localPoints.add(new CloudPoint(12.0, 14.0)); // merges with old(12,14)
        localPoints.add(new CloudPoint(20.0, 20.0)); // new point

        TrackedObject obj = new TrackedObject("Obj_2", 5, "MergedDesc", localPoints);

        // POSTCONDITION: We expect the method to average old points with new for overlap, 
        // and add the new extra point. So final coords => 
        // old(10,10) merges with new(11,10)-> average => (10.5,10)
        // old(12,14) merges with new(12,14)-> average => (12,14)
        // new(20,20) is appended.

        LandMark updated = fusionService.updateLandMark(obj, xRobot, yRobot, sinYaw, cosYaw);

        // updated is the existing landmark or null depending on your code
        LandMark after = fusion.getLandmarkById("Obj_2");
        assertNotNull(after, "Landmark 'Obj_2' should remain");
        assertEquals(3, after.getCoordinates().size(), 
            "Now we have 3 points after merging");

        CloudPoint c0 = after.getCoordinates().get(0); 
        CloudPoint c1 = after.getCoordinates().get(1);
        CloudPoint c2 = after.getCoordinates().get(2);

        // first => average of (10,10) and (11,10) => (10.5,10)
        assertEquals(10.5, c0.getX(), 0.001);
        assertEquals(10.0, c0.getY(), 0.001);

        // second => average of (12,14) and (12,14) => (12,14)
        assertEquals(12.0, c1.getX(), 0.001);
        assertEquals(14.0, c1.getY(), 0.001);

        // third => appended => (20,20)
        assertEquals(20.0, c2.getX(), 0.001);
        assertEquals(20.0, c2.getY(), 0.001);

        // // Check we did NOT increment the landmark count, since the ID was existing
        // assertEquals(0, StatisticalFolder.getInstance().getNumLandmarks(),
        //     "No new landmarks should be added in a merge scenario");
    }
}
