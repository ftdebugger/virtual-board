package com.nikolay.vb.listener; 

import android.util.Log;

import com.henry.dcoll.dlist.IDListListener;
import com.nikolay.vb.constants.Constants;

public class DSpaceListListener implements IDListListener {
	
	
	@Override
	public void dListPartFound(String owner) {
		Log.i(Constants.TAG,"I found part of D-list. Owner: " + owner);
	}
	
	@Override
	public void dListPartLost(String owner) {
		Log.i(Constants.TAG,"I lost part of D-list. Owner: " + owner);
		
	}
}