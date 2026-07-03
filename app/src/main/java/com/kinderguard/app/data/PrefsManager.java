package com.kinderguard.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.kinderguard.app.model.UserRole;
import com.kinderguard.app.utils.Constants;

public class PrefsManager {
    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setRole(UserRole role) {
        prefs.edit().putString(Constants.KEY_ROLE, role == null ? null : role.name()).apply();
    }

    public UserRole getRole() {
        String value = prefs.getString(Constants.KEY_ROLE, null);
        return value == null ? null : UserRole.valueOf(value);
    }

    public void setLinkedParentUid(String uid) {
        prefs.edit().putString(Constants.KEY_LINKED_PARENT_UID, uid).apply();
    }

    public String getLinkedParentUid() {
        return prefs.getString(Constants.KEY_LINKED_PARENT_UID, null);
    }

    public void setLinkCode(String code) {
        prefs.edit().putString(Constants.KEY_LINK_CODE, code).apply();
    }

    public String getLinkCode() {
        return prefs.getString(Constants.KEY_LINK_CODE, null);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(Constants.KEY_ONBOARDING_DONE, done).apply();
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(Constants.KEY_ONBOARDING_DONE, false);
    }

    public void setSelectedChildUid(String uid) {
        prefs.edit().putString(Constants.KEY_SELECTED_CHILD_UID, uid).apply();
    }

    public String getSelectedChildUid() {
        return prefs.getString(Constants.KEY_SELECTED_CHILD_UID, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
