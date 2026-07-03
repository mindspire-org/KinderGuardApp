package com.kinderguard.app.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivitySettingsBinding;
import com.kinderguard.app.ui.welcome.WelcomeActivity;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            binding.tvEmail.setText(user.getEmail());
            String displayName = user.getDisplayName();
            binding.tvName.setText(displayName != null ? displayName : "");
            binding.etName.setText(displayName != null ? displayName : "");
        }

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show());

        binding.btnLogout.setOnClickListener(v -> logout());

        binding.btnDeleteAccount.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Account")
                        .setMessage("This will permanently delete your account. This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    private void logout() {
        authRepository.logout();
        new PrefsManager(this).clear();
        goToWelcome();
    }

    private void deleteAccount() {
        authRepository.deleteAccount(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                new PrefsManager(SettingsActivity.this).clear();
                goToWelcome();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
