package com.diary.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.diary.manager.controllers.DashboardController;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Load main dashboard
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/light-theme.css")).toExternalForm());

        // Configure stage
        stage.setTitle("Personal Diary Manager");
        stage.setScene(scene);
        // stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();

        // Apply saved theme preference
        DashboardController dashboardController = loader.getController();
        dashboardController.applySavedTheme();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}