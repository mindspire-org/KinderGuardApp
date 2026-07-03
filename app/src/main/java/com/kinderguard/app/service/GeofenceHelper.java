package com.kinderguard.app.service;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.GeofenceZone;
import com.kinderguard.app.model.LocationPoint;
import com.kinderguard.app.utils.Constants;

import java.util.List;

/** Checks a location against a set of geofence zones and reports transitions. */
public final class GeofenceHelper {

    private static final String TAG = "KinderGuard";

    private GeofenceHelper() {}

    public static void checkGeofences(Context ctx, String childUid, LocationPoint currentPoint,
                                       List<GeofenceZone> zones) {
        if (ctx == null || childUid == null || currentPoint == null || zones == null) return;
        MonitoringRepository monitoringRepository = new MonitoringRepository();
        for (GeofenceZone zone : zones) {
            try {
                if (zone == null || zone.id == null) continue;
                double distance = currentPoint.distanceTo(zone.latitude, zone.longitude);
                boolean nowInside = distance <= zone.radiusMeters;
                if (nowInside != zone.childInside) {
                    monitoringRepository.setGeofenceInside(childUid, zone.id, nowInside);
                    postTransitionNotification(ctx, zone, nowInside);
                }
            } catch (Exception e) {
                Log.w(TAG, "GeofenceHelper: failed checking zone", e);
            }
        }
    }

    private static void postTransitionNotification(Context ctx, GeofenceZone zone, boolean nowInside) {
        try {
            String zoneName = zone.name != null ? zone.name : "zone";
            String title = nowInside ? ("Entered " + zoneName) : ("Left " + zoneName);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, Constants.CHANNEL_ALERTS)
                    .setSmallIcon(com.kinderguard.app.R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            int notifId = Constants.NOTIF_ID_ALERT + (zone.id != null ? zone.id.hashCode() : 0);
            NotificationManagerCompat manager = NotificationManagerCompat.from(ctx);
            manager.notify(notifId, builder.build());
        } catch (SecurityException se) {
            Log.w(TAG, "GeofenceHelper: notification permission not granted", se);
        } catch (Exception e) {
            Log.w(TAG, "GeofenceHelper: failed to post transition notification", e);
        }
    }
}
