package com.kinderguard.app.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.kinderguard.app.service.KinderGuardDeviceAdminReceiver;

/**
 * Static helpers for checking the various runtime / special permissions the child app needs.
 */
public final class PermissionUtils {

    private PermissionUtils() {}

    public static boolean hasLocationPermission(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasBackgroundLocationPermission(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasUsageAccessPermission(Context ctx) {
        AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                ctx.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static boolean hasOverlayPermission(Context ctx) {
        return Settings.canDrawOverlays(ctx);
    }

    public static boolean hasNotificationPermission(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasSmsCallPermissions(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isDeviceAdminActive(Context ctx) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm == null) return false;
        ComponentName admin = new ComponentName(ctx, KinderGuardDeviceAdminReceiver.class);
        return dpm.isAdminActive(admin);
    }

    public static boolean isBatteryOptimizationIgnored(Context ctx) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (pm == null) return false;
        return pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
    }

    /**
     * Required-for-onboarding set: location, background location, usage access, overlay,
     * notifications, device admin. Battery optimization and SMS/Call access are best-effort
     * and intentionally excluded since they're unreliable to gate on across OEMs.
     */
    public static boolean allRequiredPermissionsGranted(Context ctx) {
        return hasLocationPermission(ctx)
                && hasBackgroundLocationPermission(ctx)
                && hasUsageAccessPermission(ctx)
                && hasOverlayPermission(ctx)
                && hasNotificationPermission(ctx)
                && isDeviceAdminActive(ctx);
    }
}
