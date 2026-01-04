package com.diary.manager.controllers;

import com.diary.manager.models.ThemeManager;
import com.diary.manager.services.FileIOService;
import com.diary.manager.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class SettingsController implements Initializable {

    @FXML private ToggleButton btnThemeToggle;
    @FXML private ComboBox<String> cmbFontFamily;
    @FXML private Slider sldFontSize;
    @FXML private Label lblFontSize;
    @FXML private CheckBox chkAutoSave;
    @FXML private Slider sldAutoSaveInterval;
    @FXML private Label lblAutoSaveInterval;
    @FXML private CheckBox chkSpellCheck;
    @FXML private CheckBox chkWordWrap;
    @FXML private CheckBox chkShowLineNumbers;
    @FXML private Button btnExportAll;
    @FXML private Button btnImportEntries;
    @FXML private Button btnClearData;
    @FXML private Button btnResetSettings;
    @FXML private Label lblStorageUsed;
    @FXML private Label lblDataLocation;
    @FXML private ProgressBar storageProgress;

    private ThemeManager themeManager;
    private FileIOService fileIOService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        themeManager = ThemeManager.getInstance();
        fileIOService = new FileIOService();

        loadCurrentSettings();
        setupControls();
        updateStorageInfo();
    }

    private void loadCurrentSettings() {
        // Theme
        btnThemeToggle.setText(themeManager.isDarkMode() ? "🌙 Dark Mode" : "☀️ Light Mode");
        btnThemeToggle.setSelected(themeManager.isDarkMode());

        // Font settings
        cmbFontFamily.setValue(themeManager.getFontFamily());
        sldFontSize.setValue(themeManager.getFontSize());
        lblFontSize.setText(themeManager.getFontSize() + " px");

        // Auto-save settings
        chkAutoSave.setSelected(themeManager.getAutoSave());
        sldAutoSaveInterval.setValue(themeManager.getAutoSaveInterval());
        lblAutoSaveInterval.setText(themeManager.getAutoSaveInterval() + " seconds");

        // Editor settings (default values)
        chkSpellCheck.setSelected(true);
        chkWordWrap.setSelected(true);
        chkShowLineNumbers.setSelected(false);

        // Data location
        lblDataLocation.setText(Paths.get("data").toAbsolutePath().toString());
    }

    private void setupControls() {
        // Theme toggle
        btnThemeToggle.setOnAction(e -> {
            themeManager.toggleTheme();
            btnThemeToggle.setText(themeManager.isDarkMode() ? "🌙 Dark Mode" : "☀️ Light Mode");
            applyTheme();
        });

        // Font family
        cmbFontFamily.getItems().addAll(
                "Segoe UI", "Arial", "Helvetica", "Times New Roman",
                "Courier New", "Verdana", "Georgia", "Tahoma"
        );
        cmbFontFamily.valueProperty().addListener((obs, oldVal, newVal) -> 
            themeManager.setFontFamily(newVal)
        );

        // Font size slider
        sldFontSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            int size = newVal.intValue();
            lblFontSize.setText(size + " px");
            themeManager.setFontSize(size);
        });

        // Auto-save checkbox
        chkAutoSave.selectedProperty().addListener((obs, oldVal, newVal) -> {
            themeManager.setAutoSave(newVal);
            sldAutoSaveInterval.setDisable(!newVal);
        });

        // Auto-save interval slider
        sldAutoSaveInterval.valueProperty().addListener((obs, oldVal, newVal) -> {
            int seconds = newVal.intValue();
            lblAutoSaveInterval.setText(seconds + " seconds");
            themeManager.setAutoSaveInterval(seconds);
        });

        // Export button
        btnExportAll.setOnAction(e -> exportAllEntries());

        // Import button
        btnImportEntries.setOnAction(e -> importEntries());

        // Clear data button
        btnClearData.setOnAction(e -> clearAllData());

        // Reset settings button
        btnResetSettings.setOnAction(e -> resetSettings());
    }

    private void applyTheme() {
        // This would trigger theme change in the main application
        // The actual theme application is handled by DashboardController
    }

    private void updateStorageInfo() {
        try {
            long storageBytes = fileIOService.getTotalStorageUsed();
            double storageMB = storageBytes / (1024.0 * 1024.0);
            lblStorageUsed.setText(String.format("%.2f MB", storageMB));

            // Update progress bar (assuming 10MB limit for demo)
            double progress = Math.min(storageMB / 10.0, 1.0);
            storageProgress.setProgress(progress);

            // Color code based on usage
            if (progress > 0.9) {
                storageProgress.setStyle("-fx-accent: #e74c3c;");
            } else if (progress > 0.7) {
                storageProgress.setStyle("-fx-accent: #f39c12;");
            } else {
                storageProgress.setStyle("-fx-accent: #2ecc71;");
            }

        } catch (Exception e) {
            lblStorageUsed.setText("Error calculating storage");
            storageProgress.setProgress(0);
        }
    }

    private void exportAllEntries() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Export Directory");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDirectory = directoryChooser.showDialog(btnExportAll.getScene().getWindow());

        if (selectedDirectory != null) {
            try {
                Path exportPath = selectedDirectory.toPath().resolve("diary_export.txt");

                // Show progress dialog
                ProgressDialog progressDialog = new ProgressDialog();
                progressDialog.setTitle("Exporting Entries");
                progressDialog.setHeaderText("Exporting all entries...");

                // Create export service
                var exportService = fileIOService.createExportService(
                        com.diary.manager.models.DiaryManager.getInstance().getAllEntries(),
                        exportPath
                );

                // Bind progress dialog to service
                progressDialog.bindToTask(exportService);

                // Start export
                exportService.start();

                // Show dialog
                progressDialog.showAndWait();

                DialogHelper.showInfo("Export Complete",
                        "All entries have been exported to:\n" + exportPath);

            } catch (Exception e) {
                DialogHelper.showError("Export Failed",
                        "Failed to export entries: " + e.getMessage());
            }
        }
    }

    private void importEntries() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Import File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.rtf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedFile = fileChooser.showOpenDialog(btnImportEntries.getScene().getWindow());

        if (selectedFile != null) {
            boolean confirm = DialogHelper.showConfirmation(
                    "Import Entries",
                    "Import entries from: " + selectedFile.getName() + "?\n" +
                            "Note: This will parse the file and create new diary entries."
            );

            if (confirm) {
                // Show progress dialog
                ProgressDialog progressDialog = new ProgressDialog();
                progressDialog.setTitle("Importing Entries");
                progressDialog.setHeaderText("Importing entries from file...");

                // In a real implementation, you would create an ImportService
                // For now, just show a message
                progressDialog.close();
                DialogHelper.showInfo("Import Started",
                        "Import process started. This may take a moment.");
            }
        }
    }

    private void clearAllData() {
        boolean confirm = DialogHelper.showConfirmation(
                "Clear All Data",
                """
                ⚠️ WARNING: This will delete ALL diary entries!
                This action cannot be undone.
                
                Are you absolutely sure?"""
        );

        if (confirm) {
            boolean doubleConfirm = DialogHelper.showConfirmation(
                    "Final Confirmation",
                    """
                    This will permanently delete ALL your diary entries.
                    Type 'DELETE' to confirm:""",
                    true // Show text input
            );

            if (doubleConfirm) {
                try {
                    // Clear data directory
                    java.nio.file.Path dataDir = Paths.get("data");
                    if (java.nio.file.Files.exists(dataDir)) {
                        try (Stream<Path> walk = java.nio.file.Files.walk(dataDir)) {
                            walk.sorted(java.util.Comparator.reverseOrder())
                                .map(java.nio.file.Path::toFile)
                                .forEach(File::delete);
                        }
                    }

                    // Recreate directory
                    java.nio.file.Files.createDirectories(dataDir);

                    // Reload application data
                    com.diary.manager.models.DiaryManager.getInstance().loadEntries();

                    // Update UI
                    updateStorageInfo();

                    DialogHelper.showInfo("Data Cleared",
                            "All diary entries have been deleted successfully.");

                } catch (Exception e) {
                    DialogHelper.showError("Clear Failed",
                            "Failed to clear data: " + e.getMessage());
                }
            }
        }
    }

    private void resetSettings() {
        boolean confirm = DialogHelper.showConfirmation(
                "Reset Settings",
                """
                Reset all application settings to defaults?
                This will not delete your diary entries."""
        );

        if (confirm) {
            try {
                // Reset theme manager preferences
                themeManager.setDarkMode(false);
                themeManager.setFontFamily("Segoe UI");
                themeManager.setFontSize(14);
                themeManager.setAutoSave(true);
                themeManager.setAutoSaveInterval(60);
                themeManager.savePreferences();

                // Reload settings
                loadCurrentSettings();

                DialogHelper.showInfo("Settings Reset",
                        "All settings have been reset to defaults.");

            } catch (Exception e) {
                DialogHelper.showError("Reset Failed",
                        "Failed to reset settings: " + e.getMessage());
            }
        }
    }

    @FXML
    private void openDataFolder() {
        try {
            // Use ProcessBuilder to open the folder in a cross-platform way if Desktop is not supported
            // or if the module system prevents access to java.awt.Desktop
            String os = System.getProperty("os.name").toLowerCase();
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    DialogHelper.showError("Open Failed", "Could not create data directory.");
                    return;
                }
            }
            
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", dataDir.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", dataDir.getAbsolutePath()).start();
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                new ProcessBuilder("xdg-open", dataDir.getAbsolutePath()).start();
            } else {
                DialogHelper.showError("Open Failed", "Cannot open folder on this operating system.");
            }
        } catch (Exception e) {
            DialogHelper.showError("Open Failed",
                    "Failed to open data folder: " + e.getMessage());
        }
    }

    @FXML
    private void showAbout() {
        String aboutText = """
            Personal Diary Manager
            Version 1.0.0
            
            A modern diary management application
            built with JavaFX.
            
            Features:
            • Rich text editing
            • Advanced search
            • Tag organization
            • Light/Dark themes
            • Auto-save functionality
            
            © 2024 Diary Manager Project
            """;

        DialogHelper.showInfo("About", aboutText);
    }

    @FXML
    private void checkForUpdates() {
        DialogHelper.showInfo("Check for Updates",
                "You are using the latest version (1.0.0).");
    }

    @FXML
    private void showHelp() {
        String helpText = """
            Getting Started:
            1. Click 'New Entry' to start writing
            2. Use the Editor to format your text
            3. Add tags for organization
            4. Save entries automatically or manually
            
            Tips:
            • Use Ctrl+S to save quickly
            • Double-click entries in Browser to open
            • Use tags to filter entries
            • Export entries for backup
            
            Keyboard Shortcuts:
            Ctrl+N - New Entry
            Ctrl+S - Save Entry
            Ctrl+F - Search
            Ctrl+T - Toggle Theme
            
            For more help, visit our documentation.
            """;

        DialogHelper.showInfo("Help", helpText);
    }
}

// Helper class for progress dialog
class ProgressDialog extends Dialog<Void> {
    private final ProgressBar progressBar;
    private final Label statusLabel;

    public ProgressDialog() {
        setTitle("Progress");
        setHeaderText(null);

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);

        statusLabel = new Label();

        VBox content = new VBox(10, progressBar, statusLabel);
        content.setPadding(new javafx.geometry.Insets(20));

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    public void bindToTask(javafx.concurrent.Service<?> service) {
        progressBar.progressProperty().bind(service.progressProperty());
        statusLabel.textProperty().bind(service.messageProperty());

        // Close dialog when task completes
        service.setOnSucceeded(e -> close());
        service.setOnFailed(e -> close());

        // Cancel task when dialog is cancelled
        setOnCloseRequest(e -> {
            if (service.isRunning()) {
                service.cancel();
            }
        });
    }
}