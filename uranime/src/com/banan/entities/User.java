package com.banan.entities;

public class User {
	
	public int id;
	
	public Error error;
	
	private class Error{
		public String code;
		public String message;
	}
}
