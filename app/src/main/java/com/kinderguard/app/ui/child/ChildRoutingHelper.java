package com.kinderguard.app.ui.child;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.kinderguard.app.data.ChildRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.model.ChildUser;

/** Decides where to send a logged-in child: link-to-parent, permission wizard, or dashboard. */
public final class ChildRoutingHelper {

    private ChildRoutingHelper() {}

    public static void routeChildAfterAuth(Activity activity) {
        String childUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (childUid == null) {
            navigateTo(activity, LinkParentActivity.class);
            return;
        }

        new ChildRepository().getChildProfile(childUid, new ChildRepository.ResultCallback<ChildUser>() {
            @Override
            public void onResult(ChildUser child) {
                if (child == null || child.parentUid == null || child.parentUid.isEmpty()) {
                    navigateTo(activity, LinkParentActivity.class);
                } else {
                    proceedPastLinking(activity);
                }
            }

            @Override
            public void onError(String message) {
                // Network hiccup reading the profile shouldn't trap the user - assume linked
                // and let ChildDashboardActivity re-check state on its own.
                proceedPastLinking(activity);
            }
        });
    }

    /** For flows (e.g. "Skip for now") that intentionally bypass the linking check. */
    public static void routePastLinkingCheck(Activity activity) {
        proceedPastLinking(activity);
    }

    private static void proceedPastLinking(Activity activity) {
        PrefsManager prefsManager = new PrefsManager(activity);
        if (prefsManager.isOnboardingDone()) {
            navigateTo(activity, ChildDashboardActivity.class);
        } else {
            navigateTo(activity, PermissionWizardActivity.class);
        }
    }

    private static void navigateTo(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
