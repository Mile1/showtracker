package hr.miran.seriesapp.controller;


import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.entity.TaggedShow;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.thetvdbapi.TheTVDBApi;
import hr.miran.seriesapp.thetvdbapi.TvDbException;
import hr.miran.seriesapp.thetvdbapi.model.Episode;
import hr.miran.seriesapp.thetvdbapi.model.Series;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TvShowAboutController {

	private Integer seriesId; 
	
	public TvShowAboutController() {
		
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
	private TableColumn<SeriesOld, String> episodeNameColumn;
	@FXML
	private TableColumn<SeriesOld, String> watchingColumn;
	@FXML 
	private Hyperlink showNameLabel;
	@FXML
	private Label showTimeLabel;
	@FXML
	private Label showYearsLabel;
	@FXML
	private Label showRankLabel;
	@FXML
	private Label showGenreLabel;
	@FXML
	private Pane showSeriesBanner;
	@FXML
	private Label showOverviewLabel;
	@FXML
	private Label showCastLabel;
	
	private ArrayList<TaggedShow> taggedShowsList;
	
	private static DbModule dbm =  new DbModule();
	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	private Stage stage;
	private TheTVDBApi tvdbApi;
	private Scene scene;
	private String imdbLink = "http://www.imdb.com/title/";
	private Connection conn = jdbcConnect.connectToUserDb();;
	@FXML
	public void initialize(){
//		showNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("showName"));
		episodeNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("episodeName"));
		seasonColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("seasonNum"));
//		episodeColumn.setCellValueFactory(new PropertyValueFactory<Series, Integer>("episodeNum"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("status"));
		watchingColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("watchedStatus"));
//		dateColumn.setCellValueFactory(new PropertyValueFactory<Series, String>("episodeDate"));
	}
	void initData(Integer showId) {
		this.seriesId = showId;
		showSeriesInfo();
		try {
			showTaggedShows();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	private void showSeriesInfo() {
		
		if (tvdbApi == null) {
			tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");			
		}
		
		try {
			Series seriesInfo = tvdbApi.getSeries(seriesId.toString(), "en");
			
			
			imdbLink += seriesInfo.getImdbId() + "/";
			showNameLabel.setText(seriesInfo.getSeriesName());
			showTimeLabel.setText(seriesInfo.getRuntime() + "min");		
			showYearsLabel.setText(seriesInfo.getFirstAired().substring(0, 4) + " - 2016"); //TODO: treba napravit funkciju za Last Aired
			showSeriesBanner.setStyle("-fx-background-image: url('https://thetvdb.com/banners/posters/" + seriesId + "-1.jpg');-fx-background-repeat: no-repeat;-fx-background-size: 190 240;");				
			showRankLabel.setText(dbm.getImdbRating(conn, seriesId));
			showRankLabel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("Icon_Star.png"))));
			
			String genre = "";
			List <String> seriesGenre = seriesInfo.getGenres();
			int count = 0;
			for (String row : seriesGenre) {
				if (count >0) {
					genre += " | ";
				}
				genre += row;
				count++;
			}
			showGenreLabel.setText(genre);
			
			String showCast = "";
			count = 0;
			for (String actor : seriesInfo.getActors()) {
				if (count > 0) {
					if (count > 4) { //max 5 actors will be displayed on info screen 
						break;
					}
					showCast += ", ";
				}
				showCast += actor;
				count++;
			}
			
			showCastLabel.setText(showCast);
			
			String showOverview = "";
			
			count = 0;
			for (String partOfOverview : seriesInfo.getOverview().split(" ")) {
				
				if (count > 56) {
					showOverview += "...";
					break;
				}
				
				if(count % 12 == 0) {
					showOverview += "\n";
				}
				showOverview += partOfOverview + " ";
				count++;
			}
			
			showOverviewLabel.setText(seriesInfo.getOverview());
			showOverviewLabel.setWrapText(true);
			
//			System.out.println(showOverview);
			
		} catch (TvDbException e) {
			e.printStackTrace();
		}		
	}
	
	public void showTaggedShows() throws SQLException { 
		
		scene.setCursor(Cursor.WAIT);
		Task<Void> task = new Task<Void>() {
		    @Override
		    public Void call() {
		    	
		    	try {
		    		List<SeriesOld>  seriesList = getSeries();
					ObservableList<SeriesOld> seriesObservableList = FXCollections.observableArrayList(seriesList); 
					showsTable.setItems(seriesObservableList); 
					showsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		        return null ;
		    }
		};
		task.setOnSucceeded(e -> scene.setCursor(Cursor.DEFAULT));
		new Thread(task).start();
		
		 

	}
	
	public ArrayList<SeriesOld> getSeries() throws SQLException {
		
		if(conn.isClosed()) {
//			System.out.println("Reopenning connection");
			conn = jdbcConnect.connectToUserDb();
		} 
		
		ArrayList<SeriesOld> seriesList = new ArrayList<SeriesOld>();
		ArrayList<TaggedShow> tagedShowList = new ArrayList<TaggedShow>();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
//		System.out.println(dateFormat.format(currentDate));
		
		List<Episode> episodeList = null;
		try {
			episodeList = tvdbApi.getAllEpisodes(seriesId.toString(), "en");
		} catch (TvDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Episode episode : episodeList) {
			Integer showID = seriesId;
			String showName = ""; 
			String episodeName = episode.getEpisodeNumber() + ". " + episode.getEpisodeName(); 
			String rating = episode.getRating();
			int seasonNum = episode.getSeasonNumber();
			int episodeNum = episode.getEpisodeNumber();
			String status = episode.getFirstAired();
			String episodeDate = episode.getFirstAired();
			String watchedStatus = "No";
			episode.getImdbId();
			tagedShowList.addAll(dbm.getWatchedEpisodes(conn, showID));
			for (TaggedShow taggedShow : tagedShowList ) {
				if (taggedShow.getShowSeason().equals(episode.getSeasonNumber()) && taggedShow.getShowEpisode().equals(episode.getEpisodeNumber())) {
					watchedStatus = "Yes";						
				} 
			}
			
			SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, null, null);
			seriesList.add(show);
			
		}
		jdbcConnect.closeConnectionToDatabase(conn);

		return seriesList;
		
	}
	
	public void markEpisodeAsWatched() {
//		System.out.println("MARK EPISODE");
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ObservableList<SeriesOld> tvShowList = showsTable.getSelectionModel().getSelectedItems();
		
		for (SeriesOld tvShow: tvShowList) {
			if (dbm.checkIfEpisodeExistsInDb(conn, tvShow)) {
				dbm.updateTaggedShowWatchStatus(conn, MainController.loggedUserId, tvShow, 1);
			}else {
				dbm.insertIntoTaggedShow(conn,MainController.loggedUserId, tvShow, true);				
			}
		}

		
		jdbcConnect.closeConnectionToDatabase(conn);
		
		try {
			showTaggedShows();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void unmarkEpisodeAsWatched() {
//		System.out.println("UNMARK EPISODE");
		Connection conn = jdbcConnect.connectToUserDb();;
		
		ObservableList<SeriesOld> tvShowList = showsTable.getSelectionModel().getSelectedItems();
		
		for (SeriesOld tvShow: tvShowList) {
			if (dbm.checkIfEpisodeExistsInDb(conn, tvShow)) {
				dbm.updateTaggedShowWatchStatus(conn, MainController.loggedUserId, tvShow, 0);
			}
		}

		
		jdbcConnect.closeConnectionToDatabase(conn);
		
		try {
			showTaggedShows();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void goToIMDbPage() {
		
        try {
        	URI u = new URI(imdbLink);
			java.awt.Desktop.getDesktop().browse(u);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void goBack() {
		MainController mainScreen = new MainController(MainController.loggedUser);
		mainScreen.redirectHome(MainController.getStage(), "Home");
		mainScreen.initData();
		
	} 

	private void setSeriesId(Integer seriesId) {
		this.seriesId = seriesId;
	}
	public void setScene(Scene scene) {
		this.scene = scene;
		
	}
	
}
