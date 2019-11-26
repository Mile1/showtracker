package hr.miran.seriesapp.entity;

import javafx.scene.image.Image;

public class SeriesOld {

	private Integer showid;
	private String showName;
	private String episodeName;
	private Integer seasonNum;
	private Integer episodeNum;
	private String status;
	private String episodeDate;
	private String watchedStatus;
	private String rating;
	private Image recommended;
	private String recomCount;

	public SeriesOld(Integer showID, String showName, String episodeName, int seasonNum, int episodeNum,String status,String episodeDate, 
			String watchedStatus,  String rating, Image recommended, String recomCount) {
		this.showid = showID;
		this.showName = showName;
		this.episodeName = episodeName;
		this.seasonNum = seasonNum;
		this.episodeNum = episodeNum;
		this.status = status;
		this.episodeDate = episodeDate;
		this.watchedStatus = watchedStatus;
		this.rating = rating;
		this.recommended = recommended;
		this.recomCount = recomCount;
	}

	public Integer getShowid() {
		return showid;
	}

	public void setShowid(Integer showid) {
		this.showid = showid;
	}

	public String getShowName() {
		return showName;
	}

	public void setShowName(String showName) {
		this.showName = showName;
	}

	public String getEpisodeName() {
		return episodeName;
	}

	public void setEpisodeName(String episodeName) {
		this.episodeName = episodeName;
	}

	public int getSeasonNum() {
		return seasonNum;
	}

	public void setSeasonNum(Integer seasonNum) {
		this.seasonNum = seasonNum;
	}

	public int getEpisodeNum() {
		return episodeNum;
	}

	public void setEpisodeNum(Integer episodeNum) {
		this.episodeNum = episodeNum;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	public String getEpisodeDate() {
		return episodeDate;
	}

	public void setEpisodeDate(String episodeDate) {
		this.episodeDate = episodeDate;
	}

	public String getWatchedStatus() {
		return watchedStatus;
	}

	public void setWatchedStatus(String watching) {
		this.watchedStatus = watching;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
	
	public String getStatus() {
		return status;
	}

	public Image getRecommendedImage() {
		return recommended;
	}

	public void setRecommendedImage(Image recommended) {
		this.recommended = recommended;
	}

	public String getRecomCount() {
		return recomCount;
	}

	public void setRecomCount(String recomCount) {
		this.recomCount = recomCount;
	}
	
	
	
	
	
}
