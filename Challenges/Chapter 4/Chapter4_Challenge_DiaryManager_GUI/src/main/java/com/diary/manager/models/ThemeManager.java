package com.diary.manager.models;

import java.io.*;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static ThemeManager instance;
    private Preferences preferences;
    private boolean darkMode;

    private ThemeManager() {
        preferences = Preferences.userNodeForPackage(ThemeManager.class);
        darkMode = preferences.getBoolean("darkMode", false);
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        preferences.putBoolean("darkMode", darkMode);
    }

    public void toggleTheme() {
        darkMode = !darkMode;
        preferences.putBoolean("darkMode", darkMode);
    }

    public String getCurrentThemePath() {
        return darkMode ? "/css/dark-theme.css" : "/css/light-theme.css";
    }

    public void savePreferences() {
        try {
            preferences.flush();
        } catch (Exception e) {
            System.err.println("Failed to save preferences: " + e.getMessage());
        }
    }

    public String getFontFamily() {
        return preferences.get("fontFamily", "Segoe UI");
    }

    public void setFontFamily(String fontFamily) {
        preferences.put("fontFamily", fontFamily);
    }

    public int getFontSize() {
        return preferences.getInt("fontSize", 14);
    }

    public void setFontSize(int fontSize) {
        preferences.putInt("fontSize", fontSize);
    }

    public boolean getAutoSave() {
        return preferences.getBoolean("autoSave", true);
    }

    public void setAutoSave(boolean autoSave) {
        preferences.putBoolean("autoSave", autoSave);
    }

    public int getAutoSaveInterval() {
        return preferences.getInt("autoSaveInterval", 60); // seconds
    }

    public void setAutoSaveInterval(int seconds) {
        preferences.putInt("autoSaveInterval", seconds);
    }
}