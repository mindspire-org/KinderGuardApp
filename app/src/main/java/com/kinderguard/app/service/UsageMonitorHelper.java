package com.kinderguard.app.service;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.UsageInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Queries today's per-app usage stats and uploads them to Firebase. */
public final class UsageMonitorHelper {

    private static final String TAG = "KinderGuard";

    private UsageMonitorHelper() {}

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
            Log.w(TAG, "UsageMonitorHelper: usage access check failed", e);
            return false;
        }
    }

    public static void uploadTodayUsage(Context ctx, String childUid) {
        if (ctx == null || childUid == null || !hasUsageAccess(ctx)) return;
        try {
            UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            long now = System.currentTimeMillis();

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date(now));

            List<UsageStats> statsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now);
            if (statsList == null) return;

            PackageManager pm = ctx.getPackageManager();
            MonitoringRepository monitoringRepository = new MonitoringRepository();

            for (UsageStats stats : statsList) {
                try {
                    if (stats == null || stats.getTotalTimeInForeground() <= 0) continue;
                    String packageName = stats.getPackageName();
                    if (packageName == null) continue;
                    String label;
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                        label = String.valueOf(pm.getApplicationLabel(appInfo));
                    } catch (PackageManager.NameNotFoundException e) {
                        continue;
                    }
                    UsageInfo usage = new UsageInfo(packageName, label, stats.getTotalTimeInForeground(), today);
                    monitoringRepository.uploadUsage(childUid, usage);
                } catch (Exception inner) {
                    Log.w(TAG, "UsageMonitorHelper: failed to process usage stat", inner);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "UsageMonitorHelper: uploadTodayUsage failed", e);
        }
    }
}
