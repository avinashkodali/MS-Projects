package com.example.Context_Monitoring_Application.calculations;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class CalculateHeartRate {
    public Double heartRate = 0.0;

    public CalculateHeartRate(){
    }

    public Double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Double heartRate) {
        this.heartRate = heartRate;
    }

    public static Double calculateHeartRate(List<Bitmap> frameList) {
        List<Long> a = new ArrayList<>();
        long redBucket = 0;

        for (Bitmap i : frameList) {
            redBucket = 0;
            for (int y = 0; y < i.getHeight(); y++) {
                for (int x = 0; x < i.getWidth(); x++) {
                    int c = i.getPixel(x, y);
                    redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                }
            }
            a.add(redBucket);
        }

        List<Long> b = new ArrayList<>();
        for (int i = 0; i < a.size() - 5; i++) {
            long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
            b.add(temp);
        }

        long x = b.get(0);
        int count = 0;
        for (int i = 1; i < b.size(); i++) {
            long p = b.get(i);
            if ((p - x) > 3500) {
                count++;
            }
            x = b.get(i);
        }

        int rate = (int) ((count * 60.0) / 90.0);
        return (double) (rate/2);
    }
}
