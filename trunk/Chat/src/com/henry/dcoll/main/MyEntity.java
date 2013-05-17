package com.henry.dcoll.main;

import java.io.Serializable;

public class MyEntity implements IMyEntity, Serializable {
	private static final long serialVersionUID = 3899433504503344067L;
	private String line;
	private int number;

	public MyEntity(String line, int number) {
		super();
		this.line = line;
		this.number = number;
	}

	@Override
	public String getLine() {
		return line;
	}

	@Override
	public void setLine(String line) {
		this.line = line;
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "MyEntity [line=" + line + ", number=" + number + "]";
	}
}
