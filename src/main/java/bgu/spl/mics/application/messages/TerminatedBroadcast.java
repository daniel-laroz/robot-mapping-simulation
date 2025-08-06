package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {
    private final Class<? extends MicroService> senderType;

    public TerminatedBroadcast(Class<? extends MicroService> senderType) {
        this.senderType = senderType;
    }

    public Class<? extends MicroService> getSenderClass() {
        return senderType;
    }
}