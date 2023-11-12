package com.example.Context_Monitoring_Application.calculations;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CalculateRespiratoryRate {
    public Double respiratoryRate = 0.0;

    public CalculateRespiratoryRate(){
    }

    public Double getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Double respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public static double calculateRespiratoryRate(List<Float> csvValues) {
        float previousValue;
        float currentValue;
        previousValue = 10f;
        int k = 0;

        List<Float> x_values = new ArrayList<>();
        List<Float> y_values = new ArrayList<>();
        List<Float> z_values = new ArrayList<>();

        splitList(csvValues, x_values, y_values, z_values);


        for (int i = 0; i < 1279; i++) {
            currentValue = (float) Math.sqrt(
                    Math.pow(x_values.get(i).doubleValue(), 2.0) +
                            Math.pow(y_values.get(i).doubleValue(), 2.0) +
                            Math.pow(z_values.get(i).doubleValue(), 2.0)
            );

            if (Math.abs(previousValue - currentValue) > 0.085f) {
                k++;
            }
            previousValue = currentValue;
        }
        double ret = k / 45.00;
        Log.d("respiratory_rate", String.valueOf((ret * 30)));
        return (ret * 30);
    }

    public static void splitList(List<Float> inputList, List<Float> x_values,
                                 List<Float> y_values, List<Float> z_values) {
        List<Float> currentList = x_values;

        for (Float value : inputList) {
            if (value == 0.0f) {
                if (!currentList.isEmpty()) {
                    if (currentList == x_values) {
                        currentList = y_values;
                    } else if (currentList == y_values) {
                        currentList = z_values;
                    }
                }
            } else {
                currentList.add(value);
            }
        }
    }
}
