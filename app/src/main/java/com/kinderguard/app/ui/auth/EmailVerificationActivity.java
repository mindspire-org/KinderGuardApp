package com.kinderguard.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityEmailVerificationBinding;
import com.kinderguard.app.model.UserRole;
import com.kinderguard.app.ui.parent.ParentDashboardActivity;

public class EmailVerificationActivity extends AppCompatActivity {

    private ActivityEmailVerificationBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private String roleExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roleExtra = getIntent().getStringExtra(LoginActivity.EXTRA_ROLE);

        binding.btnResend.setOnClickListener(v -> resendEmail());
        binding.btnVerified.setOnClickListener(v -> checkVerified());

        if (authRepository.isEmailVerified()) {
            routeAfterVerified();
        }
    }

    private void resendEmail() {
        setLoading(true);
        authRepository.sendEmailVerification(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(EmailVerificationActivity.this, "Verification email sent", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkVerified() {
        setLoading(true);
        authRepository.reloadUser(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                if (authRepository.isEmailVerified()) {
                    routeAfterVerified();
                } else {
                    Toast.makeText(EmailVerificationActivity.this,
                            "Email not verified yet, please check your inbox.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void routeAfterVerified() {
        UserRole role = roleExtra != null ? UserRole.valueOf(roleExtra) : UserRole.PARENT;
        PrefsManager prefsManager = new PrefsManager(this);
        prefsManager.setRole(role);

        if (role == UserRole.CHILD) {
            com.kinderguard.app.ui.child.ChildRoutingHelper.routeChildAfterAuth(this);
            return;
        }

        Intent intent = new Intent(this, ParentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnResend.setEnabled(!loading);
        binding.btnVerified.setEnabled(!loading);
    }
}
