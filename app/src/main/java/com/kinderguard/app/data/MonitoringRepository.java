package com.kinderguard.app.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kinderguard.app.model.AppInfo;
import com.kinderguard.app.model.CallLogEntry;
import com.kinderguard.app.model.Command;
import com.kinderguard.app.model.GeofenceZone;
import com.kinderguard.app.model.LocationPoint;
import com.kinderguard.app.model.SmsEntry;
import com.kinderguard.app.model.SosAlert;
import com.kinderguard.app.model.UsageInfo;

import java.util.ArrayList;
import java.util.List;

/** Shared read/write access to child monitoring data in Firebase RTDB.
 * Used by child-side background services (write) and parent-side UI (read/listen). */
public class MonitoringRepository {

    public interface ListResultCallback<T> {
        void onResult(List<T> items);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    // ---- Installed apps ----

    public void uploadApp(String childUid, AppInfo appInfo) {
        FirebaseRefs.childApps(childUid).child(safeKey(appInfo.packageName)).setValue(appInfo.toMap());
    }

    public void removeApp(String childUid, String packageName) {
        FirebaseRefs.childApps(childUid).child(safeKey(packageName)).removeValue();
    }

    public void setAppBlocked(String childUid, String packageName, boolean blocked) {
        FirebaseRefs.childApps(childUid).child(safeKey(packageName)).child("blocked").setValue(blocked);
    }

    public void listenApps(String childUid, ListResultCallback<AppInfo> callback) {
        FirebaseRefs.childApps(childUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AppInfo> apps = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AppInfo app = child.getValue(AppInfo.class);
                    if (app != null) apps.add(app);
                }
                callback.onResult(apps);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ---- Usage ----

    public void uploadUsage(String childUid, UsageInfo usage) {
        FirebaseRefs.childUsage(childUid).child(usage.date).child(safeKey(usage.packageName))
                .setValue(usage.toMap());
    }

    public void listenUsageForDate(String childUid, String date, ListResultCallback<UsageInfo> callback) {
        FirebaseRefs.childUsage(childUid).child(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UsageInfo> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    UsageInfo u = child.getValue(UsageInfo.class);
                    if (u != null) list.add(u);
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ---- Call logs ----

    public void uploadCallLog(String childUid, CallLogEntry entry) {
        FirebaseRefs.childCallLogs(childUid).child(entry.id).setValue(entry.toMap());
    }

    public void listenCallLogs(String childUid, ListResultCallback<CallLogEntry> callback) {
        FirebaseRefs.childCallLogs(childUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CallLogEntry> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CallLogEntry entry = child.getValue(CallLogEntry.class);
                    if (entry != null) list.add(entry);
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ---- SMS logs ----

    public void uploadSms(String childUid, SmsEntry entry) {
        FirebaseRefs.childSmsLogs(childUid).child(entry.id).setValue(entry.toMap());
    }

    public void listenSmsLogs(String childUid, ListResultCallback<SmsEntry> callback) {
        FirebaseRefs.childSmsLogs(childUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SmsEntry> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    SmsEntry entry = child.getValue(SmsEntry.class);
                    if (entry != null) list.add(entry);
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ---- Location ----

    public void uploadLocation(String childUid, LocationPoint point) {
        FirebaseRefs.childLocation(childUid).setValue(point.toMap());
        FirebaseRefs.childLocationHistory(childUid).child(String.valueOf(point.timestamp)).setValue(point.toMap());
    }

    public void listenLocation(String childUid, ValueEventListener listener) {
        FirebaseRefs.childLocation(childUid).addValueEventListener(listener);
    }

    public void getLastLocationOnce(String childUid, ListResultCallback<LocationPoint> callback) {
        FirebaseRefs.childLocation(childUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LocationPoint point = snapshot.getValue(LocationPoint.class);
                java.util.List<LocationPoint> result = new ArrayList<>();
                if (point != null) result.add(point);
                callback.onResult(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(new ArrayList<>());
            }
        });
    }

    // ---- Geofences ----

    public void saveGeofence(String childUid, GeofenceZone zone) {
        FirebaseRefs.childGeofences(childUid).child(zone.id).setValue(zone.toMap());
    }

    public void deleteGeofence(String childUid, String zoneId) {
        FirebaseRefs.childGeofences(childUid).child(zoneId).removeValue();
    }

    public void listenGeofences(String childUid, ListResultCallback<GeofenceZone> callback) {
        FirebaseRefs.childGeofences(childUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GeofenceZone> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    GeofenceZone zone = child.getValue(GeofenceZone.class);
                    if (zone != null) list.add(zone);
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void setGeofenceInside(String childUid, String zoneId, boolean inside) {
        FirebaseRefs.childGeofences(childUid).child(zoneId).child("childInside").setValue(inside);
    }

    // ---- Commands (parent -> child) ----

    public void sendCommand(String childUid, Command command) {
        FirebaseRefs.childCommands(childUid).child(command.id).setValue(command.toMap());
    }

    public void listenNewCommands(String childUid, ChildEventListener listener) {
        FirebaseRefs.childCommands(childUid).addChildEventListener(listener);
    }

    public void acknowledgeCommand(String childUid, String commandId) {
        FirebaseRefs.childCommands(childUid).child(commandId).child("acknowledged").setValue(true);
    }

    // ---- SOS (child -> parent) ----

    public void sendSos(String childUid, SosAlert alert) {
        FirebaseRefs.childSos(childUid).child(alert.id).setValue(alert.toMap());
    }

    public void listenSos(String childUid, ChildEventListener listener) {
        FirebaseRefs.childSos(childUid).addChildEventListener(listener);
    }

    /** Streams the full list of SOS alerts for a child, most-recent-first is left to the caller. */
    public void listenAllSos(String childUid, ListResultCallback<SosAlert> callback) {
        FirebaseRefs.childSos(childUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SosAlert> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    SosAlert alert = child.getValue(SosAlert.class);
                    if (alert != null) list.add(alert);
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void acknowledgeSos(String childUid, String alertId) {
        FirebaseRefs.childSos(childUid).child(alertId).child("acknowledged").setValue(true);
    }

    private String safeKey(String raw) {
        if (raw == null) return "unknown";
        return raw.replace(".", "_").replace("#", "_").replace("$", "_")
                .replace("[", "_").replace("]", "_").replace("/", "_");
    }
}
