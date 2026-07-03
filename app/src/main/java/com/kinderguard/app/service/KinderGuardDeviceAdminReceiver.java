package com.kinderguard.app.service;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Device admin receiver enabling remote-lock capability via DevicePolicyManager. */
public class KinderGuardDeviceAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "KinderGuard";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.i(TAG, "KinderGuardDeviceAdminReceiver: device admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Removing device admin will reduce monitoring protection.";
    }
}
