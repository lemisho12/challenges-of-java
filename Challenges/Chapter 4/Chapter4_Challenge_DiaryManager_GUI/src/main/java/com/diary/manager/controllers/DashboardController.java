package com.diary.manager.controllers;

import com.diary.manager.models.DiaryEntry;
import com.diary.manager.models.DiaryManager;
import com.diary.manager.models.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private HBox navigationBar;
    @FXML private Button btnDashboard;
    @FXML private Button btnEditor;
    @FXML private Button btnBrowser;
    @FXML private Button btnSearch;
    @FXML private Button btnSettings;
    @FXML private Button btnNewEntry;
    @FXML private Button btnToggleTheme;
    @FXML private Label lblTotalEntries;
    @FXML private Label lblRecentEntries;
    @FXML private Label lblFavorites;
    @FXML private ListView<DiaryEntry> recentEntriesList;

    private DiaryManager diaryManager;
    private ThemeManager themeManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diaryManager = DiaryManager.getInstance();
        themeManager = ThemeManager.getInstance();

        setupNavigation();
        setupTooltips();
        loadDashboardData();
    }

    private void setupNavigation() {
        btnDashboard.setOnAction(e -> loadDashboard());
        btnEditor.setOnAction(e -> loadEditor(null));
        btnBrowser.setOnAction(e -> loadBrowser());
        btnSearch.setOnAction(e -> loadSearch());
        btnSettings.setOnAction(e -> loadSettings());
        btnNewEntry.setOnAction(e -> loadEditor(null));
        btnToggleTheme.setOnAction(e -> toggleTheme());

        // Set initial active button
        btnDashboard.getStyleClass().add("active-nav-btn");
    }

    private void setupTooltips() {
        Tooltip.install(btnDashboard, new Tooltip("Dashboard"));
        Tooltip.install(btnEditor, new Tooltip("Create/Edit Entry"));
        Tooltip.install(btnBrowser, new Tooltip("Browse Entries"));
        Tooltip.install(btnSearch, new Tooltip("Search Entries"));
        Tooltip.install(btnSettings, new Tooltip("Settings"));
        Tooltip.install(btnNewEntry, new Tooltip("New Entry"));
        Tooltip.install(btnToggleTheme, new Tooltip("Toggle Theme"));
    }

    private void loadDashboardData() {
        lblTotalEntries.setText(String.valueOf(diaryManager.getTotalEntries()));
        lblRecentEntries.setText(String.valueOf(diaryManager.getEntriesThisMonth()));
        lblFavorites.setText(String.valueOf(diaryManager.getFavoriteEntries().size()));

        // Load recent entries
        recentEntriesList.getItems().setAll(diaryManager.getAllEntries());
        recentEntriesList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(DiaryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getTitle() + " - " + item.getFormattedDate());
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            loadEditor(item);
                        }
                    });
                }
            }
        });
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboardContent = loader.load();
            mainContainer.setCenter(dashboardContent);
            updateActiveButton(btnDashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEditor(DiaryEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editor.fxml"));
            Parent editorContent = loader.load();
            EditorController controller = loader.getController();

            if (entry != null) {
                controller.loadEntry(entry);
            }

            mainContainer.setCenter(editorContent);
            updateActiveButton(btnEditor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBrowser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/browser.fxml"));
            Parent browserContent = loader.load();
            BrowserController controller = loader.getController();
            controller.setDashboardController(this);
            mainContainer.setCenter(browserContent);
            updateActiveButton(btnBrowser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search.fxml"));
            Parent searchContent = loader.load();
            mainContainer.setCenter(searchContent);
            updateActiveButton(btnSearch);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent settingsContent = loader.load();
            mainContainer.setCenter(settingsContent);
            updateActiveButton(btnSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleTheme() {
        themeManager.toggleTheme();
        applyTheme();
    }

    public void applySavedTheme() {
        applyTheme();
    }

    private void applyTheme() {
        Scene scene = mainContainer.getScene();
        if (scene != null) {
            String currentTheme = themeManager.isDarkMode() ? "/css/dark-theme.css" : "/css/light-theme.css";
            scene.getStylesheets().clear();
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(currentTheme)).toExternalForm());
        }
    }

    private void updateActiveButton(Button activeButton) {
        // Remove active class from all buttons
        btnDashboard.getStyleClass().remove("active-nav-btn");
        btnEditor.getStyleClass().remove("active-nav-btn");
        btnBrowser.getStyleClass().remove("active-nav-btn");
        btnSearch.getStyleClass().remove("active-nav-btn");
        btnSettings.getStyleClass().remove("active-nav-btn");

        // Add active class to clicked button
        activeButton.getStyleClass().add("active-nav-btn");
    }

    @FXML
    private void createNewEntry() {
        loadEditor(null);
    }
}