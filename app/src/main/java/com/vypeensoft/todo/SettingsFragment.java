package com.vypeensoft.todo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private EditText editPath;
    private AppSettings settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editPath = view.findViewById(R.id.editPath);
        Button btnSave = view.findViewById(R.id.btnSaveSettings);

        settings = AppSettings.load();
        editPath.setText(settings.masterListPath);

        btnSave.setOnClickListener(v -> {
            String newPath = editPath.getText().toString().trim();
            if (!newPath.isEmpty()) {
                if (!newPath.endsWith("/")) {
                    newPath += "/";
                }
                settings.masterListPath = newPath;
                settings.save();
                Toast.makeText(requireContext(), "Settings saved to settings.json", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Path cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
