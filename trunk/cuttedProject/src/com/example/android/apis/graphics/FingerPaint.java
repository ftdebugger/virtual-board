/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import java.util.ArrayList;

import java.util.List;

import com.example.android.apis.R;
import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.dlist.IDListListener;
import com.henry.dcoll.main.IMyEntity;
import com.henry.dcoll.main.Runner;
import com.henry.dcoll.main.Runner.MyDListListener;
import com.henry.dcoll.peer.PeerInfo;
import com.nikolay.vb.container.IMyMotionEvent;
import com.nikolay.vb.container.MyMotionEvent;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class FingerPaint extends GraphicsActivity implements
		ColorPickerDialog.OnColorChangedListener {
	static {
		Log.i("Test", "System.loadLibrary(\"alljoyn_java\")");
		System.loadLibrary("alljoyn_java");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new MyView(this));

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	}

	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;

	public void colorChanged(int color) {
		mPaint.setColor(color);
	}

	public class DSpaceListListener implements IDListListener {

		private Boolean listFounded = false;

		public Boolean getListFounded() {
			return listFounded;
		}

		public void setListFounded(Boolean listFounded) {
			this.listFounded = listFounded;
		}

		@Override
		public void dListPartFound(String owner) {
			listFounded = true;
			Log.i(Runner.TAG, "I found part of D-list. Owner: " + owner);
			CharSequence text = "I found part of D-list. Owner: " + owner;
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(getBaseContext(), text, duration);
			toast.show();
		}

		@Override
		public void dListPartLost(String owner) {
			listFounded = false;
			Log.i(Runner.TAG, "I lost part of D-list. Owner: " + owner);
			CharSequence text = "I lost part of D-list. Owner: " + owner;
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(getBaseContext(), text, duration);
			toast.show();

		}
	}

	public class MyView extends View {
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;
		private String activeSeed = null;
		public DList<IMyMotionEvent> dList;

		public MyView(Context c) {
			super(c);
			try{
				Runner.main();
			}catch(Exception ex){
				Log.i(Runner.TAG,ex.toString());
			}
			DSpaceController.connect(android.os.Build.MODEL.replace(" ", ""));
			dList = DSpaceController.createNewDList("mySpace",
					"list1", // space namelist logger mylist interface.name
					new DSpaceListListener(), new ArrayList<IMyMotionEvent>(),
					IMyMotionEvent.class);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.butter);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0xFFAAAAAA);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			dList.add(new MyMotionEvent(event));

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				if (((DSpaceListListener) dList.getListener()).getListFounded()) {
//					if(dList.size()>0){
					getRemoteEvents();
//				}
				}
				break;
			}
			return true;
		}

		public List<IMyMotionEvent> getRemoteEvents() {
			List<IMyMotionEvent> events = null;

			try {
				List<PeerInfo> peers = new ArrayList<PeerInfo>();
				peers.addAll(DSpaceController.getPeerContainer().getPeers()
						.keySet());
				Boolean fetched = false;
				for (PeerInfo peer : peers) {
					try {
						events = dList.get(peer.getPeerName());
					} catch (Exception ex) {
						Log.i(Runner.TAG, "no events " + ex.toString());
						
						dList = DSpaceController.createNewDList(
								"mySpace",
								"list1", // space namelist logger mylist
											// interface.name
								new MyDListListener(),
								dList.get(),
								IMyMotionEvent.class);
					}
					if (events.isEmpty() || events == null) {
						fetched = false;
					} else {
						fetched = true;
						activeSeed = peer.getPeerName();
						break;
					}
				}
				if (fetched) {
					drawRemoteEvents(events);
				}

				showToast("fectched " + fetched.toString());

			} catch (Exception ex) {
				Log.i(Runner.TAG, "get RemoteEvents Exeption " + ex.toString());
				reconectDlist();
			}
			return events;
		}

		public void showToast(String message) {
			CharSequence text = message;
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(getContext(), text, duration);
			toast.show();
		}

		public void drawRemoteEvents(List<IMyMotionEvent> events) {
			for (int i = 0; i < events.size(); i++) {
				drawMyEvent(events.get(i));
				if (activeSeed != null && !"".equals(activeSeed)) {
					dList.remove(activeSeed, i);
				}
			}
		}

		public void drawMyEvent(IMyMotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			switch (event.getMotionEvent()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
		}

		private void reconectDlist() {
			showToast("recreating Dlist");
			dList = DSpaceController.createNewDList("mySpace", "list1", // space
																		// namelist
																		// logger
																		// mylist
																		// interface.name
					new DSpaceListListener(), dList.get(), IMyMotionEvent.class);
		}

	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
	private static final int BLUR_MENU_ID = Menu.FIRST + 2;
	private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
	private static final int RECONNETC_MENU_ID = Menu.FIRST + 5;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
		menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
		menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
		menu.add(0, RECONNETC_MENU_ID, 0, "Reconnect").setShortcut('6', 'y');

		/****
		 * Is this the mechanism to extend with filter effects? Intent intent =
		 * new Intent(null, getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions( Menu.ALTERNATIVE, 0, new ComponentName(this,
		 * NotesList.class), null, intent, 0, null);
		 *****/
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);

		switch (item.getItemId()) {
		case COLOR_MENU_ID:
			new ColorPickerDialog(this, this, mPaint.getColor()).show();
			return true;
		case EMBOSS_MENU_ID:
			if (mPaint.getMaskFilter() != mEmboss) {
				mPaint.setMaskFilter(mEmboss);
			} else {
				mPaint.setMaskFilter(null);
			}
			return true;
		case BLUR_MENU_ID:
			if (mPaint.getMaskFilter() != mBlur) {
				mPaint.setMaskFilter(mBlur);
			} else {
				mPaint.setMaskFilter(null);
			}
			return true;
		case ERASE_MENU_ID:
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			return true;
		case SRCATOP_MENU_ID:
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
			mPaint.setAlpha(0x80);
			return true;
		case RECONNETC_MENU_ID:
			DSpaceController.disconnect();
			DSpaceController.connect(android.os.Build.MODEL.replace(" ", ""));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
