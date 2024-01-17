package com.friendly_machines.frbpdoctor.watchprotocol.notification;

import androidx.annotation.NonNull;

public class WatchRawResponse {
    int serial;
    int ackSerial;

    public short command;
    public byte[] arguments;

    public WatchRawResponse(int serial, int ackSerial, short command, byte[] arguments) {
        this.serial = serial;
        this.ackSerial = ackSerial;
        this.command = command;
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%d: %s", this.command, this.arguments);
    }
}
