package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String faultySensor;
    private final String Error;

    
    public CrashedBroadcast(String faultySensor, String Error) {
        this.faultySensor = faultySensor;
        this.Error = Error;
    }

    public String getfaultySensor() {
        return faultySensor;
    }

    public String getErrorDescription() {
        return Error;
    }
}