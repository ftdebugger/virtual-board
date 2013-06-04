package com.nikolay.vb.container;

import android.view.MotionEvent;

public interface IMyMotionEvent {
	float getX();
	void setX(float x);
	float getY();
	void setY(float y);
	int getMotionEvent();
	void setMotionEvent(int action);
	String getPeerName();
	void setPeerName(String peerName);
}
