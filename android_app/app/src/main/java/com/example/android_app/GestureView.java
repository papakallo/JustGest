package com.example.android_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/*
 * THIS CLASS IS NOT USED FOR NOW - IT'S BEEN FUSED --> CHECK PATTERNGESTUREVIEW
 */

public class GestureView extends View {

    private ArrayList<GesturePoint> gesturePoints = new ArrayList<>();
    private Paint paint;

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.argb(100, 0, 255, 255));
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 1; i < gesturePoints.size(); i++) {
            GesturePoint p1 = gesturePoints.get(i - 1);
            GesturePoint p2 = gesturePoints.get(i);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float pressure = event.getPressure();
        long time = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gesturePoints.clear();
                gesturePoints.add(new GesturePoint(x, y, time, pressure));
                break;
            case MotionEvent.ACTION_MOVE:
                gesturePoints.add(new GesturePoint(x, y, time, pressure));
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) listener.onGestureComplete();
                saveGestureToJson();
                break;
        }

        invalidate();
        return true;
    }

    private void saveGestureToJson() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (GesturePoint p : gesturePoints) {
                JSONObject obj = new JSONObject();
                obj.put("x", p.x);
                obj.put("y", p.y);
                obj.put("time", p.time);
                obj.put("pressure", p.pressure);
                jsonArray.put(obj);
            }

            JSONObject gestureObject = new JSONObject();
            gestureObject.put("points", jsonArray);


            File folder = new File(getContext().getExternalFilesDir("gestures"), "data");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filename = "gesture_" + System.currentTimeMillis() + ".json";
            File file = new File(folder, filename);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(gestureObject.toString().getBytes());
            fos.close();

            System.out.println("Gesture saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private OnGestureCompleteListener listener;

    public interface OnGestureCompleteListener {
        void onGestureComplete();
    }

    public void setOnGestureCompleteListener(OnGestureCompleteListener l) {
        this.listener = l;
    }

}
