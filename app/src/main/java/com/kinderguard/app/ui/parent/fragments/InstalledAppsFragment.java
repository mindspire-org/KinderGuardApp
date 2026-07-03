package com.kinderguard.app.ui.parent.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kinderguard.app.data.MonitoringRepository;
import com.kinderguard.app.databinding.FragmentInstalledAppsBinding;
import com.kinderguard.app.model.AppInfo;
import com.kinderguard.app.model.Command;
import com.kinderguard.app.ui.parent.InstalledAppsAdapter;

public class InstalledAppsFragment extends Fragment {

    private static final String ARG_CHILD_UID = "child_uid";

    private FragmentInstalledAppsBinding binding;
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();
    private InstalledAppsAdapter adapter;
    private String childUid;

    public static InstalledAppsFragment newInstance(String childUid) {
        InstalledAppsFragment fragment = new InstalledAppsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentInstalledAppsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }

        adapter = new InstalledAppsAdapter(appInfo -> {
            boolean newBlocked = !appInfo.blocked;
            monitoringRepository.setAppBlocked(childUid, appInfo.packageName, newBlocked);
            String type = newBlocked ? Command.TYPE_BLOCK_APP : Command.TYPE_UNBLOCK_APP;
            Command command = new Command(UUID.randomUUID().toString(), type,
                    appInfo.packageName, System.currentTimeMillis());
            monitoringRepository.sendCommand(childUid, command);
        });
        binding.rvApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvApps.setAdapter(adapter);

        monitoringRepository.listenApps(childUid, apps -> {
            if (binding == null) return;
            adapter.submitList(apps);
            boolean empty = apps == null || apps.isEmpty();
            binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvApps.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
