/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tododiary;

import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author eh
 */
public class TodoDiaryStartup extends Application {
    
    public Stage stage;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        // get window location from user preferences: use x=100, y=100, width=400, height=400 as default
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        double x = userPrefs.getDouble("stage.x", 100);
        double y = userPrefs.getDouble("stage.y", 100);
        double w = userPrefs.getDouble("stage.width", 550);
        double h = userPrefs.getDouble("stage.height", 450);
        
        
        
        Parent root = FXMLLoader.load(getClass().getResource("TodoDiaryFXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(w);
        stage.setHeight(h);
        stage.setTitle("TodoDiary");
        stage.getIcons().add(new Image(TodoDiaryStartup.class.
                getResourceAsStream( "icon.png" )));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        userPrefs.putDouble("stage.x", stage.getX());
        userPrefs.putDouble("stage.y", stage.getY());
        userPrefs.putDouble("stage.width", stage.getWidth());
        userPrefs.putDouble("stage.height", stage.getHeight());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
