package com.diary.manager.utils;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DialogHelper {

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait().orElse(noButton) == yesButton;
    }

    public static boolean showConfirmation(String title, String message, boolean withInput) {
        if (!withInput) {
            return showConfirmation(title, message);
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Create confirmation text field
        TextField confirmationField = new TextField();
        confirmationField.setPromptText("Type 'DELETE' to confirm");

        VBox content = new VBox(10,
                new Label(message),
                confirmationField
        );
        content.setPadding(new javafx.geometry.Insets(10));

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        // Enable/disable confirm button based on input
        Button confirmBtn = (Button) dialog.getDialogPane().lookupButton(confirmButton);
        confirmBtn.setDisable(true);

        confirmationField.textProperty().addListener((obs, oldVal, newVal) -> {
            confirmBtn.setDisable(!"DELETE".equalsIgnoreCase(newVal.trim()));
        });

        // Set result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButton) {
                return "DELETE".equalsIgnoreCase(confirmationField.getText().trim());
            }
            return false;
        });

        return dialog.showAndWait().orElse(false);
    }

    public static String showInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.initModality(Modality.APPLICATION_MODAL);

        return dialog.showAndWait().orElse(null);
    }

    public static void showExceptionDialog(String title, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(exception.getMessage());

        // Create expandable Exception
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Stack trace:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        VBox expContent = new VBox(10, label, textArea);
        expContent.setPadding(new javafx.geometry.Insets(10));

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static void showProgressDialog(String title, String message, javafx.concurrent.Task<?> task) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(message);
        dialog.initModality(Modality.APPLICATION_MODAL);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.progressProperty().bind(task.progressProperty());

        Label progressLabel = new Label();
        progressLabel.textProperty().bind(task.messageProperty());

        VBox content = new VBox(10, progressBar, progressLabel);
        content.setPadding(new javafx.geometry.Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Cancel the task if dialog is cancelled
        dialog.setOnCloseRequest(event -> {
            if (task.isRunning()) {
                task.cancel();
            }
        });

        // Close dialog when task completes
        task.setOnSucceeded(event -> dialog.close());
        task.setOnFailed(event -> dialog.close());
        task.setOnCancelled(event -> dialog.close());

        // Start task in background thread
        new Thread(task).start();

        dialog.showAndWait();
    }
}