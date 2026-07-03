package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

/** A command the parent sends to the child device, delivered via the RTDB "commands" node. */
public class Command {
    public static final String TYPE_LOCK = "LOCK";
    public static final String TYPE_UNLOCK = "UNLOCK";
    public static final String TYPE_BLOCK_APP = "BLOCK_APP";
    public static final String TYPE_UNBLOCK_APP = "UNBLOCK_APP";

    public String id;
    public String type;
    public String payload; // e.g. package name for BLOCK_APP
    public long timestamp;
    public boolean acknowledged;

    public Command() {}

    public Command(String id, String type, String payload, long timestamp) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
        this.acknowledged = false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type);
        map.put("payload", payload);
        map.put("timestamp", timestamp);
        map.put("acknowledged", acknowledged);
        return map;
    }
}
