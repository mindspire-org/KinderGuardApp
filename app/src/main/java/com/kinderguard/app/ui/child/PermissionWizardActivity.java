package com.kinderguard.app.ui.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.kinderguard.app.R;
import com.kinderguard.app.data.ChildRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityPermissionWizardBinding;
import com.kinderguard.app.service.MonitoringForegroundService;
import com.kinderguard.app.utils.PermissionUtils;

import androidx.core.content.ContextCompat;

/**
 * 7-step onboarding wizard that walks the child through granting all permissions required
 * for monitoring to work, then hands off to {@link ChildDashboardActivity}.
 */
public class PermissionWizardActivity extends AppCompatActivity {

    private ActivityPermissionWizardBinding binding;
    private final ImageView[] dots = new ImageView[PermissionWizardPagerAdapter.STEP_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewPager.setAdapter(new PermissionWizardPagerAdapter(this));
        binding.viewPager.setUserInputEnabled(true);
        buildDots();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                updateNextButtonLabel(position);
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current > 0) {
                binding.viewPager.setCurrentItem(current - 1);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < PermissionWizardPagerAdapter.STEP_COUNT - 1) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                onFinishSetup();
            }
        });

        updateDots(0);
        updateNextButtonLabel(0);
    }

    private void buildDots() {
        binding.dotContainer.removeAllViews();
        for (int i = 0; i < PermissionWizardPagerAdapter.STEP_COUNT; i++) {
            ImageView dot = new ImageView(this);
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams params =
                    new android.widget.LinearLayout.LayoutParams(size, size);
            params.setMarginStart(margin);
            params.setMarginEnd(margin);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.dot_indicator_inactive);
            dots[i] = dot;
            binding.dotContainer.addView(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == selected
                    ? R.drawable.dot_indicator_active
                    : R.drawable.dot_indicator_inactive);
        }
    }

    private void updateNextButtonLabel(int position) {
        boolean lastStep = position == PermissionWizardPagerAdapter.STEP_COUNT - 1;
        binding.btnNext.setText(lastStep ? R.string.finish_setup : R.string.next);
        binding.btnBack.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void onFinishSetup() {
        if (!PermissionUtils.allRequiredPermissionsGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.finish_setup)
                    .setMessage("Not all permissions have been granted. Monitoring may be limited until you grant them.")
                    .setPositiveButton("Continue Anyway", (dialog, which) -> completeOnboarding())
                    .setNegativeButton("Go Back", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        new PrefsManager(this).setOnboardingDone(true);
        ContextCompat.startForegroundService(this,
                new Intent(this, MonitoringForegroundService.class));

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String childUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            new ChildRepository().setMonitoringActive(childUid, true);
        }

        Intent intent = new Intent(this, ChildDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
