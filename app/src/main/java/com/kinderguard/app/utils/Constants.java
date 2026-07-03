package com.kinderguard.app.utils;

public final class Constants {
    private Constants() {}

    // Notification channels
    public static final String CHANNEL_MONITORING = "kg_monitoring";
    public static final String CHANNEL_ALERTS = "kg_alerts";

    // Notification ids
    public static final int NOTIF_ID_FOREGROUND = 1001;
    public static final int NOTIF_ID_ALERT = 1002;

    // Intent extras
    public static final String EXTRA_CHILD_UID = "extra_child_uid";
    public static final String EXTRA_CHILD_NAME = "extra_child_name";
    public static final String EXTRA_BLOCKED_APP_LABEL = "extra_blocked_app_label";
    public static final String EXTRA_BLOCKED_PACKAGE = "extra_blocked_package";

    // SharedPreferences keys
    public static final String PREFS_NAME = "kinderguard_prefs";
    public static final String KEY_ROLE = "key_role";
    public static final String KEY_LINKED_PARENT_UID = "key_linked_parent_uid";
    public static final String KEY_LINK_CODE = "key_link_code";
    public static final String KEY_ONBOARDING_DONE = "key_onboarding_done";
    public static final String KEY_SELECTED_CHILD_UID = "key_selected_child_uid";

    // Monitoring intervals (ms)
    public static final long LOCATION_UPDATE_INTERVAL_MS = 60_000L;
    public static final long APP_SCAN_INTERVAL_MS = 30_000L;
    public static final long USAGE_UPLOAD_INTERVAL_MS = 15 * 60_000L;
    public static final long LOG_UPLOAD_INTERVAL_MS = 5 * 60_000L;

    // Geofence
    public static final double DEFAULT_GEOFENCE_RADIUS_METERS = 200;
}
