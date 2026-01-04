package com.diary.manager.utils;

import com.diary.manager.models.DiaryEntry;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Validation patterns
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\p{P}\\p{Zs}]{1,100}$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s-]{0,49}$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[\\w\\-. ()]{1,255}$");

    // Maximum limits
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_TAG_LENGTH = 50;
    private static final int MAX_CONTENT_LENGTH = 100000; // 100KB
    private static final int MAX_TAGS_PER_ENTRY = 20;

    public static ValidationResult validateEntry(DiaryEntry entry) {
        ValidationResult result = new ValidationResult();

        if (entry == null) {
            result.addError("Entry cannot be null");
            return result;
        }

        // Validate title
        if (entry.getTitle() == null || entry.getTitle().trim().isEmpty()) {
            result.addError("Title cannot be empty");
        } else if (entry.getTitle().length() > MAX_TITLE_LENGTH) {
            result.addError("Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        } else if (!TITLE_PATTERN.matcher(entry.getTitle()).matches()) {
            result.addError("Title contains invalid characters");
        }

        // Validate content
        if (entry.getContent() == null) {
            result.addError("Content cannot be null");
        } else if (entry.getContent().length() > MAX_CONTENT_LENGTH) {
            result.addError("Content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }

        // Validate dates
        if (entry.getCreatedDate() == null) {
            result.addError("Created date cannot be null");
        } else if (entry.getCreatedDate().isAfter(LocalDateTime.now().plusMinutes(5))) {
            result.addWarning("Created date is in the future");
        }

        if (entry.getModifiedDate() == null) {
            result.addError("Modified date cannot be null");
        } else if (entry.getModifiedDate().isAfter(LocalDateTime.now().plusMinutes(5))) {
            result.addWarning("Modified date is in the future");
        }

        if (entry.getCreatedDate() != null && entry.getModifiedDate() != null) {
            if (entry.getModifiedDate().isBefore(entry.getCreatedDate())) {
                result.addWarning("Modified date is before created date");
            }
        }

        // Validate tags
        if (entry.getTags() != null) {
            if (entry.getTags().size() > MAX_TAGS_PER_ENTRY) {
                result.addError("Cannot have more than " + MAX_TAGS_PER_ENTRY + " tags");
            }

            for (String tag : entry.getTags()) {
                if (tag == null || tag.trim().isEmpty()) {
                    result.addError("Tag cannot be empty");
                } else if (tag.length() > MAX_TAG_LENGTH) {
                    result.addError("Tag '" + tag + "' exceeds " + MAX_TAG_LENGTH + " characters");
                } else if (!TAG_PATTERN.matcher(tag).matches()) {
                    result.addError("Tag '" + tag + "' contains invalid characters");
                }
            }

            // Check for duplicate tags (case-insensitive)
            long uniqueTags = entry.getTags().stream()
                    .map(String::toLowerCase)
                    .distinct()
                    .count();
            if (uniqueTags < entry.getTags().size()) {
                result.addWarning("Duplicate tags detected");
            }
        }

        // Validate mood
        if (entry.getMood() != null && entry.getMood().length() > 50) {
            result.addWarning("Mood description is too long");
        }

        return result;
    }

    public static ValidationResult validateTitle(String title) {
        ValidationResult result = new ValidationResult();

        if (title == null || title.trim().isEmpty()) {
            result.addError("Title cannot be empty");
        } else if (title.length() > MAX_TITLE_LENGTH) {
            result.addError("Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        } else if (!TITLE_PATTERN.matcher(title).matches()) {
            result.addError("Title contains invalid characters");
        }

        return result;
    }

    public static ValidationResult validateContent(String content) {
        ValidationResult result = new ValidationResult();

        if (content == null) {
            result.addError("Content cannot be null");
        } else if (content.length() > MAX_CONTENT_LENGTH) {
            result.addError("Content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }

        return result;
    }

    public static ValidationResult validateTag(String tag) {
        ValidationResult result = new ValidationResult();

        if (tag == null || tag.trim().isEmpty()) {
            result.addError("Tag cannot be empty");
        } else if (tag.length() > MAX_TAG_LENGTH) {
            result.addError("Tag cannot exceed " + MAX_TAG_LENGTH + " characters");
        } else if (!TAG_PATTERN.matcher(tag).matches()) {
            result.addError("Tag contains invalid characters");
        }

        return result;
    }

    public static ValidationResult validateFileName(String fileName) {
        ValidationResult result = new ValidationResult();

        if (fileName == null || fileName.trim().isEmpty()) {
            result.addError("File name cannot be empty");
        } else if (fileName.length() > 255) {
            result.addError("File name cannot exceed 255 characters");
        } else if (!FILENAME_PATTERN.matcher(fileName).matches()) {
            result.addError("File name contains invalid characters");
        } else if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            result.addError("File name contains invalid path characters");
        }

        return result;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    public static boolean isValidUrl(String url) {
        if (url == null) return false;

        String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return Pattern.matches(urlRegex, url);
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^A-Za-z0-9]").matcher(password).find();

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors;
        private java.util.List<String> warnings;

        public ValidationResult() {
            this.valid = true;
            this.errors = new java.util.ArrayList<>();
            this.warnings = new java.util.ArrayList<>();
        }

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public boolean isValid() {
            return valid && errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public java.util.List<String> getWarnings() {
            return warnings;
        }

        public String getErrorString() {
            return String.join("\n", errors);
        }

        public String getWarningString() {
            return String.join("\n", warnings);
        }

        public String getAllMessages() {
            StringBuilder sb = new StringBuilder();

            if (!errors.isEmpty()) {
                sb.append("Errors:\n").append(getErrorString());
            }

            if (!warnings.isEmpty()) {
                if (sb.length() > 0) sb.append("\n\n");
                sb.append("Warnings:\n").append(getWarningString());
            }

            return sb.toString();
        }
    }
}