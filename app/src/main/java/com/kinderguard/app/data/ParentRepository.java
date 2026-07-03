package com.kinderguard.app.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kinderguard.app.model.ChildUser;
import com.kinderguard.app.model.ParentUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParentRepository {

    public interface ResultCallback<T> {
        void onResult(T value);
        void onError(String message);
    }

    public void createParentProfile(ParentUser parent, ResultCallback<Void> callback) {
        FirebaseRefs.parent(parent.uid).setValue(parent.toMap())
                .addOnSuccessListener(unused -> callback.onResult(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getParentProfile(String parentUid, ResultCallback<ParentUser> callback) {
        FirebaseRefs.parent(parentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.getValue(ParentUser.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /** Generates a random 6-character alphanumeric code for parent-child linking. */
    public String generateLinkCode(String parentUid, ResultCallback<String> callback) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        FirebaseRefs.linkCode(code).setValue(parentUid)
                .addOnSuccessListener(unused -> callback.onResult(code))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
        return code;
    }

    public void listenLinkedChildren(String parentUid, ResultCallback<List<ChildUser>> callback) {
        FirebaseRefs.parent(parentUid).child("linkedChildren")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> childUids = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            childUids.add(child.getKey());
                        }
                        fetchChildren(childUids, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void fetchChildren(List<String> uids, ResultCallback<List<ChildUser>> callback) {
        if (uids.isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        List<ChildUser> results = new ArrayList<>();
        int[] remaining = {uids.size()};
        for (String uid : uids) {
            FirebaseRefs.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ChildUser child = snapshot.getValue(ChildUser.class);
                    if (child != null) results.add(child);
                    remaining[0]--;
                    if (remaining[0] == 0) callback.onResult(results);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    remaining[0]--;
                    if (remaining[0] == 0) callback.onResult(results);
                }
            });
        }
    }

    public void linkChildToParent(String linkCode, String childUid, ResultCallback<String> callback) {
        FirebaseRefs.linkCode(linkCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String parentUid = snapshot.getValue(String.class);
                if (parentUid == null) {
                    callback.onError("Invalid or expired link code");
                    return;
                }
                FirebaseRefs.child(childUid).child("parentUid").setValue(parentUid);
                FirebaseRefs.parent(parentUid).child("linkedChildren").child(childUid).setValue(true)
                        .addOnSuccessListener(unused -> callback.onResult(parentUid))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
