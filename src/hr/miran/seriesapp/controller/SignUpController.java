package hr.miran.seriesapp.controller;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.main.Main;
import hr.miran.seriesapp.main.SHA1Encryption;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class SignUpController {

    private Parent parent;
    private Scene scene;
    private Stage stage;
    @FXML
    private TextField userName;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confPasswordField;
    
    private HomeController homeController;
    private MainController mainController;
	private static BorderPane root;
	private BorderPane rootPane;
	private SHA1Encryption sha1Encryption;
	private JDBCConnect jdbcConnect = new JDBCConnect();
	private LoginController loginController;
	private static DbModule dbm =  new DbModule();
	
	
    public SignUpController() {
    	
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/signUp.fxml"));
        fxmlLoader.setController(this);
        try {
			rootPane = (BorderPane) fxmlLoader.load();
			root = rootPane;
		} catch (IOException e) {			
			e.printStackTrace();
		}
        
        scene = new Scene(root, 400,200);
        
    }

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {
    	
        if (userName.getText().trim().length() >= 5 && passwordField.getText().trim().length() >= 5) {
        	Connection conn = jdbcConnect.connectToUserDb();;
        	
        	sha1Encryption = new SHA1Encryption();
        	
        	if (passwordField.getText().equals(confPasswordField.getText())){
        		//check if user name exists and insert new user
        		if (!dbm.checkIfUserExists(conn, userName.getText())) {
        			dbm.signUpUser(conn, userName.getText(), sha1Encryption.encryptPassword(passwordField.getText()));
        			
        			dbm.upsertAccChanges(conn, dbm.getUserId(conn, userName.getText()), 0, 0, "lime");
        			jdbcConnect.closeConnectionToDatabase(conn);
//        			try {
//						if  (getResponseCode("https://www.thetvdb.com/") == 503) {
//							DialogFX alertOk = new DialogFX(Type.ERROR);
//							alertOk.setTitleText("The TVDB.com service is down");
//							alertOk.setMessage("The TVDB.com service is currently unavailable, please try to login again later!");
//							alertOk.showDialog();
//							
//						} else {
							mainController = new MainController(userName.getText());
							mainController.redirectHome(stage, userName.getText().trim());
							mainController.initData();
//						}
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
        		}else {
        			DialogFX alertOk = new DialogFX(Type.INFO);
        			alertOk.setTitleText("Sign Up Error");
        			alertOk.setMessage("User with same username already exists!");
        			alertOk.showDialog();
        		}
        	}else {
        		DialogFX alertOk = new DialogFX(Type.ERROR);
        		alertOk.setTitleText("Confirmation Password Error");
        		alertOk.setMessage("You have entered wrong password!");
        		alertOk.showDialog(); 
        	}
        	
        	try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
        	DialogFX alertOk = new DialogFX(Type.ERROR);
    		alertOk.setTitleText("Don't be lazy");
    		alertOk.setMessage("Username and password have to be at least 5 characters long!");
    		alertOk.showDialog(); 
        }

    }
    
    public Stage getStage() {
		return stage;
	}

	public static void setCenterPane(BorderPane centerPane) { 
		root.setCenter(centerPane); 
	}
	
	public void signInExistingUser() {
		loginController = new LoginController();
		loginController.redirectHome(stage, userName.getText().trim()); 
	}
	
	public void redirectHome(Stage stage, String name) {
        this.stage = stage;
		stage.setTitle("Show Tracker - User Sign Up");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../controller/showTrackLogo.png")));
        stage.hide();
        stage.show();
    }
	
	private int getResponseCode(String urlString) throws  IOException {
	    URL u = new URL(urlString); 
	    HttpURLConnection huc =  (HttpURLConnection)  u.openConnection(); 
	    huc.setRequestMethod("GET"); 
	    huc.connect(); 
	    return huc.getResponseCode();
	}
}