package com.example.android_app;

public class GesturePoint {
    public float x;
    public float y;
    public long time;
    public float pressure;

    public GesturePoint(float x, float y, long time, float pressure) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.pressure = pressure;
    }
}
