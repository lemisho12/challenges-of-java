package com.diary.manager.controllers;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {

    @FXML private TableView<DiaryEntry> entriesTable;
    @FXML private TableColumn<DiaryEntry, String> colTitle;
    @FXML private TableColumn<DiaryEntry, String> colDate;
    @FXML private TableColumn<DiaryEntry, String> colPreview;
    @FXML private TableColumn<DiaryEntry, String> colTags;
    @FXML private TextArea entryDetail;
    @FXML private Label lblDetailTitle;
    @FXML private Label lblDetailDate;
    @FXML private Label lblDetailMood;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnExport;
    @FXML private Button btnToggleView;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TextField txtTagFilter;
    @FXML private VBox detailPanel;
    @FXML private HBox filterBar;

    private DiaryManager diaryManager;
    private boolean isListView = true;
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diaryManager = DiaryManager.getInstance();

        setupTable();
        setupFilters();
        setupButtons();
        loadEntries();
    }

    private void setupTable() {
        // Configure columns
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        colPreview.setCellValueFactory(new PropertyValueFactory<>("preview"));
        colTags.setCellValueFactory(cellData -> {
            DiaryEntry entry = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(String.join(", ", entry.getTags()));
        });

        // Make title column wider
        colTitle.setPrefWidth(200);
        colDate.setPrefWidth(150);
        colPreview.setPrefWidth(300);
        colTags.setPrefWidth(150);

        // Set up selection listener
        entriesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showEntryDetails(newSelection)
        );

        // Set up right-click context menu
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("View Entry");
        viewItem.setOnAction(e -> viewSelectedEntry());

        MenuItem editItem = new MenuItem("Edit Entry");
        editItem.setOnAction(e -> editSelectedEntry());

        MenuItem deleteItem = new MenuItem("Delete Entry");
        deleteItem.setOnAction(e -> deleteSelectedEntry());

        MenuItem favoriteItem = new MenuItem("Toggle Favorite");
        favoriteItem.setOnAction(e -> toggleFavorite());

        contextMenu.getItems().addAll(viewItem, editItem, deleteItem, new SeparatorMenuItem(), favoriteItem);

        entriesTable.setContextMenu(contextMenu);
    }

    private void setupFilters() {
        // Setup filter combo box
        cmbFilter.getItems().addAll("All Entries", "Favorites", "Today", "This Week", "This Month", "By Tag");
        cmbFilter.setValue("All Entries");

        cmbFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateFilter.setVisible("Today".equals(newVal) || "This Week".equals(newVal) || "This Month".equals(newVal));
            txtTagFilter.setVisible("By Tag".equals(newVal));
            applyFilter();
        });

        // Setup date filter
        dateFilter.setValue(LocalDate.now());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // Setup tag filter
        txtTagFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // Initially hide specific filters
        dateFilter.setVisible(false);
        txtTagFilter.setVisible(false);
    }

    private void setupButtons() {
        btnView.setOnAction(e -> viewSelectedEntry());
        btnEdit.setOnAction(e -> editSelectedEntry());
        btnDelete.setOnAction(e -> deleteSelectedEntry());
        btnExport.setOnAction(e -> exportSelectedEntry());
        btnToggleView.setOnAction(e -> toggleView());

        // Initially disable action buttons
        btnView.setDisable(true);
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnExport.setDisable(true);
    }

    private void loadEntries() {
        entriesTable.getItems().setAll(diaryManager.getAllEntries());
    }

    private void applyFilter() {
        String filterType = cmbFilter.getValue();

        switch (filterType) {
            case "All Entries":
                entriesTable.getItems().setAll(diaryManager.getAllEntries());
                break;

            case "Favorites":
                entriesTable.getItems().setAll(diaryManager.getFavoriteEntries());
                break;

            case "Today":
                entriesTable.getItems().setAll(diaryManager.getEntriesByDate(LocalDate.now()));
                break;

            case "This Week":
                // Implement week filter
                entriesTable.getItems().setAll(diaryManager.getAllEntries().filtered(entry ->
                        entry.getCreatedDate().toLocalDate().isAfter(LocalDate.now().minusDays(7))
                ));
                break;

            case "This Month":
                // Implement month filter
                entriesTable.getItems().setAll(diaryManager.getAllEntries().filtered(entry ->
                        entry.getCreatedDate().getMonth() == LocalDate.now().getMonth() &&
                                entry.getCreatedDate().getYear() == LocalDate.now().getYear()
                ));
                break;

            case "By Tag":
                String tag = txtTagFilter.getText().trim();
                if (!tag.isEmpty()) {
                    entriesTable.getItems().setAll(diaryManager.getEntriesByTag(tag));
                }
                break;
        }
    }

    private void showEntryDetails(DiaryEntry entry) {
        if (entry != null) {
            // Enable action buttons
            btnView.setDisable(false);
            btnEdit.setDisable(false);
            btnDelete.setDisable(false);
            btnExport.setDisable(false);

            // Update detail panel
            lblDetailTitle.setText(entry.getTitle());
            lblDetailDate.setText("Created: " + entry.getFormattedDate());
            lblDetailMood.setText("Mood: " + entry.getMood());

            // Display content with basic formatting
            String content = entry.getContent();
            // Simple markdown-like formatting
            content = content.replace("**", "").replace("*", "").replace("_", "");
            entryDetail.setText(content);

            // Highlight tags
            if (!entry.getTags().isEmpty()) {
                String tags = "Tags: " + String.join(", ", entry.getTags());
                entryDetail.appendText("\n\n" + tags);
            }

            // Add favorite indicator
            if (entry.isFavorite()) {
                entryDetail.appendText("\n\n⭐ Favorite Entry");
            }
        } else {
            // Clear detail panel
            lblDetailTitle.setText("No Entry Selected");
            lblDetailDate.setText("");
            lblDetailMood.setText("");
            entryDetail.setText("");

            // Disable action buttons
            btnView.setDisable(true);
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
            btnExport.setDisable(true);
        }
    }

    private void viewSelectedEntry() {
        DiaryEntry selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Show in read-only mode
            entryDetail.setEditable(false);
            entryDetail.setStyle("-fx-background-color: #f8f9fa;");
        }
    }

    private void editSelectedEntry() {
        DiaryEntry selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Switch to editor with this entry
            if (dashboardController != null) {
                dashboardController.loadEditor(selected);
            }
        }
    }

    private void deleteSelectedEntry() {
        DiaryEntry selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = DialogHelper.showConfirmation(
                    "Delete Entry",
                    "Are you sure you want to delete '" + selected.getTitle() + "'?\nThis action cannot be undone."
            );

            if (confirm) {
                try {
                    diaryManager.deleteEntry(selected);
                    entriesTable.getItems().remove(selected);
                    showEntryDetails(null);
                    DialogHelper.showInfo("Entry Deleted", "Entry has been deleted successfully.");
                } catch (Exception e) {
                    DialogHelper.showError("Delete Error", "Failed to delete entry: " + e.getMessage());
                }
            }
        }
    }

    private void exportSelectedEntry() {
        DiaryEntry selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create export dialog
            TextInputDialog dialog = new TextInputDialog(selected.getTitle() + ".txt");
            dialog.setTitle("Export Entry");
            dialog.setHeaderText("Export entry as text file");
            dialog.setContentText("File name:");

            dialog.showAndWait().ifPresent(fileName -> {
                try {
                    // Simple export implementation
                    String content = "Title: " + selected.getTitle() + "\n"
                            + "Date: " + selected.getFormattedDate() + "\n"
                            + "Mood: " + selected.getMood() + "\n"
                            + "Tags: " + String.join(", ", selected.getTags()) + "\n"
                            + "Favorite: " + (selected.isFavorite() ? "Yes" : "No") + "\n\n"
                            + "Content:\n" + selected.getContent();

                    // In a real implementation, you would use FileChooser
                    java.nio.file.Files.writeString(
                            java.nio.file.Path.of(fileName),
                            content
                    );

                    DialogHelper.showInfo("Export Successful", "Entry exported to: " + fileName);

                } catch (Exception e) {
                    DialogHelper.showError("Export Failed", "Failed to export entry: " + e.getMessage());
                }
            });
        }
    }

    private void toggleFavorite() {
        DiaryEntry selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setFavorite(!selected.isFavorite());
            try {
                diaryManager.saveEntry(selected);
                entriesTable.refresh();
                showEntryDetails(selected);
            } catch (Exception e) {
                DialogHelper.showError("Error", "Failed to update favorite status: " + e.getMessage());
            }
        }
    }

    private void toggleView() {
        isListView = !isListView;

        if (isListView) {
            // Switch to list view
            btnToggleView.setText("📄 Card View");
            entriesTable.setVisible(true);
            detailPanel.setVisible(false);
        } else {
            // Switch to card view (simplified)
            btnToggleView.setText("📋 List View");
            entriesTable.setVisible(false);
            detailPanel.setVisible(true);
            // Note: Full card view implementation would require additional UI components
        }
    }
}