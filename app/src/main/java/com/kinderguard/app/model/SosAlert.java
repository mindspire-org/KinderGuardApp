package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class SosAlert {
    public String id;
    public String childUid;
    public String childName;
    public double latitude;
    public double longitude;
    public long timestamp;
    public boolean acknowledged;

    public SosAlert() {}

    public SosAlert(String id, String childUid, String childName, double latitude, double longitude, long timestamp) {
        this.id = id;
        this.childUid = childUid;
        this.childName = childName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.acknowledged = false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("childUid", childUid);
        map.put("childName", childName);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("timestamp", timestamp);
        map.put("acknowledged", acknowledged);
        return map;
    }
}
