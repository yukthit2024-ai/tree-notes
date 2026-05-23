package com.vypeensoft.todo;

import android.content.Context;
import android.os.Environment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class JsonManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static File getDirectory() {
        String basePath = AppSettings.load().masterListPath;
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static TodoMaster read(Context context) {
        TodoMaster master = new TodoMaster();
        File dir = getDirectory();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    TodoList list = gson.fromJson(reader, TodoList.class);
                    if (list != null) {
                        master.lists.add(list);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Sort lists alphabetically by name
        java.util.Collections.sort(master.lists, (l1, l2) -> l1.name.compareToIgnoreCase(l2.name));
        
        return master;
    }

    public static void saveList(TodoList list) {
        File dir = getDirectory();
        // Sanitize filename: replace non-alphanumeric with underscore
        String fileName = list.name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".json";
        File file = new File(dir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(list, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void softDelete(TodoList list) {
        File dir = getDirectory();
        String fileName = list.name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".json";
        File sourceFile = new File(dir, fileName);

        if (sourceFile.exists()) {
            File deletedDir = new File(dir, "deleted");
            if (!deletedDir.exists()) deletedDir.mkdirs();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd.HHmmss", java.util.Locale.getDefault());
            String timestamp = sdf.format(new java.util.Date());
            String newFileName = list.name.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + timestamp + ".json";
            File destFile = new File(deletedDir, newFileName);

            sourceFile.renameTo(destFile);
        }
    }

    public static void saveAll(TodoMaster master) {
        for (TodoList list : master.lists) {
            saveList(list);
        }
    }
}
