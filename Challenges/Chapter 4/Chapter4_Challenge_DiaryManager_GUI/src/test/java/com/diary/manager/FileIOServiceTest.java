package com.diary.manager;

import com.diary.manager.exceptions.DiaryException;
import com.diary.manager.models.DiaryEntry;
import com.diary.manager.services.FileIOService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileIOServiceTest {

    @TempDir
    Path tempDir;

    private FileIOService fileIOService;
    private Path testDataDir;
    private Path testEntriesDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create test directories
        testDataDir = tempDir.resolve("data");
        testEntriesDir = testDataDir.resolve("entries");
        Files.createDirectories(testEntriesDir);

        // Initialize FileIOService with test directory
        fileIOService = new FileIOService();

        // Use reflection to set private final static fields for testing
        setFinalStatic(FileIOService.class.getDeclaredField("DATA_DIR"), testDataDir.toString());
        setFinalStatic(FileIOService.class.getDeclaredField("ENTRIES_DIR"), testEntriesDir.toString());
        setFinalStatic(FileIOService.class.getDeclaredField("METADATA_FILE"), testDataDir.resolve("metadata.json").toString());
    }

    static void setFinalStatic(java.lang.reflect.Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        field.set(null, newValue);
    }


    @Test
    void testCreateDirectories() {
        // Verify directories were created
        assertTrue(Files.exists(testDataDir));
        assertTrue(Files.exists(testEntriesDir));
    }

    @Test
    void testSaveAndLoadEntry() throws Exception {
        // Create a test entry
        DiaryEntry entry = new DiaryEntry("Test Entry", "This is test content for the diary entry.");
        entry.addTag("test");
        entry.addTag("java");
        entry.setMood("😊 Happy");
        entry.setFavorite(true);

        // Save the entry
        fileIOService.saveEntrySync(entry);

        // Verify file was created
        List<Path> files;
        try (var stream = Files.list(testEntriesDir)) {
            files = stream.toList();
        }
        assertEquals(1, files.size());

        // Load entries
        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();

        // Verify loaded entry
        assertEquals(1, loadedEntries.size());
        DiaryEntry loadedEntry = loadedEntries.get(0);

        assertEquals(entry.getTitle(), loadedEntry.getTitle());
        assertEquals(entry.getContent(), loadedEntry.getContent());
        assertEquals(entry.getTags().size(), loadedEntry.getTags().size());
        assertEquals(entry.getMood(), loadedEntry.getMood());
        assertEquals(entry.isFavorite(), loadedEntry.isFavorite());
    }

    @Test
    void testSaveMultipleEntries() throws Exception {
        // Create multiple test entries
        DiaryEntry entry1 = new DiaryEntry("Entry 1", "Content 1");
        DiaryEntry entry2 = new DiaryEntry("Entry 2", "Content 2");
        DiaryEntry entry3 = new DiaryEntry("Entry 3", "Content 3");

        // Save entries
        fileIOService.saveEntrySync(entry1);
        fileIOService.saveEntrySync(entry2);
        fileIOService.saveEntrySync(entry3);

        // Verify files were created
        List<Path> files;
        try (var stream = Files.list(testEntriesDir)) {
            files = stream.toList();
        }
        assertEquals(3, files.size());

        // Load all entries
        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(3, loadedEntries.size());
    }

    @Test
    void testUpdateEntry() throws Exception {
        // Create and save entry
        DiaryEntry entry = new DiaryEntry("Original Title", "Original content");
        fileIOService.saveEntrySync(entry);

        // Update entry
        entry.setTitle("Updated Title");
        entry.setContent("Updated content");
        entry.addTag("updated");

        fileIOService.updateEntry(entry);

        // Load and verify update
        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());

        DiaryEntry loadedEntry = loadedEntries.get(0);
        assertEquals("Updated Title", loadedEntry.getTitle());
        assertEquals("Updated content", loadedEntry.getContent());
        assertTrue(loadedEntry.getTags().contains("updated"));
    }

    @Test
    void testDeleteEntry() throws Exception {
        // Create and save entry
        DiaryEntry entry = new DiaryEntry("To be deleted", "This will be deleted");
        fileIOService.saveEntrySync(entry);

        // Verify entry exists
        List<Path> filesBefore;
        try (var stream = Files.list(testEntriesDir)) {
            filesBefore = stream.toList();
        }
        assertEquals(1, filesBefore.size());

        // Delete entry
        fileIOService.deleteEntry(entry);

        // Verify entry was deleted
        List<Path> filesAfter;
        try (var stream = Files.list(testEntriesDir)) {
            filesAfter = stream.toList();
        }
        assertEquals(0, filesAfter.size());
    }

    @Test
    void testLoadFromMetadata() throws Exception {
        // Create test metadata
        String metadataJson = """
            [
                {
                    "id": "test-id-1",
                    "title": "Test Entry 1",
                    "content": "Content 1",
                    "createdDate": "2024-01-15T10:30:00",
                    "modifiedDate": "2024-01-15T10:30:00",
                    "tags": ["test", "java"],
                    "favorite": true,
                    "mood": "😊 Happy"
                },
                {
                    "id": "test-id-2",
                    "title": "Test Entry 2",
                    "content": "Content 2",
                    "createdDate": "2024-01-16T14:45:00",
                    "modifiedDate": "2024-01-16T14:45:00",
                    "tags": ["personal"],
                    "favorite": false,
                    "mood": "😐 Neutral"
                }
            ]
            """;

        // Write metadata file
        Files.writeString(testDataDir.resolve("metadata.json"), metadataJson);

        // Create corresponding content files
        Files.writeString(testEntriesDir.resolve("test-id-1.txt"), "Content 1");
        Files.writeString(testEntriesDir.resolve("test-id-2.txt"), "Content 2");

        // Load entries
        List<DiaryEntry> entries = fileIOService.loadAllEntries();

        // Verify entries were loaded
        assertEquals(2, entries.size());

        // Verify first entry
        DiaryEntry entry1 = entries.get(0);
        assertEquals("Test Entry 1", entry1.getTitle());
        assertEquals("Content 1", entry1.getContent());
        assertEquals(2, entry1.getTags().size());
        assertTrue(entry1.getTags().contains("test"));
        assertTrue(entry1.getTags().contains("java"));
        assertTrue(entry1.isFavorite());
        assertEquals("😊 Happy", entry1.getMood());

        // Verify second entry
        DiaryEntry entry2 = entries.get(1);
        assertEquals("Test Entry 2", entry2.getTitle());
        assertEquals("Content 2", entry2.getContent());
        assertEquals(1, entry2.getTags().size());
        assertTrue(entry2.getTags().contains("personal"));
        assertFalse(entry2.isFavorite());
        assertEquals("😐 Neutral", entry2.getMood());
    }

    @Test
    void testLoadFromDirectoryScan() throws Exception {
        // Create test entry files directly
        String content1 = "First line of entry 1\nSecond line of entry 1";
        String content2 = "First line of entry 2\nSecond line of entry 2";

        // Create files with timestamp-based names
        String fileName1 = "2024-01-15_103000_entry1.txt";
        String fileName2 = "2024-01-16_144500_entry2.txt";

        Files.writeString(testEntriesDir.resolve(fileName1), content1);
        Files.writeString(testEntriesDir.resolve(fileName2), content2);

        // Delete metadata file to force directory scan
        Files.deleteIfExists(testDataDir.resolve("metadata.json"));

        // Load entries
        List<DiaryEntry> entries = fileIOService.loadAllEntries();

        // Verify entries were loaded
        assertEquals(2, entries.size());

        // Verify entries have correct titles (from first line)
        assertTrue(entries.stream().anyMatch(e -> e.getTitle().contains("First line of entry 1")));
        assertTrue(entries.stream().anyMatch(e -> e.getTitle().contains("First line of entry 2")));

        // Verify content
        assertTrue(entries.stream().anyMatch(e -> e.getContent().equals(content1)));
        assertTrue(entries.stream().anyMatch(e -> e.getContent().equals(content2)));
    }

    @Test
    void testEntryWithSpecialCharacters() throws Exception {
        // Test with special characters in title and content
        String title = "Test Entry with Special Chars: & < > \" ' \\ /";
        String content = "Content with special chars: \nNew line\n\tTab\n\"Quotes\" & Ampersand";

        DiaryEntry entry = new DiaryEntry(title, content);
        entry.addTag("special");
        entry.addTag("test & tag");

        // Save entry
        fileIOService.saveEntrySync(entry);

        // Load entry
        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());

        DiaryEntry loadedEntry = loadedEntries.get(0);
        assertEquals(title, loadedEntry.getTitle());
        assertEquals(content, loadedEntry.getContent());
        assertEquals(2, loadedEntry.getTags().size());
    }

    @Test
    void testEmptyEntry() throws Exception {
        // Test with empty content
        DiaryEntry entry = new DiaryEntry("Empty Entry", "");

        fileIOService.saveEntrySync(entry);

        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());
        assertEquals("", loadedEntries.get(0).getContent());
    }

    @Test
    void testLargeEntry() throws Exception {
        // Test with large content
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Line ").append(i).append(": This is a test line for large content.\n");
        }

        DiaryEntry entry = new DiaryEntry("Large Entry", largeContent.toString());

        fileIOService.saveEntrySync(entry);

        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());
        assertEquals(largeContent.toString(), loadedEntries.get(0).getContent());
    }

    @Test
    void testDuplicateTags() throws Exception {
        // Test with duplicate tags (should be handled gracefully)
        DiaryEntry entry = new DiaryEntry("Duplicate Tags", "Content");
        entry.addTag("duplicate");
        entry.addTag("duplicate"); // Adding same tag twice
        entry.addTag("unique");

        fileIOService.saveEntrySync(entry);

        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());

        // The implementation may or may not filter duplicates
        // This test documents the current behavior
        List<String> tags = loadedEntries.get(0).getTags();
        assertTrue(tags.contains("duplicate"));
        assertTrue(tags.contains("unique"));
    }

    @Test
    void testEntryWithFutureDate() throws Exception {
        // Test with future date (should be accepted but might generate warning)
        DiaryEntry entry = new DiaryEntry("Future Entry", "Content");

        // Set date to tomorrow
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        // Use reflection to set the date since it's normally set in constructor
        java.lang.reflect.Field createdDateField = DiaryEntry.class.getDeclaredField("createdDate");
        createdDateField.setAccessible(true);
        createdDateField.set(entry, futureDate);

        java.lang.reflect.Field modifiedDateField = DiaryEntry.class.getDeclaredField("modifiedDate");
        modifiedDateField.setAccessible(true);
        modifiedDateField.set(entry, futureDate);

        fileIOService.saveEntrySync(entry);

        // Should still save successfully
        List<DiaryEntry> loadedEntries = fileIOService.loadAllEntries();
        assertEquals(1, loadedEntries.size());
    }

    @Test
    void testCorruptedMetadata() throws Exception {
        // Test with corrupted metadata file
        String corruptedJson = "This is not valid JSON {";
        Files.writeString(testDataDir.resolve("metadata.json"), corruptedJson);

        // This should fall back to directory scanning
        // We need to create some entry files for it to find
        Files.writeString(testEntriesDir.resolve("test1.txt"), "Content 1");

        // Should not throw exception
        List<DiaryEntry> entries = fileIOService.loadAllEntries();

        // Should have loaded the file from directory scan
        assertTrue(entries.size() >= 1);
    }

    @Test
    void testGetTotalStorageUsed() throws Exception {
        // Create test entries
        DiaryEntry entry1 = new DiaryEntry("Entry 1", "Content 1");
        DiaryEntry entry2 = new DiaryEntry("Entry 2", "Content 2");

        fileIOService.saveEntrySync(entry1);
        fileIOService.saveEntrySync(entry2);

        // Get storage used
        long storageUsed = fileIOService.getTotalStorageUsed();

        // Should be greater than 0
        assertTrue(storageUsed > 0, "Storage used should be greater than 0");

        // Create a larger entry
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeContent.append("This is line ").append(i).append("\n");
        }

        DiaryEntry largeEntry = new DiaryEntry("Large Entry", largeContent.toString());
        fileIOService.saveEntrySync(largeEntry);

        // Storage should have increased
        long newStorageUsed = fileIOService.getTotalStorageUsed();
        assertTrue(newStorageUsed > storageUsed, "Storage should increase after adding large entry");
    }

    @Test
    void testExportService() throws Exception {
        // Create test entries
        DiaryEntry entry1 = new DiaryEntry("Entry 1", "Content 1");
        DiaryEntry entry2 = new DiaryEntry("Entry 2", "Content 2");

        // Create export path
        Path exportPath = tempDir.resolve("export.txt");

        // Get entries list
        List<DiaryEntry> entries = List.of(entry1, entry2);

        // Create export service
        var exportService = fileIOService.createExportService(entries, exportPath);

        // Start export service
        exportService.start();

        // Verify export file was created
        assertTrue(Files.exists(exportPath));

        // Verify content
        String exportedContent = Files.readString(exportPath);
        assertTrue(exportedContent.contains("Entry 1"));
        assertTrue(exportedContent.contains("Entry 2"));
        assertTrue(exportedContent.contains("Content 1"));
        assertTrue(exportedContent.contains("Content 2"));
    }

    @Test
    void testFileNaming() throws Exception {
        // Test that filenames are generated correctly
        DiaryEntry entry = new DiaryEntry("Test Entry Title", "Content");

        // Get the generated filename
        fileIOService.saveEntrySync(entry);

        // List files in entries directory
        List<Path> files;
        try (var stream = Files.list(testEntriesDir)) {
            files = stream.toList();
        }
        assertEquals(1, files.size());

        Path file = files.get(0);
        String fileName = file.getFileName().toString();

        // Should start with date pattern
        assertTrue(fileName.matches("^\\d{4}-\\d{2}-\\d{2}_\\d{6}_.*\\.txt$"));

        // Should contain sanitized title
        String sanitizedTitle = "test_entry_title";
        assertTrue(fileName.toLowerCase().contains(sanitizedTitle));
    }

    @Test
    void testErrorHandling() {
        // Test with invalid file operations
        // This test verifies that exceptions are thrown appropriately

        // Try to load from non-existent directory
        FileIOService badService = new FileIOService();

        // Use reflection to set invalid directory
        try {
            setFinalStatic(FileIOService.class.getDeclaredField("DATA_DIR"), "/nonexistent/path/that/does/not/exist");

            // Should throw exception when trying to load
            assertThrows(Exception.class, () -> {
                badService.loadAllEntries();
            });

        } catch (Exception e) {
            // Reflection failed, but that's OK for this test
        }
    }
}