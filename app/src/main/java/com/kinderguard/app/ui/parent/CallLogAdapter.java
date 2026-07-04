package com.kinderguard.app.ui.parent;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.ItemCallLogBinding;
import com.kinderguard.app.model.CallLogEntry;

import java.util.ArrayList;
import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    private final List<CallLogEntry> items = new ArrayList<>();

    public void submitList(List<CallLogEntry> newList) {
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
        ItemCallLogBinding binding = ItemCallLogBinding.inflate(
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
        private final ItemCallLogBinding binding;

        ViewHolder(ItemCallLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CallLogEntry entry) {
            String name = (entry.contactName == null || entry.contactName.trim().isEmpty())
                    ? entry.phoneNumber : entry.contactName;
            binding.tvContactName.setText(name);

            String type = entry.callType == null ? "" : entry.callType;
            binding.tvCallType.setText(type);
            int color;
            switch (type) {
                case "INCOMING":
                    color = R.color.kg_success;
                    break;
                case "OUTGOING":
                    color = R.color.kg_primary_blue;
                    break;
                case "MISSED":
                    color = R.color.kg_danger;
                    break;
                case "REJECTED":
                    color = R.color.kg_warning;
                    break;
                default:
                    color = R.color.kg_text_gray;
            }
            binding.tvCallType.setTextColor(binding.getRoot().getContext().getColor(color));
            binding.iconBadge.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            binding.getRoot().getContext().getColor(color)));

            long minutes = entry.durationSeconds / 60;
            long seconds = entry.durationSeconds % 60;
            binding.tvDuration.setText(minutes + "m " + seconds + "s");

            binding.tvTimestamp.setText(
                    DateUtils.getRelativeTimeSpanString(entry.timestamp));
        }
    }
}
