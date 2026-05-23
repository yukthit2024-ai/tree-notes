package com.vypeensoft.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.ViewHolder> {

    private final List<TodoItem> items;
    private final OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemChanged();
    }

    public TodoItemAdapter(List<TodoItem> items, OnItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem item = items.get(position);
        holder.textName.setText(item.name);
        
        // Remove listener before setting state to avoid recursion
        holder.checkCompleted.setOnCheckedChangeListener(null);
        holder.checkCompleted.setChecked(item.isCompleted);
        updateStrikeThrough(holder.textName, item.isCompleted);

        holder.checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.isCompleted = isChecked;
            updateStrikeThrough(holder.textName, isChecked);
            if (listener != null) listener.onItemChanged();
        });

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                items.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                if (listener != null) listener.onItemChanged();
            }
        });
    }

    private void updateStrikeThrough(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(android.graphics.Color.GRAY);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(android.graphics.Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        CheckBox checkCompleted;
        ImageButton btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            checkCompleted = itemView.findViewById(R.id.checkCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
