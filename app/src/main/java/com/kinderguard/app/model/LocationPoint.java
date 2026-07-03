package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class LocationPoint {
    public double latitude;
    public double longitude;
    public float accuracy;
    public long timestamp;

    public LocationPoint() {}

    public LocationPoint(double latitude, double longitude, float accuracy, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("accuracy", accuracy);
        map.put("timestamp", timestamp);
        return map;
    }

    /** Returns distance in meters to another point (haversine). */
    public double distanceTo(double lat, double lng) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat - latitude);
        double dLng = Math.toRadians(lng - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
