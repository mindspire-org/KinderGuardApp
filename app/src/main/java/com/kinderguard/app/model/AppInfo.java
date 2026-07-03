package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class AppInfo {
    public String packageName;
    public String appLabel;
    public long installedAt;
    public boolean systemApp;
    public boolean blocked;

    public AppInfo() {}

    public AppInfo(String packageName, String appLabel, long installedAt, boolean systemApp) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.installedAt = installedAt;
        this.systemApp = systemApp;
        this.blocked = false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("packageName", packageName);
        map.put("appLabel", appLabel);
        map.put("installedAt", installedAt);
        map.put("systemApp", systemApp);
        map.put("blocked", blocked);
        return map;
    }
}
