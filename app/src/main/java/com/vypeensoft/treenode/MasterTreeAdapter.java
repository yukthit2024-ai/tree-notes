package com.vypeensoft.treenode;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vypeensoft.treenode.databinding.ItemMasterListBinding;
import java.util.List;

public class MasterTreeAdapter extends RecyclerView.Adapter<MasterTreeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TreeDocument document);
        void onDeleteClick(TreeDocument document);
        void onRenameClick(TreeDocument document);
    }

    private final List<TreeDocument> documents;
    private final OnItemClickListener listener;

    public MasterTreeAdapter(List<TreeDocument> documents, OnItemClickListener listener) {
        this.documents = documents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMasterListBinding binding = ItemMasterListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreeDocument doc = documents.get(position);
        holder.binding.textListName.setText(doc.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(doc));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onRenameClick(doc);
            return true;
        });
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(doc));
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMasterListBinding binding;

        ViewHolder(ItemMasterListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

