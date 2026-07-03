package com.kinderguard.app.ui.parent;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kinderguard.app.R;
import com.kinderguard.app.databinding.ItemChildBinding;
import com.kinderguard.app.model.ChildUser;

import java.util.ArrayList;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {

    public interface OnChildClickListener {
        void onChildClick(ChildUser child);
    }

    private final List<ChildUser> children = new ArrayList<>();
    private final OnChildClickListener listener;

    public ChildAdapter(OnChildClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChildUser> newList) {
        children.clear();
        if (newList != null) children.addAll(newList);
        notifyDataSetChanged();
    }

    public int getChildCount() {
        return children.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChildBinding binding = ItemChildBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChildUser child = children.get(position);
        holder.bind(child, listener);
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemChildBinding binding;

        ViewHolder(ItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChildUser child, OnChildClickListener listener) {
            binding.tvChildName.setText(child.name);
            binding.tvChildEmail.setText(child.email);
            if (child.online) {
                binding.dotStatus.setBackgroundResource(R.drawable.dot_status_green);
                binding.tvStatus.setText(R.string.online);
                binding.tvStatus.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.kg_success));
            } else {
                binding.dotStatus.setBackgroundResource(R.drawable.dot_status_gray);
                binding.tvStatus.setText(R.string.offline);
                binding.tvStatus.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.kg_text_gray));
            }
            binding.rowChild.setOnClickListener(v -> {
                if (listener != null) listener.onChildClick(child);
            });
        }
    }
}
