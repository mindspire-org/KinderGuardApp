package com.kinderguard.app.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.AppCompatActivity;

import com.kinderguard.app.R;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityWelcomeBinding;
import com.kinderguard.app.model.UserRole;
import com.kinderguard.app.ui.auth.LoginActivity;
import com.kinderguard.app.ui.child.ChildDashboardActivity;
import com.kinderguard.app.ui.child.PermissionWizardActivity;
import com.kinderguard.app.ui.parent.ParentDashboardActivity;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (authRepository.isLoggedIn()) {
            routeExistingUser();
            return;
        }

        setBrandText();

        binding.btnParent.setOnClickListener(v -> onRoleSelected(UserRole.PARENT, "PARENT"));
        binding.btnChild.setOnClickListener(v -> onRoleSelected(UserRole.CHILD, "CHILD"));
    }

    private void setBrandText() {
        String kinder = "Kinder";
        String guard = "Guard";
        SpannableString spannable = new SpannableString(kinder + guard);
        spannable.setSpan(new ForegroundColorSpan(getColor(R.color.kg_teal)), 0, kinder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getColor(R.color.kg_primary_blue)), kinder.length(),
                kinder.length() + guard.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvBrand.setText(spannable);
    }

    private void onRoleSelected(UserRole role, String roleExtra) {
        new PrefsManager(this).setRole(role);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_ROLE, roleExtra);
        startActivity(intent);
    }

    private void routeExistingUser() {
        PrefsManager prefsManager = new PrefsManager(this);
        UserRole role = prefsManager.getRole();
        Intent intent;
        if (role == UserRole.CHILD) {
            if (prefsManager.isOnboardingDone()) {
                intent = new Intent(this, ChildDashboardActivity.class);
            } else {
                intent = new Intent(this, PermissionWizardActivity.class);
            }
        } else {
            intent = new Intent(this, ParentDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
