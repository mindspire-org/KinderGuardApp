package com.kinderguard.app.ui.parent;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.R;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.data.ParentRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityParentDashboardBinding;
import com.kinderguard.app.model.ChildUser;
import com.kinderguard.app.model.ParentUser;
import com.kinderguard.app.model.SosAlert;
import com.kinderguard.app.ui.common.AboutActivity;
import com.kinderguard.app.ui.common.SettingsActivity;
import com.kinderguard.app.ui.welcome.WelcomeActivity;
import com.kinderguard.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParentDashboardActivity extends AppCompatActivity {

    private static final long SOS_NOTIFY_FRESHNESS_MS = 5 * 60_000L;

    private ActivityParentDashboardBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final ParentRepository parentRepository = new ParentRepository();
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private ChildAdapter childAdapter;

    private final Set<String> sosListenersAttached = new HashSet<>();
    private final Map<String, List<SosAlert>> sosByChild = new HashMap<>();
    private final Set<String> notifiedAlertIds = new HashSet<>();

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
                attachSosListeners(value);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ParentDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---- SOS alerts ----

    private void attachSosListeners(List<ChildUser> children) {
        if (children == null) return;
        for (ChildUser child : children) {
            if (child == null || child.uid == null || !sosListenersAttached.add(child.uid)) continue;
            monitoringRepository.listenAllSos(child.uid, alerts -> {
                sosByChild.put(child.uid, alerts != null ? alerts : new ArrayList<>());
                notifyIfNewSos(alerts);
                updateSosBanner();
            });
        }
    }

    private void notifyIfNewSos(List<SosAlert> alerts) {
        if (alerts == null) return;
        long now = System.currentTimeMillis();
        for (SosAlert alert : alerts) {
            if (alert == null || alert.acknowledged || alert.id == null) continue;
            if (notifiedAlertIds.contains(alert.id)) continue;
            notifiedAlertIds.add(alert.id);
            if (now - alert.timestamp <= SOS_NOTIFY_FRESHNESS_MS) {
                postSosNotification(alert);
            }
        }
    }

    private void postSosNotification(SosAlert alert) {
        try {
            String name = alert.childName != null ? alert.childName : "Your child";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ALERTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.sos_alert_title))
                    .setContentText(name + " sent an emergency SOS alert")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(this)
                    .notify(Constants.NOTIF_ID_ALERT + alert.id.hashCode(), builder.build());
        } catch (SecurityException se) {
            // notification permission not granted; the in-app banner still covers this.
        }
    }

    private void updateSosBanner() {
        List<SosAlert> pending = new ArrayList<>();
        for (List<SosAlert> list : sosByChild.values()) {
            for (SosAlert alert : list) {
                if (alert != null && !alert.acknowledged) pending.add(alert);
            }
        }
        if (pending.isEmpty()) {
            binding.cardSosBanner.setVisibility(android.view.View.GONE);
            return;
        }
        SosAlert latest = pending.get(0);
        for (SosAlert alert : pending) {
            if (alert.timestamp > latest.timestamp) latest = alert;
        }
        String name = latest.childName != null ? latest.childName : "A linked child";
        CharSequence when = DateUtils.getRelativeTimeSpanString(latest.timestamp);
        String text = name + " sent an SOS alert " + when;
        if (pending.size() > 1) {
            text += " (+" + (pending.size() - 1) + " more)";
        }
        binding.tvSosBanner.setText(text);
        binding.cardSosBanner.setVisibility(android.view.View.VISIBLE);

        SosAlert finalLatest = latest;
        binding.cardSosBanner.setOnClickListener(v -> openChildDetail(finalLatest));
        binding.btnDismissSos.setOnClickListener(v ->
                monitoringRepository.acknowledgeSos(finalLatest.childUid, finalLatest.id));
    }

    private void openChildDetail(SosAlert alert) {
        new PrefsManager(this).setSelectedChildUid(alert.childUid);
        Intent intent = new Intent(this, ChildDetailActivity.class);
        intent.putExtra(Constants.EXTRA_CHILD_UID, alert.childUid);
        intent.putExtra(Constants.EXTRA_CHILD_NAME, alert.childName);
        startActivity(intent);
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
