package hr.miran.seriesapp.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.entity.AccountInfo;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.main.SHA1Encryption;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class AccountController {

	private Stage stage;
	
	@FXML
	private Label helloLabel;
	
	@FXML
	private ComboBox<String> comboBackgroundCover;
	
	@FXML
	private ComboBox<String> comboChangeInterval;
	
	@FXML
	private ComboBox<String> comboColour;
	
	
	@FXML
	private Label descriptionLbl;
	
	@FXML
	private Label showTrackLogoLbl;
	
	@FXML
	private PasswordField passwordOldTft;
	
	@FXML
	private PasswordField passwordNewTft;
	
	@FXML
	private PasswordField passwordConfirmTft;
	
	@FXML
	private CheckBox changePassCheckBox;

	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	private static DbModule dbm =  new DbModule();
	private AccountInfo accountInfo;
	
	public AccountController() {
		
	}
	
		@FXML
	public void initialize() {

			helloLabel.setText("Hello " + MainController.loggedUser);
			
			fillBackgroundCombo();
			fillIntervalCombo();
			fillAirdColorCombo();
			
			
			
	}
	
	public void fillBackgroundCombo(){
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ArrayList<String> taggedShowNamesList = dbm.getTaggedShowNamesList(conn); 
			
		comboBackgroundCover.getItems().add("Default");
		comboBackgroundCover.getItems().add("Interchangable");
		comboBackgroundCover.getItems().addAll(taggedShowNamesList);
		
						
		comboBackgroundCover.setPromptText("Default");
		
		comboBackgroundCover.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				if (comboBackgroundCover.getSelectionModel().getSelectedIndex() != 1) {
					comboChangeInterval.setDisable(true);
				} else {
					
					comboChangeInterval.setDisable(false);
				} 
			}
			});
		
		passwordOldTft.setDisable(true);
		passwordNewTft.setDisable(true);
		passwordConfirmTft.setDisable(true);
		
		changePassCheckBox.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				if (changePassCheckBox.isSelected()) {
					passwordOldTft.setDisable(false);
					passwordNewTft.setDisable(false);
					passwordConfirmTft.setDisable(false);
				} else {
					passwordOldTft.setDisable(true);
					passwordNewTft.setDisable(true);
					passwordConfirmTft.setDisable(true);
				}
			}
			
		});
		
		
		jdbcConnect.closeConnectionToDatabase(conn);
		
	}	
	
	
	
	public void fillIntervalCombo(){
		
		comboChangeInterval.getItems().add("30 sec");
		comboChangeInterval.getItems().add("1 min");
		comboChangeInterval.getItems().add("5 min");
		comboChangeInterval.getItems().add("10 min");
		comboChangeInterval.getItems().add("30 min");
						
		comboChangeInterval.setPromptText("30 sec"); 
		
	}
	
	public void fillAirdColorCombo(){
		
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ArrayList<String> colorList = new ArrayList();
		
		colorList = dbm.getColorList(conn);
		
		comboChangeInterval.getItems().add("None");
		comboColour.getItems().addAll(colorList);
		
		jdbcConnect.closeConnectionToDatabase(conn);
	}
	 

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	private void fillAccountForm() {
		Connection conn = jdbcConnect.connectToUserDb();;
		
		String backgroundString = "";
		
		if (accountInfo.getBackgroundCover() == null || accountInfo.getBackgroundCover() == 0) {
			backgroundString = "Default";
		} else if (accountInfo.getBackgroundCover() == 1) {
			backgroundString = "Interchangable";
		} else {
			backgroundString = dbm.getShowNameFromId(conn, accountInfo.getBackgroundCover());
		}
		
		comboBackgroundCover.getSelectionModel().select(backgroundString);
		
		
		if (accountInfo.getChangeInterval() == 30) {
			comboChangeInterval.getSelectionModel().select(0);
		} else {
			comboChangeInterval.getSelectionModel().select(accountInfo.getChangeInterval() / 60 + " min");
		}
		
		comboColour.getSelectionModel().select(accountInfo.getAiredColor());
		
		jdbcConnect.closeConnectionToDatabase(conn); 
	}
	
	private boolean changePassword(Connection conn) {
		
		boolean passwordChanged = false;
		
		SHA1Encryption sha1Encryption = new SHA1Encryption();
		boolean isOldPassword = dbm.checkUserPassword(conn, MainController.loggedUserId, sha1Encryption.encryptPassword(passwordOldTft.getText()));
		
		if (!isOldPassword) {
			DialogFX alertOk = new DialogFX(Type.ERROR);
			alertOk.setTitleText("Wrong password");
			alertOk.setMessage("You have entered wrong password!");
			alertOk.showDialog();
			return false;
		}
		
		if (passwordNewTft.getText().equals(passwordConfirmTft.getText())) {
			dbm.updatePassword(conn, MainController.loggedUserId, sha1Encryption.encryptPassword(passwordConfirmTft.getText()));
		} else {
			DialogFX alertOk = new DialogFX(Type.ERROR);
			alertOk.setTitleText("Wrong password");
			alertOk.setMessage("New password and confirmation password has to be the same!");
			alertOk.showDialog();
			return false;
		}
		
		return true;
		
		
	}
	
	public void saveAccInfo(){
		Connection conn = jdbcConnect.connectToUserDb();;
		
		if (changePassCheckBox.isSelected()) {
			if (!changePassword(conn)) {
				return;
			}
		}
		
		Integer userID = MainController.loggedUserId;

		
		Integer backgroundId = 0; 
		Integer intervalInSeconds = 0;
		String colorAired = null;
		
		if (comboColour.getSelectionModel() != null && comboColour.getSelectionModel().getSelectedIndex() == 0) {
			colorAired = null;
		} else {
			colorAired = comboColour.getSelectionModel().getSelectedItem();
		}
		
		if (comboChangeInterval.getSelectionModel() != null) {
			if ( comboChangeInterval.getSelectionModel().getSelectedIndex() == 0) {
				intervalInSeconds = 30;
			} else if (comboChangeInterval.getSelectionModel().getSelectedIndex() == -1) {
				intervalInSeconds = 0;
			} else {
				intervalInSeconds = Integer.parseInt(comboChangeInterval.getSelectionModel().getSelectedItem().split(" min")[0]) * 60;
			}
		} else {
			intervalInSeconds = 0;
		}
		
		if (comboBackgroundCover.getSelectionModel().getSelectedIndex() <= 1) {
			backgroundId = comboBackgroundCover.getSelectionModel().getSelectedIndex();
		} else {
			backgroundId = dbm.getShowIdFromName(conn, comboBackgroundCover.getSelectionModel().getSelectedItem());
		}
		
		accountInfo.setChangeInterval(intervalInSeconds);
		accountInfo.setBackgroundCover(backgroundId);
		accountInfo.setAiredColor(colorAired);
		
		
		dbm.upsertAccChanges(conn, userID, backgroundId,intervalInSeconds,colorAired);
		
		
		
		jdbcConnect.closeConnectionToDatabase(conn);
		closeStage();
	}
	
	public void closeStage(){
		stage.close();
	}

	public void initData(Object accountInfo) {
		// TODO Auto-generated method stub
		
	}

	public void setAccInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
		
		fillAccountForm();
		
	}
	
}
