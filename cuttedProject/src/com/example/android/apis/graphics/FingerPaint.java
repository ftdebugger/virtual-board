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
import java.util.Timer;
import java.util.TimerTask;

import java.util.List;

import com.example.android.apis.R;
import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.dlist.IDListListener;
import com.henry.dcoll.main.IMyEntity;
import com.henry.dcoll.main.Runner;
import com.henry.dcoll.main.Runner.MyDListListener;
import com.henry.dcoll.peer.PeerInfo;
import com.nikolay.vb.constants.Constants;
import com.nikolay.vb.container.DrawController;
import com.nikolay.vb.container.IMyMotionEvent;
import com.nikolay.vb.container.MyMotionEvent;
import com.nikolay.vb.container.IDrawController;
import com.nikolay.vb.factory.HandlerFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.Toast;

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
		private ScaleGestureDetector detector;
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

		private static final float MIN_ZOOM = 1f;
		private static final float MAX_ZOOM = 5f;

		private float scaleFactor = 1.f;

		Matrix matrix;
		int viewWidth, viewHeight;
		static final int CLICK = 3;
		float saveScale = 1f;
		protected float origWidth, origHeight;
		int oldMeasuredWidth, oldMeasuredHeight;
		float[] m;

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			viewWidth = MeasureSpec.getSize(widthMeasureSpec);
			viewHeight = MeasureSpec.getSize(heightMeasureSpec);

			//
			// Rescales image on rotation
			//
			if (oldMeasuredHeight == viewWidth
					&& oldMeasuredHeight == viewHeight || viewWidth == 0
					|| viewHeight == 0)
				return;
			oldMeasuredHeight = viewHeight;
			oldMeasuredWidth = viewWidth;

			if (saveScale == 1) {
				// Fit to screen.
				float scale;

				if (mBitmap == null || mBitmap.getWidth() == 0
						|| mBitmap.getHeight() == 0)
					return;
				int bmWidth = mBitmap.getWidth();
				int bmHeight = mBitmap.getHeight();

				Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : "
						+ bmHeight);

				float scaleX = (float) viewWidth / (float) bmWidth;
				float scaleY = (float) viewHeight / (float) bmHeight;
				scale = Math.min(scaleX, scaleY);
				matrix.setScale(scale, scale);

				// Center the image
				float redundantYSpace = (float) viewHeight
						- (scale * (float) bmHeight);
				float redundantXSpace = (float) viewWidth
						- (scale * (float) bmWidth);
				redundantYSpace /= (float) 2;
				redundantXSpace /= (float) 2;

				matrix.postTranslate(redundantXSpace, redundantYSpace);

				origWidth = viewWidth - 2 * redundantXSpace;
				origHeight = viewHeight - 2 * redundantYSpace;
				// mCanvas.setMatrix(matrix);
				mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) origWidth,
						(int) origHeight, false);
				mCanvas.setBitmap(mBitmap);
			}
			fixTrans();
		}

		void fixTrans() {
			matrix.getValues(m);
			// mCanvas.setMatrix(matrix);
			float transX = m[Matrix.MTRANS_X];
			float transY = m[Matrix.MTRANS_Y];

			float fixTransX = getFixTrans(transX, viewWidth, origWidth
					* saveScale);
			float fixTransY = getFixTrans(transY, viewHeight, origHeight
					* saveScale);

			if (fixTransX != 0 || fixTransY != 0)
				matrix.postTranslate(fixTransX, fixTransY);
		}

		float getFixTrans(float trans, float viewSize, float contentSize) {
			float minTrans, maxTrans;

			if (contentSize <= viewSize) {
				minTrans = 0;
				maxTrans = viewSize - contentSize;
			} else {
				minTrans = viewSize - contentSize;
				maxTrans = 0;
			}

			if (trans < minTrans)
				return -trans + minTrans;
			if (trans > maxTrans)
				return -trans + maxTrans;
			return 0;
		}

		float minScale = 1f;
		float maxScale = 3f;
		static final int ZOOM = 2;
		int mode = 0;

		@SuppressLint("NewApi")
		private class ScaleListener extends
				ScaleGestureDetector.SimpleOnScaleGestureListener {

			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				mode = ZOOM;
				return true;
			}

			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				mScaleFactor = detector.getScaleFactor();
				float origScale = saveScale;
				saveScale *= mScaleFactor;
				if (saveScale > maxScale) {
					saveScale = maxScale;
					mScaleFactor = maxScale / origScale;
				} else if (saveScale < minScale) {
					saveScale = minScale;
					mScaleFactor = minScale / origScale;
				}

				if (origWidth * saveScale <= viewWidth
						|| origHeight * saveScale <= viewHeight)
					matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
							viewHeight / 2);
				else
					matrix.postScale(mScaleFactor, mScaleFactor,
							detector.getFocusX(), detector.getFocusY());

				fixTrans();
				return true;
			}
			
			@Override
			public void onScaleEnd(ScaleGestureDetector arg0) {
				// TODO Auto-generated method stub
				super.onScaleEnd(arg0);
				mode = 0;
			}
		}

		@SuppressLint("NewApi")
		public MyView(Context c) {
			super(c);
			matrix = new Matrix();
			m = new float[9];
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.butter);
			// mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
			mCanvas = new Canvas(mBitmap);
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
			// new Thread(runnableSpread).start();
			detector = new ScaleGestureDetector(getContext(),
					new ScaleListener());
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			// mBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.butter);
			// // mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			// mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
			// mCanvas = new Canvas(mBitmap);
		}

		private float mScaleFactor = 1;

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.save();
			canvas.scale(mScaleFactor, mScaleFactor);
			// canvas.setMatrix(matrix);

			// Paint paint = new Paint();
			// paint.setColor(Color.WHITE);
			// paint.setStyle(Style.FILL);
			// canvas.drawPaint(paint);

			// paint.setColor(Color.BLACK);
			// paint.setTextSize(20);

			// canvas.drawColor(0xFFAAAAAA);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
			canvas.drawPath(mRemotePath, mPaint);
			if (!"".equals(activeSeed)) {
				canvas.drawCircle(rmX, rmY, TOUCH_TOLERANCE + 2, mPaint);
			}
			// canvas.drawText(activeSeed, rmX, rmY, paint);

			canvas.restore();

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
			detector.onTouchEvent(event);
			float x = event.getX();
			float y = event.getY();
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				touch_start(x, y);
//				break;
//			case MotionEvent.ACTION_MOVE:
//				if (mode != ZOOM) {
//					touch_move(x, y);
//				}
//				break;
//			case MotionEvent.ACTION_UP:
//				touch_up();
//				break;
//			}
			invalidate();
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
