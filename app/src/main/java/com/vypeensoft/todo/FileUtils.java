package com.vypeensoft.todo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static boolean exportToZip(Context context, JsonStorageManager storageManager, Uri zipUri) {
        String uriStr = storageManager.getStorageUriString();
        try (OutputStream os = context.getContentResolver().openOutputStream(zipUri);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os))) {

            if (uriStr.isEmpty()) {
                // Export from default local directory
                List<File> files = storageManager.listAllTreeFilesLocal();
                byte[] buffer = new byte[4096];
                for (File file : files) {
                    if (file.exists() && file.isFile()) {
                        ZipEntry entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);
                        try (FileInputStream fis = new FileInputStream(file)) {
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                    }
                }
            } else {
                // Export from SAF directory
                List<DocumentFile> files = storageManager.listAllTreeFilesSAF();
                byte[] buffer = new byte[4096];
                for (DocumentFile docFile : files) {
                    if (docFile.exists() && docFile.isFile()) {
                        ZipEntry entry = new ZipEntry(docFile.getName());
                        zos.putNextEntry(entry);
                        try (InputStream is = context.getContentResolver().openInputStream(docFile.getUri())) {
                            if (is != null) {
                                int len;
                                while ((len = is.read(buffer)) > 0) {
                                    zos.write(buffer, 0, len);
                                }
                            }
                        }
                        zos.closeEntry();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error exporting to zip: " + e.getMessage(), e);
        }
        return false;
    }

    public static boolean importFromZip(Context context, JsonStorageManager storageManager, Uri zipUri) {
        String uriStr = storageManager.getStorageUriString();
        byte[] buffer = new byte[4096];

        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Security check for Zip Slip vulnerability
                if (name == null || name.contains("..") || !name.endsWith(".json")) {
                    zis.closeEntry();
                    continue;
                }

                if (uriStr.isEmpty()) {
                    // Import into default local directory
                    File destFile = new File(context.getExternalFilesDir(null) + "/TreeNotes", name);
                    // Ensure directories exist
                    File parent = destFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                } else {
                    // Import into SAF directory
                    Uri treeUri = Uri.parse(uriStr);
                    DocumentFile dir = DocumentFile.fromTreeUri(context, treeUri);
                    if (dir != null) {
                        DocumentFile file = dir.findFile(name);
                        if (file == null) {
                            file = dir.createFile("application/json", name);
                        }
                        if (file != null) {
                            try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri(), "rwt")) {
                                if (os != null) {
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        os.write(buffer, 0, len);
                                    }
                                }
                            }
                        }
                    }
                }
                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error importing from zip: " + e.getMessage(), e);
        }
        return false;
    }
}
