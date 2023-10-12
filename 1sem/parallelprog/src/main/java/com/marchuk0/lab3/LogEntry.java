package com.marchuk0.lab3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LogEntry {
    final int type;
    final long timestamp;
    static final int SIZE = Integer.BYTES + Long.BYTES;

    public LogEntry(int type, long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public static LogEntry from(DataInputStream inputStream) throws IOException {
        int type = inputStream.readInt();
        long timestampt = inputStream.readLong();

        return new LogEntry(type, timestampt);
    }

    public static void write(DataOutputStream outputStream, LogEntry logEntry) throws IOException {
        outputStream.writeInt(logEntry.type);
        outputStream.writeLong(logEntry.timestamp);
    }
}
