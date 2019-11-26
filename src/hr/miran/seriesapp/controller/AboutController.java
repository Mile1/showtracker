package hr.miran.seriesapp.controller;

import java.net.URI;
import java.sql.Connection;
import java.util.ArrayList;

import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class AboutController {

	public AboutController() {
		
	}
	
	private Stage stage;
	
	@FXML
	private Label descriptionLbl;
	
	@FXML
	private Label showTrackLogoLbl;

	@FXML
	private Label versionLabel;
	
	@FXML
	private Label versionDescLabel;
	
	@FXML
	private Hyperlink versionLink ;
	
	private float currentVersionId;
	private String versionDesc = "Series Tracker is up to date";

	private String versionlocation;
	
	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	private static DbModule dbm =  new DbModule();
		@FXML
	public void initialize() {
		
			currentVersionId = (float) 1.1;
			
			descriptionLbl.setText("Show tracker is application for tracking your\n favourite shows, made as a part of diploma thesis.");

			Image image = new Image(getClass().getResourceAsStream("showTrackLogo.png"));
			showTrackLogoLbl.setGraphic(new ImageView(image));
		
			checkIfLatestVersion(); 
			
			versionLabel.setText("Version " + currentVersionId);
			versionDescLabel.setText(versionDesc);
	}


	private boolean checkIfLatestVersion() {
		boolean latestVersion = true;
		
		float versionId;
		String versiondesc = "";
		versionlocation = "";
		
		
		Connection conn = jdbcConnect.connectToUserDb();
		
		ArrayList<Object> versionInfo =  dbm.getLatestVersion(conn);
		
		versionId = (float) versionInfo.get(0);
		versionlocation = (String) versionInfo.get(2);
		
		
		if (versionId > currentVersionId) {
			latestVersion = false;
			versionDesc = "New version of Series tracker available! \nWhat/s new:\n " +  versionInfo.get(1);
			
			versionLink.setText("Download!");
			versionLink.setVisible(true);
		} else {
			versionLink.setVisible(false);
			latestVersion = true;
		}
		
		jdbcConnect.closeConnectionToDatabase(conn); 
		return latestVersion;
	}

	public void goToDownloadPage() {
		
        try {
        	URI u = new URI(versionlocation);
			java.awt.Desktop.getDesktop().browse(u);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	
	public void closeStage(){
		stage.close();
	}
	
}
