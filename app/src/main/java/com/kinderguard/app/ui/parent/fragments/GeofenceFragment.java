package com.kinderguard.app.ui.parent.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.DialogAddGeofenceBinding;
import com.kinderguard.app.databinding.FragmentGeofenceBinding;
import com.kinderguard.app.model.GeofenceZone;
import com.kinderguard.app.ui.parent.GeofenceAdapter;

public class GeofenceFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentGeofenceBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private GeofenceAdapter adapter;
    private String childUid;

    public static GeofenceFragment newInstance(String childUid) {
        GeofenceFragment fragment = new GeofenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentGeofenceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new GeofenceAdapter(zone ->
                monitoringRepository.deleteGeofence(childUid, zone.id));
        binding.rvGeofences.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGeofences.setAdapter(adapter);

        binding.btnAddSafeZone.setOnClickListener(v -> showAddDialog());

        monitoringRepository.listenGeofences(childUid, list -> {
            if (binding == null) return;
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvGeofences.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void showAddDialog() {
        DialogAddGeofenceBinding dialogBinding =
                DialogAddGeofenceBinding.inflate(LayoutInflater.from(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Safe Zone")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) ->
                        onSaveGeofence(dialogBinding))
                .setNegativeButton(com.kinderguard.app.R.string.cancel, null)
                .show();
    }

    private void onSaveGeofence(DialogAddGeofenceBinding dialogBinding) {
        String name = getText(dialogBinding.etZoneName);
        String latStr = getText(dialogBinding.etLatitude);
        String lngStr = getText(dialogBinding.etLongitude);
        String radiusStr = getText(dialogBinding.etRadius);

        if (name.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || radiusStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat, lng, radius;
        try {
            lat = Double.parseDouble(latStr);
            lng = Double.parseDouble(lngStr);
            radius = Double.parseDouble(radiusStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Latitude, longitude, and radius must be numbers",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        GeofenceZone zone = new GeofenceZone(UUID.randomUUID().toString(), name, lat, lng, radius);
        monitoringRepository.saveGeofence(childUid, zone);
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
