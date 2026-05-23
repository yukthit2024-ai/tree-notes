package com.vypeensoft.treenode;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    public static final String PREFS_NAME = "TreeNotesPrefs";
    public static final String PREF_THEME = "app_theme";
    public static final String THEME_LIGHT = "Light";
    public static final String THEME_DARK = "Dark";
    public static final String THEME_SYSTEM = "System";

    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String theme = prefs.getString(PREF_THEME, THEME_SYSTEM);
        applyTheme(theme);
    }

    public static void applyTheme(String themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}

