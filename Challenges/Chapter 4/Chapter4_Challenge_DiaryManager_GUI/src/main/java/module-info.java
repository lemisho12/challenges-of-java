module com.example.chapter4_challenge_diarymanager_gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    // Other dependencies
    requires java.prefs;
    requires com.google.gson;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.undo;

    opens com.diary.manager to javafx.fxml;
    opens com.diary.manager.controllers to javafx.fxml;
    opens com.diary.manager.models to com.google.gson, javafx.base;

    exports com.diary.manager;
    exports com.diary.manager.controllers;
    exports com.diary.manager.models;
    exports com.diary.manager.services;
    exports com.diary.manager.utils;
    exports com.diary.manager.exceptions;
    exports com.diary.manager.tasks;
}