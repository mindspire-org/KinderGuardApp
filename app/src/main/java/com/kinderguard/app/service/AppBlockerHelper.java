package com.kinderguard.app.service;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;

/** Detects the current foreground app via UsageStatsManager, for blocked-app enforcement. */
public final class AppBlockerHelper {

    private static final String TAG = "KinderGuard";

    private AppBlockerHelper() {}

    private static boolean hasUsageAccess(Context ctx) {
        try {
            AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) return false;
            int mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    ctx.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.w(TAG, "AppBlockerHelper: usage access check failed", e);
            return false;
        }
    }

    /** Returns the package name currently in the foreground, or null if unknown/unavailable. */
    public static String getForegroundApp(Context ctx) {
        if (ctx == null || !hasUsageAccess(ctx)) return null;
        try {
            UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return null;
            long now = System.currentTimeMillis();
            long start = now - 10_000L;
            UsageEvents events = usm.queryEvents(start, now);
            String lastForegroundPackage = null;
            UsageEvents.Event event = new UsageEvents.Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastForegroundPackage = event.getPackageName();
                }
            }
            return lastForegroundPackage;
        } catch (Exception e) {
            Log.w(TAG, "AppBlockerHelper: getForegroundApp failed", e);
            return null;
        }
    }
}
