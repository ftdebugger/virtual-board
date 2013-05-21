package com.nikolay.vb.factory;

import com.nikolay.vb.constants.Constants;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HandlerFactory {

	@SuppressLint("HandlerLeak")
	public static Handler getRefreshDisplayHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.i(Constants.TAG, "Testing Handler");
			}

		};
	}
}
