package com.kinderguard.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.model.SmsEntry;

import java.util.UUID;

/**
 * Captures incoming SMS in near-real-time and uploads them to Firebase.
 * This is intentionally redundant with the periodic catch-up scan in
 * MonitoringForegroundService (which also covers sent messages and anything missed).
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "KinderGuard";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null || context == null) return;
            if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
            String childUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (childUid == null) return;

            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages == null) return;

            MonitoringRepository monitoringRepository = new MonitoringRepository();
            for (SmsMessage smsMessage : messages) {
                try {
                    if (smsMessage == null) continue;
                    SmsEntry entry = new SmsEntry(
                            UUID.randomUUID().toString(),
                            smsMessage.getOriginatingAddress(),
                            "",
                            smsMessage.getMessageBody(),
                            "INBOX",
                            System.currentTimeMillis());
                    monitoringRepository.uploadSms(childUid, entry);
                } catch (Exception inner) {
                    Log.w(TAG, "SmsReceiver: failed to process sms message", inner);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "SmsReceiver: onReceive failed", e);
        }
    }
}
