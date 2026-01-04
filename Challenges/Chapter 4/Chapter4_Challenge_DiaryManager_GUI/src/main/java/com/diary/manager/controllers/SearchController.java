package com.diary.manager.controllers;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.services.SearchService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<DiaryEntry> searchResults;
    @FXML private TableColumn<DiaryEntry, String> colResultTitle;
    @FXML private TableColumn<DiaryEntry, String> colResultDate;
    @FXML private TableColumn<DiaryEntry, String> colResultPreview;
    @FXML private TextArea resultDetail;
    @FXML private Label lblResultCount;
    @FXML private Label lblSearchStatus;
    @FXML private ProgressBar searchProgress;
    @FXML private CheckBox chkSearchTitle;
    @FXML private CheckBox chkSearchContent;
    @FXML private CheckBox chkSearchTags;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private CheckBox chkFavoritesOnly;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private VBox searchPanel;

    private DiaryManager diaryManager;
    private SearchService searchService;
    private ExecutorService searchExecutor;
    private String lastSearchQuery = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diaryManager = DiaryManager.getInstance();
        searchService = new SearchService();
        searchExecutor = Executors.newSingleThreadExecutor();

        setupSearchTable();
        setupSearchOptions();
        setupSearchListener();
        setupSortOptions();
    }

    private void setupSearchTable() {
        colResultTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colResultDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        colResultPreview.setCellValueFactory(new PropertyValueFactory<>("preview"));

        // Make columns resizable
        colResultTitle.setPrefWidth(200);
        colResultDate.setPrefWidth(150);
        colResultPreview.setPrefWidth(350);

        // Set up selection listener
        searchResults.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showSearchResultDetail(newSelection)
        );

        // Enable double-click to open
        searchResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSelectedEntry();
            }
        });
    }

    private void setupSearchOptions() {
        // Set default search options
        chkSearchTitle.setSelected(true);
        chkSearchContent.setSelected(true);
        chkSearchTags.setSelected(true);

        // Set up date filters
        dateFrom.setValue(LocalDate.now().minusMonths(1));
        dateTo.setValue(LocalDate.now());

        // Add listeners to options
        chkSearchTitle.selectedProperty().addListener((obs, oldVal, newVal) -> performSearch());
        chkSearchContent.selectedProperty().addListener((obs, oldVal, newVal) -> performSearch());
        chkSearchTags.selectedProperty().addListener((obs, oldVal, newVal) -> performSearch());
        dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> performSearch());
        dateTo.valueProperty().addListener((obs, oldVal, newVal) -> performSearch());
        chkFavoritesOnly.selectedProperty().addListener((obs, oldVal, newVal) -> performSearch());
    }

    private void setupSortOptions() {
        cmbSortBy.getItems().addAll(
                "Relevance",
                "Date (Newest First)",
                "Date (Oldest First)",
                "Title (A-Z)",
                "Title (Z-A)"
        );
        cmbSortBy.setValue("Relevance");

        cmbSortBy.valueProperty().addListener((obs, oldVal, newVal) -> sortResults());
    }

    private void setupSearchListener() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(lastSearchQuery)) {
                lastSearchQuery = newValue;
                performSearch();
            }
        });
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();

        if (query.isEmpty()) {
            // Show all entries if search is empty
            searchResults.getItems().setAll(diaryManager.getAllEntries());
            updateResultCount(searchResults.getItems().size());
            lblSearchStatus.setText("Showing all entries");
            searchProgress.setProgress(0);
            return;
        }

        // Show searching status
        lblSearchStatus.setText("Searching...");
        searchProgress.setProgress(-1); // Indeterminate progress

        // Submit search task to executor
        searchExecutor.submit(() -> {
            try {
                // Get search options
                boolean searchTitle = chkSearchTitle.isSelected();
                boolean searchContent = chkSearchContent.isSelected();
                boolean searchTags = chkSearchTags.isSelected();
                LocalDate fromDate = dateFrom.getValue();
                LocalDate toDate = dateTo.getValue();
                boolean favoritesOnly = chkFavoritesOnly.isSelected();

                // Perform search
                var results = searchService.search(
                        query,
                        diaryManager.getAllEntries(),
                        searchTitle,
                        searchContent,
                        searchTags,
                        fromDate,
                        toDate,
                        favoritesOnly
                );

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    searchResults.getItems().setAll(results);
                    updateResultCount(results.size());
                    lblSearchStatus.setText("Search completed");
                    searchProgress.setProgress(1.0);

                    // Sort results
                    sortResults();

                    // Auto-select first result if any
                    if (!results.isEmpty()) {
                        searchResults.getSelectionModel().selectFirst();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblSearchStatus.setText("Search error: " + e.getMessage());
                    searchProgress.setProgress(0);
                });
            }
        });
    }

    private void sortResults() {
        String sortBy = cmbSortBy.getValue();

        searchResults.getItems().sort((entry1, entry2) -> {
            switch (sortBy) {
                case "Date (Newest First)":
                    return entry2.getCreatedDate().compareTo(entry1.getCreatedDate());

                case "Date (Oldest First)":
                    return entry1.getCreatedDate().compareTo(entry2.getCreatedDate());

                case "Title (A-Z)":
                    return entry1.getTitle().compareToIgnoreCase(entry2.getTitle());

                case "Title (Z-A)":
                    return entry2.getTitle().compareToIgnoreCase(entry1.getTitle());

                case "Relevance":
                default:
                    // Keep original order (relevance-based from search)
                    return 0;
            }
        });
    }

    private void updateResultCount(int count) {
        lblResultCount.setText(count + " result" + (count != 1 ? "s" : "") + " found");
    }

    private void showSearchResultDetail(DiaryEntry entry) {
        if (entry != null) {
            String content = entry.getContent();
            String query = txtSearch.getText().trim().toLowerCase();

            // Highlight search terms in content
            if (!query.isEmpty()) {
                String highlighted = content.replaceAll(
                        "(?i)(" + query.replaceAll("[\\[\\](){}.*+?^$|\\\\]", "\\\\$0") + ")",
                        "**$1**"  // Simple markdown-style highlighting
                );
                resultDetail.setText(highlighted);
            } else {
                resultDetail.setText(content);
            }

            // Add metadata
            String metadata = "\n\n---\n"
                    + "Title: " + entry.getTitle() + "\n"
                    + "Date: " + entry.getFormattedDate() + "\n"
                    + "Mood: " + entry.getMood() + "\n"
                    + "Tags: " + String.join(", ", entry.getTags()) + "\n"
                    + "Favorite: " + (entry.isFavorite() ? "⭐ Yes" : "No");

            resultDetail.appendText(metadata);
        } else {
            resultDetail.setText("");
        }
    }

    private void openSelectedEntry() {
        DiaryEntry selected = searchResults.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Switch to editor with this entry
            DashboardController dashboardController = getDashboardController();
            if (dashboardController != null) {
                dashboardController.loadEditor(selected);
            }
        }
    }

    @FXML
    private void clearSearch() {
        txtSearch.clear();
        searchResults.getItems().clear();
        resultDetail.clear();
        lblResultCount.setText("0 results");
        lblSearchStatus.setText("Ready");
        searchProgress.setProgress(0);
    }

    @FXML
    private void exportResults() {
        if (!searchResults.getItems().isEmpty()) {
            // Create export dialog
            TextInputDialog dialog = new TextInputDialog("search_results.txt");
            dialog.setTitle("Export Search Results");
            dialog.setHeaderText("Export " + searchResults.getItems().size() + " entries");
            dialog.setContentText("File name:");

            dialog.showAndWait().ifPresent(fileName -> {
                try {
                    StringBuilder exportContent = new StringBuilder();
                    exportContent.append("Search Results for: ").append(txtSearch.getText()).append("\n");
                    exportContent.append("Generated: ").append(LocalDate.now()).append("\n");
                    exportContent.append("Total Entries: ").append(searchResults.getItems().size()).append("\n\n");

                    for (DiaryEntry entry : searchResults.getItems()) {
                        exportContent.append("=== ").append(entry.getTitle()).append(" ===\n");
                        exportContent.append("Date: ").append(entry.getFormattedDate()).append("\n");
                        exportContent.append("Mood: ").append(entry.getMood()).append("\n");
                        exportContent.append("Tags: ").append(String.join(", ", entry.getTags())).append("\n");
                        exportContent.append("Favorite: ").append(entry.isFavorite() ? "Yes" : "No").append("\n");
                        exportContent.append("Content:\n").append(entry.getContent()).append("\n\n");
                    }

                    java.nio.file.Files.writeString(
                            java.nio.file.Path.of(fileName),
                            exportContent.toString()
                    );

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Search results exported to: " + fileName);
                    alert.showAndWait();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to export results: " + e.getMessage());
                    alert.showAndWait();
                }
            });
        }
    }

    private DashboardController getDashboardController() {
        // This would require a reference to the main controller
        return null;
    }

    public void cleanup() {
        if (searchExecutor != null && !searchExecutor.isShutdown()) {
            searchExecutor.shutdown();
        }
    }
}