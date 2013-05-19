package com.nikolay.vb.container;

import android.view.MotionEvent;

public class MyMotionEvent implements IMyMotionEvent {
	private float x, y;
	private int action;

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

}
