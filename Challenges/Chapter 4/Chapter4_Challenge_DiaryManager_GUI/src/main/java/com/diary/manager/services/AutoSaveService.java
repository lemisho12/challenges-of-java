package com.diary.manager.services;

import com.diary.manager.controllers.EditorController;
import com.diary.manager.models.ThemeManager;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

public class AutoSaveService {

    private EditorController editorController;
    private Timer timer;
    private boolean isRunning;
    private int intervalSeconds;

    public AutoSaveService(EditorController editorController) {
        this.editorController = editorController;
        this.isRunning = false;

        // Get interval from preferences
        ThemeManager themeManager = ThemeManager.getInstance();
        this.intervalSeconds = themeManager.getAutoSaveInterval();
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            timer = new Timer(true); // Daemon timer

            // Schedule auto-save task
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (editorController != null) {
                            editorController.autoSave();
                        }
                    });
                }
            }, intervalSeconds * 1000L, intervalSeconds * 1000L); // Convert seconds to milliseconds

            System.out.println("Auto-save service started with interval: " + intervalSeconds + " seconds");
        }
    }

    public void stop() {
        if (isRunning && timer != null) {
            timer.cancel();
            timer = null;
            isRunning = false;
            System.out.println("Auto-save service stopped");
        }
    }

    public void setInterval(int seconds) {
        if (seconds < 5) {
            seconds = 5; // Minimum 5 seconds
        }

        this.intervalSeconds = seconds;

        // Restart timer with new interval
        if (isRunning) {
            stop();
            start();
        }
    }

    public int getInterval() {
        return intervalSeconds;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setEditorController(EditorController editorController) {
        this.editorController = editorController;
    }
}