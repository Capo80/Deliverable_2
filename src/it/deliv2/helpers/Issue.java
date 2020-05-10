package it.deliv2.helpers;

public class Issue {
	
	private String key;
	private int introVersion;
	private int openVersion;
	private int fixVersion;
	
	public final static int FIX_DEF = -1;
	public final static int INTRO_DEF = 100000000;
	
	
	public Issue(String key, int introVersion,int openVersion, int fixVersion) {
		this.key = key;
		this.introVersion = introVersion;
		this.fixVersion = fixVersion;
		this.openVersion = openVersion;
	}
	
	public int getOpenVersion() {
		return openVersion;
	}

	public void setOpenVersion(int openVersion) {
		this.openVersion = openVersion;
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
