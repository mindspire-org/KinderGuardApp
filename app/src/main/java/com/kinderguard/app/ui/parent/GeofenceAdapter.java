package com.kinderguard.app.ui.parent;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.ItemGeofenceBinding;
import com.kinderguard.app.model.GeofenceZone;

import java.util.ArrayList;
import java.util.List;

public class GeofenceAdapter extends RecyclerView.Adapter<GeofenceAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(GeofenceZone zone);
    }

    private final List<GeofenceZone> items = new ArrayList<>();
    private final OnDeleteListener listener;

    public GeofenceAdapter(OnDeleteListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GeofenceZone> newList) {
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
        ItemGeofenceBinding binding = ItemGeofenceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemGeofenceBinding binding;

        ViewHolder(ItemGeofenceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GeofenceZone zone, OnDeleteListener listener) {
            binding.tvZoneName.setText(zone.name);
            binding.tvZoneRadius.setText(((int) zone.radiusMeters) + " m radius");
            binding.dotInside.setBackgroundResource(
                    zone.childInside ? R.drawable.dot_status_green : R.drawable.dot_status_gray);
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(zone);
            });
        }
    }
}
