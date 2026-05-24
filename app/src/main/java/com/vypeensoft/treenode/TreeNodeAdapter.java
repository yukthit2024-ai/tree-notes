package com.vypeensoft.treenode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vypeensoft.treenode.databinding.ItemTreeNodeBinding;
import java.util.List;

public class TreeNodeAdapter extends RecyclerView.Adapter<TreeNodeAdapter.ViewHolder> {

    public interface OnNodeClickListener {
        void onNodeClick(TreeNode node);
        void onAddChild(TreeNode node);
        void onEdit(TreeNode node);
        void onDelete(TreeNode node);
    }

    private final List<TreeNode> nodes;
    private final OnNodeClickListener listener;

    public TreeNodeAdapter(List<TreeNode> nodes, OnNodeClickListener listener) {
        this.nodes = nodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTreeNodeBinding binding = ItemTreeNodeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreeNode node = nodes.get(position);
        
        holder.binding.textNodeTitle.setText(node.getTitle() == null || node.getTitle().isEmpty() ? "Untitled" : node.getTitle());

        String preview = node.getContent();
        if (preview == null || preview.trim().isEmpty()) {
            holder.binding.textNotePreview.setText("(No notes)");
            holder.binding.textNotePreview.setAlpha(0.5f);
        } else {
            holder.binding.textNotePreview.setText(preview);
            holder.binding.textNotePreview.setAlpha(1.0f);
        }

        if (node.hasChildren()) {
            holder.binding.imgNodeIcon.setImageResource(R.drawable.ic_folder);
            holder.binding.textChildCount.setText(String.valueOf(node.getChildren().size()));
            holder.binding.textChildCount.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imgNodeIcon.setImageResource(R.drawable.ic_note);
            holder.binding.textChildCount.setVisibility(View.GONE);
        }

        // Card tap navigation
        holder.itemView.setOnClickListener(v -> listener.onNodeClick(node));

        // Inline Action button clicks
        holder.binding.btnActionAddChild.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddChild(node);
            }
        });

        holder.binding.btnActionEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(node);
            }
        });

        holder.binding.btnActionDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(node);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTreeNodeBinding binding;

        ViewHolder(ItemTreeNodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
