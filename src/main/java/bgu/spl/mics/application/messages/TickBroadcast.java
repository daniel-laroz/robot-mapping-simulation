package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast that carries the current time/tick in the simulation.
 * Sent by the TimeService.
 */
public class TickBroadcast implements Broadcast {
    
    private final int currentTick;

    public TickBroadcast(int currentTick) {
        this.currentTick = currentTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}