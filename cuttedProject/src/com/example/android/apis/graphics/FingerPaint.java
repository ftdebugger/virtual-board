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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.apis.R;
import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.dlist.IDListListener;
import com.henry.dcoll.peer.PeerInfo;
import com.nikolay.vb.constants.Constants;
import com.nikolay.vb.container.DrawController;
import com.nikolay.vb.container.IDrawController;
import com.nikolay.vb.container.IMyMotionEvent;
import com.nikolay.vb.container.MyMotionEvent;
import com.nikolay.vb.factory.HandlerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FingerPaint extends GraphicsActivity implements
		ColorPickerDialog.OnColorChangedListener {
	public Handler displayRefreshHandler;

	static {
		Log.i("Test", "System.loadLibrary(\"alljoyn_java\")");
		System.loadLibrary("alljoyn_java");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		displayRefreshHandler = HandlerFactory.getRefreshDisplayHandler();
		setContentView(new MyView(this));

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);
		mPaint.setTextSize(25F);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	}

	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;
	private Boolean erase = false;

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
			Log.i(Constants.TAG, "I found part of D-list. Owner: " + owner);
			// CharSequence text = "I found part of D-list. Owner: " + owner;
			// int duration = Toast.LENGTH_LONG;
			// Toast toast = Toast.makeText(getBaseContext(), text, duration);
			// toast.show();
		}

		@Override
		public void dListPartLost(String owner) {
			listFounded = false;
			Log.i(Constants.TAG, "I lost part of D-list. Owner: " + owner);
			// CharSequence text = "I lost part of D-list. Owner: " + owner;
			// int duration = Toast.LENGTH_LONG;
			// Toast toast = Toast.makeText(getBaseContext(), text, duration);
			// toast.show();

		}
	}

	public class MyView extends View {
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Path mRemotePath;
		private Paint mBitmapPaint;
		private String activeSeed = "";
		public DList<IMyMotionEvent> dList;
		public DList<IDrawController> drawController;
		private List<MyMotionEvent> spreadEvents = new ArrayList<MyMotionEvent>();
		private int drawed = 0;
		private int spreaded = 0;
		private String seedName = android.os.Build.MODEL.replace(" ", "");

		public Runnable runnableSpread = new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						if (spreadEvents.size() > spreaded) {
							for (int i = spreaded; i < spreadEvents.size(); i++) {
								spreadRemoteEvents(spreadEvents.get(i));
							}
							spreaded = spreadEvents.size();
						} else {
							// reconnectDlist();
						}

						Thread.sleep(50);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		public MyView(Context c) {
			super(c);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					DSpaceController.disconnect();
				}
			});
			DSpaceController.connect(seedName);
			dList = DSpaceController.createNewDList("mySpace",
					"list1", // space namelist logger mylist interface.name
					new DSpaceListListener(), new ArrayList<IMyMotionEvent>(),
					IMyMotionEvent.class);
			drawController = DSpaceController.createNewDList("mySpace",
					"controller", new ArrayList<IDrawController>(),
					IDrawController.class);
			drawController.add(new DrawController());
			callAsynchronousTask();
		    callSpreadEventsTask();
			mPath = new Path();
			mRemotePath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
//			new Thread(runnableSpread).start();
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
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Style.FILL);
			canvas.drawPaint(paint);

			paint.setColor(Color.BLACK);
			paint.setTextSize(20);

			canvas.drawColor(0xFFAAAAAA);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
			canvas.drawPath(mRemotePath, mPaint);
			if (!"".equals(activeSeed)) {
				canvas.drawCircle(rmX, rmY, TOUCH_TOLERANCE + 2, mPaint);
			}
			// canvas.drawText(activeSeed, rmX, rmY, paint);

		}

		private float mX, mY;
		private float rmX, rmY;
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
			// displayRefreshHandler.sendEmptyMessage(0);

			float x = event.getX();
			float y = event.getY();
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
				break;
			}
			spreadEvents.add(new MyMotionEvent(event));
			// spreadRemoteEvents(new MyMotionEvent(event));
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
					events = dList.get(peer.getPeerName());
					if (events.isEmpty() || events == null) {
						fetched = false;
					} else {
						fetched = true;
						activeSeed = peer.getPeerName();
					}
					if (fetched) {
						drawRemoteEvents(events);
					}

				}
			} catch (Exception ex) {
				Log.i(Constants.TAG,
						"get RemoteEvents Exeption " + ex.toString());
				// reconnectDlist();
			}
			return events;
		}

		public void spreadRemoteEvents(MyMotionEvent myMotionEvent) {
			try {
				List<PeerInfo> peers = new ArrayList<PeerInfo>();
				peers.addAll(DSpaceController.getPeerContainer().getPeers()
						.keySet());
				for (PeerInfo peer : peers) {
					dList.add(peer.getPeerName(), myMotionEvent);
				}
			} catch (Exception ex) {
				Log.i(Constants.TAG, "spreadEvents Exeption " + ex.toString());
			}
		}

		public void drawRemoteEvents(List<IMyMotionEvent> events) {
			// int remoteListSize = events.size();
			for (int i = drawed; i < events.size(); i++) {
				drawMyEvent(events.get(i));
			}
			// while ((dList.size() > 0) || (remoteListSize > 0)) {
			// dList.remove(0);
			// remoteListSize--;
			// }
			// while (dList.size() > 0) {
			// dList.remove(0);
			// }
		}

		public void drawMyEvent(IMyMotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getMotionEvent()) {
			case MotionEvent.ACTION_DOWN:
				remoteTouch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				remoteTouch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				event.setPeerName("");
				remoteTouch_up();
				invalidate();
				break;
			}
			activeSeed = event.getPeerName();
		}

		private void reconnectDlist() {
			showToast("recreating Dlist");
			DSpaceController.destroyDList(dList);
			dList = DSpaceController
					.createNewDList("mySpace", "list1",
							new DSpaceListListener(), dList.get(),
							IMyMotionEvent.class);
		}

		public void showToast(String message) {
			CharSequence text = message;
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(getContext(), text, duration);
			toast.show();
		}

		public void callAsynchronousTask() {
			final Handler handler = new Handler();
			Timer timer = new Timer();
			TimerTask doAsynchronousTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							try {
								if (erase) {
									erase = false;
									erase();
								}
								// if (drawController.get().get(0).getErase()) {
								// drawController.get().get(0).setErase(false);
								// erase();
								// }
								if (dList.size() > drawed) {
									processEvents();
									drawed = dList.size();
								} else {
									// reconnectDlist();
								}
							} catch (Exception e) {
								Log.i(Constants.TAG, e.toString());
							}
						}
					});
				}
			};
			timer.schedule(doAsynchronousTask, 0, 100); // execute in every
														// 50000 ms
		}

		public void callSpreadEventsTask() {
			final Handler handler = new Handler();
			Timer timer = new Timer();
			TimerTask doAsynchronousTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							try {

								if (spreadEvents.size() > spreaded) {
									for (int i = spreaded; i < spreadEvents
											.size(); i++) {
										spreadRemoteEvents(spreadEvents.get(i));
									}
									spreaded = spreadEvents.size();
								} else {
									// reconnectDlist();
								}
							} catch (Exception e) {
								Log.i(Constants.TAG, e.toString());
							}
						}
					});
				}
			};
			timer.schedule(doAsynchronousTask, 0, 100); // execute in every
														// 50000 ms
		}

		private void remoteTouch_start(float x, float y) {
			mRemotePath.reset();
			mRemotePath.moveTo(x, y);
			rmX = x;
			rmY = y;
		}

		private void remoteTouch_move(float x, float y) {
			float dx = Math.abs(x - rmX);
			float dy = Math.abs(y - rmY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mRemotePath.quadTo(rmX, rmY, (x + rmX) / 2, (y + rmY) / 2);
				rmX = x;
				rmY = y;
			}
		}

		private void remoteTouch_up() {
			mRemotePath.lineTo(rmX, rmY);
			// commit the path to our offscreen
			mCanvas.drawPath(mRemotePath, mPaint);
			// kill this so we don't double draw
		}

		private void clearDlist() {
			dList.removeAll();
		}

		private void erase() {
			mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			drawed = 0;
			mPath.reset();
			mRemotePath.reset();
			clearDlist();
			invalidate();
		}

		public void eraseAll() {
			try {
				List<PeerInfo> peers = new ArrayList<PeerInfo>();
				peers.addAll(DSpaceController.getPeerContainer().getPeers()
						.keySet());
				for (PeerInfo peer : peers) {
					drawController.get(peer.getPeerName(), 0).setErase(true);
				}
			} catch (Exception ex) {
				Log.i(Constants.TAG, "spreadEvents Exeption " + ex.toString());
			}
		}

		public void processEvents() {
			List<IMyMotionEvent> events = null;
			try {
				events = dList.get();
				if (!events.isEmpty() || events != null) {
					drawRemoteEvents(events);
				}
			} catch (Exception ex) {
				Log.i(Constants.TAG, "draw events Exeption " + ex.toString());
			}
		}

	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
	private static final int BLUR_MENU_ID = Menu.FIRST + 2;
	private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
	private static final int CLEAR_SCREEN_ID = Menu.FIRST + 5;
	private static final int CLEAR_ALL = Menu.FIRST + 6;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
		menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
		menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
		menu.add(0, CLEAR_SCREEN_ID, 0, "Clear").setShortcut('6', 'y');
		menu.add(0, CLEAR_SCREEN_ID, 0, "Clear All").setShortcut('7', 'y');

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
		case CLEAR_SCREEN_ID:
			// DSpaceController.disconnect();
			// DSpaceController.connect(android.os.Build.MODEL.replace(" ",
			// ""));
			erase = true;
			return true;
		case CLEAR_ALL:
			erase = true;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
