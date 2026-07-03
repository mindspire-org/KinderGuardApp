package com.kinderguard.app.ui.child;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.kinderguard.app.data.ParentRepository;
import com.kinderguard.app.databinding.ActivityLinkParentBinding;

public class LinkParentActivity extends AppCompatActivity {

    private ActivityLinkParentBinding binding;
    private final ParentRepository parentRepository = new ParentRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLinkParentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLink.setOnClickListener(v -> submitCode());
        binding.tvSkip.setOnClickListener(v -> proceedWithoutLinking());
    }

    private void submitCode() {
        String code = binding.etCode.getText() != null
                ? binding.etCode.getText().toString().trim().toUpperCase()
                : "";
        if (TextUtils.isEmpty(code)) {
            binding.etCode.setError("Enter the code from your parent's app");
            return;
        }

        String childUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (childUid == null) {
            Toast.makeText(this, "You're not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        parentRepository.linkChildToParent(code, childUid, new ParentRepository.ResultCallback<String>() {
            @Override
            public void onResult(String parentUid) {
                setLoading(false);
                Toast.makeText(LinkParentActivity.this, "Linked to your parent!", Toast.LENGTH_SHORT).show();
                ChildRoutingHelper.routeChildAfterAuth(LinkParentActivity.this);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LinkParentActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void proceedWithoutLinking() {
        // Lets the child continue setup now and link later from the dashboard/settings.
        ChildRoutingHelper.routePastLinkingCheck(this);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLink.setEnabled(!loading);
    }
}
