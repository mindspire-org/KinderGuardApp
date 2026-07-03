package com.kinderguard.app.ui.parent;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.databinding.ItemUsageBinding;
import com.kinderguard.app.model.UsageInfo;

import java.util.ArrayList;
import java.util.List;

public class ScreenUsageAdapter extends RecyclerView.Adapter<ScreenUsageAdapter.ViewHolder> {

    private final List<UsageInfo> items = new ArrayList<>();

    public void submitList(List<UsageInfo> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    public int getCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUsageBinding binding = ItemUsageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUsageBinding binding;

        ViewHolder(ItemUsageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UsageInfo usageInfo) {
            binding.tvAppLabel.setText(usageInfo.appLabel);
            binding.tvDuration.setText(formatDuration(usageInfo.totalTimeMs));
        }

        private String formatDuration(long ms) {
            long totalMinutes = ms / 60000;
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            if (hours > 0) {
                return hours + "h " + minutes + "m";
            }
            return minutes + "m";
        }
    }
}
