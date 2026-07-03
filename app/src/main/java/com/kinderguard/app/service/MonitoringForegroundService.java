package com.kinderguard.app.service;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kinderguard.app.R;
import com.kinderguard.app.data.ChildRepository;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.Command;
import com.kinderguard.app.model.GeofenceZone;
import com.kinderguard.app.model.LocationPoint;
import com.kinderguard.app.utils.Constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Umbrella foreground service that runs continuously on the child device, handling
 * app scanning, blocked-app enforcement, usage/call/SMS uploads, location & geofencing,
 * and listening for remote commands from the parent.
 */
public class MonitoringForegroundService extends Service {

    private static final String TAG = "KinderGuard";
    private static final long BLOCKED_APP_CHECK_INTERVAL_MS = 4000L;
    private static final long BLOCKED_RELAUNCH_DEBOUNCE_MS = 3000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ChildRepository childRepository;
    private MonitoringRepository monitoringRepository;

    private String childUid;

    private final Set<String> blockedPackages = Collections.synchronizedSet(new HashSet<>());
    private final List<GeofenceZone> geofenceZones = new CopyOnWriteArrayList<>();

    private String lastCheckedForegroundPackage;
    private String lastBlockedLaunchedPackage;
    private long lastBlockedLaunchTime;

    private long callLogSinceTimestamp;
    private long smsSinceTimestamp;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // ---- Runnables ----

    private final Runnable appScanRunnable = new Runnable() {
        @Override
        public void run() {
            AppMonitorHelper.scanAndUploadApps(MonitoringForegroundService.this, childUid);
            handler.postDelayed(this, Constants.APP_SCAN_INTERVAL_MS);
        }
    };

    private final Runnable blockedAppCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkForBlockedForegroundApp();
            handler.postDelayed(this, BLOCKED_APP_CHECK_INTERVAL_MS);
        }
    };

    private final Runnable usageUploadRunnable = new Runnable() {
        @Override
        public void run() {
            UsageMonitorHelper.uploadTodayUsage(MonitoringForegroundService.this, childUid);
            handler.postDelayed(this, Constants.USAGE_UPLOAD_INTERVAL_MS);
        }
    };

    private final Runnable callLogUploadRunnable = new Runnable() {
        @Override
        public void run() {
            CallLogMonitorHelper.uploadRecentCallLogs(MonitoringForegroundService.this, childUid, callLogSinceTimestamp);
            callLogSinceTimestamp = System.currentTimeMillis();
            handler.postDelayed(this, Constants.LOG_UPLOAD_INTERVAL_MS);
        }
    };

    private final Runnable smsUploadRunnable = new Runnable() {
        @Override
        public void run() {
            SmsMonitorHelper.uploadRecentSms(MonitoringForegroundService.this, childUid, smsSinceTimestamp);
            smsSinceTimestamp = System.currentTimeMillis();
            handler.postDelayed(this, Constants.LOG_UPLOAD_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        childRepository = new ChildRepository();
        monitoringRepository = new MonitoringRepository();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        childUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (childUid == null) {
            stopSelf();
            return;
        }

        boolean hasLocationPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        if (!hasLocationPermission) {
            // Android 14+ throws SecurityException starting a location-type foreground
            // service without the permission already granted. Bail out cleanly instead of
            // crashing; the service will be restarted once permission is granted.
            Log.w(TAG, "onCreate: location permission not granted yet, stopping service");
            stopSelf();
            return;
        }

        startForeground(Constants.NOTIF_ID_FOREGROUND, buildForegroundNotification());

        try {
            childRepository.setOnlineStatus(childUid, true);
            childRepository.updateDeviceInfo(childUid, Build.MODEL, Build.VERSION.RELEASE);
        } catch (Exception e) {
            Log.w(TAG, "onCreate: failed updating child status", e);
        }

        long now = System.currentTimeMillis();
        callLogSinceTimestamp = now - 24L * 60 * 60 * 1000;
        smsSinceTimestamp = now - 24L * 60 * 60 * 1000;

        // Kick off periodic tasks.
        handler.post(appScanRunnable);
        handler.postDelayed(blockedAppCheckRunnable, BLOCKED_APP_CHECK_INTERVAL_MS);
        handler.postDelayed(usageUploadRunnable, Constants.USAGE_UPLOAD_INTERVAL_MS);
        handler.postDelayed(callLogUploadRunnable, Constants.LOG_UPLOAD_INTERVAL_MS);
        handler.postDelayed(smsUploadRunnable, Constants.LOG_UPLOAD_INTERVAL_MS);

        setupGeofenceListener();
        setupCommandsListener();
        setupLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (childUid != null) {
                childRepository.setOnlineStatus(childUid, false);
            }
        } catch (Exception e) {
            Log.w(TAG, "onDestroy: failed to set offline status", e);
        }
        handler.removeCallbacksAndMessages(null);
        try {
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        } catch (Exception e) {
            Log.w(TAG, "onDestroy: failed to remove location updates", e);
        }
    }

    // ---- Foreground notification ----

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, Constants.CHANNEL_MONITORING)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("KinderGuard is protecting this device")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    // ---- Blocked app enforcement ----

    private void checkForBlockedForegroundApp() {
        try {
            String foregroundPackage = AppBlockerHelper.getForegroundApp(this);
            if (foregroundPackage == null) return;
            lastCheckedForegroundPackage = foregroundPackage;

            if (!blockedPackages.contains(foregroundPackage)) return;

            long now = System.currentTimeMillis();
            boolean samePackageAsLastLaunch = foregroundPackage.equals(lastBlockedLaunchedPackage);
            boolean debounceElapsed = (now - lastBlockedLaunchTime) > BLOCKED_RELAUNCH_DEBOUNCE_MS;

            if (!samePackageAsLastLaunch || debounceElapsed) {
                launchBlockedAppActivity(foregroundPackage);
                lastBlockedLaunchedPackage = foregroundPackage;
                lastBlockedLaunchTime = now;
            }
        } catch (Exception e) {
            Log.w(TAG, "checkForBlockedForegroundApp failed", e);
        }
    }

    private void launchBlockedAppActivity(String packageName) {
        try {
            String label = packageName;
            try {
                android.content.pm.ApplicationInfo appInfo =
                        getPackageManager().getApplicationInfo(packageName, 0);
                label = String.valueOf(getPackageManager().getApplicationLabel(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                // fall back to package name
            }

            Intent intent = new Intent();
            intent.setClassName(this, "com.kinderguard.app.ui.child.BlockedAppActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.EXTRA_BLOCKED_APP_LABEL, label);
            intent.putExtra(Constants.EXTRA_BLOCKED_PACKAGE, packageName);
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "launchBlockedAppActivity failed", e);
        }
    }

    // ---- Geofences ----

    private void setupGeofenceListener() {
        try {
            monitoringRepository.listenGeofences(childUid, zones -> {
                geofenceZones.clear();
                if (zones != null) {
                    geofenceZones.addAll(zones);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "setupGeofenceListener failed", e);
        }
    }

    // ---- Commands ----

    private void setupCommandsListener() {
        try {
            monitoringRepository.listenNewCommands(childUid, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, @Nullable String previousChildName) {
                    try {
                        Command command = snapshot.getValue(Command.class);
                        if (command == null || command.id == null) return;
                        handleCommand(command);
                        monitoringRepository.acknowledgeCommand(childUid, command.id);
                    } catch (Exception e) {
                        Log.w(TAG, "onChildAdded: failed to handle command", e);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, @Nullable String previousChildName) {
                    // no-op
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                    // no-op
                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, @Nullable String previousChildName) {
                    // no-op
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // no-op
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "setupCommandsListener failed", e);
        }
    }

    private void handleCommand(Command command) {
        try {
            switch (command.type) {
                case Command.TYPE_LOCK:
                    lockDevice();
                    break;
                case Command.TYPE_UNLOCK:
                    // There is no reliable programmatic "unlock" API on stock Android for
                    // security reasons (device admin can lock, but only the user can unlock
                    // via their credentials). We simply no-op here; a soft notification is
                    // posted so the child knows their parent tried to unlock the device.
                    postUnlockRequestedNotification();
                    break;
                case Command.TYPE_BLOCK_APP:
                    if (command.payload != null) {
                        blockedPackages.add(command.payload);
                    }
                    break;
                case Command.TYPE_UNBLOCK_APP:
                    if (command.payload != null) {
                        blockedPackages.remove(command.payload);
                    }
                    break;
                default:
                    // unknown command type, ignore
                    break;
            }
        } catch (Exception e) {
            Log.w(TAG, "handleCommand failed", e);
        }
    }

    private void lockDevice() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, KinderGuardDeviceAdminReceiver.class);
            if (dpm != null && dpm.isAdminActive(admin)) {
                dpm.lockNow();
            }
        } catch (SecurityException se) {
            Log.w(TAG, "lockDevice: not device admin", se);
        } catch (Exception e) {
            Log.w(TAG, "lockDevice failed", e);
        }
    }

    private void postUnlockRequestedNotification() {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ALERTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Your parent sent an unlock request")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            androidx.core.app.NotificationManagerCompat.from(this)
                    .notify(Constants.NOTIF_ID_ALERT, builder.build());
        } catch (Exception e) {
            Log.w(TAG, "postUnlockRequestedNotification failed", e);
        }
    }

    // ---- Location ----

    private void setupLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, Constants.LOCATION_UPDATE_INTERVAL_MS)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;
                    android.location.Location location = locationResult.getLastLocation();
                    if (location == null) return;
                    onNewLocation(location);
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException se) {
            Log.w(TAG, "setupLocationUpdates: missing permission", se);
        } catch (Exception e) {
            Log.w(TAG, "setupLocationUpdates failed", e);
        }
    }

    private void onNewLocation(android.location.Location location) {
        try {
            LocationPoint point = new LocationPoint(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAccuracy(),
                    System.currentTimeMillis());
            monitoringRepository.uploadLocation(childUid, point);
            GeofenceHelper.checkGeofences(this, childUid, point, geofenceZones);
        } catch (Exception e) {
            Log.w(TAG, "onNewLocation failed", e);
        }
    }
}
