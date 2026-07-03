package com.kinderguard.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityLoginBinding;
import com.kinderguard.app.model.UserRole;
import com.kinderguard.app.ui.parent.ParentDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_ROLE = "extra_role";

    private ActivityLoginBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
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
                    String idToken = account.getIdToken();
                    handleGoogleToken(idToken);
                } catch (ApiException e) {
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : getString(R.string.error_generic),
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roleExtra = getIntent().getStringExtra(EXTRA_ROLE);

        setBrandText();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnGoogleLogin.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        binding.tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        binding.tvSignUpHere.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            intent.putExtra(EXTRA_ROLE, roleExtra);
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

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.email_hint));
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.password_hint));
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (!valid) return;

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                afterAuthSuccess();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleToken(String idToken) {
        setLoading(true);
        authRepository.loginWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                afterAuthSuccess();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void afterAuthSuccess() {
        authRepository.reloadUser(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                if (authRepository.isEmailVerified()) {
                    routeAfterVerifiedLogin();
                } else {
                    Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                    intent.putExtra(EXTRA_ROLE, roleExtra);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void routeAfterVerifiedLogin() {
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
        binding.btnLogin.setEnabled(!loading);
        binding.btnGoogleLogin.setEnabled(!loading);
    }
}
