package hr.miran.seriesapp.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

    public class HomeController {
        private Parent parent;
        private Scene scene;
        private Stage stage;
        @FXML
        private Text welcomeText;

        public HomeController() {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../sample/home.fxml"));
            fxmlLoader.setController(this);
            try {
                parent = (Parent) fxmlLoader.load();
                scene = new Scene(parent, 600, 400);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void redirectHome(Stage stage, String name) {
            stage.setTitle("Home");
            stage.setScene(scene);
            welcomeText.setText("Hello " + name + "! You are welcome.");
            stage.hide();
            stage.show();
        }
    }