package com.kinderguard.app.ui.child;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.FragmentPermissionStepBinding;
import com.kinderguard.app.service.KinderGuardDeviceAdminReceiver;
import com.kinderguard.app.utils.PermissionUtils;

/**
 * One step of the {@link PermissionWizardActivity} wizard. Parameterized by stepIndex (0..6),
 * each step shows an icon, a description, a granted/not-granted switch, and a "Grant Permission"
 * button that triggers the matching OS permission flow.
 */
public class PermissionStepFragment extends Fragment {

    private static final String ARG_STEP_INDEX = "arg_step_index";

    private static final int[] ICONS = {
            R.drawable.ic_perm_location,
            R.drawable.ic_perm_background,
            R.drawable.ic_perm_usage,
            R.drawable.ic_perm_notification,
            R.drawable.ic_perm_overlay,
            R.drawable.ic_perm_admin,
            R.drawable.ic_perm_sms
    };

    private static final int[] TITLES = {
            R.string.permission_location_title,
            R.string.permission_background_location_title,
            R.string.permission_usage_title,
            R.string.permission_notifications_title,
            R.string.permission_overlay_title,
            R.string.permission_admin_title,
            R.string.permission_sms_call_title
    };

    private static final String[] LABELS = {
            "Location permission",
            "Background location",
            "Usage access",
            "Notifications",
            "Display over apps",
            "Device admin",
            "SMS & Call access"
    };

    private FragmentPermissionStepBinding binding;
    private int stepIndex;

    private ActivityResultLauncher<String[]> permissionLauncher;

    public static PermissionStepFragment newInstance(int stepIndex) {
        PermissionStepFragment fragment = new PermissionStepFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STEP_INDEX, stepIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepIndex = getArguments() != null ? getArguments().getInt(ARG_STEP_INDEX) : 0;
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> refreshStatus());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentPermissionStepBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.imgIcon.setImageResource(ICONS[stepIndex]);
        binding.tvStepTitle.setText(TITLES[stepIndex]);
        binding.tvPermLabel.setText(LABELS[stepIndex]);
        binding.btnGrant.setOnClickListener(v -> onGrantClicked());
        refreshStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void onGrantClicked() {
        Context ctx = requireContext();
        switch (stepIndex) {
            case 0: // Location
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
                break;
            case 1: // Background location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                } else {
                    Toast.makeText(ctx, "Not required on this Android version", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: // Usage access
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                break;
            case 3: // Notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
                } else {
                    Toast.makeText(ctx, "Not required on this Android version", Toast.LENGTH_SHORT).show();
                }
                break;
            case 4: // Overlay
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + ctx.getPackageName())));
                break;
            case 5: // Device admin
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        new ComponentName(ctx, KinderGuardDeviceAdminReceiver.class));
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "KinderGuard needs device admin access to prevent uninstallation.");
                startActivity(intent);
                break;
            case 6: // SMS & Call
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS
                });
                break;
            default:
                break;
        }
    }

    private void refreshStatus() {
        if (binding == null || getContext() == null) return;
        Context ctx = requireContext();
        boolean granted;
        switch (stepIndex) {
            case 0:
                granted = PermissionUtils.hasLocationPermission(ctx);
                break;
            case 1:
                granted = PermissionUtils.hasBackgroundLocationPermission(ctx);
                break;
            case 2:
                granted = PermissionUtils.hasUsageAccessPermission(ctx);
                break;
            case 3:
                granted = PermissionUtils.hasNotificationPermission(ctx);
                break;
            case 4:
                granted = PermissionUtils.hasOverlayPermission(ctx);
                break;
            case 5:
                granted = PermissionUtils.isDeviceAdminActive(ctx);
                break;
            case 6:
                granted = PermissionUtils.hasSmsCallPermissions(ctx);
                break;
            default:
                granted = false;
        }
        binding.switchStatus.setChecked(granted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
