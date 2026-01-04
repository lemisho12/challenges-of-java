package com.diary.manager.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateFormatter {

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    private static final DateTimeFormatter MEDIUM_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    private static final DateTimeFormatter MEDIUM_TIME = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    private static final DateTimeFormatter SHORT_DATETIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private static final DateTimeFormatter MEDIUM_DATETIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter READABLE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");

    public static String formatShortDate(LocalDate date) {
        return date != null ? date.format(SHORT_DATE) : "";
    }

    public static String formatMediumDate(LocalDate date) {
        return date != null ? date.format(MEDIUM_DATE) : "";
    }

    public static String formatLongDate(LocalDate date) {
        return date != null ? date.format(LONG_DATE) : "";
    }

    public static String formatFullDate(LocalDate date) {
        return date != null ? date.format(FULL_DATE) : "";
    }

    public static String formatShortTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(SHORT_TIME) : "";
    }

    public static String formatMediumTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(MEDIUM_TIME) : "";
    }

    public static String formatShortDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(SHORT_DATETIME) : "";
    }

    public static String formatMediumDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(MEDIUM_DATETIME) : "";
    }

    public static String formatForFile(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FILE_FORMAT) : "";
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_FORMAT) : "";
    }

    public static String formatReadable(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(READABLE_FORMAT) : "";
    }

    public static String formatRelative(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);

        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        } else {
            return formatForDisplay(dateTime);
        }
    }

    public static LocalDateTime parseFromFile(String fileName) {
        try {
            // Extract date from filename like "2024-01-15_143022_title.txt"
            String datePart = fileName.substring(0, 19); // Get "2024-01-15_143022"
            return LocalDateTime.parse(datePart, FILE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }

    public static String getCurrentTimestamp() {
        return formatForFile(LocalDateTime.now());
    }

    public static String getTodayFileName() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_entries.txt";
    }

    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    public static boolean isYesterday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    public static boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        LocalDate date = dateTime.toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        return !date.isBefore(weekStart) && !date.isAfter(today);
    }

    public static boolean isThisMonth(LocalDateTime dateTime) {
        return dateTime != null &&
                dateTime.getMonth() == LocalDateTime.now().getMonth() &&
                dateTime.getYear() == LocalDateTime.now().getYear();
    }
}