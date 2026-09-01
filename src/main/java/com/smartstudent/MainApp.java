package com.smartstudent;

import com.smartstudent.service.StudentService;
import com.smartstudent.storage.StudentStorage;
import com.smartstudent.ui.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        StudentStorage storage = new StudentStorage(Path.of("data"));
        StudentService service = new StudentService(storage);
        service.load();

        MainController controller = new MainController(service);
        Scene scene = new Scene(controller.getRoot(), 1320, 820);
        scene.getStylesheets().add(Objects.requireNonNull(
                MainApp.class.getResource("/com/smartstudent/styles/app.css"),
                "Missing application stylesheet").toExternalForm());

        stage.setTitle("Smart Student Records");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
