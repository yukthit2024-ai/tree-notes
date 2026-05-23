package com.vypeensoft.todo;

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

    public JsonStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String getStorageUriString() {
        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_KEY_STORAGE_URI, "");
    }

    public void setStorageUriString(String uriString) {
        SharedPreferences prefs = context.getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_STORAGE_URI, uriString).apply();
    }

    public String getStoragePathDisplayName() {
        String uriStr = getStorageUriString();
        if (uriStr.isEmpty()) {
            File defaultDir = getDefaultStorageDir();
            return defaultDir.getAbsolutePath();
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
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }
        File defaultDir = new File(baseDir, DEFAULT_FOLDER_NAME);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        return defaultDir;
    }

    public List<TreeDocument> loadAllMasterTrees() {
        List<TreeDocument> list = new ArrayList<>();
        String uriStr = getStorageUriString();

        if (uriStr.isEmpty()) {
            File dir = getDefaultStorageDir();
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

    public TreeDocument loadTreeDocument(String docId) {
        String uriStr = getStorageUriString();
        if (uriStr.isEmpty()) {
            File dir = getDefaultStorageDir();
            File file = new File(dir, docId + ".json");
            if (file.exists()) {
                return loadFromFile(file);
            }
        } else {
            try {
                Uri treeUri = Uri.parse(uriStr);
                DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(docId + ".json");
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
        if (doc == null || doc.getId() == null || doc.getId().isEmpty()) {
            return false;
        }
        String jsonStr = gson.toJson(doc);
        String uriStr = getStorageUriString();

        if (uriStr.isEmpty()) {
            File dir = getDefaultStorageDir();
            File file = new File(dir, doc.getId() + ".json");
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
                    DocumentFile file = dir.findFile(doc.getId() + ".json");
                    if (file == null) {
                        file = dir.createFile("application/json", doc.getId() + ".json");
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

    public boolean renameTreeDocument(String docId, String newName) {
        TreeDocument doc = loadTreeDocument(docId);
        if (doc != null) {
            doc.setName(newName);
            return saveTreeDocument(doc);
        }
        return false;
    }

    public boolean deleteTreeDocument(String docId) {
        String uriStr = getStorageUriString();
        long timestamp = System.currentTimeMillis();
        String deletedFileName = docId + "_" + timestamp + ".deleted.json";

        if (uriStr.isEmpty()) {
            File dir = getDefaultStorageDir();
            File file = new File(dir, docId + ".json");
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
                    DocumentFile file = dir.findFile(docId + ".json");
                    if (file != null && file.exists()) {
                        // Rename inside the same folder to represent soft delete
                        // Since DocumentFile.renameTo might not support path changes easily, we rename it directly in-place
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
        if (!uriStr.isEmpty()) {
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
        File dir = getDefaultStorageDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.contains(".deleted"));
        if (files != null) {
            for (File f : files) {
                list.add(f);
            }
        }
        return list;
    }
}
