package com.kinderguard.app.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.CallLogEntry;

/** Queries recent call log entries and uploads them to Firebase. */
public final class CallLogMonitorHelper {

    private static final String TAG = "KinderGuard";

    private CallLogMonitorHelper() {}

    public static void uploadRecentCallLogs(Context ctx, String childUid, long sinceTimestamp) {
        if (ctx == null || childUid == null) return;
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor cursor = null;
        try {
            MonitoringRepository monitoringRepository = new MonitoringRepository();
            cursor = ctx.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    CallLog.Calls.DATE + " > ?",
                    new String[]{String.valueOf(sinceTimestamp)},
                    CallLog.Calls.DATE + " DESC");
            if (cursor == null) return;

            int idIdx = cursor.getColumnIndex(CallLog.Calls._ID);
            int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);
            int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);

            while (cursor.moveToNext()) {
                try {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : String.valueOf(System.nanoTime());
                    String name = nameIdx >= 0 ? cursor.getString(nameIdx) : null;
                    String number = numberIdx >= 0 ? cursor.getString(numberIdx) : "";
                    int typeInt = typeIdx >= 0 ? cursor.getInt(typeIdx) : -1;
                    long duration = durationIdx >= 0 ? cursor.getLong(durationIdx) : 0L;
                    long date = dateIdx >= 0 ? cursor.getLong(dateIdx) : System.currentTimeMillis();

                    String callType;
                    switch (typeInt) {
                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "INCOMING";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "OUTGOING";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callType = "MISSED";
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            callType = "REJECTED";
                            break;
                        default:
                            callType = "UNKNOWN";
                            break;
                    }

                    CallLogEntry entry = new CallLogEntry(id, name, number, callType, duration, date);
                    monitoringRepository.uploadCallLog(childUid, entry);
                } catch (Exception inner) {
                    Log.w(TAG, "CallLogMonitorHelper: failed to process call log row", inner);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "CallLogMonitorHelper: uploadRecentCallLogs failed", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
