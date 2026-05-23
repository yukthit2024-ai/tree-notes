package com.vypeensoft.todo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ListDetailFragment extends Fragment {

    private static final String ARG_LIST_INDEX = "list_index";
    private int listIndex;
    private TodoMaster todoMaster;
    private TodoList currentList;
    private TodoItemAdapter adapter;

    public static ListDetailFragment newInstance(int index) {
        ListDetailFragment fragment = new ListDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listIndex = getArguments().getInt(ARG_LIST_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        todoMaster = JsonManager.read(requireContext());
        if (listIndex >= 0 && listIndex < todoMaster.lists.size()) {
            currentList = todoMaster.lists.get(listIndex);
        } else {
            Toast.makeText(requireContext(), "List not found", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        TextView textTitle = view.findViewById(R.id.textTitle);
        textTitle.setText(currentList.name);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TodoItemAdapter(currentList.items, () -> {
            JsonManager.saveList(currentList);
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> showAddItemDialog());
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Item");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter item name");
        input.setSingleLine(true);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                currentList.items.add(new TodoItem(name));
                JsonManager.saveList(currentList);
                adapter.notifyItemInserted(currentList.items.size() - 1);
            }
        });

        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        dialog.show();
        input.requestFocus();
    }
}
