package com.diary.manager.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SearchEngine {

    // Inverted index for fast searching
    private final Map<String, Set<String>> titleIndex;      // word -> entry IDs
    private final Map<String, Set<String>> contentIndex;    // word -> entry IDs
    private final Map<String, Set<String>> tagIndex;        // tag -> entry IDs
    private final Map<String, DiaryEntry> entriesById;      // ID -> entry

    // Additional indexes for metadata
    private final Map<LocalDate, Set<String>> dateIndex;    // date -> entry IDs
    private final Set<String> favoriteIds;                  // favorite entry IDs

    // Search configuration
    private boolean caseSensitive = false;
    private boolean useStemming = false;
    private boolean useSynonyms = false;

    // Relevance scoring weights
    private static final int TITLE_WEIGHT = 10;
    private static final int CONTENT_WEIGHT = 5;
    private static final int TAG_WEIGHT = 8;
    private static final int EXACT_MATCH_BONUS = 20;
    private static final int FAVORITE_BONUS = 3;
    private static final int RECENCY_BONUS = 2;

    public SearchEngine() {
        this.titleIndex = new ConcurrentHashMap<>();
        this.contentIndex = new ConcurrentHashMap<>();
        this.tagIndex = new ConcurrentHashMap<>();
        this.entriesById = new ConcurrentHashMap<>();
        this.dateIndex = new ConcurrentHashMap<>();
        this.favoriteIds = ConcurrentHashMap.newKeySet();
    }

    /**
     * Index a list of entries
     */
    public void indexEntries(List<DiaryEntry> entries) {
        clearIndex();
        entries.forEach(this::addToIndex);
    }

    /**
     * Add a single entry to the index
     */
    public synchronized void addToIndex(DiaryEntry entry) {
        if (entry == null || entry.getId() == null) {
            return;
        }

        String entryId = entry.getId();
        entriesById.put(entryId, entry);

        // Index title
        if (entry.getTitle() != null) {
            indexText(entry.getTitle(), entryId, titleIndex);
        }

        // Index content
        if (entry.getContent() != null) {
            indexText(entry.getContent(), entryId, contentIndex);
        }

        // Index tags
        if (entry.getTags() != null) {
            for (String tag : entry.getTags()) {
                if (tag != null && !tag.trim().isEmpty()) {
                    String normalizedTag = normalizeText(tag);
                    tagIndex.computeIfAbsent(normalizedTag, k -> ConcurrentHashMap.newKeySet())
                            .add(entryId);
                }
            }
        }

        // Index date
        if (entry.getCreatedDate() != null) {
            LocalDate date = entry.getCreatedDate().toLocalDate();
            dateIndex.computeIfAbsent(date, k -> ConcurrentHashMap.newKeySet())
                    .add(entryId);
        }

        // Index favorite status
        if (entry.isFavorite()) {
            favoriteIds.add(entryId);
        }
    }

    /**
     * Update an entry in the index
     */
    public synchronized void updateIndex(DiaryEntry oldEntry, DiaryEntry newEntry) {
        if (oldEntry != null && oldEntry.getId() != null) {
            removeFromIndex(oldEntry);
        }
        addToIndex(newEntry);
    }

    /**
     * Remove an entry from the index
     */
    public synchronized void removeFromIndex(DiaryEntry entry) {
        if (entry == null || entry.getId() == null) {
            return;
        }

        String entryId = entry.getId();
        entriesById.remove(entryId);

        // Remove from all indexes
        removeFromIndexes(entryId, titleIndex);
        removeFromIndexes(entryId, contentIndex);
        removeFromIndexes(entryId, tagIndex);

        // Remove from date index
        if (entry.getCreatedDate() != null) {
            LocalDate date = entry.getCreatedDate().toLocalDate();
            Set<String> dateEntries = dateIndex.get(date);
            if (dateEntries != null) {
                dateEntries.remove(entryId);
                if (dateEntries.isEmpty()) {
                    dateIndex.remove(date);
                }
            }
        }

        // Remove from favorites
        favoriteIds.remove(entryId);
    }

    /**
     * Clear the entire index
     */
    public synchronized void clearIndex() {
        titleIndex.clear();
        contentIndex.clear();
        tagIndex.clear();
        entriesById.clear();
        dateIndex.clear();
        favoriteIds.clear();
    }

    /**
     * Perform a search with advanced filters
     */
    public ObservableList<DiaryEntry> search(String query) {
        return search(query, true, true, true, null, null, false);
    }

    /**
     * Perform an advanced search with filters
     */
    public ObservableList<DiaryEntry> search(String query,
                                             boolean searchTitle,
                                             boolean searchContent,
                                             boolean searchTags,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             boolean favoritesOnly) {

        if ((query == null || query.trim().isEmpty()) &&
                fromDate == null && toDate == null && !favoritesOnly) {
            // Return all entries
            return FXCollections.observableArrayList(entriesById.values());
        }

        // Parse query into search terms
        List<String> searchTerms = parseSearchQuery(query);

        // Get candidate entries based on search terms
        Set<String> candidateIds = getCandidateIds(searchTerms, searchTitle, searchContent, searchTags);

        // Apply filters
        candidateIds = applyFilters(candidateIds, fromDate, toDate, favoritesOnly);

        // Calculate relevance scores and sort
        List<ScoredEntry> scoredEntries = calculateRelevanceScores(candidateIds, searchTerms);

        // Convert to ObservableList
        return FXCollections.observableArrayList(
                scoredEntries.stream()
                        .map(ScoredEntry::getEntry)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Search by date range
     */
    public ObservableList<DiaryEntry> searchByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return FXCollections.observableArrayList(entriesById.values());
        }

        Set<String> candidateIds = new HashSet<>();

        for (Map.Entry<LocalDate, Set<String>> entry : dateIndex.entrySet()) {
            LocalDate date = entry.getKey();

            if ((fromDate == null || !date.isBefore(fromDate)) &&
                    (toDate == null || !date.isAfter(toDate))) {
                candidateIds.addAll(entry.getValue());
            }
        }

        return getEntriesByIds(candidateIds);
    }

    /**
     * Search by tag
     */
    public ObservableList<DiaryEntry> searchByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return FXCollections.observableArrayList();
        }

        String normalizedTag = normalizeText(tag);
        Set<String> entryIds = tagIndex.get(normalizedTag);

        if (entryIds == null || entryIds.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        return getEntriesByIds(entryIds);
    }

    /**
     * Search favorites
     */
    public ObservableList<DiaryEntry> searchFavorites() {
        return getEntriesByIds(favoriteIds);
    }

    /**
     * Get suggestions for auto-complete
     */
    public List<String> getSuggestions(String partialQuery, int maxSuggestions) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedPartial = normalizeText(partialQuery);
        Set<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // Suggest from titles
        for (String word : titleIndex.keySet()) {
            if (word.startsWith(normalizedPartial)) {
                suggestions.add(word);
                if (suggestions.size() >= maxSuggestions) {
                    break;
                }
            }
        }

        // Suggest from tags
        if (suggestions.size() < maxSuggestions) {
            for (String tag : tagIndex.keySet()) {
                if (tag.startsWith(normalizedPartial) && !suggestions.contains(tag)) {
                    suggestions.add(tag);
                    if (suggestions.size() >= maxSuggestions) {
                        break;
                    }
                }
            }
        }

        return new ArrayList<>(suggestions);
    }

    /**
     * Get statistics about the index
     */
    public Map<String, Integer> getIndexStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Total Entries", entriesById.size());
        stats.put("Title Index Size", titleIndex.size());
        stats.put("Content Index Size", contentIndex.size());
        stats.put("Tag Index Size", tagIndex.size());
        stats.put("Date Index Size", dateIndex.size());
        stats.put("Favorites", favoriteIds.size());
        return stats;
    }

    // Private helper methods

    private void indexText(String text, String entryId, Map<String, Set<String>> index) {
        String normalizedText = normalizeText(text);
        String[] words = tokenizeText(normalizedText);

        for (String word : words) {
            if (word.length() > 1) { // Ignore single characters
                index.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet())
                        .add(entryId);
            }
        }
    }

    private void removeFromIndexes(String entryId, Map<String, Set<String>> index) {
        Iterator<Map.Entry<String, Set<String>>> iterator = index.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> entry = iterator.next();
            Set<String> entryIds = entry.getValue();
            entryIds.remove(entryId);

            if (entryIds.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text.trim();

        if (!caseSensitive) {
            normalized = normalized.toLowerCase();
        }

        // Remove punctuation (except apostrophes for contractions)
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}\\s']", " ");

        return normalized;
    }

    private String[] tokenizeText(String text) {
        return text.split("\\s+");
    }

    private List<String> parseSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = normalizeText(query);
        String[] tokens = tokenizeText(normalized);

        // Remove stop words and short words
        return Arrays.stream(tokens)
                .filter(word -> word.length() > 2) // Filter out short words
                .filter(this::isNotStopWord)
                .collect(Collectors.toList());
    }

    private boolean isNotStopWord(String word) {
        // Common stop words in English
        Set<String> stopWords = Set.of(
                "a", "an", "the", "and", "or", "but", "in", "on", "at",
                "to", "for", "of", "with", "by", "is", "am", "are", "was",
                "were", "be", "been", "being", "have", "has", "had", "do",
                "does", "did", "will", "would", "should", "could", "can",
                "may", "might", "must", "shall", "this", "that", "these",
                "those", "i", "you", "he", "she", "it", "we", "they"
        );

        return !stopWords.contains(word.toLowerCase());
    }

    private Set<String> getCandidateIds(List<String> searchTerms,
                                        boolean searchTitle,
                                        boolean searchContent,
                                        boolean searchTags) {

        if (searchTerms.isEmpty()) {
            // No search terms, return all entry IDs
            return new HashSet<>(entriesById.keySet());
        }

        Set<String> candidateIds = new HashSet<>();

        for (String term : searchTerms) {
            Set<String> idsForTerm = new HashSet<>();

            if (searchTitle) {
                Set<String> titleIds = titleIndex.get(term);
                if (titleIds != null) {
                    idsForTerm.addAll(titleIds);
                }
            }

            if (searchContent) {
                Set<String> contentIds = contentIndex.get(term);
                if (contentIds != null) {
                    idsForTerm.addAll(contentIds);
                }
            }

            if (searchTags) {
                Set<String> tagIds = tagIndex.get(term);
                if (tagIds != null) {
                    idsForTerm.addAll(tagIds);
                }
            }

            if (candidateIds.isEmpty()) {
                candidateIds.addAll(idsForTerm);
            } else {
                candidateIds.retainAll(idsForTerm); // Intersection for AND search
            }

            if (candidateIds.isEmpty()) {
                break; // No matches found
            }
        }

        return candidateIds;
    }

    private Set<String> applyFilters(Set<String> entryIds,
                                     LocalDate fromDate,
                                     LocalDate toDate,
                                     boolean favoritesOnly) {

        if (entryIds.isEmpty()) {
            return entryIds;
        }

        Set<String> filteredIds = new HashSet<>(entryIds);

        // Apply date filter
        if (fromDate != null || toDate != null) {
            filteredIds.removeIf(entryId -> {
                DiaryEntry entry = entriesById.get(entryId);
                if (entry == null || entry.getCreatedDate() == null) {
                    return true;
                }

                LocalDate entryDate = entry.getCreatedDate().toLocalDate();

                if (fromDate != null && entryDate.isBefore(fromDate)) {
                    return true;
                }

                if (toDate != null && entryDate.isAfter(toDate)) {
                    return true;
                }

                return false;
            });
        }

        // Apply favorites filter
        if (favoritesOnly) {
            filteredIds.retainAll(favoriteIds);
        }

        return filteredIds;
    }

    private List<ScoredEntry> calculateRelevanceScores(Set<String> entryIds, List<String> searchTerms) {
        List<ScoredEntry> scoredEntries = new ArrayList<>();

        for (String entryId : entryIds) {
            DiaryEntry entry = entriesById.get(entryId);
            if (entry != null) {
                int score = calculateEntryRelevance(entry, searchTerms);
                scoredEntries.add(new ScoredEntry(entry, score));
            }
        }

        // Sort by score (descending)
        scoredEntries.sort((a, b) -> Integer.compare(b.score, a.score));

        return scoredEntries;
    }

    private int calculateEntryRelevance(DiaryEntry entry, List<String> searchTerms) {
        int score = 0;

        if (searchTerms.isEmpty()) {
            // No search terms, use base scoring
            score += entry.isFavorite() ? FAVORITE_BONUS : 0;
            score += isRecent(entry.getCreatedDate()) ? RECENCY_BONUS : 0;
            return score;
        }

        // Calculate relevance based on search terms
        for (String term : searchTerms) {
            // Check title
            if (entry.getTitle() != null) {
                String normalizedTitle = normalizeText(entry.getTitle());
                if (normalizedTitle.contains(term)) {
                    score += TITLE_WEIGHT;

                    // Bonus for exact title match
                    if (normalizedTitle.equals(term)) {
                        score += EXACT_MATCH_BONUS;
                    }

                    // Bonus for title starting with term
                    if (normalizedTitle.startsWith(term)) {
                        score += 5;
                    }
                }
            }

            // Check content
            if (entry.getContent() != null) {
                String normalizedContent = normalizeText(entry.getContent());
                if (normalizedContent.contains(term)) {
                    score += CONTENT_WEIGHT;

                    // Count occurrences in content
                    int occurrences = countOccurrences(normalizedContent, term);
                    score += occurrences;
                }
            }

            // Check tags
            if (entry.getTags() != null) {
                for (String tag : entry.getTags()) {
                    String normalizedTag = normalizeText(tag);
                    if (normalizedTag.contains(term)) {
                        score += TAG_WEIGHT;

                        // Bonus for exact tag match
                        if (normalizedTag.equals(term)) {
                            score += 12;
                        }
                    }
                }
            }
        }

        // Additional scoring factors
        if (entry.isFavorite()) {
            score += FAVORITE_BONUS;
        }

        if (isRecent(entry.getCreatedDate())) {
            score += RECENCY_BONUS;
        }

        return score;
    }

    private int countOccurrences(String text, String term) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }

        return count;
    }

    private boolean isRecent(LocalDateTime date) {
        if (date == null) {
            return false;
        }

        return date.isAfter(LocalDateTime.now().minusDays(7));
    }

    private ObservableList<DiaryEntry> getEntriesByIds(Set<String> entryIds) {
        List<DiaryEntry> entries = entryIds.stream()
                .map(entriesById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(entries);
    }

    // Configuration setters
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setUseStemming(boolean useStemming) {
        this.useStemming = useStemming;
    }

    public void setUseSynonyms(boolean useSynonyms) {
        this.useSynonyms = useSynonyms;
    }

    // Helper class for scoring entries
    private static class ScoredEntry {
        private final DiaryEntry entry;
        private final int score;

        ScoredEntry(DiaryEntry entry, int score) {
            this.entry = entry;
            this.score = score;
        }

        DiaryEntry getEntry() {
            return entry;
        }

        int getScore() {
            return score;
        }
    }
}