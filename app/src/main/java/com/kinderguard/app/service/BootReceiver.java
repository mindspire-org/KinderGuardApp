package com.kinderguard.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Restarts monitoring after device boot if a child was previously logged in.
 * Defensively wrapped in try/catch since boot receivers that throw can cause
 * boot loops on some OEM skins.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "KinderGuard";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null || context == null) return;
            String action = intent.getAction();
            if (action == null) return;

            boolean isBootAction = Intent.ACTION_BOOT_COMPLETED.equals(action)
                    || "android.intent.action.QUICKBOOT_POWERON".equals(action);
            if (!isBootAction) return;

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                ContextCompat.startForegroundService(context,
                        new Intent(context, MonitoringForegroundService.class));
            }
        } catch (Exception e) {
            Log.w(TAG, "BootReceiver: failed to restart monitoring", e);
        }
    }
}
