package com.vypeensoft.treenode;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vypeensoft.treenode.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private JsonStorageManager storageManager;
    private SharedPreferences prefs;
    private String selectedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageManager = new JsonStorageManager(this);
        prefs = getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Set path display to current storage location
        String currentPath = storageManager.getStorageUriString();
        if (currentPath.isEmpty()) {
            currentPath = "/sdcard/Vypeensoft/Tree_Notes/";
        }
        binding.editStoragePath.setText(currentPath);

        // Load initial theme settings
        selectedTheme = prefs.getString(ThemeUtils.PREF_THEME, ThemeUtils.THEME_SYSTEM);
        setupThemeUI();

        // Bind single "Save Configuration" click listener
        binding.btnSaveConfiguration.setOnClickListener(v -> {
            String typedPath = binding.editStoragePath.getText().toString().trim();
            if (typedPath.isEmpty()) {
                typedPath = "/sdcard/Vypeensoft/Tree_Notes/";
            }
            
            // Save path and theme both to SharedPrefs and the JSON settings file on sdcard
            storageManager.setStorageUriString(typedPath);
            storageManager.setAppThemeString(selectedTheme);
            ThemeUtils.applyTheme(selectedTheme);
            
            Toast.makeText(this, "Configuration saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupThemeUI() {
        switch (selectedTheme) {
            case ThemeUtils.THEME_LIGHT:
                binding.radioLight.setChecked(true);
                break;
            case ThemeUtils.THEME_DARK:
                binding.radioDark.setChecked(true);
                break;
            case ThemeUtils.THEME_SYSTEM:
            default:
                binding.radioSystem.setChecked(true);
                break;
        }

        binding.radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                selectedTheme = ThemeUtils.THEME_LIGHT;
            } else if (checkedId == R.id.radioDark) {
                selectedTheme = ThemeUtils.THEME_DARK;
            } else {
                selectedTheme = ThemeUtils.THEME_SYSTEM;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
