package com.diary.manager.services;

import com.diary.manager.models.DiaryEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    public ObservableList<DiaryEntry> search(
            String query,
            ObservableList<DiaryEntry> allEntries,
            boolean searchTitle,
            boolean searchContent,
            boolean searchTags,
            LocalDate fromDate,
            LocalDate toDate,
            boolean favoritesOnly) {

        List<DiaryEntry> results = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        // Split query into individual words
        String[] queryWords = queryLower.split("\\s+");

        for (DiaryEntry entry : allEntries) {
            // Apply date filter
            if (fromDate != null && toDate != null) {
                LocalDate entryDate = entry.getCreatedDate().toLocalDate();
                if (entryDate.isBefore(fromDate) || entryDate.isAfter(toDate)) {
                    continue;
                }
            }

            // Apply favorites filter
            if (favoritesOnly && !entry.isFavorite()) {
                continue;
            }

            // Calculate relevance score
            int relevance = calculateRelevance(entry, queryWords, searchTitle, searchContent, searchTags);

            if (relevance > 0) {
                // Add to results with relevance
                entry.setRelevanceScore(relevance);
                results.add(entry);
            }
        }

        // Sort by relevance (descending)
        results.sort((a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore()));

        return FXCollections.observableArrayList(results);
    }

    private int calculateRelevance(
            DiaryEntry entry,
            String[] queryWords,
            boolean searchTitle,
            boolean searchContent,
            boolean searchTags) {

        int relevance = 0;

        for (String word : queryWords) {
            if (word.length() < 2) continue; // Skip very short words

            // Search in title (highest weight)
            if (searchTitle && entry.getTitle().toLowerCase().contains(word)) {
                relevance += 10;

                // Bonus for exact title match
                if (entry.getTitle().equalsIgnoreCase(word)) {
                    relevance += 20;
                }
            }

            // Search in content
            if (searchContent && entry.getContent().toLowerCase().contains(word)) {
                relevance += 5;

                // Bonus for multiple occurrences
                int count = countOccurrences(entry.getContent().toLowerCase(), word);
                relevance += count;
            }

            // Search in tags (medium weight)
            if (searchTags) {
                for (String tag : entry.getTags()) {
                    if (tag.toLowerCase().contains(word)) {
                        relevance += 8;

                        // Bonus for exact tag match
                        if (tag.equalsIgnoreCase(word)) {
                            relevance += 12;
                        }
                    }
                }
            }
        }

        // Additional relevance factors
        if (entry.isFavorite()) {
            relevance += 3; // Small bonus for favorites
        }

        // Recent entries get small bonus
        if (entry.getCreatedDate().isAfter(LocalDateTime.now().minusDays(7))) {
            relevance += 2;
        }

        return relevance;
    }

    private int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }

        return count;
    }

    public ObservableList<DiaryEntry> searchByDateRange(
            ObservableList<DiaryEntry> allEntries,
            LocalDate fromDate,
            LocalDate toDate) {

        List<DiaryEntry> results = new ArrayList<>();

        for (DiaryEntry entry : allEntries) {
            LocalDate entryDate = entry.getCreatedDate().toLocalDate();

            if ((entryDate.isEqual(fromDate) || entryDate.isAfter(fromDate)) &&
                    (entryDate.isEqual(toDate) || entryDate.isBefore(toDate))) {
                results.add(entry);
            }
        }

        return FXCollections.observableArrayList(results);
    }

    public ObservableList<DiaryEntry> searchByTag(
            ObservableList<DiaryEntry> allEntries,
            String tag) {

        List<DiaryEntry> results = new ArrayList<>();
        String tagLower = tag.toLowerCase();

        for (DiaryEntry entry : allEntries) {
            for (String entryTag : entry.getTags()) {
                if (entryTag.toLowerCase().contains(tagLower)) {
                    results.add(entry);
                    break;
                }
            }
        }

        return FXCollections.observableArrayList(results);
    }

    public ObservableList<String> suggestTags(
            ObservableList<DiaryEntry> allEntries,
            String partialTag) {

        List<String> suggestions = new ArrayList<>();
        String partialLower = partialTag.toLowerCase();

        for (DiaryEntry entry : allEntries) {
            for (String tag : entry.getTags()) {
                if (tag.toLowerCase().startsWith(partialLower) &&
                        !suggestions.contains(tag)) {
                    suggestions.add(tag);
                }
            }
        }

        // Sort alphabetically
        suggestions.sort(String::compareToIgnoreCase);

        // Limit to 10 suggestions
        if (suggestions.size() > 10) {
            suggestions = suggestions.subList(0, 10);
        }

        return FXCollections.observableArrayList(suggestions);
    }

    // Helper method to add relevance score field to DiaryEntry
    // Note: You would need to add this field to your DiaryEntry class
    public void addRelevanceScoreSupport() {
        // This is a placeholder for the relevance score field
        // In practice, you'd add a transient field to DiaryEntry
    }
}