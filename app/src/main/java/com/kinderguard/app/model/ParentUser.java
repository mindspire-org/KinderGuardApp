package com.kinderguard.app.model;

import java.util.HashMap;
import java.util.Map;

public class ParentUser {
    public String uid;
    public String name;
    public String email;
    public String phoneNumber;
    public String profileImageUrl;
    public Map<String, Boolean> linkedChildren = new HashMap<>();
    public long createdAt;

    public ParentUser() {
        // required for Firebase
    }

    public ParentUser(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("name", name);
        map.put("email", email);
        map.put("phoneNumber", phoneNumber);
        map.put("profileImageUrl", profileImageUrl);
        map.put("linkedChildren", linkedChildren);
        map.put("createdAt", createdAt);
        return map;
    }
}
