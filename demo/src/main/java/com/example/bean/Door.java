package com.example.bean;

import java.io.Serializable;

public class Door implements Serializable   {

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Door(int id) {
		super();
		this.id = id;
	}
	
}
