package com.example.android_app;

import com.fastdtw.FastDTW;
import com.fastdtw.dtw.WarpDist;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GestureVerifierDTW {

    private static final double VERIFICATION_THRESHOLD = 8000.0;

    private List<TimeSeries> templates;
    private Gson gson;

    public GestureVerifierDTW() {
        this.templates = new ArrayList<>();
        this.gson = new Gson();
    }


    public void enroll(String gestureJson) {
        GestureData gesture = gson.fromJson(gestureJson, GestureData.class);
        if (gesture == null || gesture.points == null) return;

        TimeSeries template = GestureProcessor.preprocess(gesture.points);
        this.templates.add(template);
        System.out.println("Enrolled a new template.");
    }


    public boolean verify(String gestureJson) {
        if (templates.isEmpty()) {
            System.err.println("No templates enrolled. Cannot verify.");
            return false;
        }

        GestureData queryGesture = gson.fromJson(gestureJson, GestureData.class);
        if (queryGesture == null || queryGesture.points == null) return false;
        TimeSeries queryTimeSeries = GestureProcessor.preprocess(queryGesture.points);

        double minDistance = Double.MAX_VALUE;

        for (TimeSeries template : templates) {
            WarpDist result = FastDTW.getWarpDistBetween(queryTimeSeries, template, Distances.EUCLIDEAN_DISTANCE);
            double distance = result.getDistance();

            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        System.out.println("Verification check: Min Distance = " + minDistance);
        return minDistance < VERIFICATION_THRESHOLD;
    }
}