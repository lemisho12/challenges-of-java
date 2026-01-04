package com.diary.manager.tasks;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.services.SearchService;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchTask extends Task<ObservableList<DiaryEntry>> {

    private final String query;
    private final ObservableList<DiaryEntry> allEntries;
    private final boolean searchTitle;
    private final boolean searchContent;
    private final boolean searchTags;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final boolean favoritesOnly;
    private final SearchService searchService;

    public SearchTask(String query,
                      ObservableList<DiaryEntry> allEntries,
                      boolean searchTitle,
                      boolean searchContent,
                      boolean searchTags,
                      LocalDate fromDate,
                      LocalDate toDate,
                      boolean favoritesOnly) {

        this.query = query;
        this.allEntries = allEntries;
        this.searchTitle = searchTitle;
        this.searchContent = searchContent;
        this.searchTags = searchTags;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.favoritesOnly = favoritesOnly;
        this.searchService = new SearchService();

        updateTitle("Searching Entries");
        updateMessage("Preparing search for: " + (query.isEmpty() ? "all entries" : query));
    }

    @Override
    protected ObservableList<DiaryEntry> call() throws Exception {
        try {
            // Update status
            updateMessage("Starting search...");
            updateProgress(0.1, 1.0);

            // Check if search is empty (show all entries)
            if (query == null || query.trim().isEmpty()) {
                updateMessage("No search query, showing all entries...");
                updateProgress(0.3, 1.0);

                // Apply filters only
                ObservableList<DiaryEntry> filtered = allEntries.filtered(entry -> {
                    // Apply date filter
                    if (fromDate != null && toDate != null) {
                        LocalDate entryDate = entry.getCreatedDate().toLocalDate();
                        if (entryDate.isBefore(fromDate) || entryDate.isAfter(toDate)) {
                            return false;
                        }
                    }

                    // Apply favorites filter
                    if (favoritesOnly && !entry.isFavorite()) {
                        return false;
                    }

                    return true;
                });

                updateProgress(0.8, 1.0);
                updateMessage("Found " + filtered.size() + " entries");

                Thread.sleep(100);
                updateProgress(1.0, 1.0);

                return filtered;
            }

            // Perform search with query
            updateMessage("Searching for: '" + query + "'");
            updateProgress(0.2, 1.0);

            // Count total entries for progress tracking
            int totalEntries = allEntries.size();
            AtomicInteger processed = new AtomicInteger(0);

            // Perform search with progress updates
            ObservableList<DiaryEntry> results = searchService.search(
                    query,
                    allEntries,
                    searchTitle,
                    searchContent,
                    searchTags,
                    fromDate,
                    toDate,
                    favoritesOnly
            );

            // Simulate processing for progress feedback
            for (int i = 0; i < results.size(); i++) {
                if (isCancelled()) {
                    break;
                }

                processed.incrementAndGet();
                double progress = 0.2 + (0.6 * processed.get() / Math.max(1, results.size()));
                updateProgress(progress, 1.0);

                // Update message periodically
                if (i % 10 == 0) {
                    updateMessage("Processing results: " + processed.get() + "/" + results.size());
                }

                // Small delay to show progress
                Thread.sleep(10);
            }

            updateProgress(0.9, 1.0);
            updateMessage("Sorting results...");

            // Sort by relevance (already done in SearchService)
            Thread.sleep(100);

            updateProgress(1.0, 1.0);
            updateMessage("Search completed. Found " + results.size() + " results");

            return results;

        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Search cancelled");
            }
            Thread.currentThread().interrupt();
            return javafx.collections.FXCollections.observableArrayList();
        } catch (Exception e) {
            updateMessage("Search error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        ObservableList<DiaryEntry> results = getValue();
        updateMessage("Search completed successfully. Found " + results.size() + " entries");
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage("Search failed: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Search operation cancelled");
    }

    // Factory method for simple searches
    public static SearchTask createSimpleSearch(String query, ObservableList<DiaryEntry> allEntries) {
        return new SearchTask(
                query,
                allEntries,
                true,  // searchTitle
                true,  // searchContent
                true,  // searchTags
                null,  // fromDate
                null,  // toDate
                false  // favoritesOnly
        );
    }

    // Factory method for advanced searches
    public static SearchTask createAdvancedSearch(String query,
                                                  ObservableList<DiaryEntry> allEntries,
                                                  boolean searchTitle,
                                                  boolean searchContent,
                                                  boolean searchTags,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  boolean favoritesOnly) {
        return new SearchTask(
                query,
                allEntries,
                searchTitle,
                searchContent,
                searchTags,
                fromDate,
                toDate,
                favoritesOnly
        );
    }

    // Getter for query (useful for logging)
    public String getQuery() {
        return query;
    }

    // Get search parameters as string (for debugging)
    public String getSearchParameters() {
        return String.format(
                "Query: '%s', Title: %b, Content: %b, Tags: %b, From: %s, To: %s, Favorites: %b",
                query,
                searchTitle,
                searchContent,
                searchTags,
                fromDate != null ? fromDate.toString() : "null",
                toDate != null ? toDate.toString() : "null",
                favoritesOnly
        );
    }
}