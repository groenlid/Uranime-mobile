package com.banan.entities;

public class Media {
	
	public  String title;
	
	public  int year;
	
	public  String url;
	
	// This could be enum
	public  String certification;
	
	public  String overview;
	
	public  Image images;

	public Image getImages() {
		return images;
	}

	public  void setTitle(String title) {
		this.title = title;
	}

	public  String getTitle() {
		return title;
	}
	
	public String toString() {
		return title + "; " + url ;
	}
}
