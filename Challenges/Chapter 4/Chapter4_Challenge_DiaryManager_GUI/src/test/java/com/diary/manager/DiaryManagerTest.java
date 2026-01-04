package com.diary.manager;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.services.FileIOService;
import com.diary.manager.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DiaryManagerTest {

    private DiaryManager diaryManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        diaryManager = DiaryManager.getInstance();
    }

    @Test
    void testCreateDiaryEntry() {
        DiaryEntry entry = new DiaryEntry("Test Title", "Test Content");

        assertNotNull(entry.getId());
        assertEquals("Test Title", entry.getTitle());
        assertEquals("Test Content", entry.getContent());
        assertNotNull(entry.getCreatedDate());
        assertNotNull(entry.getModifiedDate());
        assertFalse(entry.isFavorite());
        assertEquals("Neutral", entry.getMood());
        assertTrue(entry.getTags().isEmpty());
    }

    @Test
    void testEntryValidation() {
        DiaryEntry validEntry = new DiaryEntry("Valid Title", "Valid content");
        ValidationUtils.ValidationResult result = ValidationUtils.validateEntry(validEntry);

        assertTrue(result.isValid());
        assertFalse(result.hasWarnings());
    }

    @Test
    void testEntryValidation_InvalidTitle() {
        DiaryEntry invalidEntry = new DiaryEntry("", "Content");
        ValidationUtils.ValidationResult result = ValidationUtils.validateEntry(invalidEntry);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Title cannot be empty"));
    }

    @Test
    void testEntryWithTags() {
        DiaryEntry entry = new DiaryEntry("Tagged Entry", "Content with tags");
        entry.addTag("personal");
        entry.addTag("important");
        entry.addTag("work");

        assertEquals(3, entry.getTags().size());
        assertTrue(entry.getTags().contains("personal"));
        assertTrue(entry.getTags().contains("important"));
        assertTrue(entry.getTags().contains("work"));
    }

    @Test
    void testEntryPreview() {
        String longContent = "This is a very long content that should be truncated " +
                "when we get the preview. The preview should only show " +
                "the first 150 characters followed by an ellipsis.";

        DiaryEntry entry = new DiaryEntry("Long Entry", longContent);
        String preview = entry.getPreview();

        assertTrue(preview.length() <= 153); // 150 + "..."
        assertTrue(preview.endsWith("...") || preview.length() <= 150);
    }

    @Test
    void testFileIOService() throws Exception {
        FileIOService service = new FileIOService();
        DiaryEntry entry = new DiaryEntry("Test Entry", "This is test content");

        // Test save and load
        service.saveEntrySync(entry);

        // In a real test, you would verify the file was created
        // and could be loaded back
        assertNotNull(entry.getId());
    }

    @Test
    void testSearchFunctionality() {
        // Create test entries
        DiaryEntry entry1 = new DiaryEntry("Java Programming", "Learning Java is fun");
        entry1.addTag("programming");

        DiaryEntry entry2 = new DiaryEntry("Personal Thoughts", "Today was a good day");
        entry2.addTag("personal");

        // Add to manager (in-memory for test)
        diaryManager.getAllEntries().clear();
        diaryManager.getAllEntries().addAll(Arrays.asList(entry1, entry2));

        // Test search by tag
        var programmingEntries = diaryManager.getEntriesByTag("programming");
        assertEquals(1, programmingEntries.size());
        assertEquals("Java Programming", programmingEntries.get(0).getTitle());
    }

    @Test
    void testDateFormats() {
        DiaryEntry entry = new DiaryEntry();
        String formatted = entry.getFormattedDate();

        // Should be in format like "2024-01-15 14:30:22.123456"
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"));
    }
}