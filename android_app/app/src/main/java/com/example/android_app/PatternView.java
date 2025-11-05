package com.example.android_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/*
 * THIS CLASS IS NOT USED FOR NOW - IT'S BEEN FUSED --> CHECK PATTERNGESTUREVIEW
 */

public class PatternView extends View {
    private static final int GRID_SIZE = 3;
    private static final float DOT_RADIUS = 40f;
    private Paint dotPaint, linePaint;
    private final List<PointF> dots = new ArrayList<>();
    private final List<PointF> selectedDots = new ArrayList<>();
    private PointF currentPos = null;

    public PatternView(Context context) {
        super(context);
        init();
    }

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.MAGENTA);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(10f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dots.clear();

        float spacingX = w / (GRID_SIZE + 1f);
        float spacingY = h / (GRID_SIZE + 1f);

        for (int row = 1; row <= GRID_SIZE; row++) {
            for (int col = 1; col <= GRID_SIZE; col++) {
                float x = spacingX * col;
                float y = spacingY * row;
                dots.add(new PointF(x, y));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // line
        for (int i = 1; i < selectedDots.size(); i++) {
            PointF p1 = selectedDots.get(i - 1);
            PointF p2 = selectedDots.get(i);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint);
        }

        // line in movement
        if (currentPos != null && !selectedDots.isEmpty()) {
            PointF last = selectedDots.get(selectedDots.size() - 1);
            canvas.drawLine(last.x, last.y, currentPos.x, currentPos.y, linePaint);
        }

        // dots
        for (PointF p : dots) {
            canvas.drawCircle(p.x, p.y, DOT_RADIUS, dotPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentPos = new PointF(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedDots.clear();
                checkAndAddDot(currentPos);
                break;
            case MotionEvent.ACTION_MOVE:
                checkAndAddDot(currentPos);
                break;
            case MotionEvent.ACTION_UP:
                currentPos = null;
                break;
        }
        invalidate();

        //  to GestureView
        View parent = (View) getParent();
        if (parent != null) {
            parent.dispatchTouchEvent(event);
        }

        return true;
    }

    private void checkAndAddDot(PointF p) {
        for (PointF dot : dots) {
            float dx = dot.x - p.x;
            float dy = dot.y - p.y;
            if (Math.sqrt(dx * dx + dy * dy) < DOT_RADIUS * 1.5f && !selectedDots.contains(dot)) {
                selectedDots.add(dot);
                break;
            }
        }
    }

    public List<PointF> getSelectedDots() {
        return selectedDots;
    }
}
