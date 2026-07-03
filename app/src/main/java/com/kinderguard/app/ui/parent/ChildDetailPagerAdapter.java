package com.kinderguard.app.ui.parent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kinderguard.app.ui.parent.fragments.CallLogsFragment;
import com.kinderguard.app.ui.parent.fragments.GeofenceFragment;
import com.kinderguard.app.ui.parent.fragments.InstalledAppsFragment;
import com.kinderguard.app.ui.parent.fragments.LocationFragment;
import com.kinderguard.app.ui.parent.fragments.ScreenUsageFragment;
import com.kinderguard.app.ui.parent.fragments.SmsLogsFragment;

public class ChildDetailPagerAdapter extends FragmentStateAdapter {

    private final String childUid;

    public ChildDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, String childUid) {
        super(fragmentActivity);
        this.childUid = childUid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return InstalledAppsFragment.newInstance(childUid);
            case 1:
                return ScreenUsageFragment.newInstance(childUid);
            case 2:
                return CallLogsFragment.newInstance(childUid);
            case 3:
                return SmsLogsFragment.newInstance(childUid);
            case 4:
                return LocationFragment.newInstance(childUid);
            case 5:
                return GeofenceFragment.newInstance(childUid);
            default:
                throw new IllegalArgumentException("Invalid tab position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
