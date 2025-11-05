package com.example.android_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PatternGestureView extends View {
    private static final String TAG = "PatternGestureView";

    // grid
    private static final int GRID = 3;
    private static final float DOT_RADIUS = 36f;
    private static final float HIT_RADIUS = 80f;

    // paints
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint connectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // geometry
    private final List<PointF> dots = new ArrayList<>();
    private final List<Integer> patternOrder = new ArrayList<>(); // dots in order row by row
    private final List<PointF> rawPoints = new ArrayList<>(); // exact finger path
    private PointF currentPos = null;

    // callback
    private OnPatternCompleteListener listener;

    public PatternGestureView(Context context) {
        super(context);
        init();
    }

    public PatternGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

        dotPaint.setColor(Color.LTGRAY);
        dotPaint.setStyle(Paint.Style.FILL);

        selectedDotPaint.setColor(Color.MAGENTA);
        selectedDotPaint.setStyle(Paint.Style.FILL);

        dotStrokePaint.setColor(Color.DKGRAY);
        dotStrokePaint.setStyle(Paint.Style.STROKE);
        dotStrokePaint.setStrokeWidth(3f);

        connectPaint.setColor(Color.BLUE);
        connectPaint.setStrokeWidth(12f);
        connectPaint.setStyle(Paint.Style.STROKE);

        rawPaint.setColor(Color.argb(160, 33, 150, 243));
        rawPaint.setStrokeWidth(6f);
        rawPaint.setStyle(Paint.Style.STROKE);
    }

    // adjust dot positions
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        dots.clear();
        float spacingX = w / (GRID + 1f);
        float spacingY = h / (GRID + 1f);
        for (int r = 1; r <= GRID; r++) {
            for (int c = 1; c <= GRID; c++) {
                dots.add(new PointF(spacingX * c, spacingY * r));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw raw path
        if (!rawPoints.isEmpty()) {
            for (int i = 1; i < rawPoints.size(); i++) {
                PointF a = rawPoints.get(i - 1);
                PointF b = rawPoints.get(i);
                canvas.drawLine(a.x, a.y, b.x, b.y, rawPaint);
            }
        }
        // draw raw line while moving
        if (currentPos != null && !rawPoints.isEmpty()) {
            PointF last = rawPoints.get(rawPoints.size() - 1);
            canvas.drawLine(last.x, last.y, currentPos.x, currentPos.y, rawPaint);
        }

        // connect visited dots
        for (int i = 1; i < patternOrder.size(); i++) {
            PointF p1 = dots.get(patternOrder.get(i - 1));
            PointF p2 = dots.get(patternOrder.get(i));
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, connectPaint);
        }
        // draw line while moving
        if (currentPos != null && !patternOrder.isEmpty()) {
            PointF last = dots.get(patternOrder.get(patternOrder.size() - 1));
            canvas.drawLine(last.x, last.y, currentPos.x, currentPos.y, connectPaint);
        }

        // draw dots (fill + stroke)
        for (int i = 0; i < dots.size(); i++) {
            PointF p = dots.get(i);
            boolean selected = patternOrder.contains(i);
            canvas.drawCircle(p.x, p.y, DOT_RADIUS, selected ? selectedDotPaint : dotPaint);
            canvas.drawCircle(p.x, p.y, DOT_RADIUS, dotStrokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX();
        float ey = event.getY();
        long t = System.currentTimeMillis();
        float pressure = event.getPressure();

        currentPos = new PointF(ex, ey);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // reset for new stroke
                patternOrder.clear();
                rawPoints.clear();
                addRawPoint(ex, ey, t, pressure);
                checkAndAddDot(ex, ey);
                break;
            case MotionEvent.ACTION_MOVE:
                addRawPoint(ex, ey, t, pressure);
                checkAndAddDot(ex, ey);
                break;
            case MotionEvent.ACTION_UP:
                // finalize and save
                addRawPoint(ex, ey, t, pressure);
                currentPos = null;
                saveGestureToJson();
                if (listener != null) {
                    listener.onPatternComplete(patternOrderToArray());
                }
                break;
        }
        invalidate();
        return true;
    }

    private final List<Long> rawTimes = new ArrayList<>();
    private final List<Float> rawPressures = new ArrayList<>();

    private void addRawPoint(float x, float y, long time, float pressure) {
        rawPoints.add(new PointF(x, y));
        rawTimes.add(time);
        rawPressures.add(pressure);
    }

    // if dot hit and not yet in patternOrder then add index
    private void checkAndAddDot(float x, float y) {
        for (int i = 0; i < dots.size(); i++) {
            PointF d = dots.get(i);
            float dx = d.x - x;
            float dy = d.y - y;
            double dist = Math.hypot(dx, dy);
            if (dist <= HIT_RADIUS && !patternOrder.contains(i)) {
                patternOrder.add(i);
                break;
            }
        }
    }

    private void saveGestureToJson() {
        try {
            JSONObject root = new JSONObject();

            // pattern_order
            JSONArray arrPattern = new JSONArray();
            for (Integer idx : patternOrder) arrPattern.put(idx);
            root.put("pattern_order", arrPattern);

            // points
            JSONArray arrPoints = new JSONArray();
            for (int i = 0; i < rawPoints.size(); i++) {
                PointF p = rawPoints.get(i);
                JSONObject obj = new JSONObject();
                obj.put("x", p.x);
                obj.put("y", p.y);
                obj.put("time", rawTimes.get(i));
                obj.put("pressure", rawPressures.get(i));
                arrPoints.put(obj);
            }
            root.put("points", arrPoints);

            File dir = new File(getContext().getExternalFilesDir(null), "gestures");
            if (!dir.exists()) dir.mkdirs();
            String filename = "gesture_" + System.currentTimeMillis() + ".json";
            File out = new File(dir, filename);

            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(root.toString().getBytes());
            }

            Log.i(TAG, "Gesture saved: " + out.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "saveGestureToJson error", e);
        }
    }

    private int[] patternOrderToArray() {
        int[] a = new int[patternOrder.size()];
        for (int i = 0; i < patternOrder.size(); i++) a[i] = patternOrder.get(i);
        return a;
    }

    // callback interface
    public interface OnPatternCompleteListener {
        void onPatternComplete(int[] patternOrder);
    }

    public void setOnPatternCompleteListener(OnPatternCompleteListener l) {
        this.listener = l;
    }
}
