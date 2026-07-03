package com.kinderguard.app.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.AppInfo;

import java.util.List;

/** Scans installed applications and uploads them to Firebase for parent-side visibility. */
public final class AppMonitorHelper {

    private static final String TAG = "KinderGuard";

    private AppMonitorHelper() {}

    public static void scanAndUploadApps(Context ctx, String childUid) {
        if (ctx == null || childUid == null) return;
        try {
            PackageManager pm = ctx.getPackageManager();
            String ownPackage = ctx.getPackageName();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            MonitoringRepository monitoringRepository = new MonitoringRepository();
            for (ApplicationInfo appInfo : apps) {
                try {
                    if (appInfo.packageName == null || appInfo.packageName.equals(ownPackage)) {
                        continue;
                    }
                    String label;
                    try {
                        label = String.valueOf(pm.getApplicationLabel(appInfo));
                    } catch (Exception e) {
                        label = appInfo.packageName;
                    }
                    long installedAt = 0L;
                    try {
                        PackageInfo pkgInfo = pm.getPackageInfo(appInfo.packageName, 0);
                        installedAt = pkgInfo.firstInstallTime;
                    } catch (PackageManager.NameNotFoundException e) {
                        // skip install time, keep 0
                    }
                    boolean systemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    AppInfo info = new AppInfo(appInfo.packageName, label, installedAt, systemApp);
                    monitoringRepository.uploadApp(childUid, info);
                } catch (Exception inner) {
                    Log.w(TAG, "AppMonitorHelper: failed to process app " + appInfo.packageName, inner);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "AppMonitorHelper: scanAndUploadApps failed", e);
        }
    }
}
