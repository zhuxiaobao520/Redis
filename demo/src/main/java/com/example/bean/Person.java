package com.example.bean;

import java.io.Serializable;

public class Person implements Serializable{

	private String name;
	private int age;
	private Head head;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public Head getHead() {
		return head;
	}
	public void setHead(Head head) {
		this.head = head;
	}
	public Person(String name, int age, Head head) {
		super();
		this.name = name;
		this.age = age;
		this.head = head;
	}
	
}
