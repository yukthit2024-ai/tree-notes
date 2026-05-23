package com.vypeensoft.todo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private MasterListAdapter adapter;
    private TodoMaster todoMaster;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        FloatingActionButton fab = view.findViewById(R.id.fabAddList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        fab.setOnClickListener(v -> showAddListDialog());
        
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        todoMaster = JsonManager.read(requireContext());
        adapter = new MasterListAdapter(todoMaster.lists, new MasterListAdapter.OnListClickListener() {
            @Override
            public void onListClick(TodoList list) {
                int index = todoMaster.lists.indexOf(list);
                openDetailFragment(index);
            }

            @Override
            public void onDeleteClick(TodoList list, int position) {
                JsonManager.softDelete(list);
                todoMaster.lists.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(requireContext(), "List moved to 'deleted' folder", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void openDetailFragment(int index) {
        Fragment fragment = ListDetailFragment.newInstance(index);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Master List");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter list name");
        input.setSingleLine(true);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                TodoList newList = new TodoList(name);
                todoMaster.lists.add(newList);
                JsonManager.saveList(newList);
                
                // Keep the list sorted
                java.util.Collections.sort(todoMaster.lists, (l1, l2) -> l1.name.compareToIgnoreCase(l2.name));
                adapter.notifyDataSetChanged();
                
                Toast.makeText(requireContext(), "Master list created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        dialog.show();
        input.requestFocus();
    }
}
