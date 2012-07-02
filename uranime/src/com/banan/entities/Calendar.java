package com.banan.entities;

import java.util.List;

public class Calendar {
	public String date;
	public List<CalendarEntry> episodes;
	
	public class CalendarEntry {
		
		public Anime show;
		public Episode episode;
	}
}