package hr.miran.seriesapp.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TvShowsController {

	public TvShowsController() {
		
	}
	
	@FXML
	private TextField tvShowTextfield;
	@FXML
	private TableView<SeriesOld> showsTable; 
	@FXML
	private TableColumn<SeriesOld, String> showNameColumn;
	@FXML
	private TableColumn<SeriesOld, String> ratingColumn;
	@FXML
	private TableColumn<SeriesOld, String> recomCountColumn;
	@FXML
	private TableColumn<SeriesOld, Integer> seasonColumn;
//	@FXML
//	private TableColumn<SeriesOld, Integer> episodeColumn;
	@FXML
	private TableColumn<SeriesOld, String> statusColumn;
	@FXML
	private TableColumn<SeriesOld, String> dateColumn;
	@FXML
	private TableColumn<SeriesOld, String> watchedColumn;
	private BufferedReader in;
	private ArrayList<String> taggedShowsList;
	
	@FXML
	private ComboBox<String> comboGenre;
	@FXML
	private ComboBox<String> comboCountry;
	@FXML
	private ComboBox<String> comboFilterBy;
	@FXML
	private TextField yearFrom;
	@FXML
	private TextField yearTo;
	private BorderPane root;
//	private Scene scene;
	
	private static DbModule dbm =  new DbModule();
	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	
	@FXML
	public void initialize(){
		showNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("showName"));
		ratingColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("rating"));
		recomCountColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("recomCount"));
		seasonColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("seasonNum"));
//		episodeColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("episodeNum"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("status"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("episodeDate"));
		watchedColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("watchedStatus"));
		
		recomCountColumn.setText("");
		Label recomLabel = new Label();
		Image image = new Image(getClass().getResourceAsStream("recommended.png"));
		recomLabel.setGraphic(new ImageView(image));
		recomLabel.setTooltip(new Tooltip("Number of times recommended by users"));
	    recomCountColumn.setGraphic(recomLabel);
	    
	    ratingColumn.setText("");
	    Label ratingLabel = new Label();
		Image imageRating = new Image(getClass().getResourceAsStream("star_view.png"));
		ratingLabel.setGraphic(new ImageView(imageRating));
		ratingLabel.setTooltip(new Tooltip("Show Rating"));
		ratingColumn.setGraphic(ratingLabel);

	}
	
	void setBorderPane(BorderPane root) {
		this.root = root;
		
	  }
	
	public void initData(){
		
		fillFilterByCombo();
		try {
			fillCountryComboBox();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			showTvShows();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void fillFilterByCombo(){
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ObservableList<String> seriesObservableList = FXCollections.observableArrayList(dbm.getGenreList(conn)); 
		
		comboFilterBy.getItems().add("None");
		comboFilterBy.getItems().add("Day of week");
		comboFilterBy.getItems().add("Genre");
		comboFilterBy.getItems().add("Recommended");
						
		comboFilterBy.setPromptText("None");
		
		comboFilterBy.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				if (comboFilterBy.getSelectionModel().getSelectedIndex() == 0) {
					comboGenre.setDisable(true);
				} else if (comboFilterBy.getSelectionModel().getSelectedIndex() == 1){
					fillDayOfWeekComboBox();
					comboGenre.setDisable(false);
				} else if (comboFilterBy.getSelectionModel().getSelectedIndex() == 2){
					try {
						fillGenreComboBox();
						comboGenre.setDisable(false);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				} else if (comboFilterBy.getSelectionModel().getSelectedIndex() == 3){
					comboGenre.setDisable(true);
				}
			}
		});
		
		jdbcConnect.closeConnectionToDatabase(conn);
		
	}
	
	public void fillGenreComboBox() throws SQLException { 
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ObservableList<String> seriesObservableList = FXCollections.observableArrayList(dbm.getGenreList(conn));
//		ObservableList<String> genreObservableList = FXCollections.observableArrayList(dbm.getGenreList(conn));
		
		comboGenre.getItems().clear();
		comboGenre.getItems().addAll(seriesObservableList);
		comboGenre.getSelectionModel().select(0);
							
//		jdbcConnect.closeConnectionToDatabase(conn); 
		conn.close();
	}
	
	public void fillCountryComboBox() throws SQLException { 
		
			Connection conn = jdbcConnect.connectToUserDb();;
			
			ObservableList<String> countryList = FXCollections.observableArrayList(dbm.getCountryList(conn));
			
			comboCountry.getItems().clear();
			comboCountry.getItems().addAll(countryList);
			comboCountry.getSelectionModel().select(0);
			
			jdbcConnect.closeConnectionToDatabase(conn);
			
	}
	
	public void fillDayOfWeekComboBox() { 
		
		comboGenre.getItems().clear();
		comboGenre.getItems().add("Monday");
		comboGenre.getItems().add("Tuesday");
		comboGenre.getItems().add("Wednesday");
		comboGenre.getItems().add("Thursday");
		comboGenre.getItems().add("Friday");
		comboGenre.getItems().add("Saturday");
		comboGenre.getItems().add("Sunday");
		
		comboGenre.getSelectionModel().select(0);
				 
	}
	
	public void showTvShows() throws SQLException { 
		
		
		
		if (isValidYear(yearFrom.getText()) && isValidYear(yearTo.getText())) {
//		
			root.getScene().setCursor(Cursor.WAIT);
			Task<Void> task = new Task<Void>() {
			    @Override
			    public Void call() {
			    	
			    	try {
			    		List<SeriesOld> seriesList = getSeries(); 
			    		ObservableList<SeriesOld> seriesObservableList = FXCollections.observableArrayList(seriesList); 
			    		showsTable.setItems(seriesObservableList); 
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			        return null ;
			    }
			};
			task.setOnSucceeded(e -> root.getScene().setCursor(Cursor.DEFAULT));
			new Thread(task).start();

		} else {
			DialogFX alertOk = new DialogFX(Type.ERROR);
			alertOk.setTitleText("Wrong year format!");
			alertOk.setMessage("Year should consist of 4 numeric characters.");
			alertOk.showDialog();
		}
	}
	
	
	public ArrayList<SeriesOld> getSeries() throws SQLException {
		Connection conn = jdbcConnect.connectToUserDb();;
		ArrayList<SeriesOld> seriesList = new ArrayList<SeriesOld>();
		String taggedShowsString = "(";

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		
		String queryString = "SELECT a.*,b.*,c.*,max(e.season) as season, FORMAT(count(d.rcm_showid)/max(e.season),0) as recomCount "
				+ "FROM tvseries a "
				+ "LEFT JOIN recomshows d ON (a.id = d.rcm_showid) "
				+ "LEFT JOIN networks b ON (a.network = b.network) "
				+ "LEFT JOIN countries c ON (c.iso3 = b.iso31661) "
				+ "LEFT JOIN tvseasons e ON (a.id = e.seriesid) "
				+ "WHERE rating != 10 AND rating is not null AND season is not null AND season != 0 ";
		if (comboFilterBy.getSelectionModel().getSelectedIndex() == 1) {
			queryString += " AND Airs_DayOfWeek = '" + comboGenre.getSelectionModel().getSelectedItem()+ "' AND Status = 'Continuing' ";
		} else if (comboFilterBy.getSelectionModel().getSelectedIndex() == 2){
			queryString += " AND genre like '%" + comboGenre.getSelectionModel().getSelectedItem() + "%' ";
		} else if (comboFilterBy.getSelectionModel().getSelectedIndex() == 3){ //recommended
			queryString += " AND d.rcm_showid = a.id ";
		} 
		
		if (yearFrom.getText().length() == 4) {
			queryString += " AND Year(firstAired) >= " + yearFrom.getText();
		}
		if (yearTo.getText().length() == 4) {
			queryString += " AND Year(firstAired) <= " + yearTo.getText();
		}
		
		if (comboCountry == null || comboCountry.getSelectionModel().getSelectedIndex() == 0) {
			queryString += " AND (c.shortname = 'United States' OR c.shortname = 'United Kingdom') ";
		}else {
			queryString += " AND c.shortname = '" + comboCountry.getSelectionModel().getSelectedItem() + "' ";
		}
		
		if (comboFilterBy.getSelectionModel().getSelectedIndex() != 3){ 
			queryString += " GROUP BY a.id ORDER BY rating desc LIMIT 20";
		} else {
			//recommended
			queryString += " GROUP BY a.id ORDER BY recomCount desc, rating desc LIMIT 20";			
		}
		
//		System.out.println(queryString);
		ResultSet rs = dbm.getPopularShows(conn,  queryString).executeQuery();
		while (rs.next()) {
			int showID = rs.getInt("id");
			String showName = rs.getString("SeriesName"); 
			String episodeName = ""; 
			int seasonNum = rs.getInt("season"); 
			int episodeNum = 0; 
			String status =  rs.getString("Status");
			String episodeDate = rs.getString("firstaired"); 
			String watchedStatus = "No";
			
			if (dbm.checkIfAlreadyWatching(conn, showID)) {
				watchedStatus = "Yes";
			} 
			
			
//			String rating = rs.getString("Rating");
			String rating = dbm.getImdbRating(conn, showID);
			String recomCount = rs.getString("recomCount");		
			
			SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, null, recomCount);
			seriesList.add(show);
		}
		
		jdbcConnect.closeConnectionToDatabase(conn);
		return seriesList;
		
	}
	
	
	public void addShowToWatchlist() {
		if(showsTable.getSelectionModel().getSelectedItem().getSeasonNum() > 0) {
		
			try { 
				FXMLLoader fxmlLoader = new FXMLLoader(); 
				URL location = StartFromController.class.getResource("/hr/miran/seriesapp/javafx/startFrom.fxml");
				fxmlLoader.setLocation(location); 
				fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory()); 
				Parent root = (Parent)fxmlLoader.load(location.openStream()); 
				StartFromController controller  = (StartFromController)fxmlLoader.getController(); 
				controller.setChosenSeries(showsTable.getSelectionModel().getSelectedItem());
				Stage stage = new Stage(); 
				stage.setTitle("Track Show From... " ); 
				stage.setScene(new Scene(root, 400, 150)); 
				stage.show(); 
				controller.setStage(stage); 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}  
	}
	
	public void showSeriesInfo2() {

		try { 				
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/tvShowAbout.fxml"));
			BorderPane tvShows = (BorderPane) loader.load();
			TvShowAboutController controller = 
					loader.<TvShowAboutController>getController();
			controller.setScene(root.getScene());
			controller.initData(showsTable.getSelectionModel().getSelectedItem().getShowid());
			
			root.setStyle("");
			root.setCenter(tvShows);
		} catch (Exception e) { 
			e.printStackTrace(); 
		}  
	}
	
	private boolean isValidYear(String year) {
		
		try {
			if (year.length() == 0) {
				return true;
			}
			else if (year.length() == 4) {
				Integer.parseInt(year);	
			} else {
				return false;
			}		    
		} catch(Exception e) {
		    return false;
		}
		
		return true;
	}
	
	public void goBack() {
		MainController mainScreen = new MainController(MainController.loggedUser);
		mainScreen.redirectHome(MainController.getStage(), "Home");
		mainScreen.initData();		
	} 	
}
