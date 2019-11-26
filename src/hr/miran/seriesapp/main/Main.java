package hr.miran.seriesapp.main;
	
import hr.miran.seriesapp.controller.LoginController;
import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static BorderPane root;
	public Stage primaryStage;
	
	@Override
    public void start(Stage primaryStage) throws Exception {
        LoginController loginController = new LoginController();
        loginController.launchLogingController(primaryStage);
    }
	public static void main(String[] args) {
		launch(args);
	}
	
	public static void setCenterPane(BorderPane centerPane) { 
		root.setCenter(centerPane); 
	}

	
	
}
