package com.diary.manager.exceptions;

import java.nio.file.Path;

public class FileOperationException extends DiaryException {

    private final Path filePath;
    private final FileOperation operation;

    public enum FileOperation {
        READ,
        WRITE,
        DELETE,
        CREATE,
        RENAME,
        COPY,
        MOVE,
        LIST
    }

    public FileOperationException(String message, Path filePath, FileOperation operation) {
        super(message, ErrorType.FILE_IO_ERROR);
        this.filePath = filePath;
        this.operation = operation;
    }

    public FileOperationException(String message, Path filePath, FileOperation operation, Throwable cause) {
        super(message, ErrorType.FILE_IO_ERROR, cause);
        this.filePath = filePath;
        this.operation = operation;
    }

    public Path getFilePath() {
        return filePath;
    }

    public FileOperation getOperation() {
        return operation;
    }

    @Override
    public String getUserFriendlyMessage() {
        String baseMessage = super.getUserFriendlyMessage();
        String fileName = filePath != null ? filePath.getFileName().toString() : "unknown file";

        switch (operation) {
            case READ:
                return String.format("%s\nFile: %s", baseMessage, fileName);
            case WRITE:
                return String.format("Failed to save file: %s\n%s", fileName, baseMessage);
            case DELETE:
                return String.format("Failed to delete file: %s\n%s", fileName, baseMessage);
            case CREATE:
                return String.format("Failed to create file: %s\n%s", fileName, baseMessage);
            default:
                return baseMessage;
        }
    }

    @Override
    public String getDetailedMessage() {
        return String.format("[%s on %s] %s%s",
                operation.name(),
                filePath != null ? filePath.toString() : "null",
                getMessage(),
                getCause() != null ? " (Caused by: " + getCause().getMessage() + ")" : ""
        );
    }

    public String getOperationDescription() {
        switch (operation) {
            case READ:
                return "reading from";
            case WRITE:
                return "writing to";
            case DELETE:
                return "deleting";
            case CREATE:
                return "creating";
            case RENAME:
                return "renaming";
            case COPY:
                return "copying";
            case MOVE:
                return "moving";
            case LIST:
                return "listing files in";
            default:
                return "operating on";
        }
    }
}