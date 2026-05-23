package com.vypeensoft.treenode;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.vypeensoft.treenode.databinding.ItemTreeNodeBinding;
import java.util.List;

public class TreeNodeAdapter extends RecyclerView.Adapter<TreeNodeAdapter.ViewHolder> {

    public interface OnNodeClickListener {
        void onNodeClick(TreeNode node);
        void onAddChild(TreeNode node);
        void onEdit(TreeNode node);
        void onDelete(TreeNode node);
        void onMoveUp(TreeNode node, int position);
        void onMoveDown(TreeNode node, int position);
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
            holder.binding.imgChevron.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imgNodeIcon.setImageResource(R.drawable.ic_note);
            holder.binding.textChildCount.setVisibility(View.GONE);
            holder.binding.imgChevron.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onNodeClick(node));

        holder.binding.btnMoreActions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Add Child");
            popup.getMenu().add("Edit Node");
            popup.getMenu().add("Delete");
            if (position > 0) {
                popup.getMenu().add("Move Up");
            }
            if (position < getItemCount() - 1) {
                popup.getMenu().add("Move Down");
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Add Child")) {
                    listener.onAddChild(node);
                } else if (title.equals("Edit Node")) {
                    listener.onEdit(node);
                } else if (title.equals("Delete")) {
                    listener.onDelete(node);
                } else if (title.equals("Move Up")) {
                    listener.onMoveUp(node, position);
                } else if (title.equals("Move Down")) {
                    listener.onMoveDown(node, position);
                }
                return true;
            });
            popup.show();
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

