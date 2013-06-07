package com.nikolay.vb.container;

import android.graphics.Bitmap;

public interface IImageContainer {
	Bitmap getBitmap();
	void setBitmap(Bitmap bitmap);
	void convertBitmap(Bitmap bitmap);
	byte[] getByteArray();
	void setByteArray(byte[] byteArray) ;
}
