package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class SmsEntry {
    public String id;
    public String sender;
    public String receiver;
    public String content;
    public String type; // INBOX, SENT
    public long timestamp;

    public SmsEntry() {}

    public SmsEntry(String id, String sender, String receiver, String content, String type, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("content", content);
        map.put("type", type);
        map.put("timestamp", timestamp);
        return map;
    }
}
