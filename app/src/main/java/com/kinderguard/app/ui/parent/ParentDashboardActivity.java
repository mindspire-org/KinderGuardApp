package com.kinderguard.app.ui.parent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.R;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.ParentRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityParentDashboardBinding;
import com.kinderguard.app.model.ChildUser;
import com.kinderguard.app.model.ParentUser;
import com.kinderguard.app.ui.common.AboutActivity;
import com.kinderguard.app.ui.common.SettingsActivity;
import com.kinderguard.app.ui.welcome.WelcomeActivity;
import com.kinderguard.app.utils.Constants;

public class ParentDashboardActivity extends AppCompatActivity {

    private ActivityParentDashboardBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final ParentRepository parentRepository = new ParentRepository();
    private ChildAdapter childAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            navigateToWelcome();
            return;
        }
        String parentUid = currentUser.getUid();

        setupRecyclerView();
        setupClickListeners(parentUid);

        parentRepository.getParentProfile(parentUid, new ParentRepository.ResultCallback<ParentUser>() {
            @Override
            public void onResult(ParentUser value) {
                if (value != null) {
                    binding.tvParentName.setText(value.name);
                    binding.tvParentEmail.setText(value.email);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ParentDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        parentRepository.listenLinkedChildren(parentUid, new ParentRepository.ResultCallback<java.util.List<ChildUser>>() {
            @Override
            public void onResult(java.util.List<ChildUser> value) {
                childAdapter.submitList(value);
                boolean empty = value == null || value.isEmpty();
                binding.tvEmptyState.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
                binding.rvChildren.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ParentDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        childAdapter = new ChildAdapter(child -> {
            new PrefsManager(this).setSelectedChildUid(child.uid);
            Intent intent = new Intent(this, ChildDetailActivity.class);
            intent.putExtra(Constants.EXTRA_CHILD_UID, child.uid);
            intent.putExtra(Constants.EXTRA_CHILD_NAME, child.name);
            startActivity(intent);
        });
        binding.rvChildren.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChildren.setAdapter(childAdapter);
    }

    private void setupClickListeners(String parentUid) {
        binding.btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, LinkChildActivity.class)));

        binding.btnOverflow.setOnClickListener(this::showOverflowMenu);
    }

    private void showOverflowMenu(android.view.View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.parent_dashboard_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.action_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            } else if (id == R.id.action_logout) {
                doLogout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void doLogout() {
        authRepository.logout();
        PrefsManager prefsManager = new PrefsManager(this);
        prefsManager.setSelectedChildUid(null);
        navigateToWelcome();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
