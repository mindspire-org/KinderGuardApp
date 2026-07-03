package com.kinderguard.app.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/** Central place for Firebase Realtime Database node paths.
 *
 * /parents/{parentUid}
 * /children/{childUid}
 * /children/{childUid}/apps/{packageName}
 * /children/{childUid}/usage/{date}/{packageName}
 * /children/{childUid}/callLogs/{id}
 * /children/{childUid}/smsLogs/{id}
 * /children/{childUid}/location            (latest point)
 * /children/{childUid}/locationHistory/{ts}
 * /children/{childUid}/geofences/{id}
 * /children/{childUid}/commands/{id}       (parent -> child)
 * /children/{childUid}/sos/{id}            (child -> parent)
 * /linkCodes/{code} -> childUid            (used during parent-child linking)
 */
public final class FirebaseRefs {
    private FirebaseRefs() {}

    private static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

    public static DatabaseReference parents() {
        return db().getReference("parents");
    }

    public static DatabaseReference parent(String parentUid) {
        return parents().child(parentUid);
    }

    public static DatabaseReference children() {
        return db().getReference("children");
    }

    public static DatabaseReference child(String childUid) {
        return children().child(childUid);
    }

    public static DatabaseReference childApps(String childUid) {
        return child(childUid).child("apps");
    }

    public static DatabaseReference childUsage(String childUid) {
        return child(childUid).child("usage");
    }

    public static DatabaseReference childCallLogs(String childUid) {
        return child(childUid).child("callLogs");
    }

    public static DatabaseReference childSmsLogs(String childUid) {
        return child(childUid).child("smsLogs");
    }

    public static DatabaseReference childLocation(String childUid) {
        return child(childUid).child("location");
    }

    public static DatabaseReference childLocationHistory(String childUid) {
        return child(childUid).child("locationHistory");
    }

    public static DatabaseReference childGeofences(String childUid) {
        return child(childUid).child("geofences");
    }

    public static DatabaseReference childCommands(String childUid) {
        return child(childUid).child("commands");
    }

    public static DatabaseReference childSos(String childUid) {
        return child(childUid).child("sos");
    }

    public static DatabaseReference linkCodes() {
        return db().getReference("linkCodes");
    }

    public static DatabaseReference linkCode(String code) {
        return linkCodes().child(code);
    }
}
