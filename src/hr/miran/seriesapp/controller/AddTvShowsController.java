package hr.miran.seriesapp.controller;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.entity.TaggedShow;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.thetvdbapi.TheTVDBApi;
import hr.miran.seriesapp.thetvdbapi.TvDbException;
import hr.miran.seriesapp.thetvdbapi.model.Series;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AddTvShowsController {

	public AddTvShowsController() {
		
	}
	
	@FXML
	private TextField tvShowTextfield;
	@FXML
	private TableView<SeriesOld> showsTable; 
	@FXML
	private TableColumn<SeriesOld, String> showNameColumn;
	
	@FXML
	private TableColumn<SeriesOld, Integer> seasonColumn;
	
	@FXML
	private TableColumn<SeriesOld, String> statusColumn;
	@FXML
	private TableColumn<SeriesOld, String> watchingColumn;
	@FXML
	private TableColumn<SeriesOld, String> ratingColumn;
	
	
	@FXML
	private ComboBox<String> comboGenre;
	
	private ArrayList<TaggedShow> taggedShowsList;
	
	private static DbModule dbm =  new DbModule();
	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	private Stage stage;
	private BorderPane root;
	@FXML
	public void initialize() {
		showNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("showName"));
//		episodeNameColumn.setCellValueFactory(new PropertyValueFactory<Series, String>("episodeName"));
		seasonColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("seasonNum"));
//		episodeColumn.setCellValueFactory(new PropertyValueFactory<Series, Integer>("episodeNum"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("status"));
		watchingColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("watchedStatus"));
		ratingColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("rating"));
		
		ratingColumn.setText("");
	    Label ratingLabel = new Label();
		Image imageRating = new Image(getClass().getResourceAsStream("star_view.png"));
		ratingLabel.setGraphic(new ImageView(imageRating));
		ratingLabel.setTooltip(new Tooltip("Show Rating"));
		ratingColumn.setGraphic(ratingLabel);
		
		
		showsTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
			

	

			@Override
			public void handle(MouseEvent event) {
				 
				if (event.getClickCount()>1) { 
					SeriesOld tvShow = showsTable.getSelectionModel().getSelectedItem(); 
					
					if(tvShow.getSeasonNum() > 0) {
						try{
							
							FXMLLoader fxmlLoader = new FXMLLoader(); 
//						URL location = StartFromController.class.getResource("../javafx/probniFXML.fxml");
							URL location = StartFromController.class.getResource("/hr/miran/seriesapp/javafx/startFrom.fxml");
							fxmlLoader.setLocation(location); 
							fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory()); 
							Parent root = (Parent)fxmlLoader.load(location.openStream()); 
							StartFromController controller  = (StartFromController)fxmlLoader.getController(); 
							controller.setChosenSeries(tvShow);
							stage = new Stage(); 
							stage.setTitle("Track Show From... " ); 
							stage.setScene(new Scene(root, 400, 150)); 
							stage.show(); 
							controller.setStage(stage); 
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					} else {
						DialogFX alertOk = new DialogFX(Type.INFO);
						alertOk.setTitleText("Not enough data");
						alertOk.setMessage("Unfortunately  there is no information for this show!");
						alertOk.showDialog();						
					}	
					
				} 
			
				
			}
		});
		
	}
	
	void initData(BorderPane root) {
		this.root = root;
		
	  }
	
	public void fillGenreComboBox() throws SQLException { 
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ObservableList<String> seriesObservableList = FXCollections.observableArrayList(dbm.getGenreList(conn)); 
		
		comboGenre.getItems().add("All");
		comboGenre.getItems().addAll(seriesObservableList);
		
		comboGenre.setPromptText("All");
				
		jdbcConnect.closeConnectionToDatabase(conn);
	}
	
	public void showTvShows() throws SQLException { 
		TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
		
		List<SeriesOld> seriesList = getSeries(); 

		ObservableList<SeriesOld> seriesObservableList = FXCollections.observableArrayList(seriesList); 
		showsTable.setItems(seriesObservableList); 
	}
	
	
	public ArrayList<SeriesOld> getSeries() throws SQLException {
		
		Connection conn = jdbcConnect.connectToUserDb();;
		ArrayList<SeriesOld> seriesList = new ArrayList<SeriesOld>();
		
		
		String showNameTf =  tvShowTextfield.getText().toString().replace("'", "\\'");

		ResultSet rs = dbm.getSearchedShows(conn, showNameTf).executeQuery(); 
		
		
		TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
		
		// search through API
		
		List<Series> listOfSearchedShowsThroughAPI = null;

		try {
			 listOfSearchedShowsThroughAPI = tvdbApi.searchSeries(showNameTf, "en");
			
		} catch (TvDbException e) { 
			e.printStackTrace();
		}
//		search through API end
		
		taggedShowsList = dbm.getTaggedShows(conn);

		while (rs.next()) {
			
			
			
			int showID = rs.getInt("id");
			String watchedStatus = "No";
			
			for (Series series : listOfSearchedShowsThroughAPI) {
				if (Integer.valueOf(series.getId()) == showID) {
					listOfSearchedShowsThroughAPI.remove(series);
					break;
				}
			}
			

			for (TaggedShow taggedShow : taggedShowsList) {
				if (showID == taggedShow.getShowId()) {
					watchedStatus = "Yes";
				}
			}				
			
			
			
			String showName = rs.getString("SeriesName"); 
			String episodeName = "";
			int seasonNum = rs.getInt("numOfSeasons"); 
			int episodeNum = 0; 
			String status = rs.getString("status");
			String episodeDate = "";

			String rating = dbm.getImdbRating(conn, showID);
			
			
			
			SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, null, null);
			seriesList.add(show);
		}
		
		for (Series series : listOfSearchedShowsThroughAPI) {
			
			int showID = Integer.valueOf(series.getId());
			String watchedStatus = "No";
			String showName = series.getSeriesName();
			String episodeName = "";
			int seasonNum = 1; //TODO sredi sezone
			int episodeNum = 0; 
			String status = series.getStatus();
			String episodeDate = "";

			String rating = dbm.getImdbRating(conn, showID);
			
//			System.out.println("Preko getseries: Series Nmae" + series.getSeriesName() + " | " + series.getId());
			
			SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, null, null);
			seriesList.add(show);
		}
		 
		jdbcConnect.closeConnectionToDatabase(conn);
		return seriesList;
		
	}
	
	public void goBack() {
		MainController mainScreen = new MainController(MainController.loggedUser);
		mainScreen.redirectHome(MainController.getStage(), "Home");
		mainScreen.initData();
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
		
}
