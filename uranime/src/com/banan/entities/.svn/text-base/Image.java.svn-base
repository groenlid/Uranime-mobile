package com.banan.entities;

public class Image {
	private String poster;
	private String fanart;
	private String screen;
	
	public String getScreen() {
		return screen;
	}
	public void setScreen(String screen) {
		this.screen = screen;
	}
	public void setFanart(String fanart) {
		this.fanart = fanart;
	}
	public String getFanart() {
		return fanart;
	}
	public void setPoster(String poster) {
		this.poster = poster;
	}
	public String getPoster() {
		return poster;
	}
	public String getSmallPoster() {
		int extPos = poster.lastIndexOf(".");
		String fileName = poster.substring(0, extPos);
		String fileType = poster.substring(extPos, poster.length());
		String smallPoster =  fileName + "-138" + fileType;
		return smallPoster;
	}
	
	public static String getSmallPoster(String orginal) {
		int extPos = orginal.lastIndexOf(".");
		String fileName = orginal.substring(0, extPos);
		String fileType = orginal.substring(extPos, orginal.length());
		String smallPoster =  fileName + "-138" + fileType;
		return smallPoster;
	}
	
	public String getSmallScreen() {
		return "http://src.sencha.io/200/" + screen;
		
		/*int extPos = screen.lastIndexOf(".");
		String fileName = screen.substring(0, extPos);
		String fileType = screen.substring(extPos, screen.length());
		String smallScreen =  fileName + "-218" + fileType;
		return smallScreen;*/
	}
	
}
