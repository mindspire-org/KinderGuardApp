package com.kinderguard.app.ui.child;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.kinderguard.app.databinding.ActivityBlockedAppBinding;
import com.kinderguard.app.utils.Constants;

/**
 * Full-screen interstitial shown by the monitoring service when the child opens a blocked app.
 * There is intentionally no way to dismiss back into the blocked app.
 */
public class BlockedAppActivity extends AppCompatActivity {

    private ActivityBlockedAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String appLabel = getIntent().getStringExtra(Constants.EXTRA_BLOCKED_APP_LABEL);
        binding.tvBlockedAppName.setText(appLabel != null ? appLabel : "");

        binding.btnGoBack.setOnClickListener(v -> goHome());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        });
    }

    private void goHome() {
        moveTaskToBack(true);
        finish();
    }
}
