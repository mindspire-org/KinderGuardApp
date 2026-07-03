package com.kinderguard.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.kinderguard.app.utils.Constants;

public class KinderGuardApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel monitoring = new NotificationChannel(
                Constants.CHANNEL_MONITORING,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW);
        monitoring.setDescription("Keeps KinderGuard running in the background");

        NotificationChannel alerts = new NotificationChannel(
                Constants.CHANNEL_ALERTS,
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH);
        alerts.setDescription("Geofence, SOS and parent alerts");

        manager.createNotificationChannel(monitoring);
        manager.createNotificationChannel(alerts);
    }
}
