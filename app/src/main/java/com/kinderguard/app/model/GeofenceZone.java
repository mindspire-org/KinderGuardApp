package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class GeofenceZone {
    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public double radiusMeters;
    public boolean childInside;

    public GeofenceZone() {}

    public GeofenceZone(String id, String name, double latitude, double longitude, double radiusMeters) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("radiusMeters", radiusMeters);
        map.put("childInside", childInside);
        return map;
    }
}
