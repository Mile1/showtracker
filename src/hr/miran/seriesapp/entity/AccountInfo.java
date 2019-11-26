package hr.miran.seriesapp.entity;

public class AccountInfo {

	private Integer accountId;
	private Integer background;
	private Integer changeint;
	private String airedcolour;
	
	public AccountInfo(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getBackgroundCover() {
		return background;
	}

	public void setBackgroundCover(Integer background) {
		this.background = background;
	}

	public Integer getChangeInterval() {
		return changeint;
	}

	public void setChangeInterval(Integer changeint) {
		this.changeint = changeint;
	}

	public String getAiredColor() {
		return airedcolour;
	}

	public void setAiredColor(String airedcolour) {
		this.airedcolour = airedcolour;
	}
	
	
	
	
	
}
