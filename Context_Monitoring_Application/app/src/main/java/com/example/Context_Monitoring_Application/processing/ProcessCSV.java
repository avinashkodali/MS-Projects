package com.example.Context_Monitoring_Application.processing;

import static com.example.Context_Monitoring_Application.SymptomsActivity.symptomRatings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.Context_Monitoring_Application.Constants;
import com.example.Context_Monitoring_Application.R;
import com.example.Context_Monitoring_Application.calculations.CalculateRespiratoryRate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessCSV {
    public static void processCSV(Context context, int requestCode, int resultCode, Intent resultData) {
        TextView textViewResult = ((Activity) context).findViewById(R.id.textViewRespiratoryRateResult);
        ProgressBar spinner = ((Activity) context).findViewById(R.id.spinner);
        if (requestCode == Constants.REQUEST_CODE_CSV && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
                    try {
                        List<Float> csvValues = readCSVValues(context, uri);

                        Double result = CalculateRespiratoryRate.
                                calculateRespiratoryRate(csvValues);
                        symptomRatings.put("Respiratory Rate", result);

                        spinner.setVisibility(View.GONE);
                        textViewResult.setVisibility(View.VISIBLE);
                        textViewResult.setText("Respiratory Rate: "+result.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static List<Float> readCSVValues(Context context, Uri uri) throws IOException {
        List<Float> csvValues = new ArrayList<>();
        InputStream csvFile = context.getContentResolver().openInputStream(uri);
        InputStreamReader isr = new InputStreamReader(csvFile);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                float value = Float.parseFloat(line);
                csvValues.add(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return csvValues;
    }
}
