package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int speed;
    private final int duration;
    private int clockTicks;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("Time Service");
        speed = TickTime*1000;
        duration = Duration;
        clockTicks = 1;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            // Optionally: check if we ourselves want to terminate
            // because someone else is done, or if it’s a normal end-of-run.
            // We simply terminate:
            if (terminated.getSenderClass() == FusionSlamService.class) {
                Thread.currentThread().interrupt();
            }
        });
        
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            // If a sensor detects an error (e.g., cable disconnection indicated in the JSON files),
            // it interrupts all other sensors, causing the system to stop.
            // Affected MicroServices terminate and write an output file detailing the system's state
            // before the error and indicating which sensor(s) caused the error.
            Thread.currentThread().interrupt();
        });


        // tick handling
        subscribeBroadcast(TickBroadcast.class, tick -> {

            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if(tick.getCurrentTick()<duration)
            {
                clockTicks = tick.getCurrentTick()+1;
                sendBroadcast(new TickBroadcast(clockTicks));
                // ------------------ Update statistical folder ----------------
                StatisticalFolder.getInstance().addRunTime();
            }
            else
            {
                sendBroadcast(new TerminatedBroadcast(this.getClass()));
                Thread.currentThread().interrupt();
            }

        });

    sendBroadcast(new TickBroadcast(clockTicks));
        // ------------------ Update statistical folder ----------------
        StatisticalFolder.getInstance().addRunTime();
    }
}