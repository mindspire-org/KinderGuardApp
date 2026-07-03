package com.kinderguard.app.ui.parent;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.ParentRepository;
import com.kinderguard.app.databinding.ActivityLinkChildBinding;

public class LinkChildActivity extends AppCompatActivity {

    private ActivityLinkChildBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final ParentRepository parentRepository = new ParentRepository();
    private String currentCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLinkChildBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            currentCode = parentRepository.generateLinkCode(currentUser.getUid(),
                    new ParentRepository.ResultCallback<String>() {
                        @Override
                        public void onResult(String value) {
                            currentCode = value;
                            binding.tvCode.setText(value);
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(LinkChildActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
            binding.tvCode.setText(currentCode);
        }

        binding.btnCopyCode.setOnClickListener(v -> {
            if (currentCode == null) return;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("link_code", currentCode);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, "Code copied", Toast.LENGTH_SHORT).show();
        });
    }
}
