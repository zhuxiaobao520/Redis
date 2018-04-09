package com.example.bean;

import java.io.Serializable;

public class Head implements Serializable {

	private int one;
	private int two;
	public int getOne() {
		return one;
	}
	public void setOne(int one) {
		this.one = one;
	}
	public int getTwo() {
		return two;
	}
	public void setTwo(int two) {
		this.two = two;
	}
	public Head(int one, int two) {
		super();
		this.one = one;
		this.two = two;
	}
	
}
