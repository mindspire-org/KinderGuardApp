package com.kinderguard.app.ui.child;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kinderguard.app.R;
import com.kinderguard.app.data.AuthRepository;
import com.kinderguard.app.data.ChildRepository;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.data.PrefsManager;
import com.kinderguard.app.databinding.ActivityChildDashboardBinding;
import com.kinderguard.app.databinding.RowPermissionStatusBinding;
import com.kinderguard.app.databinding.RowRunningServiceBinding;
import com.kinderguard.app.model.ChildUser;
import com.kinderguard.app.model.SosAlert;
import com.kinderguard.app.ui.common.AboutActivity;
import com.kinderguard.app.ui.common.SettingsActivity;
import com.kinderguard.app.ui.welcome.WelcomeActivity;
import com.kinderguard.app.utils.PermissionUtils;

import java.util.UUID;

/**
 * Child-side home screen: shows profile, monitoring status, permission status,
 * running services, device info, and a Send SOS button.
 */
public class ChildDashboardActivity extends AppCompatActivity {

    private ActivityChildDashboardBinding binding;
    private final ChildRepository childRepository = new ChildRepository();
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!new PrefsManager(this).isOnboardingDone()) {
            startActivity(new Intent(this, PermissionWizardActivity.class));
            finish();
            return;
        }

        binding = ActivityChildDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnOverflow.setOnClickListener(this::showOverflowMenu);
        binding.btnSendSos.setOnClickListener(v -> sendSos());

        loadProfile();
        loadDeviceInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
        startMonitoringServiceIfPermitted();
    }

    private void startMonitoringServiceIfPermitted() {
        if (PermissionUtils.hasLocationPermission(this)) {
            androidx.core.content.ContextCompat.startForegroundService(this,
                    new Intent(this, com.kinderguard.app.service.MonitoringForegroundService.class));
        }
    }

    private void loadProfile() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) return;
        binding.tvChildName.setText(user.getDisplayName() != null ? user.getDisplayName() : "-");
        binding.tvChildEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        childRepository.getChildProfile(user.getUid(), new ChildRepository.ResultCallback<ChildUser>() {
            @Override
            public void onResult(ChildUser value) {
                if (value == null) return;
                if (value.name != null && !value.name.isEmpty()) {
                    binding.tvChildName.setText(value.name);
                }
                if (value.email != null && !value.email.isEmpty()) {
                    binding.tvChildEmail.setText(value.email);
                }
            }

            @Override
            public void onError(String message) {
                // Keep Firebase Auth values as fallback.
            }
        });
    }

    private void loadDeviceInfo() {
        binding.tvDeviceModel.setText("Model: " + Build.MODEL);
        binding.tvDeviceOs.setText("Android: " + Build.VERSION.RELEASE);

        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            childRepository.updateDeviceInfo(user.getUid(), Build.MODEL, Build.VERSION.RELEASE);
        }
    }

    private void refreshUi() {
        boolean allGranted = PermissionUtils.allRequiredPermissionsGranted(this);

        binding.tvMonitoringStatus.setText(allGranted
                ? R.string.monitoring_active
                : R.string.monitoring_inactive);
        binding.tvMonitoringStatus.setBackgroundResource(allGranted
                ? R.drawable.bg_chip_success
                : R.drawable.bg_chip_danger);
        binding.tvMonitoringStatus.setTextColor(getColor(allGranted
                ? R.color.kg_success
                : R.color.kg_danger));

        buildPermissionStatusRows();
        buildRunningServicesRows(allGranted);
    }

    private void buildPermissionStatusRows() {
        binding.permissionStatusContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        String[] labels = {
                "Location permission",
                "Background location",
                "Usage access",
                "Notifications",
                "Display over apps",
                "Device admin",
                "SMS & Call access"
        };
        boolean[] granted = {
                PermissionUtils.hasLocationPermission(this),
                PermissionUtils.hasBackgroundLocationPermission(this),
                PermissionUtils.hasUsageAccessPermission(this),
                PermissionUtils.hasNotificationPermission(this),
                PermissionUtils.hasOverlayPermission(this),
                PermissionUtils.isDeviceAdminActive(this),
                PermissionUtils.hasSmsCallPermissions(this)
        };

        for (int i = 0; i < labels.length; i++) {
            RowPermissionStatusBinding row = RowPermissionStatusBinding.inflate(
                    inflater, binding.permissionStatusContainer, false);
            row.tvLabel.setText(labels[i]);
            row.imgStatus.setImageResource(granted[i]
                    ? R.drawable.ic_check_circle
                    : R.drawable.ic_cancel_circle);
            binding.permissionStatusContainer.addView(row.getRoot());
        }
    }

    private void buildRunningServicesRows(boolean active) {
        binding.runningServicesContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        String[] labels = {
                "Location Monitoring",
                "App Monitoring",
                "Call & SMS Monitoring"
        };

        for (String label : labels) {
            RowRunningServiceBinding row = RowRunningServiceBinding.inflate(
                    inflater, binding.runningServicesContainer, false);
            row.tvLabel.setText(label);
            row.imgDot.setImageResource(active
                    ? R.drawable.dot_status_green
                    : R.drawable.dot_status_gray);
            binding.runningServicesContainer.addView(row.getRoot());
        }
    }

    private void showOverflowMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.child_dashboard_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.menu_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void logout() {
        authRepository.logout();
        new PrefsManager(this).clear();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendSos() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) return;

        String childUid = user.getUid();
        String childName = binding.tvChildName.getText() != null
                ? binding.tvChildName.getText().toString()
                : (user.getDisplayName() != null ? user.getDisplayName() : "");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation()
                    .addOnSuccessListener(location -> {
                        double lat = location != null ? location.getLatitude() : 0;
                        double lng = location != null ? location.getLongitude() : 0;
                        dispatchSos(childUid, childName, lat, lng);
                    })
                    .addOnFailureListener(e -> dispatchSos(childUid, childName, 0, 0));
        } else {
            dispatchSos(childUid, childName, 0, 0);
        }
    }

    private void dispatchSos(String childUid, String childName, double lat, double lng) {
        SosAlert alert = new SosAlert(
                UUID.randomUUID().toString(),
                childUid,
                childName,
                lat,
                lng,
                System.currentTimeMillis());
        monitoringRepository.sendSos(childUid, alert);
        Toast.makeText(this, "SOS sent to your parent", Toast.LENGTH_SHORT).show();
    }
}
