package com.diary.manager.models;

import com.diary.manager.exceptions.DiaryException;
import com.diary.manager.services.FileIOService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiaryManager {
    private static DiaryManager instance;
    private ObservableList<DiaryEntry> entries;
    private FileIOService fileIOService;
    private SearchEngine searchEngine;

    private DiaryManager() {
        entries = FXCollections.observableArrayList();
        fileIOService = new FileIOService();
        searchEngine = new SearchEngine();
        loadEntries();
    }

    public static synchronized DiaryManager getInstance() {
        if (instance == null) {
            instance = new DiaryManager();
        }
        return instance;
    }

    public void loadEntries() {
        try {
            List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
            entries.setAll(loadedEntries);
            searchEngine.indexEntries(loadedEntries);
        } catch (DiaryException e) {
            System.err.println("Failed to load entries: " + e.getMessage());
        }
    }

    public void saveEntry(DiaryEntry entry) throws DiaryException {
        if (entry == null) {
            throw new DiaryException("Entry cannot be null");
        }

        if (!entries.contains(entry)) {
            entries.add(entry);
            searchEngine.addToIndex(entry);
        }

        fileIOService.saveEntry(entry);
    }

    public void updateEntry(DiaryEntry oldEntry, DiaryEntry newEntry) throws DiaryException {
        int index = entries.indexOf(oldEntry);
        if (index != -1) {
            entries.set(index, newEntry);
            searchEngine.updateIndex(oldEntry, newEntry);
            fileIOService.updateEntry(newEntry);
        }
    }

    public void deleteEntry(DiaryEntry entry) throws DiaryException {
        if (entries.remove(entry)) {
            searchEngine.removeFromIndex(entry);
            fileIOService.deleteEntry(entry);
        }
    }

    public ObservableList<DiaryEntry> getAllEntries() {
        return entries;
    }

    public ObservableList<DiaryEntry> getEntriesByDate(LocalDate date) {
        return entries.stream()
                .filter(entry -> entry.getCreatedDate().toLocalDate().equals(date))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public ObservableList<DiaryEntry> getFavoriteEntries() {
        return entries.stream()
                .filter(DiaryEntry::isFavorite)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public ObservableList<DiaryEntry> getEntriesByTag(String tag) {
        return entries.stream()
                .filter(entry -> entry.getTags().contains(tag))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public ObservableList<DiaryEntry> searchEntries(String query) {
        return searchEngine.search(query);
    }

    public List<String> getAllTags() {
        List<String> allTags = new ArrayList<>();
        for (DiaryEntry entry : entries) {
            allTags.addAll(entry.getTags());
        }
        return allTags.stream().distinct().collect(Collectors.toList());
    }

    public int getTotalEntries() {
        return entries.size();
    }

    public int getEntriesThisMonth() {
        LocalDateTime now = LocalDateTime.now();
        return (int) entries.stream()
                .filter(entry -> entry.getCreatedDate().getMonth() == now.getMonth() &&
                        entry.getCreatedDate().getYear() == now.getYear())
                .count();
    }
}