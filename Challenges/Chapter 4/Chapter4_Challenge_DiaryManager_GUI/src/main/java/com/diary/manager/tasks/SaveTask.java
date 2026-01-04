package com.diary.manager.tasks;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.services.FileIOService;
import javafx.concurrent.Task;

public class SaveTask extends Task<Void> {

    private final DiaryEntry entry;
    private final FileIOService fileIOService;

    public SaveTask(DiaryEntry entry) {
        this.entry = entry;
        this.fileIOService = new FileIOService();

        // Configure task properties
        updateTitle("Saving Entry");
        updateMessage("Preparing to save: " + (entry.getTitle() != null ? entry.getTitle() : "Untitled"));
    }

    @Override
    protected Void call() throws Exception {
        try {
            updateMessage("Saving entry: " + entry.getTitle());
            updateProgress(0.2, 1.0);

            // Simulate some processing time
            Thread.sleep(100);

            // Save the entry
            fileIOService.saveEntrySync(entry);

            updateProgress(0.8, 1.0);
            updateMessage("Entry saved successfully");

            Thread.sleep(100);

            updateProgress(1.0, 1.0);
            updateMessage("Save complete");

        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Save cancelled");
            }
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            updateMessage("Save failed: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Entry saved successfully at " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage("Failed to save entry: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Save operation cancelled");
    }
}