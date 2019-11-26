package hr.miran.seriesapp.controller;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.main.SHA1Encryption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class LoginController {

    private Parent parent;
    private Scene scene;
    private Stage stage;
    @FXML
    private TextField userName;
    @FXML
    private TextField passwordField;
    
    
    private HomeController homeController;
    private MainController mainController;
	private static BorderPane root;
	private BorderPane rootPane;
	private SHA1Encryption sha1Encryption;
	private JDBCConnect jdbcConnect = new JDBCConnect();
	private SignUpController signUpController;
	private static DbModule dbm =  new DbModule();
	
	
    public LoginController() {
    	
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/login.fxml"));
        fxmlLoader.setController(this);
        try {
			rootPane = (BorderPane) fxmlLoader.load();
			root = rootPane;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            scene = new Scene(root, 400,200);
        
    }

    @FXML
    public void handleSubmitButtonAction(ActionEvent event) {

		    	openMainController();

    }
    
    private int getResponseCode(String urlString) throws  IOException {
	    URL u = new URL(urlString); 
	    HttpURLConnection huc =  (HttpURLConnection)  u.openConnection(); 
	    huc.setRequestMethod("GET"); 
	    huc.connect(); 
	    return huc.getResponseCode();
	}
    
    private void openMainController() {

    	if (userName.getText().trim().length() > 0 && passwordField.getText().trim().length() > 0 ) {
        	Connection conn = jdbcConnect.connectToUserDb();;
			
        	        	
        	sha1Encryption = new SHA1Encryption();
        	if (dbm.checkUserLogin(conn, userName.getText(), sha1Encryption.encryptPassword(passwordField.getText()))) {
        		
        		try {
					if  (getResponseCode("https://www.thetvdb.com/") == 503) {
							DialogFX alertOk = new DialogFX(Type.ERROR);
							alertOk.setTitleText("The TVDB.com service is down");
							alertOk.setMessage("The TVDB.com service is currently unavailable, please try to login again later!");
							alertOk.showDialog();
							
						} else {
							mainController = new MainController(userName.getText());
							mainController.redirectHome(stage, userName.getText().trim());
							mainController.initData();
						}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else {
        		DialogFX alertOk = new DialogFX(Type.ERROR);
				alertOk.setTitleText("Login Error");
				alertOk.setMessage("You have entered wrong user or wrong password!");
				alertOk.showDialog();
        	}

        	try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }
    }
    
    
    public void launchLogingController(Stage stage) {
        this.stage = stage;
        stage.setTitle("Show Tracker - User Login");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("showTrackLogo.png")));
        stage.setScene(scene);
        stage.setResizable(true);
        stage.hide();
        stage.show();
    }
    
    public Stage getStage() {
		return stage;
	}

	public static void setCenterPane(BorderPane centerPane) { 
		root.setCenter(centerPane); 
	}
	
	public void signUpNewUser() {
		signUpController = new SignUpController();
		signUpController.redirectHome(stage, userName.getText().trim()); 
	}
	
	public void redirectHome(Stage stage, String name) {
		this.stage = stage;
		stage.setTitle("Show Tracker - User Login");
        stage.setScene(scene);
        stage.hide();
        stage.show();
    }
}