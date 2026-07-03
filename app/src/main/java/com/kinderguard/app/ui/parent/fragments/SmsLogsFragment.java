package com.kinderguard.app.ui.parent.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.FragmentSmsLogsBinding;
import com.kinderguard.app.ui.parent.SmsLogAdapter;

public class SmsLogsFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentSmsLogsBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private SmsLogAdapter adapter;
    private String childUid;

    public static SmsLogsFragment newInstance(String childUid) {
        SmsLogsFragment fragment = new SmsLogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSmsLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new SmsLogAdapter();
        binding.rvSmsLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSmsLogs.setAdapter(adapter);

        monitoringRepository.listenSmsLogs(childUid, list -> {
            if (binding == null) return;
            if (list != null) {
                Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            }
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvSmsLogs.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
