package hr.miran.seriesapp.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import hr.miran.seriesapp.alerts.DialogFX;
import hr.miran.seriesapp.alerts.DialogFX.Type;
import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.entity.TaggedShow;
import hr.miran.seriesapp.main.DbModule;
import hr.miran.seriesapp.main.JDBCConnect;
import hr.miran.seriesapp.thetvdbapi.TheTVDBApi;
import hr.miran.seriesapp.thetvdbapi.TvDbException;
import hr.miran.seriesapp.thetvdbapi.model.Episode;
import hr.miran.seriesapp.thetvdbapi.model.Series;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class StartFromController {

	public StartFromController() {
		
	}
	
	@FXML
	private ComboBox seasonsCombo;
	
	@FXML
	private ComboBox episodesCombo;

	private SeriesOld tvShow;

	private Stage stage;

	private ArrayList<TaggedShow> taggedShowsList;

	private List<Episode> episodeList;
	private List<Episode> seasonList;
	
	private static DbModule dbm =  new DbModule();
	
	@FXML
	public void initialize() {
		
		System.out.println("Logged User: " + MainController.loggedUser);
	}
	
	private static JDBCConnect jdbcConnect =  new JDBCConnect();

	public void setSeasonList(){
		if (seasonsCombo==null) {
			seasonsCombo = new ComboBox();
		}
		seasonsCombo.getItems().clear();

		try {
		
			TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");
			TreeSet<Season> aseasonList =  new TreeSet<>();
			try {
				List<Episode> epList = tvdbApi.getAllEpisodes(tvShow.getShowid().toString(), "en");
				Integer noOfSeasons = epList.get(epList.size()-1).getSeasonNumber();
				
				for (int i=1; i<=noOfSeasons; i++) {
					seasonsCombo.getItems().add(i);
				}
					
			} catch (TvDbException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		seasonsCombo.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				
				setEpisodeList((Integer) newValue);
			}
			
		});
		
//		jdbcConnect.closeConnectionToDatabase(conn);
//		setEpisodeList();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
			
	}
	
	public void setEpisodeList(Integer newValue){

		episodesCombo.getItems().clear();

		TheTVDBApi tvdbApi = new TheTVDBApi("26B8BE6FAC673EE0");

		try {
			Series currentShow =  tvdbApi.getSeries(tvShow.getShowid().toString(), "en");
			episodeList =  tvdbApi.getSeasonEpisodes(tvShow.getShowid().toString(), newValue, "en");
			
			for (Episode episode : episodeList) {
				episodesCombo.getItems().add(episode.getEpisodeNumber() + " - " + episode.getEpisodeName());
			}
			episodesCombo.getSelectionModel().select(0);
		} catch (TvDbException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	

	public void setChosenSeries(SeriesOld tvShow) {
		this.tvShow = tvShow;
		setSeasonList();
//		setEpisodeList();
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
		this.stage.getIcons().add(new Image(getClass().getResourceAsStream("showTrackLogo.png")));
	}
	
	
	public static class Season {
        private Integer seasonNum;
        private Integer seasonId;

        @Override
        public String toString() {
            return seasonNum+"";
        }

        public Season(Integer seasonId, Integer seasonNum) {
            this.seasonNum = seasonNum;
            this.seasonId = seasonId;
        }

        public Integer getSeasonNum() {
            return seasonNum;
        }

        public void setSeasonNum(Integer seasonNum) {
            this.seasonNum = seasonNum; 
        }

        public Integer getSeasonId() {
            return seasonId;
        }

        public void setSeasonId(Integer seasonId) {
            this.seasonId = seasonId;
        }
        
    }
	
	public void closeStage(){
		stage.close();
	}
	
	public void trackThisShow(){
		
		Connection conn = jdbcConnect.connectToUserDb();;
		

		
		String seasonNum = seasonsCombo.getSelectionModel().getSelectedItem().toString();
		String episodeNum =  episodesCombo.getSelectionModel().getSelectedItem().toString().split("-")[0].trim();
		tvShow.setSeasonNum(Integer.valueOf(seasonNum));
		tvShow.setEpisodeNum(Integer.valueOf(episodeNum));

		taggedShowsList = new ArrayList<TaggedShow>();
		taggedShowsList.addAll(dbm.getTaggedShows(conn));
		
		for (TaggedShow taggedShow : taggedShowsList) {
			if (taggedShow.getShowId().equals(tvShow.getShowid())) {
				DialogFX alertOk = new DialogFX(Type.INFO);
				alertOk.setTitleText("Show Info");
				alertOk.setMessage("This show has already been added on watch list!");
				alertOk.showDialog();
				return;
			}
		}
		
		try {
			dbm.insertIntoTaggedShow(conn,MainController.loggedUserId, tvShow, false);
			DialogFX alertOk = new DialogFX(Type.ACCEPT);
			alertOk.setTitleText("Show added successfully!");
			alertOk.setMessage("Show " + tvShow.getShowName() + " has been added successfully to your tracking list.");
			alertOk.showDialog();
		
		} catch (Exception e) {
			DialogFX alertOk = new DialogFX(Type.ERROR);
			alertOk.setTitleText("Error");
			alertOk.setMessage("Exception occured during saving this show: \n" + e.getMessage());
			alertOk.showDialog();
		}
		
		
		
		jdbcConnect.closeConnectionToDatabase(conn);
		closeStage();
		
		

	}
}
