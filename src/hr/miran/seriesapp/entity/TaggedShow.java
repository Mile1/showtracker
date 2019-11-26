package hr.miran.seriesapp.entity;

public class TaggedShow {

	private Integer showId;
	private Integer showSeason;
	private Integer showEpisode;
	private boolean isRecommended;

	public TaggedShow(Integer showId, Integer showSeason, Integer showEpisode) {
		this.showId = showId;
		this.showSeason = showSeason;
		this.showEpisode = showEpisode;
	}

	public Integer getShowId() {
		return showId;
	}

	public void setShowId(Integer showId) {
		this.showId = showId;
	}

	public Integer getShowSeason() {
		return showSeason;
	}

	public void setShowSeason(Integer showSeason) {
		this.showSeason = showSeason;
	}

	public Integer getShowEpisode() {
		return showEpisode;
	}

	public void setShowEpisode(Integer showEpisode) {
		this.showEpisode = showEpisode;
	}
	
//	public boolean isRecommended(boolean isRecommended) {
//		this.isRecommended = isRecommended;
//	}
	
}
