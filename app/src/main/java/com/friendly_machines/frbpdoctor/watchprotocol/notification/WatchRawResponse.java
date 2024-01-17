package com.friendly_machines.frbpdoctor.watchprotocol.notification;

import androidx.annotation.NonNull;

public class WatchRawResponse {
    int sequenceNumber;
    int ackedSequenceNumber;

    public short command;
    public byte[] arguments;

    public WatchRawResponse(int sequenceNumber, int ackedSequenceNumber, short command, byte[] arguments) {
        this.sequenceNumber = sequenceNumber;
        this.ackedSequenceNumber = ackedSequenceNumber;
        this.command = command;
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%d: %s", this.command, this.arguments);
    }
}
