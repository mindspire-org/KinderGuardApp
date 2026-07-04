package com.kinderguard.app.ui.parent.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.FragmentLocationBinding;
import com.kinderguard.app.model.LocationPoint;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class LocationFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentLocationBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private String childUid;
    private Marker marker;

    public static LocationFragment newInstance(String childUid) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(),
                requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(2.0);
        binding.mapView.getController().setCenter(new GeoPoint(20.0, 0.0));

        marker = new Marker(binding.mapView);
        binding.mapView.getOverlays().add(marker);

        binding.tvLastUpdated.setText("No location reported yet");
        binding.tvEmptyState.setVisibility(View.VISIBLE);
        binding.mapView.setVisibility(View.GONE);

        monitoringRepository.listenLocation(childUid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LocationPoint point = snapshot.getValue(LocationPoint.class);
                if (point == null || binding == null) return;
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.mapView.setVisibility(View.VISIBLE);
                GeoPoint geoPoint = new GeoPoint(point.latitude, point.longitude);
                marker.setPosition(geoPoint);
                binding.mapView.getController().setCenter(geoPoint);
                binding.mapView.getController().setZoom(16.0);
                binding.mapView.invalidate();
                binding.tvLastUpdated.setText(
                        "Last updated: " + DateUtils.getRelativeTimeSpanString(point.timestamp));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // no-op
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) binding.mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
