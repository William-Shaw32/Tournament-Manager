package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * This is the App class needed for javaFX
 * It loads the root node of the fxml, creates and sets the scene, and sets up the window
 * @Author William Shaw
 */
public class App extends Application 
{
    /**
     * Overide of the start method in scene builder
     * It is called when scene builder starts
     * It loads the root node of the fxml, creates and sets the scene, and sets up the window
     * @param stage The starting stage for the application
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml") );
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tournament Manager");
        Image icon = new Image(getClass().getResourceAsStream("/images/ping-pong-paddle.png"));
        stage.getIcons().add(icon);
        stage.setMaximized(true);
        stage.show();
        root.requestFocus();
    }
    
    /**
     * Standard java main function
     * Passes control to JavaFX by calling lanuch
     * @param args Command line arguments
     */
    public static void main(String[] args) 
    {
        launch();
    }
}
