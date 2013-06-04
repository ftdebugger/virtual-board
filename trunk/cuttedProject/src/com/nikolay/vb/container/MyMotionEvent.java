package com.nikolay.vb.container;

import java.io.Serializable;

import android.view.MotionEvent;

public class MyMotionEvent implements IMyMotionEvent , Serializable{

	private static final long serialVersionUID = -475829583782092474L;
	private float x, y;
	private int action;
	private String peerName = android.os.Build.MODEL.replace(" ", "");
	@Override
	public String getPeerName() {
		return peerName;
	}
	@Override
	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}

	public MyMotionEvent() {
		super();
	}

	public MyMotionEvent(float x, float y, int action) {
		super();
		this.x = x;
		this.y = y;
		this.action = action;
	}

	public MyMotionEvent(MotionEvent event) {
		super();
		this.x = event.getX();
		this.y = event.getY();
		this.action = event.getAction();
		this.peerName = android.os.Build.MODEL.replace(" ", "");
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public int getMotionEvent() {
		return action;
	}

	@Override
	public void setMotionEvent(int action) {
		this.action = action;
	}

}
