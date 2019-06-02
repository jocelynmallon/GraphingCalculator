/*
    Calculator.java
    Main Application class
 */
package dev.StylishNerds.GraphingCalculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Calculator extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMinWidth(350);
        stage.setMinHeight(510);
        stage.setResizable(true);
        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle("Calculator");
        stage.getIcons().add(new Image(getClass().getResource("/Calculator-icon.png").toExternalForm()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
