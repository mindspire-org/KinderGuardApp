package com.kinderguard.app.ui.parent;

import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.ItemSosAlertBinding;
import com.kinderguard.app.model.SosAlert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SosAlertAdapter extends RecyclerView.Adapter<SosAlertAdapter.ViewHolder> {

    public interface OnAcknowledgeListener {
        void onAcknowledge(SosAlert alert);
    }

    private final List<SosAlert> items = new ArrayList<>();
    private final OnAcknowledgeListener listener;

    public SosAlertAdapter(OnAcknowledgeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SosAlert> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
        ItemSosAlertBinding binding = ItemSosAlertBinding.inflate(
                android.view.LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSosAlertBinding binding;

        ViewHolder(ItemSosAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SosAlert alert, OnAcknowledgeListener listener) {
            android.content.Context ctx = binding.getRoot().getContext();
            binding.tvSosTimestamp.setText(
                    DateFormat.format("MMM d, h:mm a", alert.timestamp));
            binding.tvSosLocation.setText(String.format(Locale.US, "%.4f, %.4f",
                    alert.latitude, alert.longitude));

            if (alert.acknowledged) {
                binding.tvSosStatus.setText(R.string.sos_acknowledged);
                binding.tvSosStatus.setBackgroundResource(R.drawable.bg_chip_success);
                binding.tvSosStatus.setTextColor(ctx.getColor(R.color.kg_success));
                binding.btnAcknowledge.setVisibility(android.view.View.GONE);
            } else {
                binding.tvSosStatus.setText(R.string.sos_unacknowledged);
                binding.tvSosStatus.setBackgroundResource(R.drawable.bg_chip_danger);
                binding.tvSosStatus.setTextColor(ctx.getColor(R.color.kg_danger));
                binding.btnAcknowledge.setVisibility(android.view.View.VISIBLE);
            }

            binding.btnOpenMap.setOnClickListener(v -> {
                Uri uri = Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%f,%f",
                        alert.latitude, alert.longitude, alert.latitude, alert.longitude));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                    ctx.startActivity(intent);
                }
            });

            binding.btnAcknowledge.setOnClickListener(v -> {
                if (listener != null) listener.onAcknowledge(alert);
            });
        }
    }
}
