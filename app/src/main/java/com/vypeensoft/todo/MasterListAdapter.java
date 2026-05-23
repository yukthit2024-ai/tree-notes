package com.vypeensoft.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MasterListAdapter extends RecyclerView.Adapter<MasterListAdapter.ViewHolder> {

    private final List<TodoList> lists;
    private final OnListClickListener listener;

    public interface OnListClickListener {
        void onListClick(TodoList list);
        void onDeleteClick(TodoList list, int position);
    }

    public MasterListAdapter(List<TodoList> lists, OnListClickListener listener) {
        this.lists = lists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_master_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoList list = lists.get(position);
        holder.textName.setText(list.name);
        holder.itemView.setOnClickListener(v -> listener.onListClick(list));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(list, position));
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        android.widget.ImageButton btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textListName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
