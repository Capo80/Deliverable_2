package it.deliv2;

public class Issue {
	
	String key;
	int introVersion;
	int fixVersion;
	
	public Issue(String key, int introVersion, int fixVersion) {
		this.key = key;
		this.introVersion = introVersion;
		this.fixVersion = fixVersion;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getIntroVersion() {
		return introVersion;
	}
	public void setIntroVersion(int introVersion) {
		this.introVersion = introVersion;
	}
	public int getFixVersion() {
		return fixVersion;
	}
	public void setFixVersion(int fixVersion) {
		this.fixVersion = fixVersion;
	}
	
	

}
