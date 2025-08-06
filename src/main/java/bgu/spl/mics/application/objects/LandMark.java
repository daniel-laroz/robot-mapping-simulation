package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final String id;
    private final String Description;
    private final List<CloudPoint> Coordinates;

    public LandMark(String id, String Description, List<CloudPoint> Coordinates) {
        this.id = id;
        this.Description = Description;
        this.Coordinates = Coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return Description;
    }

    public List<CloudPoint> getCoordinates() {
        return Coordinates;
    }
}
