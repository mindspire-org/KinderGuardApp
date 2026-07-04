package com.kinderguard.app.ui.parent;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.ItemAppBinding;
import com.kinderguard.app.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.ViewHolder> {

    public interface OnBlockToggleListener {
        void onToggleBlock(AppInfo appInfo);
    }

    private final List<AppInfo> apps = new ArrayList<>();
    private final OnBlockToggleListener listener;

    public InstalledAppsAdapter(OnBlockToggleListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AppInfo> newList) {
        apps.clear();
        if (newList != null) apps.addAll(newList);
        notifyDataSetChanged();
    }

    public int getCount() {
        return apps.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(apps.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppBinding binding;

        ViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppInfo appInfo, OnBlockToggleListener listener) {
            binding.tvAppLabel.setText(appInfo.appLabel);
            binding.tvPackageName.setText(appInfo.packageName);
            binding.btnToggleBlock.setText(appInfo.blocked ? R.string.unblock : R.string.block);
            binding.iconBadge.setBackgroundResource(appInfo.blocked
                    ? R.drawable.bg_avatar_danger
                    : R.drawable.bg_avatar_gradient);
            binding.btnToggleBlock.setOnClickListener(v -> {
                if (listener != null) listener.onToggleBlock(appInfo);
            });
        }
    }
}
