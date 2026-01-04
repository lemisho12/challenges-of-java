package com.diary.manager.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiaryEntry {
    private String id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private List<String> tags;
    private boolean favorite;
    private String mood;
    private int relevanceScore;

    public DiaryEntry() {
        this.id = UUID.randomUUID().toString();
        this.title = "Untitled Entry";
        this.content = "";
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.tags = new ArrayList<>();
        this.favorite = false;
        this.mood = "Neutral";
        this.relevanceScore = 0;
    }

    public DiaryEntry(String title, String content) {
        this();
        this.title = title;
        this.content = content;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.modifiedDate = LocalDateTime.now();
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.modifiedDate = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            this.modifiedDate = LocalDateTime.now();
        }
    }
    public void removeTag(String tag) {
        tags.remove(tag);
        this.modifiedDate = LocalDateTime.now();
    }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        this.modifiedDate = LocalDateTime.now();
    }

    public String getMood() { return mood; }
    public void setMood(String mood) {
        this.mood = mood;
        this.modifiedDate = LocalDateTime.now();
    }

    public int getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(int relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getPreview() {
        if (content.length() > 150) {
            return content.substring(0, 150) + "...";
        }
        return content;
    }

    public String getFormattedDate() {
        return createdDate.toString().replace("T", " ");
    }

    @Override
    public String toString() {
        return "DiaryEntry{" +
                "title='" + title + '\'' +
                ", createdDate=" + createdDate +
                ", tags=" + tags +
                '}';
    }
}