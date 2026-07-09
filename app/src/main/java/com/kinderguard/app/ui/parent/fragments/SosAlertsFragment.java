package com.kinderguard.app.ui.parent.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.FragmentSosAlertsBinding;
import com.kinderguard.app.ui.parent.SosAlertAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SosAlertsFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentSosAlertsBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private SosAlertAdapter adapter;
    private String childUid;

    public static SosAlertsFragment newInstance(String childUid) {
        SosAlertsFragment fragment = new SosAlertsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSosAlertsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new SosAlertAdapter(alert ->
                monitoringRepository.acknowledgeSos(childUid, alert.id));
        binding.rvSosAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSosAlerts.setAdapter(adapter);

        monitoringRepository.listenAllSos(childUid, alerts -> {
            if (binding == null) return;
            List<com.kinderguard.app.model.SosAlert> sorted = new ArrayList<>(alerts);
            Collections.sort(sorted, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            adapter.submitList(sorted);
            boolean empty = sorted.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvSosAlerts.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
