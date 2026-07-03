package com.kinderguard.app.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kinderguard.app.model.ChildUser;

public class ChildRepository {

    public interface ResultCallback<T> {
        void onResult(T value);
        void onError(String message);
    }

    public void createChildProfile(ChildUser child, ResultCallback<Void> callback) {
        FirebaseRefs.child(child.uid).setValue(child.toMap())
                .addOnSuccessListener(unused -> callback.onResult(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getChildProfile(String childUid, ResultCallback<ChildUser> callback) {
        FirebaseRefs.child(childUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.getValue(ChildUser.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void setOnlineStatus(String childUid, boolean online) {
        FirebaseRefs.child(childUid).child("online").setValue(online);
        FirebaseRefs.child(childUid).child("lastSeen").setValue(System.currentTimeMillis());
    }

    public void setMonitoringActive(String childUid, boolean active) {
        FirebaseRefs.child(childUid).child("monitoringActive").setValue(active);
    }

    public void updateDeviceInfo(String childUid, String model, String osVersion) {
        FirebaseRefs.child(childUid).child("deviceModel").setValue(model);
        FirebaseRefs.child(childUid).child("osVersion").setValue(osVersion);
    }
}
