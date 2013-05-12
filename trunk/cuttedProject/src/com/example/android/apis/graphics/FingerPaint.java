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

import java.io.File;
import java.io.FileOutputStream;

import com.example.android.apis.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.WindowManager;

public class FingerPaint extends GraphicsActivity implements
		ColorPickerDialog.OnColorChangedListener {

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
		mPaint.setXfermode(null);
		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	}

	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;

	public void colorChanged(int color) {
		mPaint.setColor(color);
	}

	public class MyView extends View {

		private static final float MINP = 0.25f;
		private static final float MAXP = 0.75f;

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;

		public MyView(Context c) {
			super(c);

			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.butter);
			mBitmap = Bitmap.createBitmap(mBitmap,0,0,w,h);
			
			mCanvas = new Canvas(mBitmap.copy(Bitmap.Config.ARGB_8888, true));
			mCanvas.setBitmap(mBitmap);
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
//			mPath.reset();
//			try {
//				   String path = Environment.getExternalStorageDirectory().toString();
//			       File file = new File(path, "FitnessGirl.bmp");
//			       FileOutputStream out = new FileOutputStream(file);
//			       mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//			       out.flush();
//			       out.close();
//			       MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
//			} catch (Exception e) {
//			       e.printStackTrace();
//			}
		}

	
        @Override
        public boolean onTouchEvent(MotionEvent event) {
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
            return true;
        }
	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
	private static final int BLUR_MENU_ID = Menu.FIRST + 2;
	private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
		menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
		menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

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
		}
		return super.onOptionsItemSelected(item);
	}
}

//
//
//public class FingerPaint extends Activity {
//
//	// Physical display width and height.
//	private static int displayWidth = 0;
//	private static int displayHeight = 0;
//
//	/** Called when the activity is first created. */
//	@SuppressWarnings("deprecation")
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		// displayWidth and displayHeight will change depending on screen
//		// orientation. To get these dynamically, we should hook onSizeChanged().
//		// This simple example uses only landscape mode, so it's ok to get them
//		// once on startup and use those values throughout.
//		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		displayWidth = display.getWidth(); 		
//		displayHeight = display.getHeight(); 	
//
//		// SampleView constructor must be constructed last as it needs the 
//		// displayWidth and displayHeight we just got.
//		setContentView(new SampleView(this));
//	}
//	
//	private static class SampleView extends View {
//		private static Bitmap bmLargeImage; //bitmap large enough to be scrolled
//		private static Rect displayRect = null; //rect we display to
//		private Rect scrollRect = null; //rect we scroll over our bitmap with
//		private int scrollRectX = 0; //current left location of scroll rect
//		private int scrollRectY = 0; //current top location of scroll rect
//		private float scrollByX = 0; //x amount to scroll by
//		private float scrollByY = 0; //y amount to scroll by
//		private float startX = 0; //track x from one ACTION_MOVE to the next
//		private float startY = 0; //track y from one ACTION_MOVE to the next
//
//		public SampleView(Context context) {
//			super(context);
//
//			// Destination rect for our main canvas draw. It never changes.
//			displayRect = new Rect(0, 0, displayWidth, displayHeight);
//			// Scroll rect: this will be used to 'scroll around' over the 
//			// bitmap in memory. Initialize as above.
//			scrollRect = new Rect(0, 0, displayWidth, displayHeight);
//
//			// Load a large bitmap into an offscreen area of memory.
//			bmLargeImage = BitmapFactory.decodeResource(getResources(), 
//				R.drawable.butter1);
//		}
//		
//		@Override
//		public boolean onTouchEvent(MotionEvent event) {
//
//			switch (event.getAction()) {
//				case MotionEvent.ACTION_DOWN:
//					// Remember our initial down event location.
//					startX = event.getRawX();
//					startY = event.getRawY();
//					break;
//
//				case MotionEvent.ACTION_MOVE:
//					float x = event.getRawX();
//					float y = event.getRawY();
//					// Calculate move update. This will happen many times
//					// during the course of a single movement gesture.
//					scrollByX = x - startX; //move update x increment
//					scrollByY = y - startY; //move update y increment
//					startX = x; //reset initial values to latest
//					startY = y;
//					invalidate(); //force a redraw
//					break;
//			}
//			return true; //done with this event so consume it
//		}
//
//		@Override
//		protected void onDraw(Canvas canvas) {
//
//			// Our move updates are calculated in ACTION_MOVE in the opposite direction
//			// from how we want to move the scroll rect. Think of this as dragging to
//			// the left being the same as sliding the scroll rect to the right.
//			int newScrollRectX = scrollRectX - (int)scrollByX;
//			int newScrollRectY = scrollRectY - (int)scrollByY;
//
//			// Don't scroll off the left or right edges of the bitmap.
//			if (newScrollRectX < 0)
//				newScrollRectX = 0;
//			else if (newScrollRectX > (bmLargeImage.getWidth() - displayWidth))
//				newScrollRectX = (bmLargeImage.getWidth() - displayWidth);
//
//			// Don't scroll off the top or bottom edges of the bitmap.
//			if (newScrollRectY < 0)
//				newScrollRectY = 0;
//			else if (newScrollRectY > (bmLargeImage.getHeight() - displayHeight))
//				newScrollRectY = (bmLargeImage.getHeight() - displayHeight);
//
//			// We have our updated scroll rect coordinates, set them and draw.
//			scrollRect.set(newScrollRectX, newScrollRectY, 
//				newScrollRectX + displayWidth, newScrollRectY + displayHeight);
//			Paint paint = new Paint();
//			canvas.drawBitmap(bmLargeImage, scrollRect, displayRect, paint);
//
//			// Reset current scroll coordinates to reflect the latest updates, 
//			// so we can repeat this update process.
//			scrollRectX = newScrollRectX;
//			scrollRectY = newScrollRectY;
//		}
//	}
//}