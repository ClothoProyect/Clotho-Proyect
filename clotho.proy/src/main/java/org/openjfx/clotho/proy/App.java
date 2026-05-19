package org.openjfx.clotho.proy;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	Image icono = new Image(getClass().getResourceAsStream("/imagenes/Clotho.png"));

        scene = new Scene(loadFXML("principal"), 640, 480);

    	// Se la asignamos a la ventana
        stage.setTitle("Clotho");
        stage.getIcons().add(icono);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}