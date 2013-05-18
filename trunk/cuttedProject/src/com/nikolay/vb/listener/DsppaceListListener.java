package com.nikolay.vb.listener; 

import android.util.Log;

import com.henry.dcoll.dlist.IDListListener;
import com.henry.dcoll.main.Runner;

public class DsppaceListListener implements IDListListener {
	
	
	@Override
	public void dListPartFound(String owner) {
		Log.i(Runner.TAG,"I found part of D-list. Owner: " + owner);
	}
	
	@Override
	public void dListPartLost(String owner) {
		Log.i(Runner.TAG,"I lost part of D-list. Owner: " + owner);
		
	}
}