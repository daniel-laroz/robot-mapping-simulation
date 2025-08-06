package bgu.spl.mics.application;
import java.util.concurrent.CountDownLatch;

public class Latch {
    private final CountDownLatch latch;
    private static volatile  Latch instance = null;

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

    private Latch(int count) {
        latch = new CountDownLatch(count);
    }

    public static Latch getInstance(int count){
        if (instance == null) {
            synchronized(Latch.class) {
                if (instance == null) {
                    Latch x = new Latch(count);
                    instance = x;
                }
            }
        }
        return instance;
    }

    public CountDownLatch geLatch() {
        return latch;
    }
}