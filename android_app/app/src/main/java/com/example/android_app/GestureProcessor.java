package com.example.android_app;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBuilder;
import java.util.List;

public class GestureProcessor {

    public static TimeSeries preprocess(List<PointData> points) {
        if (points == null || points.isEmpty()) {
            return new TimeSeriesBuilder().build();
        }

        TimeSeriesBuilder builder = new TimeSeriesBuilder();

        PointData firstPoint = points.get(0);
        double startX = firstPoint.x;
        double startY = firstPoint.y;

        for (int i = 0; i < points.size(); i++) {
            PointData p = points.get(i);


            double normX = p.x - startX;
            double normY = p.y - startY;

            double[] featureVector = new double[]{normX, normY, p.pressure};
            
            builder.add(p.time, featureVector);
        }

        return builder.build();
    }
}