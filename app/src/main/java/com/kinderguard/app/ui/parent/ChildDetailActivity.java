package com.kinderguard.app.ui.parent;

import android.os.Bundle;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayoutMediator;
import com.kinderguard.app.R;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityChildDetailBinding;
import com.kinderguard.app.model.Command;
import com.kinderguard.app.utils.Constants;

public class ChildDetailActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {
            "tab_apps", "tab_usage", "tab_calls", "tab_sms", "tab_location", "tab_geofence"
    };

    private ActivityChildDetailBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private String childUid;
    private boolean locked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChildDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        childUid = getIntent().getStringExtra(Constants.EXTRA_CHILD_UID);
        if (childUid == null) {
            childUid = new PrefsManager(this).getSelectedChildUid();
        }
        String childName = getIntent().getStringExtra(Constants.EXTRA_CHILD_NAME);
        binding.tvChildName.setText(childName);

        binding.btnBack.setOnClickListener(v -> finish());

        setupTabs();
        setupLockToggle();
    }

    private void setupTabs() {
        ChildDetailPagerAdapter pagerAdapter = new ChildDetailPagerAdapter(this, childUid);
        binding.viewPager.setAdapter(pagerAdapter);

        int[] tabTitleRes = {
                R.string.tab_apps, R.string.tab_usage, R.string.tab_calls,
                R.string.tab_sms, R.string.tab_location, R.string.tab_geofence
        };

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitleRes[position])).attach();
    }

    private void setupLockToggle() {
        updateLockButtonText();
        binding.btnLockToggle.setOnClickListener(v -> {
            locked = !locked;
            String type = locked ? Command.TYPE_LOCK : Command.TYPE_UNLOCK;
            Command command = new Command(UUID.randomUUID().toString(), type, null,
                    System.currentTimeMillis());
            monitoringRepository.sendCommand(childUid, command);
            updateLockButtonText();
        });
    }

    private void updateLockButtonText() {
        binding.btnLockToggle.setText(locked ? R.string.unlock_device : R.string.lock_device);
    }
}
