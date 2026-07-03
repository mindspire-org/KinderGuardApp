package com.kinderguard.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.R;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.ChildRepository;
import com.kinderguard.app.data.ParentRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivitySignupBinding;
import com.kinderguard.app.model.ChildUser;
import com.kinderguard.app.model.ParentUser;
import com.kinderguard.app.model.UserRole;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final ParentRepository parentRepository = new ParentRepository();
    private final ChildRepository childRepository = new ChildRepository();
    private GoogleSignInClient googleSignInClient;
    private String roleExtra;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    handleGoogleToken(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : getString(R.string.error_generic),
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roleExtra = getIntent().getStringExtra(LoginActivity.EXTRA_ROLE);

        setBrandText();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnSignUp.setOnClickListener(v -> attemptSignUp());
        binding.btnGoogleSignUp.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        binding.tvLoginHere.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.EXTRA_ROLE, roleExtra);
            startActivity(intent);
        });
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

    private void attemptSignUp() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError(getString(R.string.name_hint));
            valid = false;
        } else {
            binding.tilName.setError(null);
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.email_hint));
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_hint));
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (!valid) return;

        setLoading(true);
        authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                createProfileAndContinue(user.getUid(), name, email);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleToken(String idToken) {
        setLoading(true);
        authRepository.loginWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                String name = user.getDisplayName() != null ? user.getDisplayName() : "";
                String email = user.getEmail() != null ? user.getEmail() : "";
                createProfileAndContinue(user.getUid(), name, email);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createProfileAndContinue(String uid, String name, String email) {
        UserRole role = roleExtra != null ? UserRole.valueOf(roleExtra) : UserRole.PARENT;
        new PrefsManager(this).setRole(role);

        if (role == UserRole.CHILD) {
            ChildUser child = new ChildUser(uid, name, email);
            childRepository.createChildProfile(child, new ChildRepository.ResultCallback<Void>() {
                @Override
                public void onResult(Void value) {
                    goToVerification();
                }

                @Override
                public void onError(String message) {
                    setLoading(false);
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ParentUser parent = new ParentUser(uid, name, email);
            parentRepository.createParentProfile(parent, new ParentRepository.ResultCallback<Void>() {
                @Override
                public void onResult(Void value) {
                    goToVerification();
                }

                @Override
                public void onError(String message) {
                    setLoading(false);
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void goToVerification() {
        setLoading(false);
        Intent intent = new Intent(this, EmailVerificationActivity.class);
        intent.putExtra(LoginActivity.EXTRA_ROLE, roleExtra);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSignUp.setEnabled(!loading);
        binding.btnGoogleSignUp.setEnabled(!loading);
    }
}
