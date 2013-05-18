package com.nikolay.vb.container;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageContainer implements Serializable, IImageContainer {

	private static final long serialVersionUID = 7101621174879483961L;
	byte[] byteArray;
	public ImageContainer(byte[] byteArray) {
		super();
		this.byteArray = byteArray;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(byteArray);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageContainer other = (ImageContainer) obj;
		if (!Arrays.equals(byteArray, other.byteArray))
			return false;
		return true;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

//	@Override
//	public Bitmap getBitmap() {
//		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//	}
//
//	@Override
//	public void setBitmap(Bitmap bitmap) {
//		convertBitmap(bitmap);
//	}
//	
//	public void convertBitmap(Bitmap bitmap){
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//		byteArray = stream.toByteArray();
//	}

}
