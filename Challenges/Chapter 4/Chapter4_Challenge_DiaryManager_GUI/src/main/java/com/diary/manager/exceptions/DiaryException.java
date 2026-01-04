package com.diary.manager.exceptions;

public class DiaryException extends Exception {

    private final ErrorType errorType;

    public enum ErrorType {
        FILE_IO_ERROR,
        VALIDATION_ERROR,
        DATA_CORRUPTION,
        ENTRY_NOT_FOUND,
        DUPLICATE_ENTRY,
        PERMISSION_DENIED,
        STORAGE_FULL,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

    public DiaryException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN_ERROR;
    }

    public DiaryException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public DiaryException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNKNOWN_ERROR;
    }

    public DiaryException(String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getUserFriendlyMessage() {
        switch (errorType) {
            case FILE_IO_ERROR:
                return "Unable to access diary file. Please check permissions.";
            case VALIDATION_ERROR:
                return "Invalid diary entry data. Please check your input.";
            case DATA_CORRUPTION:
                return "Diary data appears to be corrupted. Some entries may be lost.";
            case ENTRY_NOT_FOUND:
                return "The requested diary entry could not be found.";
            case DUPLICATE_ENTRY:
                return "An entry with this title already exists.";
            case PERMISSION_DENIED:
                return "Permission denied. You don't have access to this operation.";
            case STORAGE_FULL:
                return "Storage is full. Please free up space and try again.";
            case NETWORK_ERROR:
                return "Network error occurred. Please check your connection.";
            case UNKNOWN_ERROR:
            default:
                return "An unexpected error occurred: " + getMessage();
        }
    }

    public String getDetailedMessage() {
        return String.format("[%s] %s%s",
                errorType.name(),
                getMessage(),
                getCause() != null ? " (Caused by: " + getCause().getMessage() + ")" : ""
        );
    }

    @Override
    public String toString() {
        return getDetailedMessage();
    }
}