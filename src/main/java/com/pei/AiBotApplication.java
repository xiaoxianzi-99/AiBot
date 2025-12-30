package com.pei;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX AI Chat Bot Application
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class AiBotApplication extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AiBotApplication.class.getResource("/fxml/chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 600);
        
        // Add CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        stage.setTitle("AI Chat Bot");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
