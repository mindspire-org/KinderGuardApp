package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class ChildUser {
    public String uid;
    public String name;
    public String email;
    public String parentUid;
    public String linkCode;
    public boolean monitoringActive;
    public boolean online;
    public long lastSeen;
    public String deviceModel;
    public String osVersion;
    public LocationPoint lastLocation;
    public long createdAt;
    public boolean deviceLocked;

    public ChildUser() {
        // required for Firebase
    }

    public ChildUser(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("name", name);
        map.put("email", email);
        map.put("parentUid", parentUid);
        map.put("linkCode", linkCode);
        map.put("monitoringActive", monitoringActive);
        map.put("online", online);
        map.put("lastSeen", lastSeen);
        map.put("deviceModel", deviceModel);
        map.put("osVersion", osVersion);
        map.put("createdAt", createdAt);
        return map;
    }
}
