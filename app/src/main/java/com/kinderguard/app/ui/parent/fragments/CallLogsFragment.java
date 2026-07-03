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
import com.kinderguard.app.databinding.FragmentCallLogsBinding;
import com.kinderguard.app.ui.parent.CallLogAdapter;

public class CallLogsFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentCallLogsBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private CallLogAdapter adapter;
    private String childUid;

    public static CallLogsFragment newInstance(String childUid) {
        CallLogsFragment fragment = new CallLogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new CallLogAdapter();
        binding.rvCallLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCallLogs.setAdapter(adapter);

        monitoringRepository.listenCallLogs(childUid, list -> {
            if (binding == null) return;
            if (list != null) {
                Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            }
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvCallLogs.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
