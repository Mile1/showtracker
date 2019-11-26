package hr.miran.seriesapp.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import ch.qos.logback.core.db.dialect.DBUtil;
import hr.miran.seriesapp.controller.MainController;
import hr.miran.seriesapp.entity.SeriesOld;
import hr.miran.seriesapp.entity.TaggedShow;
import hr.miran.seriesapp.thetvdbapi.TheTVDBApi;
import hr.miran.seriesapp.thetvdbapi.model.Episode;
import hr.miran.seriesapp.thetvdbapi.model.Series;
import hr.miran.seriesapp.thetvdbapi.model.SeriesUpdate;
import javafx.scene.control.MenuItem;

public class DbModule {
	

	private PreparedStatement preparedStatement;
	private ResultSet rs;

	public PreparedStatement getIncomingEpisodesMain(Connection conn, Integer showId, Integer showSeason, Integer showEpisode) {
		
		
		String queryString = "SELECT a.id, a.seriesname, b.season, c.episodename, c.episodenumber, c.firstaired FROM tvseries a, tvseasons b, tvepisodes c "
				+ " WHERE a.id = b.seriesid AND b.seriesid = c.seriesid AND c.seasonid = b.id "
				+ " AND a.id = ? AND b.season >= ? AND c.episodenumber >= ? " ;
		queryString += " GROUP BY a.id, a.seriesname,c.episodenumber ORDER BY b.season,c.episodenumber "
					+ " LIMIT 3";
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, showId);
			preparedStatement.setInt(2, showSeason);
			preparedStatement.setInt(3, showEpisode);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return preparedStatement;
		
		
		
	}
	
	public ArrayList<String> getGenreList(Connection conn) {
		ArrayList<String> genreList =  new ArrayList<String>();
		
		String queryString = "SELECT * FROM genres ORDER BY genre;";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
							
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				genreList.add(rs.getString("genre"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return genreList;
	}
	
	public ArrayList<String> getCountryList(Connection conn) {
		ArrayList<String> countryList =  new ArrayList<String>();
		
		String queryString = "SELECT * FROM countries ORDER BY shortname;";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
							
			rs = preparedStatement.executeQuery();
			countryList.add("US & UK");
			while(rs.next()) {
				countryList.add(rs.getString("shortname"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return countryList;
	}
	
	public PreparedStatement getSearchedShows(Connection conn, String showNameTf) {
		String queryString = "SELECT a.id,a.seriesname, max(b.season) as numOfSeasons, a.status, a.rating FROM tvseries a, tvseasons b "
				+ " WHERE a.id = b.seriesid ";

		if (showNameTf.length() > 2) {
			queryString += " AND seriesname LIKE '" + showNameTf + "%' ";
		}
		queryString += " GROUP BY a.id, a.seriesname ORDER BY b.season";
		
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return preparedStatement;
	}
	
	public  ArrayList<String> getSeasonUpdatesForShows(Connection conn) {
		
		ArrayList<String> idList = new ArrayList<>();
		
		String queryString = "select id from tvseries where id not in (select seriesid from tvseasons) ";
		
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				idList.add(rs.getString("id"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return idList;
	}
	
	public PreparedStatement getSearchedTaggedShows(Connection conn, String showNameTf, String taggedShowsString) {
		String queryString = "SELECT a.id, a.seriesname, b.season, c.episodename, c.episodenumber, c.firstaired FROM tvseries a, tvseasons b, tvepisodes c "
				+ " WHERE a.id = b.seriesid AND b.seriesid = c.seriesid AND c.seasonid = b.id "
				+ " AND a.id IN "+ taggedShowsString +" " ;

		if (showNameTf.length() > 2) {
		queryString += " AND seriesname LIKE '" + showNameTf + "%' ";
		}
		queryString += " GROUP BY a.id, a.seriesname,c.episodenumber ORDER BY a.seriesname,b.season,c.episodenumber ";
		
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return preparedStatement;
	}
	
	public boolean checkUserLogin(Connection conn, String userName, String password) {
		
		
		
		boolean credentialsOK = false; 
		
		String queryString = "SELECT user_alias, user_pass FROM users WHERE user_alias = ? AND user_pass = ?"; 
		
		try {
			preparedStatement =  (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, userName);
			preparedStatement.setString(2, password);
			
			rs = preparedStatement.executeQuery();
			
        	
			
			if (rs.next()) {
				credentialsOK = true;
			}
			
//			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return credentialsOK;
	}
	
	public boolean checkUserPassword(Connection conn, Integer userID, String password) {
		
		String queryString = "SELECT user_id, user_pass FROM users WHERE user_id = ? AND user_pass = ?"; 
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, userID);
			preparedStatement.setString(2, password);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				return true;
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
	}
	
//	public void setSqlMode(Connection conn) {
//		
//		String queryString = "SET sql_mode = ''"; 
//		
//		try {
//			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
//		
//			rs = preparedStatement.executeQuery();
//			
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
	
	public boolean checkIfUserExists(Connection conn, String userName) {
		
		String queryString = "SELECT user_alias FROM users WHERE user_alias = ?"; 
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, userName);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				return true;
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
	}
	
	public void signUpUser(Connection conn, String userName, String password) {
		
		String queryString = "INSERT INTO users (user_alias, user_pass) VALUES (?,?)";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, userName);
			preparedStatement.setString(2, password);
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void updatePassword(Connection conn, Integer userID, String password) {
		
		String queryString = "UPDATE users SET user_pass = ? where user_id = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, password);
			preparedStatement.setInt(2, userID);
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public void insertIntoTaggedShow(Connection conn, Integer userID, SeriesOld tvShow, boolean watchStatus) {
		
		String queryString = "INSERT INTO taggedshows (tag_user_id, tag_series_id, tag_season_num, tag_episode_num, tag_watch_status) " +
							"VALUES (?,?,?,?,?)";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, userID);
			preparedStatement.setInt(2, tvShow.getShowid());
			preparedStatement.setInt(3, tvShow.getSeasonNum());
			preparedStatement.setInt(4, tvShow.getEpisodeNum());
			
			if (watchStatus) {
				preparedStatement.setInt(5, 1); //1 oznacava da je pogledano				
			}else {
				preparedStatement.setInt(5, 0); 
			}
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public void updateTaggedShowWatchStatus(Connection conn, Integer userId, SeriesOld tvShow, int watchStatus) {
		
		String queryString = "UPDATE taggedshows SET tag_watch_status = ? WHERE tag_user_id = ? AND tag_series_id = ? AND tag_season_num = ? AND tag_episode_num = ?" ;
							
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, watchStatus);
			preparedStatement.setInt(2, userId);
			preparedStatement.setInt(3, tvShow.getShowid());
			preparedStatement.setInt(4, tvShow.getSeasonNum());
			preparedStatement.setInt(5, tvShow.getEpisodeNum());
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void recommendThisShow(Connection conn, Integer userId, SeriesOld tvShow, String comment) {
		
		String queryString = "INSERT INTO recomshows (rcm_showid, rcm_userid, rcm_comment) VALUES (?,?,?)" ;
							
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, tvShow.getShowid());
			preparedStatement.setInt(2, userId);
			preparedStatement.setString(3, comment);
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void unrecommendThisShow(Connection conn, Integer userId, SeriesOld tvShow) {
		
		String queryString = "DELETE FROM recomshows WHERE rcm_showid = ? AND rcm_userid = ?" ;
							
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, tvShow.getShowid());
			preparedStatement.setInt(2, userId);
			preparedStatement.executeUpdate();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public ArrayList<TaggedShow> getWatchedEpisodes(Connection conn, Integer seriesId) {

		ArrayList<TaggedShow> taggedShowsList =  new ArrayList<TaggedShow>();
		
		String queryString = "SELECT tag_series_id, tag_season_num, tag_episode_num, tag_watch_status FROM taggedshows WHERE tag_user_id = ? "
							+ "AND tag_series_id = ? AND tag_watch_status = 1";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, seriesId);
							
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				taggedShowsList.add(new TaggedShow(rs.getInt("tag_series_id"), rs.getInt("tag_season_num"), rs.getInt("tag_episode_num")));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return taggedShowsList;
		
	}
	
	public String getImdbRating(Connection conn, Integer seriesId) { //TODO

		String imdbRating = "";
		
		String queryString = "select * from tvseries, imdbratings where "
							+ "show_id = CAST(REPLACE(REPLACE(REPLACE(imdb_id, 'tt', ''), 'title', ''), '/', '')  AS UNSIGNED) "
							+ "and id = ?;";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, seriesId);
							
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				imdbRating = rs.getString("avgrating");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return imdbRating;
		
	}
	
	public ArrayList<TaggedShow> getTaggedShows(Connection conn) {

		ArrayList<TaggedShow> taggedShowsList =  new ArrayList<TaggedShow>();
		
//		String queryString = "SELECT tag_series_id, tag_season_num, tag_episode_num, tag_watch_status FROM taggedshows WHERE tag_user_id = ? GROUP BY tag_series_id";
		
//		String queryString = "select  tag_series_id, tag_season_num, tag_episode_num, tag_watch_status from ("
//				+ "select * from taggedshows WHERE tag_user_id = ? order by tag_series_id, tag_season_num desc, tag_episode_num desc) x "
//				+ "group by tag_season_num desc, tag_episode_num desc";
		String queryString = "SELECT * FROM taggedshows WHERE (tag_series_id, tag_season_num, tag_episode_num) IN "
				+ "(SELECT tag_series_id, tag_season_num, max(tag_episode_num)  FROM  taggedshows "
				+ "WHERE tag_user_id = ? AND ( tag_series_id , tag_season_num) IN "
				+ "(SELECT tag_series_id, max(tag_season_num)  FROM taggedshows WHERE tag_user_id = ? "
				+ "GROUP BY tag_series_id) GROUP BY tag_series_id) "
				+ "AND tag_user_id = ? GROUP BY tag_series_id";
		
//		System.out.println(queryString);
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, MainController.loggedUserId);
			preparedStatement.setInt(3, MainController.loggedUserId);
				
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				taggedShowsList.add(new TaggedShow(rs.getInt("tag_series_id"), rs.getInt("tag_season_num"), rs.getInt("tag_episode_num")));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return taggedShowsList;
		
	}
	
	public ArrayList<String> getTaggedShowNamesList(Connection conn) {

		ArrayList<String> taggedShowsList =  new ArrayList();
		
		String queryString = "SELECT SeriesName FROM tvseries WHERE id in (SELECT distinct(tag_series_id) FROM taggedshows WHERE tag_user_id = ?) ORDER BY SeriesName asc;";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
				
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				taggedShowsList.add(rs.getString("SeriesName"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return taggedShowsList;
		
	}
	
	public Integer getShowIdFromName(Connection conn, String seriesName) {

		Integer showId = 0 ;
		
		String queryString = "SELECT id FROM tvseries WHERE SeriesName = ?;";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, seriesName);
				
			rs = preparedStatement.executeQuery();

			if(rs.next()) {
				showId = rs.getInt("id");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return showId;
		
	}
	
	public String getShowNameFromId(Connection conn, Integer id) {

		String showName = "" ;
		
		String queryString = "SELECT SeriesName FROM tvseries WHERE id = ?;";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, id);
				
			rs = preparedStatement.executeQuery();

			if(rs.next()) {
				showName = rs.getString("SeriesName");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return showName;
		
	}
	
	public ArrayList<Integer> getTaggedShowsList(Connection conn) {

		ArrayList<Integer> taggedShowsList =  new ArrayList<Integer>();
		
		String queryString = "SELECT distinct(tag_series_id) FROM taggedshows WHERE tag_user_id = ? order by RAND();";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
				
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				taggedShowsList.add(rs.getInt("tag_series_id"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return taggedShowsList;
		
	}
	
	public void insertNextEpisodeInDB(Connection conn, Episode nextEpisode) {
		
		String queryString = "INSERT INTO tvepisodes (seasonid,episodenumber,episodename,firstaired,DVD_season,seriesid) VALUES (?,?,?,?,?,?)";
		
		if (nextEpisode.getSeasonId().trim().isEmpty()) {
			//skip
			return;
		}
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, Integer.valueOf(nextEpisode.getSeasonId()));
			preparedStatement.setInt(2, nextEpisode.getEpisodeNumber());
			preparedStatement.setString(3, nextEpisode.getEpisodeName());
			preparedStatement.setString(4, nextEpisode.getFirstAired());
			preparedStatement.setInt(5, nextEpisode.getSeasonNumber());
			preparedStatement.setInt(6, Integer.valueOf(nextEpisode.getSeriesId()));
			preparedStatement.executeUpdate();
			
						
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public Series getSeriesByIdFromDB(Connection conn, String showId) {
		Series tvSeries =  new Series();
		
		String queryString = "SELECT SeriesName, Network, Status, Rating  FROM tvseries WHERE id = ? ";
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, showId);
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				tvSeries.setSeriesName(rs.getString("SeriesName"));
				tvSeries.setNetwork(rs.getString("Network"));
				tvSeries.setStatus(rs.getString("Status"));
				tvSeries.setRating(rs.getString("Rating"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		
		return tvSeries;
	}
	
	public ArrayList<Series> getSeriesByIdListFromDB(Connection conn, ArrayList<TaggedShow> showList) {
		ArrayList<Series> tvSeriesList =  new ArrayList<>();
		
		
		
		String queryString = "SELECT id, SeriesName, Network, Status, Rating  FROM tvseries WHERE id IN (";
		
		for (int i = 0; i < showList.size(); i++) {
			
			queryString += showList.get(i).getShowId();

			if (i+1 < showList.size()) {
				queryString += ", ";
			}
			
		}
		queryString += ")";
		
//		System.out.println(queryString);
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
//			preparedStatement.setString(1, showId);
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				Series tvSeries =  new Series();
				
				tvSeries.setSeriesId(rs.getInt("id")+"");
				tvSeries.setSeriesName(rs.getString("SeriesName"));
				tvSeries.setNetwork(rs.getString("Network"));
				tvSeries.setStatus(rs.getString("Status"));
				tvSeries.setRating(rs.getString("Rating"));
				
				tvSeriesList.add(tvSeries);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		
		return tvSeriesList;
	}
	
	public Episode getNextEpisodeFromDB(Connection conn, String showId, Integer seasonNum, int episodeNum) {
		 
		Episode nextep = null;
		
		String queryString = "SELECT seasonid, episodenumber, episodename, firstaired, DVD_season, seriesid FROM tvepisodes WHERE seriesid = ? AND DVD_season = ? AND episodenumber = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, showId);
			preparedStatement.setInt(2, seasonNum);
			preparedStatement.setInt(3, episodeNum);
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				nextep = new Episode();
				nextep.setSeasonId(rs.getString("seasonid"));
				nextep.setEpisodeNumber(rs.getInt("episodenumber"));
				nextep.setEpisodeName(rs.getString("episodename"));
				nextep.setFirstAired(rs.getString("firstaired"));
				nextep.setSeasonNumber(rs.getInt("DVD_season"));
				nextep.setSeriesId(rs.getString("seriesid"));
			} else {
				
				preparedStatement.setString(1, showId);
				preparedStatement.setInt(2, seasonNum+1);
				preparedStatement.setInt(3, 1);
					
				rs = preparedStatement.executeQuery();
				
				if (rs.next()) {
					nextep = new Episode();
					nextep.setSeasonId(rs.getString("seasonid"));
					nextep.setEpisodeNumber(rs.getInt("episodenumber"));
					nextep.setEpisodeName(rs.getString("episodename"));
					nextep.setFirstAired(rs.getString("firstaired"));
					nextep.setSeasonNumber(rs.getInt("DVD_season"));
					nextep.setSeriesId(rs.getString("seriesid"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return nextep;
		
	}
	
	
	public Integer getShowWatchedStatus(Connection conn, String showId, Integer seasonNum, int episodeNum) {//ne radi jos
		
		Integer watchStatus =  0;
		
//		String queryString = "SELECT tag_watch_status FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ? AND tag_season_num = ? AND tag_episode_num = ?";
		
		String queryString = "SELECT * FROM taggedshows WHERE (tag_series_id, tag_season_num, tag_episode_num) IN "
				+ "(SELECT tag_series_id, tag_season_num, max(tag_episode_num)  FROM  taggedshows "
				+ "WHERE tag_user_id = ? AND ( tag_series_id , tag_season_num) IN "
				+ "(SELECT tag_series_id, max(tag_season_num)  FROM taggedshows WHERE tag_user_id = ? "
				+ "GROUP BY tag_series_id) GROUP BY tag_series_id) "
				+ "AND tag_user_id = ? AND tag_series_id = ? GROUP BY tag_series_id ";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, MainController.loggedUserId);
			preparedStatement.setInt(3, MainController.loggedUserId);
			preparedStatement.setString(4, showId);
//			preparedStatement.setInt(3, seasonNum);
//			preparedStatement.setInt(4, episodeNum);
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				watchStatus = rs.getInt("tag_watch_status");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return watchStatus;
		
	}
	
	public Integer getEpisodeWatchStatus(Connection conn, String showId, Integer seasonNum, int episodeNum) {
		
		Integer watchStatus =  0;
		
		String queryString = "SELECT tag_watch_status FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ? AND tag_season_num = ? AND tag_episode_num = ?";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setString(2, showId);
			preparedStatement.setInt(3, seasonNum);
			preparedStatement.setInt(4, episodeNum);
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				watchStatus = rs.getInt("tag_watch_status");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return watchStatus;
		
	}
	
	public boolean checkIfAlreadyWatching(Connection conn, Integer showId) {
		
		boolean watchStatus =  false;
		
		String queryString = "SELECT * FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ? LIMIT 1";
		
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, showId);
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				watchStatus = true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return watchStatus;
		
	}
	
	public boolean checkIfEpisodeExistsInDb (Connection conn, SeriesOld tvShow) {
		
		
		
		String queryString = "SELECT tag_watch_status FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ? AND tag_season_num = ? AND tag_episode_num = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, tvShow.getShowid());
			preparedStatement.setInt(3, tvShow.getSeasonNum());
			preparedStatement.setInt(4, tvShow.getEpisodeNum());
			
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
		
	}
	
	public boolean checkIfShowExistsInDb (Connection conn, SeriesOld tvShow) {
		
		
		
		String queryString = "SELECT * FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, tvShow.getShowid());
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
		
	}
	
	public boolean checkIfAlreadyRecommended (Connection conn, Integer showId) {
		
		
		
		String queryString = "SELECT * FROM recomshows WHERE rcm_showid = ? AND rcm_userId = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, showId);
			preparedStatement.setInt(2, MainController.loggedUserId);
							
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
		
	}
	
	public boolean deleteFromTaggedShows (Connection conn, SeriesOld tvShow) {
		
		
		
		String queryString = "SELECT tag_watch_status FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setInt(1, MainController.loggedUserId);
			preparedStatement.setInt(2, tvShow.getShowid());
			
				
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				String queryStringDelete = "DELETE FROM taggedshows WHERE tag_user_id = ? AND tag_series_id = ?";
				preparedStatement = (PreparedStatement) conn.prepareStatement(queryStringDelete);
				preparedStatement.setInt(1, MainController.loggedUserId);
				preparedStatement.setInt(2, tvShow.getShowid());
				preparedStatement.executeUpdate();
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return false;
		
	}

	public int getUserId(Connection conn, String userAlias) {
		
		String queryString = "SELECT * FROM users WHERE user_alias = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, userAlias);	
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return rs.getInt("user_id");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		
		return 0;
	}
	
	public ArrayList<String> getListOfShows(Connection conn) {
		ArrayList<String> showIdList = new ArrayList<>();
		
		String queryString = "SELECT id FROM tvseries WHERE SeriesName != '' and firstaired like '2017%'";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				showIdList.add(String.valueOf(rs.getInt("id")));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		
		return showIdList;
	}
	
	public PreparedStatement getPopularShows(Connection conn, String queryString) {
		

		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return preparedStatement;
		
	}
	
	public void updateRatingOnTvSeriesTable(Connection conn, Series series) {
		String queryString = "UPDATE tvseries SET Rating = ?, ContentRating = ? WHERE id = ?";
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, series.getRating());
			preparedStatement.setString(2, series.getContentRating());
			preparedStatement.setString(3, series.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void updateTvSeasons(Connection conn, List<Episode> episodeList) {
		
		//Date object
		 Date date= new Date();
	         //getTime() returns current time in milliseconds
		 long time = date.getTime();
	         //Passed the milliseconds to constructor of Timestamp class 
		 Timestamp ts = new Timestamp(time);
//		 System.out.println("Current Time Stamp: "+ts);
		 
		 if (episodeList.size() == 0) {
			 return;
		 }
		 Integer seasonMin = episodeList.get(0).getSeasonNumber();
		 Integer seasonMax = episodeList.get(episodeList.size()-1).getSeasonNumber();;
		 
		String queryString = "INSERT INTO tvseasons (seriesid, season, locked, lockedby, tms_wanted "
				+ ") VALUES (?,?,?,?,?)";
//		String queryString = "INSERT INTO USERS (USER_ALIAS, USER_PASS) VALUES ('"+userName+"', '"+password+"')";
		
		try {
			
			 for (int i = seasonMin; i <= seasonMax; i++) {
				preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
				preparedStatement.setInt(1, Integer.valueOf(episodeList.get(0).getSeriesId()));
				preparedStatement.setInt(2, i);
				preparedStatement.setString(3, "no");
				preparedStatement.setInt(4, 0);
				preparedStatement.setInt(5, 0);
				preparedStatement.executeUpdate();
			 }
			 
				
		} catch (MysqlDataTruncation ce) {
			System.out.println("Errorq " + episodeList.get(0).getSeriesId());
		}  
		catch (MySQLIntegrityConstraintViolationException me) {
			System.out.println("Error " + episodeList.get(0).getSeriesId());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void updateTvSeriesTable(Connection conn, Series series) {
		
		//Date object
		 Date date= new Date();
	         //getTime() returns current time in milliseconds
		 long time = date.getTime();
	         //Passed the milliseconds to constructor of Timestamp class 
		 Timestamp ts = new Timestamp(time);
//		 System.out.println("Current Time Stamp: "+ts);
		
		String queryString = "INSERT INTO tvseries (id, SeriesName, SeriesID, Status, FirstAired, Network, Runtime, Genre, Actors,"
				+ " Overview, bannerrequest, lastupdated, Airs_DayOfWeek, Airs_Time, ContentRating, flagged, forceupdate, hits, updateID, requestcomment, locked,"
				+ " mirrorupdate, lockedby, autoimport, disabled, IMDB_ID, zap2it_id, tms_wanted_old, tms_priority, tms_wanted, Rating"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//		String queryString = "INSERT INTO USERS (USER_ALIAS, USER_PASS) VALUES ('"+userName+"', '"+password+"')";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			preparedStatement.setString(1, series.getId());
			preparedStatement.setString(2, series.getSeriesName());
			preparedStatement.setString(3, series.getSeriesId());
			if (series.getSeriesId().equals("")) {
				preparedStatement.setNull(3, Types.NULL);
			}
			preparedStatement.setString(4, series.getStatus());
			preparedStatement.setString(5, series.getFirstAired());
			preparedStatement.setString(6, series.getNetwork());
			
			preparedStatement.setString(7, series.getRuntime());
			preparedStatement.setString(8, series.getGenres().toString());
			preparedStatement.setString(9, series.getActors().toString());
			preparedStatement.setString(10, series.getOverview());
			preparedStatement.setString(11, "0"); //bannerequest
			preparedStatement.setString(12, series.getLastUpdated());
			preparedStatement.setString(13, series.getAirsDayOfWeek());
			preparedStatement.setString(14, series.getAirsTime());
			preparedStatement.setString(15, series.getContentRating());
			preparedStatement.setString(16, "0"); //flagged
			preparedStatement.setString(17, "0"); //forceupdate
			preparedStatement.setString(18, "0"); //hits
			preparedStatement.setString(19, "0"); //updateId
			preparedStatement.setString(20, ""); //requestComment
			preparedStatement.setString(21, "0"); //locked
			preparedStatement.setTimestamp(22, ts); //mirrorupdate
			preparedStatement.setString(23, "0"); //lockedBY
			preparedStatement.setString(24, "miran"); //autoimport
			preparedStatement.setString(25, "No"); //disabled
			
			preparedStatement.setString(26, series.getImdbId());
			preparedStatement.setString(27, series.getZap2ItId());

			if (series.getImdbId().equals("")) {
				preparedStatement.setNull(26, Types.NULL);
			}
			if (series.getZap2ItId().equals("")) {
				preparedStatement.setNull(27, Types.NULL);
			}
			preparedStatement.setString(28, "0"); //tms_wanted_old
			preparedStatement.setString(29, "0"); //tms_priority
			preparedStatement.setString(30, "0"); //tms_wanted
			preparedStatement.setString(31, series.getRating());
			
			
			preparedStatement.executeUpdate();
				
		} catch (MysqlDataTruncation ce) {
			System.out.println("Errorq " + series.getId() + "|" + series.getSeriesName());
		}  
		catch (MySQLIntegrityConstraintViolationException me) {
			System.out.println("Error" + series.getId() + "|" + series.getSeriesName());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}

	public ArrayList<String> getColorList(Connection conn) {
		
		ArrayList colorList = new ArrayList<>(); 
		
		String queryString = "SELECT colorName FROM colors";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
							
			rs = preparedStatement.executeQuery();

			while(rs.next()) {
				colorList.add(rs.getString("colorName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return colorList;
	}
	
	public boolean checkIfAcInfoExists(Connection conn, Integer accid) {
		
		String checkIfExists = "SELECT accid FROM useracc WHERE accid = ?";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(checkIfExists);
			preparedStatement.setInt(1, accid);
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		return false;
		
	}
	
	public ArrayList<Object> getAccInfo(Connection conn, Integer accid) {
		
		ArrayList<Object> accountInfoList = new ArrayList<>();
		
		String checkIfExists = "SELECT accid, background, changeint, airedcolour FROM useracc WHERE accid = ?";
		
		Integer background = null;
		Integer	changeint = null;
		String airedcolour = null;
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(checkIfExists);
			preparedStatement.setInt(1, accid);
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				background = rs.getInt("background");
				changeint = rs.getInt("changeint");
				airedcolour = rs.getString("airedcolour");
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		accountInfoList.add(background);
		accountInfoList.add(changeint);
		accountInfoList.add(airedcolour);
		
		return accountInfoList;
		
	}

	public void upsertAccChanges(Connection conn, Integer accid, Integer backgroundId, Integer intervalInSeconds, String colorAired) {
		
		try {
			
			if (checkIfAcInfoExists(conn, accid)) {
				
				String queryString = "UPDATE USERACC SET background = ?, changeint = ?, airedcolour = ? WHERE accid = ?";
				
				preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
				preparedStatement.setInt(1, backgroundId);
				preparedStatement.setInt(2, intervalInSeconds);
				preparedStatement.setString(3, colorAired);
				preparedStatement.setInt(4, accid);
				
				preparedStatement.executeUpdate();
				
			} else  {
				String queryString = "INSERT INTO USERACC (accid, background, changeint, airedcolour) VALUES (?,?,?,?)";
				
				preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
				
				preparedStatement.setInt(1, accid);
				preparedStatement.setInt(2, backgroundId);
				preparedStatement.setInt(3, intervalInSeconds);
				preparedStatement.setString(4, colorAired);
				
				preparedStatement.executeUpdate();
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	} 
	
	public ArrayList<Object> getLatestVersion(Connection conn) {
		
		ArrayList<Object> version = new ArrayList<>();
		
		String queryString = "select version, description, location from versioning group by version, description order by version desc limit 1";
		
		try {
			preparedStatement = (PreparedStatement) conn.prepareStatement(queryString);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				version.add(rs.getFloat("version"));
				version.add(rs.getString("description"));
				version.add(rs.getString("location"));
				
			} 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			 try { rs.close(); } catch (Exception e) { /* ignored */ }
			 try { preparedStatement.close(); } catch (Exception e) { /* ignored */ }
		}
		
		return version;
	}
}
