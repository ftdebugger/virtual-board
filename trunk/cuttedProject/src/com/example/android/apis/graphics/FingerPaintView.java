package com.example.android.apis.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.apis.R;
import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.peer.PeerInfo;
import com.nikolay.vb.constants.Constants;
import com.nikolay.vb.container.DrawController;
import com.nikolay.vb.container.IDrawController;
import com.nikolay.vb.container.IMyMotionEvent;
import com.nikolay.vb.container.MyMotionEvent;
import com.nikolay.vb.listener.DSpaceListListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FingerPaintView extends View {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Path mRemotePath;
    private Paint mBitmapPaint;
    private Paint paint = new Paint();
    private String activeSeed = "";
    public DList<IMyMotionEvent> dList;
    public DList<IDrawController> drawController;
    private List<MyMotionEvent> spreadEvents = new ArrayList<MyMotionEvent>();
    private int drawed = 0;
    private int spreaded = 0;
    private String seedName = Build.MODEL.replace(" ", "");
    private FingerPaint fingerPaint;

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

    public FingerPaintView(Context c) {
        super(c);
        this.fingerPaint = (FingerPaint) c;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DSpaceController.disconnect();
            }
        });
        DSpaceController.connect(seedName);
        dList = DSpaceController.createNewDList("mySpace",
                "list1", new DSpaceListListener(), new ArrayList<IMyMotionEvent>(),
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
        mBitmap = BitmapFactory.decodeResource(fingerPaint.getResources(),
                R.drawable.butter);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        canvas.drawColor(0xFFAAAAAA);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, fingerPaint.getPaint());
        canvas.drawPath(mRemotePath, fingerPaint.getPaint());
        if (!"".equals(activeSeed)) {
            canvas.drawCircle(rmX, rmY, TOUCH_TOLERANCE + 2, fingerPaint.getPaint());
        }
        canvas.drawText(activeSeed, rmX, rmY, paint);
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
        mCanvas.drawPath(mPath, fingerPaint.getPaint());
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
                            if (fingerPaint.getErase()) {
                                fingerPaint.setErase(false);
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
        mCanvas.drawPath(mRemotePath, fingerPaint.getPaint());
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
