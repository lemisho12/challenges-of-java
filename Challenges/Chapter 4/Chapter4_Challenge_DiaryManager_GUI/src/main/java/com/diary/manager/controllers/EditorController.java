package com.diary.manager.controllers;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.services.AutoSaveService;
import com.diary.manager.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EditorController implements Initializable {

    @FXML private TextField txtTitle;
    @FXML private HTMLEditor htmlEditor;
    @FXML private TextArea txtContent;
    @FXML private TextField txtTags;
    @FXML private ComboBox<String> cmbMood;
    @FXML private CheckBox chkFavorite;
    @FXML private Button btnSave;
    @FXML private Button btnSaveAs;
    @FXML private Button btnFormatBold;
    @FXML private Button btnFormatItalic;
    @FXML private Button btnFormatUnderline;
    @FXML private Button btnInsertBullet;
    @FXML private Button btnInsertNumber;
    @FXML private ToolBar formattingToolbar;
    @FXML private Label lblStatus;
    @FXML private Label lblWordCount;
    @FXML private Label lblCharCount;
    @FXML private ProgressBar progressSave;
    @FXML private VBox editorContainer;

    private DiaryEntry currentEntry;
    private DiaryManager diaryManager;
    private AutoSaveService autoSaveService;
    private boolean isModified;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diaryManager = DiaryManager.getInstance();
        autoSaveService = new AutoSaveService(this);

        setupEditor();
        setupFormatting();
        setupListeners();
        setupKeyboardShortcuts();
        setupMoodSelector();

        // Start auto-save service
        autoSaveService.start();
    }

    private void setupEditor() {
        // Use plain text area or HTML editor based on preference
        txtContent.setWrapText(true);

        // Default to plain text editor
        htmlEditor.setVisible(false);
        txtContent.setVisible(true);

        // Set up HTML editor toolbar
        formattingToolbar.setVisible(false); // Hide custom toolbar for now
    }

    private void setupFormatting() {
        btnFormatBold.setOnAction(e -> applyFormatting("bold"));
        btnFormatItalic.setOnAction(e -> applyFormatting("italic"));
        btnFormatUnderline.setOnAction(e -> applyFormatting("underline"));
        btnInsertBullet.setOnAction(e -> insertBulletList());
        btnInsertNumber.setOnAction(e -> insertNumberedList());
    }

    private void setupListeners() {
        // Track modifications
        txtTitle.textProperty().addListener((obs, oldVal, newVal) -> markModified());
        txtContent.textProperty().addListener((obs, oldVal, newVal) -> {
            updateCounters();
            markModified();
        });
        txtTags.textProperty().addListener((obs, oldVal, newVal) -> markModified());
        cmbMood.valueProperty().addListener((obs, oldVal, newVal) -> markModified());
        chkFavorite.selectedProperty().addListener((obs, oldVal, newVal) -> markModified());

        // Save buttons
        btnSave.setOnAction(e -> saveEntry());
        btnSaveAs.setOnAction(e -> saveAsNewEntry());

        // Update counters initially
        updateCounters();
    }

    private void setupKeyboardShortcuts() {
        // Save: Ctrl+S
        KeyCombination saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        editorContainer.setOnKeyPressed(e -> {
            if (saveCombo.match(e)) {
                saveEntry();
                e.consume();
            }
        });
    }

    private void setupMoodSelector() {
        cmbMood.getItems().addAll(
                "😊 Happy", "😢 Sad", "😡 Angry", "😴 Tired",
                "😃 Excited", "😌 Peaceful", "😔 Lonely", "😎 Confident",
                "😖 Stressed", "😍 Loved", "😐 Neutral", "🤔 Thoughtful"
        );
        cmbMood.setValue("😐 Neutral");
    }

    private void updateCounters() {
        String content = txtContent.getText();
        int words = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
        int chars = content.length();

        lblWordCount.setText("Words: " + words);
        lblCharCount.setText("Chars: " + chars);
    }

    private void markModified() {
        if (!isModified) {
            isModified = true;
            lblStatus.setText("Modified");
            lblStatus.setStyle("-fx-text-fill: orange;");
        }
    }

    private void clearModified() {
        isModified = false;
        lblStatus.setText("Saved");
        lblStatus.setStyle("-fx-text-fill: green;");
    }

    public void loadEntry(DiaryEntry entry) {
        this.currentEntry = entry;

        txtTitle.setText(entry.getTitle());
        txtContent.setText(entry.getContent());

        // Load tags
        if (entry.getTags() != null && !entry.getTags().isEmpty()) {
            txtTags.setText(String.join(", ", entry.getTags()));
        }

        // Load mood
        if (entry.getMood() != null) {
            cmbMood.setValue(entry.getMood());
        }

        // Load favorite
        chkFavorite.setSelected(entry.isFavorite());

        updateCounters();
        clearModified();
    }

    public void newEntry() {
        this.currentEntry = new DiaryEntry();
        txtTitle.setText("");
        txtContent.setText("");
        txtTags.setText("");
        cmbMood.setValue("😐 Neutral");
        chkFavorite.setSelected(false);
        updateCounters();
        clearModified();
    }

    public void saveEntry() {
        try {
            if (currentEntry == null) {
                currentEntry = new DiaryEntry();
            }

            // Update entry with current data
            currentEntry.setTitle(txtTitle.getText());
            currentEntry.setContent(txtContent.getText());
            currentEntry.setModifiedDate(LocalDateTime.now());
            currentEntry.setMood(cmbMood.getValue());
            currentEntry.setFavorite(chkFavorite.isSelected());

            // Parse tags
            String[] tagArray = txtTags.getText().split(",");
            List<String> tags = new ArrayList<>();
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    tags.add(trimmedTag);
                }
            }
            currentEntry.setTags(tags);

            // Save entry
            diaryManager.saveEntry(currentEntry);

            // Update status
            clearModified();
            progressSave.setProgress(1.0);

            DialogHelper.showInfo("Entry Saved", "Entry '" + currentEntry.getTitle() + "' has been saved successfully.");

        } catch (Exception e) {
            DialogHelper.showError("Save Error", "Failed to save entry: " + e.getMessage());
        }
    }

    public void saveAsNewEntry() {
        DiaryEntry newEntry = new DiaryEntry();
        newEntry.setTitle(txtTitle.getText() + " (Copy)");
        newEntry.setContent(txtContent.getText());
        newEntry.setMood(cmbMood.getValue());
        newEntry.setFavorite(chkFavorite.isSelected());

        // Parse tags
        String[] tagArray = txtTags.getText().split(",");
        List<String> tags = new ArrayList<>();
        for (String tag : tagArray) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                tags.add(trimmedTag);
            }
        }
        newEntry.setTags(tags);

        try {
            diaryManager.saveEntry(newEntry);
            DialogHelper.showInfo("Entry Saved", "New entry created successfully.");
        } catch (Exception e) {
            DialogHelper.showError("Save Error", "Failed to save as new entry: " + e.getMessage());
        }
    }

    public void autoSave() {
        if (isModified && !txtContent.getText().isEmpty()) {
            try {
                if (currentEntry == null) {
                    currentEntry = new DiaryEntry();
                }

                // Quick save without full metadata
                currentEntry.setTitle(txtTitle.getText().isEmpty() ? "Untitled" : txtTitle.getText());
                currentEntry.setContent(txtContent.getText());

                diaryManager.saveEntry(currentEntry);

                lblStatus.setText("Auto-saved at " + LocalDateTime.now().getHour() + ":" +
                        String.format("%02d", LocalDateTime.now().getMinute()));
                lblStatus.setStyle("-fx-text-fill: blue;");

            } catch (Exception e) {
                lblStatus.setText("Auto-save failed");
                lblStatus.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void applyFormatting(String format) {
        // For plain text area, we'll just insert formatting markers
        String selectedText = txtContent.getSelectedText();
        if (!selectedText.isEmpty()) {
            String formattedText = "";
            switch (format) {
                case "bold":
                    formattedText = "**" + selectedText + "**";
                    break;
                case "italic":
                    formattedText = "*" + selectedText + "*";
                    break;
                case "underline":
                    formattedText = "_" + selectedText + "_";
                    break;
            }

            int start = txtContent.getSelection().getStart();
            int end = txtContent.getSelection().getEnd();
            txtContent.replaceText(start, end, formattedText);
        }
    }

    private void insertBulletList() {
        String text = txtContent.getText();
        int caretPos = txtContent.getCaretPosition();
        txtContent.insertText(caretPos, "\n• ");
    }

    private void insertNumberedList() {
        String text = txtContent.getText();
        int caretPos = txtContent.getCaretPosition();
        txtContent.insertText(caretPos, "\n1. ");
    }

    public DiaryEntry getCurrentEntry() {
        return currentEntry;
    }

    public String getCurrentContent() {
        return txtContent.getText();
    }

    public void cleanup() {
        if (autoSaveService != null) {
            autoSaveService.stop();
        }
    }
}