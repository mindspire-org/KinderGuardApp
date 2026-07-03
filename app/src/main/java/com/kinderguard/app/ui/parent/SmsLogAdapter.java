package com.kinderguard.app.ui.parent;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.databinding.ItemSmsLogBinding;
import com.kinderguard.app.model.SmsEntry;

import java.util.ArrayList;
import java.util.List;

public class SmsLogAdapter extends RecyclerView.Adapter<SmsLogAdapter.ViewHolder> {

    private final List<SmsEntry> items = new ArrayList<>();

    public void submitList(List<SmsEntry> newList) {
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
        ItemSmsLogBinding binding = ItemSmsLogBinding.inflate(
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
        private final ItemSmsLogBinding binding;

        ViewHolder(ItemSmsLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SmsEntry entry) {
            String party;
            if ("SENT".equals(entry.type)) {
                party = "To: " + entry.receiver;
            } else {
                party = "From: " + entry.sender;
            }
            binding.tvSmsParty.setText(party);
            binding.tvContent.setText(entry.content);
            binding.tvTimestamp.setText(DateUtils.getRelativeTimeSpanString(entry.timestamp));
        }
    }
}
