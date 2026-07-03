package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class UsageInfo {
    public String packageName;
    public String appLabel;
    public long totalTimeMs;
    public String date; // yyyy-MM-dd

    public UsageInfo() {}

    public UsageInfo(String packageName, String appLabel, long totalTimeMs, String date) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.totalTimeMs = totalTimeMs;
        this.date = date;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("packageName", packageName);
        map.put("appLabel", appLabel);
        map.put("totalTimeMs", totalTimeMs);
        map.put("date", date);
        return map;
    }
}
