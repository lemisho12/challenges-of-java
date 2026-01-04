package com.diary.manager.tasks;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.services.FileIOService;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

public class LoadTask extends Task<ObservableList<DiaryEntry>> {

    private final FileIOService fileIOService;

    public LoadTask() {
        this.fileIOService = new FileIOService();

        updateTitle("Loading Entries");
        updateMessage("Preparing to load diary entries...");
    }

    @Override
    protected ObservableList<DiaryEntry> call() throws Exception {
        try {
            updateMessage("Scanning data directory...");
            updateProgress(0.1, 1.0);

            // Get list of entry files
            java.nio.file.Path dataDir = java.nio.file.Paths.get("data/entries");
            long totalFiles = 0;

            if (java.nio.file.Files.exists(dataDir)) {
                totalFiles = java.nio.file.Files.list(dataDir)
                        .filter(path -> path.toString().endsWith(".txt"))
                        .count();
            }

            updateMessage("Found " + totalFiles + " entries to load");
            updateProgress(0.3, 1.0);

            if (totalFiles == 0) {
                updateProgress(1.0, 1.0);
                updateMessage("No entries found");
                return javafx.collections.FXCollections.observableArrayList();
            }

            // Load entries
            List<DiaryEntry> entries = fileIOService.loadAllEntries();

            updateProgress(0.7, 1.0);
            updateMessage("Processing " + entries.size() + " entries...");

            // Simulate processing
            Thread.sleep(200);

            // Sort by date (newest first)
            entries.sort((e1, e2) -> e2.getCreatedDate().compareTo(e1.getCreatedDate()));

            updateProgress(0.9, 1.0);
            updateMessage("Finalizing load...");

            Thread.sleep(100);

            updateProgress(1.0, 1.0);
            updateMessage("Successfully loaded " + entries.size() + " entries");

            return javafx.collections.FXCollections.observableArrayList(entries);

        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Load cancelled");
            }
            Thread.currentThread().interrupt();
            return javafx.collections.FXCollections.observableArrayList();
        } catch (Exception e) {
            updateMessage("Load failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Load completed successfully");
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage("Failed to load entries: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Load operation cancelled");
    }
}