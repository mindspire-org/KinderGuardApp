package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class CallLogEntry {
    public String id;
    public String contactName;
    public String phoneNumber;
    public String callType; // INCOMING, OUTGOING, MISSED, REJECTED
    public long durationSeconds;
    public long timestamp;

    public CallLogEntry() {}

    public CallLogEntry(String id, String contactName, String phoneNumber, String callType,
                         long durationSeconds, long timestamp) {
        this.id = id;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.durationSeconds = durationSeconds;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("contactName", contactName);
        map.put("phoneNumber", phoneNumber);
        map.put("callType", callType);
        map.put("durationSeconds", durationSeconds);
        map.put("timestamp", timestamp);
        return map;
    }
}
