package com.kinderguard.app.ui.child;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/** Hosts the 7 {@link PermissionStepFragment} steps for {@link PermissionWizardActivity}. */
public class PermissionWizardPagerAdapter extends FragmentStateAdapter {

    public static final int STEP_COUNT = 7;

    public PermissionWizardPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PermissionStepFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return STEP_COUNT;
    }
}
