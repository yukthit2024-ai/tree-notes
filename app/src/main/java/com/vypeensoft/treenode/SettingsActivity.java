package com.vypeensoft.treenode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.vypeensoft.treenode.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private JsonStorageManager storageManager;
    private SharedPreferences prefs;

    // Activity launchers for SAF intents
    private final ActivityResultLauncher<Uri> chooseDirectoryLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    try {
                        // Persist permissions
                        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        
                        storageManager.setStorageUriString(uri.toString());
                        updatePathUI();
                        Toast.makeText(this, "Storage folder updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to set storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> importZipLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    boolean success = FileUtils.importFromZip(this, storageManager, uri);
                    if (success) {
                        Toast.makeText(this, "Import completed successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Import failed. Please check zip file.", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> exportZipLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/zip"),
            uri -> {
                if (uri != null) {
                    boolean success = FileUtils.exportToZip(this, storageManager, uri);
                    if (success) {
                        Toast.makeText(this, "Export completed successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Export failed.", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

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

        // Setup UI values
        updatePathUI();
        setupThemeUI();

        // Bind clicks
        binding.btnChooseDirectory.setOnClickListener(v -> {
            chooseDirectoryLauncher.launch(null);
        });

        binding.btnResetStorage.setOnClickListener(v -> {
            storageManager.setStorageUriString("");
            updatePathUI();
            Toast.makeText(this, "Storage reset to default application files", Toast.LENGTH_SHORT).show();
        });

        binding.btnExportZip.setOnClickListener(v -> {
            exportZipLauncher.launch("treenotes_backup.zip");
        });

        binding.btnImportZip.setOnClickListener(v -> {
            importZipLauncher.launch(new String[]{"application/zip"});
        });
    }

    private void updatePathUI() {
        binding.textCurrentPath.setText("Current: " + storageManager.getStoragePathDisplayName());
    }

    private void setupThemeUI() {
        String currentTheme = prefs.getString(ThemeUtils.PREF_THEME, ThemeUtils.THEME_SYSTEM);
        switch (currentTheme) {
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
            String selectedTheme = ThemeUtils.THEME_SYSTEM;
            if (checkedId == R.id.radioLight) {
                selectedTheme = ThemeUtils.THEME_LIGHT;
            } else if (checkedId == R.id.radioDark) {
                selectedTheme = ThemeUtils.THEME_DARK;
            }

            prefs.edit().putString(ThemeUtils.PREF_THEME, selectedTheme).apply();
            ThemeUtils.applyTheme(selectedTheme);
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

