package com.vypeensoft.treenode;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageManager {
    private static final String TAG = "JsonStorageManager";
    private static final String PREF_KEY_STORAGE_URI = "storage_directory_uri";
    private static final String DEFAULT_FOLDER_NAME = "TreeNotes";
    
    private final Context context;
    private final Gson gson;

    public static class SettingsModel {
        public String storage_directory_uri = "";
        public String app_theme = "system";
    }

    public JsonStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        // Load initial settings from the public settings file
        loadSettingsFromFile();
    }

    public String getStorageUriString() {
        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_KEY_STORAGE_URI, "");
    }

    public void setStorageUriString(String uriString) {
        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_STORAGE_URI, uriString).apply();
        String currentTheme = prefs.getString(ThemeUtils.PREF_THEME, "system");
        saveSettingsToFile(uriString, currentTheme);
    }

    public void setAppThemeString(String themeString) {
        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(ThemeUtils.PREF_THEME, themeString).apply();
        String currentUri = prefs.getString(PREF_KEY_STORAGE_URI, "");
        saveSettingsToFile(currentUri, themeString);
    }

    public String getStoragePathDisplayName() {
        String uriStr = getStorageUriString();
        if (uriStr.isEmpty() || !uriStr.startsWith("content://")) {
            return getResolvedStorageDir().getAbsolutePath();
        } else {
            try {
                Uri uri = Uri.parse(uriStr);
                return uri.getPath();
            } catch (Exception e) {
                return uriStr;
            }
        }
    }

    private File getDefaultStorageDir() {
        File defaultDir = new File("/sdcard/Vypeensoft/Tree_Notes");
        try {
            if (!defaultDir.exists()) {
                defaultDir.mkdirs();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating default sdcard dir: " + e.getMessage());
        }
        
        // Fallback to sandboxed directory if not writable or creation failed
        if (!defaultDir.exists() || !defaultDir.canWrite()) {
            File baseDir = context.getExternalFilesDir(null);
            if (baseDir == null) {
                baseDir = context.getFilesDir();
            }
            defaultDir = new File(baseDir, DEFAULT_FOLDER_NAME);
            if (!defaultDir.exists()) {
                defaultDir.mkdirs();
            }
        }
        return defaultDir;
    }

    public File getResolvedStorageDir() {
        String uriStr = getStorageUriString();
        if (uriStr.isEmpty() || uriStr.startsWith("content://")) {
            return getDefaultStorageDir();
        }
        File customDir = new File(uriStr);
        try {
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating custom storage dir: " + e.getMessage());
        }
        if (customDir.exists() && customDir.canWrite()) {
            return customDir;
        }
        return getDefaultStorageDir();
    }

    public void saveSettingsToFile(String uriString, String themeString) {
        try {
            File settingsFile = new File("/sdcard/Vypeensoft/Tree_Notes/settings/settings.json");
            File parentDir = settingsFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            SettingsModel model = new SettingsModel();
            model.storage_directory_uri = uriString;
            model.app_theme = themeString;
            String jsonStr = gson.toJson(model);
            try (FileOutputStream fos = new FileOutputStream(settingsFile);
                 OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, "UTF-8")) {
                osw.write(jsonStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving settings JSON to sdcard: " + e.getMessage(), e);
        }
    }

    public void loadSettingsFromFile() {
        try {
            File settingsFile = new File("/sdcard/Vypeensoft/Tree_Notes/settings/settings.json");
            if (settingsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(settingsFile);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    SettingsModel model = gson.fromJson(sb.toString(), SettingsModel.class);
                    if (model != null) {
                        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        if (model.storage_directory_uri != null) {
                            editor.putString(PREF_KEY_STORAGE_URI, model.storage_directory_uri);
                        }
                        if (model.app_theme != null) {
                            editor.putString(ThemeUtils.PREF_THEME, model.app_theme);
                        }
                        editor.apply();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading settings JSON from sdcard: " + e.getMessage(), e);
        }
    }

    public List<TreeDocument> loadAllMasterTrees() {
        List<TreeDocument> list = new ArrayList<>();
        String uriStr = getStorageUriString();

        if (uriStr.isEmpty() || !uriStr.startsWith("content://")) {
            File dir = getResolvedStorageDir();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.contains(".deleted"));
            if (files != null) {
                for (File file : files) {
                    TreeDocument doc = loadFromFile(file);
                    if (doc != null) {
                        list.add(doc);
                    }
                }
            }
        } else {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    DocumentFile[] files = dir.listFiles();
                    for (DocumentFile file : files) {
                        String name = file.getName();
                        if (name != null && name.endsWith(".json") && !name.contains(".deleted")) {
                            TreeDocument doc = loadFromDocumentFile(file);
                            if (doc != null) {
                                list.add(doc);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading from SAF directory: " + e.getMessage(), e);
            }
        }
        return list;
    }

    public TreeDocument loadTreeDocument(String docName) {
        String uriStr = getStorageUriString();
        if (uriStr.isEmpty() || !uriStr.startsWith("content://")) {
            File dir = getResolvedStorageDir();
            File file = new File(dir, docName + ".json");
            if (file.exists()) {
                return loadFromFile(file);
            }
        } else {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(docName + ".json");
                    if (file != null && file.exists()) {
                        return loadFromDocumentFile(file);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading document via SAF: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public boolean saveTreeDocument(TreeDocument doc) {
        if (doc == null || doc.getName() == null || doc.getName().isEmpty()) {
            return false;
        }
        String jsonStr = gson.toJson(doc);
        String uriStr = getStorageUriString();

        if (uriStr.isEmpty() || !uriStr.startsWith("content://")) {
            File dir = getResolvedStorageDir();
            File file = new File(dir, doc.getName() + ".json");
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, "UTF-8")) {
                osw.write(jsonStr);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error saving file locally: " + e.getMessage(), e);
            }
        } else {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(doc.getName() + ".json");
                    if (file == null) {
                        file = dir.createFile("application/json", doc.getName() + ".json");
                    }
                    if (file != null) {
                        try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri(), "rwt")) {
                            if (os != null) {
                                os.write(jsonStr.getBytes("UTF-8"));
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving document via SAF: " + e.getMessage(), e);
            }
        }
        return false;
    }

    public boolean renameTreeDocument(String oldName, String newName) {
        TreeDocument doc = loadTreeDocument(oldName);
        if (doc != null) {
            if (getStorageUriString().isEmpty() || !getStorageUriString().startsWith("content://")) {
                File dir = getResolvedStorageDir();
                File oldFile = new File(dir, oldName + ".json");
                File newFile = new File(dir, newName + ".json");
                if (oldFile.exists()) {
                    oldFile.renameTo(newFile);
                }
            } else {
                try {
                    Uri treeUri = Uri.parse(getStorageUriString());
                    DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                    if (dir != null) {
                        DocumentFile file = dir.findFile(oldName + ".json");
                        if (file != null) {
                            file.renameTo(newName + ".json");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error renaming SAF file: " + e.getMessage());
                }
            }
            doc.setName(newName);
            return saveTreeDocument(doc);
        }
        return false;
    }

    public boolean deleteTreeDocument(String docName) {
        String uriStr = getStorageUriString();
        long timestamp = System.currentTimeMillis();
        String deletedFileName = docName + "_" + timestamp + ".deleted.json";

        if (uriStr.isEmpty() || !uriStr.startsWith("content://")) {
            File dir = getResolvedStorageDir();
            File file = new File(dir, docName + ".json");
            if (file.exists()) {
                // Soft delete: Move to a "deleted" subfolder with timestamp
                File deletedDir = new File(dir, "deleted");
                if (!deletedDir.exists()) {
                    deletedDir.mkdirs();
                }
                File dest = new File(deletedDir, deletedFileName);
                return file.renameTo(dest);
            }
        } else {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(docName + ".json");
                    if (file != null && file.exists()) {
                        return file.renameTo(deletedFileName);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error soft-deleting via SAF: " + e.getMessage(), e);
            }
        }
        return false;
    }

    private TreeDocument loadFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return gson.fromJson(sb.toString(), TreeDocument.class);
        } catch (Exception e) {
            Log.e(TAG, "Error loading document from local file: " + e.getMessage(), e);
        }
        return null;
    }

    private TreeDocument loadFromDocumentFile(DocumentFile docFile) {
        try (InputStream is = context.getContentResolver().openInputStream(docFile.getUri());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return gson.fromJson(sb.toString(), TreeDocument.class);
        } catch (Exception e) {
            Log.e(TAG, "Error loading document via SAF stream: " + e.getMessage(), e);
        }
        return null;
    }

    public List<DocumentFile> listAllTreeFilesSAF() {
        List<DocumentFile> list = new ArrayList<>();
        String uriStr = getStorageUriString();
        if (!uriStr.isEmpty() && uriStr.startsWith("content://")) {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    DocumentFile[] files = dir.listFiles();
                    for (DocumentFile file : files) {
                        String name = file.getName();
                        if (name != null && name.endsWith(".json") && !name.contains(".deleted")) {
                            list.add(file);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error listing SAF files: " + e.getMessage(), e);
            }
        }
        return list;
    }

    public List<File> listAllTreeFilesLocal() {
        List<File> list = new ArrayList<>();
        File dir = getResolvedStorageDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.contains(".deleted"));
        if (files != null) {
            for (File f : files) {
                list.add(f);
            }
        }
        return list;
    }
}
