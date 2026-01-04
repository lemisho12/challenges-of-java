package com.diary.manager.services;

import com.diary.manager.exceptions.DiaryException;
import com.diary.manager.exceptions.FileOperationException;
import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.tasks.SaveTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileIOService {

    private static final String DATA_DIR = "data";
    private static final String ENTRIES_DIR = DATA_DIR + "/entries";
    private static final String METADATA_FILE = DATA_DIR + "/metadata.json";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    public FileIOService() {
        try {
            createDirectories();
        } catch (FileOperationException e) {
            // Or handle it more gracefully
            throw new RuntimeException(e);
        }
    }

    private void createDirectories() throws FileOperationException {
        try {
            Files.createDirectories(Paths.get(ENTRIES_DIR));
        } catch (IOException e) {
            throw new FileOperationException("Failed to create data directories", Paths.get(ENTRIES_DIR), FileOperationException.FileOperation.CREATE, e);
        }
    }

    public void saveEntry(DiaryEntry entry) {
        SaveTask saveTask = new SaveTask(entry);
        new Thread(saveTask).start();

        // Also save metadata
        try {
            saveMetadata();
        } catch (DiaryException e) {
            // Log this error, as it's happening in a background-like thread
            System.err.println("Error saving metadata after async save: " + e.getMessage());
        }
    }

    public void saveEntrySync(DiaryEntry entry) throws DiaryException {
        try {
            String fileName = generateFileName(entry);
            Path filePath = Paths.get(ENTRIES_DIR, fileName);

            // Save content
            Files.writeString(filePath, entry.getContent(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Update entry's filename reference
            entry.setId(fileName.replace(".txt", ""));

        } catch (IOException e) {
            throw new FileOperationException("Failed to save entry: " + entry.getTitle(), Paths.get(ENTRIES_DIR, entry.getTitle()), FileOperationException.FileOperation.WRITE, e);
        }
    }

    private String generateFileName(DiaryEntry entry) {
        String baseName = entry.getCreatedDate().format(FILE_NAME_FORMATTER);
        String titlePart = entry.getTitle()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        if (titlePart.length() > 20) {
            titlePart = titlePart.substring(0, 20);
        }

        return baseName + "_" + titlePart + ".txt";
    }

    public List<DiaryEntry> loadAllEntries() throws DiaryException {
        List<DiaryEntry> entries;

        try {
            // Load from metadata if exists
            if (Files.exists(Paths.get(METADATA_FILE))) {
                entries = loadFromMetadata();
            } else {
                // Fallback to scanning files
                entries = scanEntriesDirectory();
                saveMetadata(entries);
            }

            return entries;

        } catch (IOException e) {
            throw new FileOperationException("Failed to load entries", Paths.get(ENTRIES_DIR), FileOperationException.FileOperation.READ, e);
        }
    }

    private List<DiaryEntry> loadFromMetadata() throws IOException {
        String json = Files.readString(Paths.get(METADATA_FILE));
        Type listType = new TypeToken<List<DiaryEntry>>(){}.getType();
        List<DiaryEntry> entries = gson.fromJson(json, listType);

        // Load content for each entry
        if (entries != null) {
            for (DiaryEntry entry : entries) {
                loadEntryContent(entry);
            }
        } else {
            entries = new ArrayList<>();
        }

        return entries;
    }

    private List<DiaryEntry> scanEntriesDirectory() throws IOException {
        List<DiaryEntry> entries = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(ENTRIES_DIR), "*.txt")) {
            for (Path file : stream) {
                try {
                    DiaryEntry entry = loadEntryFromFile(file);
                    entries.add(entry);
                } catch (IOException e) {
                    System.err.println("Failed to load file: " + file.getFileName());
                }
            }
        }

        return entries;
    }

    private DiaryEntry loadEntryFromFile(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        String content = Files.readString(file);

        DiaryEntry entry = new DiaryEntry();
        entry.setId(fileName.replace(".txt", ""));
        entry.setContent(content);

        // Parse title from first line or filename
        String firstLine = content.lines().findFirst().orElse("Untitled");
        entry.setTitle(firstLine.length() > 50 ? firstLine.substring(0, 50) + "..." : firstLine);

        // Try to parse date from filename
        try {
            String datePart = fileName.substring(0, 19); // yyyy-MM-dd_HHmmss
            LocalDateTime date = LocalDateTime.parse(datePart, FILE_NAME_FORMATTER);
            entry.setCreatedDate(date);
            entry.setModifiedDate(date);
        } catch (Exception e) {
            // Use current date if parsing fails
            entry.setCreatedDate(LocalDateTime.now());
            entry.setModifiedDate(LocalDateTime.now());
        }

        return entry;
    }

    private void loadEntryContent(DiaryEntry entry) throws IOException {
        if (entry.getId() == null) return;
        String fileName = entry.getId() + ".txt";
        Path filePath = Paths.get(ENTRIES_DIR, fileName);

        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            entry.setContent(content);
        }
    }

    private void saveMetadata() throws DiaryException {
        try {
            List<DiaryEntry> entries = DiaryManager.getInstance().getAllEntries();
            saveMetadata(entries);
        } catch (IOException e) {
            throw new FileOperationException("Failed to save metadata", Paths.get(METADATA_FILE), FileOperationException.FileOperation.WRITE, e);
        }
    }

    private void saveMetadata(List<DiaryEntry> entries) throws IOException {
        String json = gson.toJson(entries);
        Files.writeString(Paths.get(METADATA_FILE), json,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void updateEntry(DiaryEntry entry) throws DiaryException {
        saveEntrySync(entry); // Use sync version for direct update
        saveMetadata();
    }

    public void deleteEntry(DiaryEntry entry) throws DiaryException {
        try {
            if (entry.getId() == null) return;
            String fileName = entry.getId() + ".txt";
            Path filePath = Paths.get(ENTRIES_DIR, fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            saveMetadata();

        } catch (IOException e) {
            throw new FileOperationException("Failed to delete entry: " + entry.getTitle(), Paths.get(ENTRIES_DIR, entry.getId()), FileOperationException.FileOperation.DELETE, e);
        }
    }

    public Service<Void> createExportService(List<DiaryEntry> entries, Path exportPath) {
        return new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        updateMessage("Exporting entries...");

                        int total = entries.size();
                        for (int i = 0; i < total; i++) {
                            DiaryEntry entry = entries.get(i);
                            String content = "Title: " + entry.getTitle() + "\n"
                                    + "Date: " + entry.getFormattedDate() + "\n"
                                    + "Tags: " + String.join(", ", entry.getTags()) + "\n"
                                    + "Content:\n" + entry.getContent() + "\n"
                                    + "---\n\n";

                            Files.writeString(exportPath, content,
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                            updateProgress(i + 1, total);
                        }

                        updateMessage("Export completed!");
                        return null;
                    }
                };
            }
        };
    }

    public long getTotalStorageUsed() throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(DATA_DIR))) {
            return walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        }
    }
}