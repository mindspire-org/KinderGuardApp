package com.kinderguard.app.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.SmsEntry;

/** Queries recent SMS (inbox + sent) and uploads them to Firebase. */
public final class SmsMonitorHelper {

    private static final String TAG = "KinderGuard";

    private SmsMonitorHelper() {}

    public static void uploadRecentSms(Context ctx, String childUid, long sinceTimestamp) {
        if (ctx == null || childUid == null) return;
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        MonitoringRepository monitoringRepository = new MonitoringRepository();
        queryAndUpload(ctx, childUid, sinceTimestamp, Telephony.Sms.Inbox.CONTENT_URI, true, monitoringRepository);
        queryAndUpload(ctx, childUid, sinceTimestamp, Telephony.Sms.Sent.CONTENT_URI, false, monitoringRepository);
    }

    private static void queryAndUpload(Context ctx, String childUid, long sinceTimestamp,
                                        android.net.Uri uri, boolean inbox,
                                        MonitoringRepository monitoringRepository) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(
                    uri,
                    null,
                    Telephony.Sms.DATE + " > ?",
                    new String[]{String.valueOf(sinceTimestamp)},
                    Telephony.Sms.DATE + " DESC");
            if (cursor == null) return;

            int idIdx = cursor.getColumnIndex(Telephony.Sms._ID);
            int addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
            int bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY);
            int dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE);

            while (cursor.moveToNext()) {
                try {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : String.valueOf(System.nanoTime());
                    String address = addressIdx >= 0 ? cursor.getString(addressIdx) : "";
                    String body = bodyIdx >= 0 ? cursor.getString(bodyIdx) : "";
                    long date = dateIdx >= 0 ? cursor.getLong(dateIdx) : System.currentTimeMillis();

                    SmsEntry entry;
                    if (inbox) {
                        entry = new SmsEntry(id, address, "", body, "INBOX", date);
                    } else {
                        entry = new SmsEntry(id, "", address, body, "SENT", date);
                    }
                    monitoringRepository.uploadSms(childUid, entry);
                } catch (Exception inner) {
                    Log.w(TAG, "SmsMonitorHelper: failed to process sms row", inner);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "SmsMonitorHelper: queryAndUpload failed", e);
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
