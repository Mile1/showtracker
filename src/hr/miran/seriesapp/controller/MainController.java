package hr.miran.seriesapp.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.entity.AccountInfo;
import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.entity.TaggedShow;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.thetvdbapi.TheTVDBApi;
import hr.miran.seriesapp.thetvdbapi.TvDbException;
import hr.miran.seriesapp.thetvdbapi.model.Episode;
import hr.miran.seriesapp.thetvdbapi.model.Series;
import hr.miran.seriesapp.thetvdbapi.model.SeriesUpdate;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MainController {
	
	@FXML
	private TableView<SeriesOld> showsTable; 
	@FXML
	private TableColumn<SeriesOld, String> showNameColumn;
	@FXML
	private TableColumn<SeriesOld, String> episodeNameColumn;
	@FXML
	private TableColumn<SeriesOld, Integer> seasonColumn;
	@FXML
	private TableColumn<SeriesOld, Integer> episodeColumn;
	@FXML
	private TableColumn<SeriesOld, String> statusColumn;
	@FXML
	private TableColumn<SeriesOld, String> dateColumn;
	@FXML
	private TableColumn<SeriesOld, String> watchedColumn;
	@FXML
	private TableColumn<SeriesOld,Image> recommendColumn;
	
	private static JDBCConnect jdbcConnect =  new JDBCConnect();
	private static DbModule dbm =  new DbModule();
	public static BorderPane root;
	private static Stage stage;
	private BorderPane rootPane;
	private Scene scene;
	private List<SeriesOld> seriesList;
	public static String loggedUser;
	public static Integer loggedUserId;
	private Integer counterTimer = 0;
	private Timer timer; 
	private Connection conn;
	
	private AccountInfo accountInfo;
	private boolean accInfoExists;
	
	public MainController(String loggedUser) {
        
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/main.fxml"));
        fxmlLoader.setController(this);
        this.loggedUser = loggedUser;
        this.conn = jdbcConnect.connectToUserDb();;
        this.loggedUserId = dbm.getUserId(conn, loggedUser);

        try {
        	rootPane = (BorderPane) fxmlLoader.load();
        	root = rootPane;

            scene = new Scene(root, 650,600);
            

            String image = getClass().getResource("showTracker_background.jpg").toExternalForm();
            root.setStyle("-fx-background-image: url('" + image + "');-fx-background-repeat: stretch;-fx-background-size: 650 600;");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
			ex.printStackTrace();
		}
    }

	@FXML
	public void initialize(){
		
		showsTable.setPlaceholder(new Label("Searching for shows, please wait a moment."));
		showNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("showName"));
		episodeNameColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("episodeName"));
		seasonColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("seasonNum"));
		episodeColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, Integer>("episodeNum"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("status"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<SeriesOld, String>("episodeDate"));
		
		if (dbm.checkIfAcInfoExists(conn, loggedUserId)) {
			accInfoExists = true;
		}
		
		dateColumn.setCellFactory(new Callback<TableColumn<SeriesOld,String>, TableCell<SeriesOld, String>>() {

			@Override
			public TableCell<SeriesOld, String> call(TableColumn<SeriesOld, String> param) {
				
				return new TableCell<SeriesOld,String>() {
					
					 @Override
			            protected void updateItem(String date, boolean empty) {
			                super.updateItem(date, empty);
			                
			                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			        		Date currentDate = new Date();
//			        		Date episodeDate = null;
			        		
							try {
								if (accInfoExists) {
									if (date != null && !date.contains("N/A") && !date.equals("")) {
										setText(date);
										Date episodeDate = dateFormat.parse(date);
															        			 
							        		if (currentDate.after(new Date(episodeDate.getTime() + (1000 * 60 * 60 * 24)))) {
							        			TableRow currentRow = getTableRow();
							        			currentRow.setStyle("-fx-background-color: " + accountInfo.getAiredColor() + ";");
											}  else {
												setText(date);
												TableRow currentRow = getTableRow();
												currentRow.setStyle(null);
											}
						        		
									} else {
										setText(date);
										TableRow currentRow = getTableRow();
										currentRow.setStyle(null);
									}
								
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
							}
			            }
				};
				
			}
			
		});
		recommendColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<Image>(cellData.getValue().getRecommendedImage()));
		
		recommendColumn.setText("");
		Label recomLabel = new Label("Recom");
		recomLabel.setTooltip(new Tooltip("Recommend show to other users"));
		recommendColumn.setGraphic(recomLabel);
		
		recommendColumn.setCellFactory(new Callback<TableColumn<SeriesOld,Image>, TableCell<SeriesOld,Image>>() {
			
			@Override
			public TableCell<SeriesOld, Image> call(TableColumn<SeriesOld, Image> param) {
				
				 final ImageView imageview = new ImageView();
		         imageview.setFitHeight(20);
		         
		         TableCell<SeriesOld, Image> cell = new TableCell<SeriesOld, Image>() {
		        	 @Override
		        	 public void updateItem(Image icon, boolean empty) {
		        		 imageview.setImage(icon);
		        	 }
		         }; 
		         
		        cell.setGraphic(imageview); 
				return cell;
			}
		});
		

	}
	
	public void initData() {
		try {
			initializeAcount();
			
			if (!dbm.getTaggedShows(conn).isEmpty()) {
				showTaggedShows();				
			} else {
	    		showsTable.setPlaceholder(new Label("No shows on the list, click on 'Add Show' button"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeAcount() throws SQLException { 
		
		ArrayList<Object> accInfoList = new ArrayList<>();
		
		accInfoList = dbm.getAccInfo(conn, loggedUserId);
		
		accountInfo = new AccountInfo(loggedUserId);
		accountInfo.setBackgroundCover((Integer) accInfoList.get(0));
		accountInfo.setChangeInterval((Integer) accInfoList.get(1));
		if (accInfoList.get(2) != null) {			
			accountInfo.setAiredColor(accInfoList.get(2).toString());
		} else {
			accountInfo.setAiredColor("lime");
		}
		
		
	}
	
	private void showTaggedShows() throws SQLException {
		scene.setCursor(Cursor.WAIT);
		Task<Void> task = new Task<Void>() {
		    @Override
		    public Void call() {
		    	
		    	try {
		    		seriesList = getSeries(); 
		    		ObservableList<SeriesOld> seriesObservableList = FXCollections.observableArrayList(seriesList); 						
					showsTable.setItems(seriesObservableList);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
		        return null ;
		    }
		};
		task.setOnSucceeded(e -> scene.setCursor(Cursor.DEFAULT));
		new Thread(task).start();
		
		if (accountInfo.getBackgroundCover() == 1) { //Interchangable
			ArrayList<Integer> taggedShowIdList = dbm.getTaggedShowsList(conn);
			
			Integer timeInterval = 0;
			if (taggedShowIdList.size() > 0) {
				if (timer == null) {
					timer = new Timer();
					if (accountInfo.getChangeInterval() == null) {
						timeInterval = 30000;
					} else {
						timeInterval = accountInfo.getChangeInterval() * 1000;
					}
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							if (counterTimer < taggedShowIdList.size()) {
								try {
									root.setStyle("-fx-background-image: url('https://thetvdb.com/banners/posters/" + taggedShowIdList.get(counterTimer++) + "-1.jpg');-fx-background-repeat: stretch;-fx-background-size: 650 600;");												
								} catch (ConcurrentModificationException e) {
		//							System.out.println(e.getMessage()); 
									counterTimer++;
								} 
							} else {
								counterTimer = 0;						
							}
							
						}
					}, 0, timeInterval);		
				}
			}
		
		} else if (accountInfo.getBackgroundCover() != 0) { //0 = Default
			root.setStyle("-fx-background-image: url('https://thetvdb.com/banners/posters/" + accountInfo.getBackgroundCover() + "-1.jpg');-fx-background-repeat: stretch;-fx-background-size: 650 600;");
		}

	}
	
	private void refreshTableViewRecomm(Connection conn) {
		
		if (seriesList!=null && seriesList.size() > 0) {
			for (SeriesOld show : seriesList) {
				Image recommended = null;
				if (dbm.checkIfAlreadyRecommended(conn, show.getShowid())) {
					recommended = new Image(getClass().getResourceAsStream("recommended.png"));
				} else {
					recommended = new Image(getClass().getResourceAsStream("recommended_bw.png"));
				} 
				
				show.setRecommendedImage(recommended);
			}
			
			ObservableList<SeriesOld> seriesObservableList = FXCollections.observableArrayList(seriesList);
			
			showsTable.getItems().clear();
			showsTable.setItems(seriesObservableList);	
		}
		 
	}
	
	private ArrayList<SeriesOld> getSeriesOld() throws SQLException {
		
		Connection conn = jdbcConnect.connectToUserDb();;
		ArrayList<SeriesOld> seriesList = new ArrayList<SeriesOld>();
		ArrayList<TaggedShow> tagedShowList = new ArrayList<TaggedShow>();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();

		tagedShowList.addAll(dbm.getTaggedShows(conn));
		try {

			TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
			
			for (TaggedShow taggedShow : tagedShowList ) {
				
//				System.out.println("countPass " + countPass++ + " " + new Date());
			
				Series currentShow = dbm.getSeriesByIdFromDB(conn , taggedShow.getShowId().toString());
				
				if (currentShow == null) {
					currentShow =  tvdbApi.getSeries(taggedShow.getShowId().toString(), "en"); 
				}
				
				Episode nextEpisode = getNextPossibleUnwatchedEpisode(tvdbApi, taggedShow.getShowId().toString(), taggedShow.getShowSeason(), (taggedShow.getShowEpisode()), "en", conn);
				
				if (nextEpisode == null) {
					
					Integer showID = taggedShow.getShowId();
					String showName = currentShow.getSeriesName(); 
					String episodeName = "N/A"; 
					String rating = currentShow.getRating();
					int seasonNum = 0;
					int episodeNum = 0;
					String status = "N/A";
					String episodeDate = "N/A";
					String watchedStatus = "";
					
					if (currentShow.getStatus() != null) {
						status = currentShow.getStatus();
					} 
					
					Image recommended = null;
					if (dbm.checkIfAlreadyRecommended(conn, showID)) {
						recommended = new Image(getClass().getResourceAsStream("recommended.png"));
					} else {
						recommended = new Image(getClass().getResourceAsStream("recommended_bw.png"));
					} 
					
					SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, recommended, null);
					seriesList.add(show);
					
					continue;
				} else {
					
					Integer showID = taggedShow.getShowId();
					String showName = currentShow.getSeriesName(); 
					String episodeName = nextEpisode.getEpisodeName(); 
					String rating = currentShow.getRating();
					int seasonNum = nextEpisode.getSeasonNumber();
					int episodeNum = nextEpisode.getEpisodeNumber();
					String status = "N/A";
					String episodeDate = nextEpisode.getFirstAired();
					String watchedStatus = "";
					
					
					
					Image recommended = null;
					if (dbm.checkIfAlreadyRecommended(conn, showID)) {
						recommended = new Image(getClass().getResourceAsStream("recommended.png"));
					} else {
						recommended = new Image(getClass().getResourceAsStream("recommended_bw.png"));
					} 
					
					if (dbm.getEpisodeWatchStatus(conn, showID.toString(), seasonNum, episodeNum) == 1) {
						watchedStatus = "Yes";						
					} else {
						watchedStatus = "No";	
					}
					
					
					try {
						if (episodeDate != null && !episodeDate.equals("")) {
							Date episodeAirDate = dateFormat.parse(episodeDate);
							if (currentDate.compareTo(episodeAirDate) <=0) {
								status = "Coming on "+ currentShow.getNetwork();
							} else {
								status = "Aired on " + currentShow.getNetwork();
							}						
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception ee) {
						ee.printStackTrace();
					}
					
					
					SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, recommended, null);
					seriesList.add(show);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		conn.close();

		return seriesList;
		
	}
	
	private ArrayList<SeriesOld> getSeries() throws SQLException {
		
		Connection conn = jdbcConnect.connectToUserDb();;
		ArrayList<SeriesOld> seriesList = new ArrayList<SeriesOld>();
		ArrayList<TaggedShow> tagedShowList = new ArrayList<TaggedShow>();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
//		System.out.println(dateFormat.format(currentDate));
		
		tagedShowList.addAll(dbm.getTaggedShows(conn));
		try {

			TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
			
			int countPass =0;
			for (Series currentShow : dbm.getSeriesByIdListFromDB(conn, tagedShowList) ){
				
//				System.out.println("countPass " + countPass++ + " " + new Date());
			
				if (currentShow == null) {
//					System.out.println("API currentshow " +   currentShow.getSeriesId().toString() );
					currentShow =  tvdbApi.getSeries(currentShow.getSeriesId().toString(), "en"); 
				}
				
				Episode nextEpisode = null;
				
				for (TaggedShow taggedShow : tagedShowList) {
					if (taggedShow.getShowId().toString().equals(currentShow.getSeriesId().toString())) {
						nextEpisode = getNextPossibleUnwatchedEpisode(tvdbApi, currentShow.getSeriesId().toString(), taggedShow.getShowSeason(), (taggedShow.getShowEpisode()), "en", conn);
						break;
					} 
				}
				
				if (nextEpisode == null) {
					
					Integer showID = Integer.valueOf(currentShow.getSeriesId());
					String showName = currentShow.getSeriesName(); 
					String episodeName = "N/A"; 
					String rating = currentShow.getRating();
					int seasonNum = 0;
					int episodeNum = 0;
					String status = "N/A";
					String episodeDate = "N/A";
					String watchedStatus = "";
					
					if (currentShow.getStatus() != null) {
						status = currentShow.getStatus();
					} 
					
					Image recommended = null;
					if (dbm.checkIfAlreadyRecommended(conn, showID)) {
						recommended = new Image(getClass().getResourceAsStream("recommended.png"));
					} else {
						recommended = new Image(getClass().getResourceAsStream("recommended_bw.png"));
					} 
					
					SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, recommended, null);
					seriesList.add(show);
					
					continue;
				} else {
					
					Integer showID = Integer.valueOf(currentShow.getSeriesId());
					String showName = currentShow.getSeriesName(); 
					String episodeName = nextEpisode.getEpisodeName(); 
					String rating = currentShow.getRating();
					int seasonNum = nextEpisode.getSeasonNumber();
					int episodeNum = nextEpisode.getEpisodeNumber();
					String status = "N/A";
					String episodeDate = nextEpisode.getFirstAired();
					String watchedStatus = "";
					
					
					
					Image recommended = null;
					if (dbm.checkIfAlreadyRecommended(conn, showID)) {
						recommended = new Image(getClass().getResourceAsStream("recommended.png"));
					} else {
						recommended = new Image(getClass().getResourceAsStream("recommended_bw.png"));
					} 
					
					if (dbm.getEpisodeWatchStatus(conn, showID.toString(), seasonNum, episodeNum) == 1) {
						watchedStatus = "Yes";						
					} else {
						watchedStatus = "No";	
					}
					
					
					try {
						if (episodeDate != null && !episodeDate.equals("")) {
							Date episodeAirDate = dateFormat.parse(episodeDate);
							if (currentDate.compareTo(episodeAirDate) <=0) {
								status = "Coming on "+ currentShow.getNetwork();
							} else {
								status = "Aired on " + currentShow.getNetwork();
							}						
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception ee) {
//						System.out.println("episodeDate" + episodeDate);
						ee.printStackTrace();
					}
					
					
					SeriesOld show = new SeriesOld(showID, showName, episodeName, seasonNum, episodeNum, status, episodeDate, watchedStatus, rating, recommended, null);
					seriesList.add(show);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		conn.close();
		
//		jdbcConnect.closeConnectionToDatabase(conn); 
		return seriesList;
		
	}

	public void markEpisodeAsWatched() {	
		
		if (showsTable.getSelectionModel().getSelectedItem() != null) {
		
//			System.out.println("MARK EPISODE");
			Connection conn = jdbcConnect.connectToUserDb();;
			
			try {
				if(conn.isClosed()) {
//					System.out.println("Reopenning connection");
					conn = jdbcConnect.connectToUserDb();
				} 
				
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			Date currentDate = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			SeriesOld tvShow = showsTable.getSelectionModel().getSelectedItem();
			
			Date episodeAirDate = null;
			try {
				episodeAirDate = dateFormat.parse(tvShow.getEpisodeDate());
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
	//			e1.printStackTrace();
			}
			
			if (episodeAirDate != null && !episodeAirDate.equals("")) {
				if (currentDate.compareTo(episodeAirDate) <=0) {
					DialogFX alertOk = new DialogFX(Type.INFO);
					alertOk.setTitleText("Episode not aired yet");
					alertOk.setMessage("Unable to mark this show as watched because this episode has not been aired yet!");
					alertOk.showDialog();
					return;
				}			
			} 
			
			if (dbm.checkIfEpisodeExistsInDb(conn, tvShow)) {
				dbm.updateTaggedShowWatchStatus(conn, loggedUserId, tvShow, 1);
			}else {
				dbm.insertIntoTaggedShow(conn,loggedUserId, tvShow, true);				
			}
			try {
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				showTaggedShows();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	}
	
	public void unmarkEpisodeAsWatched() {
		
		if (showsTable.getSelectionModel().getSelectedItem() != null) {
		
			SeriesOld tvShow = showsTable.getSelectionModel().getSelectedItem();
			
			DialogFX choiceAddShow = new DialogFX(Type.QUESTION);
			choiceAddShow.setTitleText("Remove show");
			choiceAddShow.setMessage("Are you sure you want to remove this show from tracking list?");
			if(choiceAddShow.showDialog() == 0) {
				dbm.deleteFromTaggedShows(conn, tvShow); 
				
				try {
					showTaggedShows();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
//		jdbcConnect.closeConnectionToDatabase(conn); 

	}
	
	public void showMostPopularTvShows() {
		
		try {
			turnOffTimer(timer);			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/tvShows.fxml"));
			
			BorderPane popularTvShows = (BorderPane) loader.load();
			TvShowsController controller = 
					loader.<TvShowsController>getController();
//			controller.setScene(getStage().getScene());
			controller.setBorderPane(root);
			controller.initData();
			
			root.setStyle("");
			setCenterPane(popularTvShows);
			
			
		} catch (Exception e) { 
			e.printStackTrace(); 
		}  
	}
	
	public void showSeriesInfo() {
		
		if (showsTable.getSelectionModel().getSelectedItem() != null) {
		
			try { 
				turnOffTimer(timer);
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/tvShowAbout.fxml"));
				
				BorderPane tvShows = (BorderPane) loader.load();
				TvShowAboutController controller = 
						loader.<TvShowAboutController>getController();
				controller.setScene(getStage().getScene());
				controller.initData(showsTable.getSelectionModel().getSelectedItem().getShowid());

				root.setStyle("");
				setCenterPane(tvShows);
				
			} catch (Exception e) { 
				e.printStackTrace(); 
			}  
		}
	}
	
	
	public void addTvShows() { 
		
		try { 
			turnOffTimer(timer);
//			BorderPane tvShows = (BorderPane) FXMLLoader.load(getClass().getResource("/hr/miran/seriesapp/javafx/addTvShows.fxml")); 
//			setCenterPane(tvShows); 
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/addTvShows.fxml"));
			
			BorderPane tvShows = (BorderPane) loader.load();
			AddTvShowsController controller = 
					loader.<AddTvShowsController>getController();
			controller.initData(root);
			
			root.setStyle("");
			setCenterPane(tvShows);
			
			
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	}
	
	public void redirectHome(Stage stage, String name) {
		
		this.stage = stage;
		stage.setResizable(false);
        stage.setTitle("Show Tracker");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("showTrackLogo.png")));
        stage.show();
    }
	
	public static Stage getStage() {
		return stage;
	}



	



	public static void setCenterPane(BorderPane centerPane) { 
		root.setCenter(centerPane); 
	}
	
	private Episode getNextPossibleUnwatchedEpisode(TheTVDBApi tvdbApi, String showId, Integer seasonNum, Integer episodeNum, String language, Connection conn) {
		
		Episode nextEpisode = null;
		boolean searchNextSeason = false;

		
		if (dbm.getShowWatchedStatus(conn, showId, seasonNum, episodeNum) == 1) {
			episodeNum = episodeNum +1;		
		} 
		
		nextEpisode = dbm.getNextEpisodeFromDB(conn, showId, seasonNum, episodeNum);
		
		
		if (nextEpisode == null) { //ako nema u bazi, gledaj api i pisi u bazu
//			System.out.println("API nextEpisode " +   showId);
			try {
				nextEpisode = tvdbApi.getEpisode(showId, seasonNum, episodeNum, language);					
			} catch (TvDbException tvdbe) {
				tvdbe.printStackTrace();
				searchNextSeason = true;
			}
			if (searchNextSeason) {
				List<Episode> episodeList;
				try {
					
					episodeList = tvdbApi.getAllEpisodes(showId, language); //sve epizode
					for (Episode epis : episodeList) {
						if (epis.getSeasonNumber() == (seasonNum+1)) {
	//						nextEpisode = tvdbApi.getEpisode(epis.getSeriesId().toString(), epis.getSeasonNumber(), epis.getEpisodeNumber(), language);
							nextEpisode = epis;
							break;
						}
					}
				} catch (TvDbException e) {
//					e.printStackTrace();
				}  
				
			}
			
			if (nextEpisode != null && !nextEpisode.getFirstAired().equals("")) {
				dbm.insertNextEpisodeInDB(conn, nextEpisode);
			}
		}
		
		return nextEpisode;
	}
	
	public void updateDatabaseRatings() throws TvDbException {
		
		ArrayList<String> showList = new ArrayList<>();
		TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
//		Connection conn2 = jdbcConnect.connectToUserDb();
		Series seriesObject = new Series();
		
		showList.addAll(dbm.getListOfShows(conn));
//		System.out.println("Number of shows = " + showList.size());
		int countShows = 1;
//		for (String showId : showList) {
		for (int i =0; i < showList.size(); i++) {	
			
			try {
				seriesObject = tvdbApi.getSeries(showList.get(i), "en");
			} catch (Exception e) {
				System.err.println("Tough Luck");
			}
			
			countShows++;
			
			if (seriesObject.getSeriesName().length() !=0) {	
				dbm.updateRatingOnTvSeriesTable(conn, seriesObject);
			}
		}
	} 
	
	public void updateDatabaseWithNewSeries() throws TvDbException {
		
		ArrayList<String> newShows =  new ArrayList<>();
		TreeSet<String> removeTheseShowsFromList =  new TreeSet<>();
		ArrayList<String> existingShows = new ArrayList<>();
		existingShows.addAll(dbm.getListOfShows(conn)); 
		TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
		ArrayList<SeriesUpdate> seriesUpdates = new ArrayList<>();
		
		try {
			seriesUpdates.addAll(tvdbApi.getWeeklyUpdates().getSeriesUpdates());
		} catch (TvDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (int i = 0; i<seriesUpdates.size(); i++) {
//			newShows.add(seriesUpdates.get(i).getId());	
//		}
		
//		System.out.println("Broj Serija na pocetku: " + seriesUpdates.size());
//		for (int i = 0; i<seriesUpdates.size(); i++) {
		
		boolean isNewShow = false;
		for (SeriesUpdate newShow : seriesUpdates)	{
			isNewShow = true;
			
			for (String oldShow : existingShows) {			
				if (newShow.getId().equals(oldShow)) {
					isNewShow = false;
					break;
				}					
			}
			if (isNewShow) {
				newShows.add(newShow.getId());				
			}
			
		}
		
		int count = 0;
		ArrayList<String> updateSeasonsForIds = new ArrayList<>();
		updateSeasonsForIds = dbm.getSeasonUpdatesForShows(conn);

		for (String showId : updateSeasonsForIds) {
			count++;
			try {
				dbm.updateTvSeasons(conn, tvdbApi.getAllEpisodes(showId, "en"));
					
			} catch (TvDbException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void about() {
		try {
			
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/about.fxml"));
			
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory()); 
			Parent root = (Parent)fxmlLoader.load(fxmlLoader.getLocation().openStream()); 
			AboutController controller  = (AboutController)fxmlLoader.getController(); 
			Stage stageAbout = new Stage(); 
			stageAbout.getIcons().add(new Image(getClass().getResourceAsStream("showTrackLogo.png")));
			stageAbout.setTitle("About Show Tracker..." ); 
			stageAbout.setScene(new Scene(root, 400, 213)); 
			stageAbout.initModality(Modality.APPLICATION_MODAL);
			stageAbout.show();
			stageAbout.setResizable(false);
			controller.setStage(stageAbout); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	
	public void account() {
		try {
			
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hr/miran/seriesapp/javafx/account.fxml"));

			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory()); 
			Parent root = (Parent)fxmlLoader.load(fxmlLoader.getLocation().openStream());

			AccountController controller  = (AccountController)fxmlLoader.getController(); 
			Stage stageAccount = new Stage(); 
			stageAccount.getIcons().add(new Image(getClass().getResourceAsStream("showTrackLogo.png")));
			stageAccount.setTitle("My Account" ); 
			stageAccount.setScene(new Scene(root, 332, 440)); 
			stageAccount.initModality(Modality.APPLICATION_MODAL);
			stageAccount.show();
			stageAccount.setResizable(false);
			controller.setAccInfo(accountInfo);
			controller.setStage(stageAccount); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	
	public void recommendShow() {
		if (showsTable.getSelectionModel().getSelectedItem() != null) {
		
			SeriesOld tvShow = showsTable.getSelectionModel().getSelectedItem();
			
			if (dbm.checkIfShowExistsInDb(conn, tvShow)) {
				if (!dbm.checkIfAlreadyRecommended(conn, tvShow.getShowid())) {
					dbm.recommendThisShow(conn, loggedUserId, tvShow, "");				
				}
			}
			
			refreshTableViewRecomm(conn);

		}
	} 
	
	public void removeRecommendShow() {
		if (showsTable.getSelectionModel().getSelectedItem() != null) {
			
			SeriesOld tvShow = showsTable.getSelectionModel().getSelectedItem();
			
			if (dbm.checkIfShowExistsInDb(conn, tvShow)) {
				dbm.unrecommendThisShow(conn, loggedUserId, tvShow);
			}
			
			refreshTableViewRecomm(conn); 
		}
	} 
	
	private String getAccColor() {
		String colorName = "";
		
		colorName = dbm.getAccInfo(conn, loggedUserId).get(2).toString();
		 
		return colorName;
	}
	
	private Timer turnOffTimer (Timer timer) {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		} 
		return timer;
	}
	
	public void exit() {
		Platform.exit();
		System.exit(0);
	} 
	
	public void changeCursorToWait() {
		getStage().getScene().getCursor();
	}
	
	
}
