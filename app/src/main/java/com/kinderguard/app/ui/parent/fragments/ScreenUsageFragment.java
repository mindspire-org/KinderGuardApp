package com.kinderguard.app.ui.parent.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.FragmentScreenUsageBinding;
import com.kinderguard.app.model.UsageInfo;
import com.kinderguard.app.ui.parent.ScreenUsageAdapter;

public class ScreenUsageFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentScreenUsageBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private ScreenUsageAdapter adapter;
    private String childUid;

    public static ScreenUsageFragment newInstance(String childUid) {
        ScreenUsageFragment fragment = new ScreenUsageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentScreenUsageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new ScreenUsageAdapter();
        binding.rvUsage.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsage.setAdapter(adapter);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        monitoringRepository.listenUsageForDate(childUid, date, usageList -> {
            if (binding == null) return;
            if (usageList != null) {
                Collections.sort(usageList, (a, b) -> Long.compare(b.totalTimeMs, a.totalTimeMs));
            }
            adapter.submitList(usageList);
            boolean empty = usageList == null || usageList.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvUsage.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
