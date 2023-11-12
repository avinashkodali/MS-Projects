package com.example.Context_Monitoring_Application;

import static com.example.Context_Monitoring_Application.SymptomsActivity.symptomRatings;
import static com.example.Context_Monitoring_Application.Constants.REQUEST_CODE_CSV;
import static com.example.Context_Monitoring_Application.Constants.ROW_ID;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Context_Monitoring_Application.calculations.CalculateHeartRate;
import com.example.Context_Monitoring_Application.calculations.CalculateRespiratoryRate;
import com.example.Context_Monitoring_Application.database.DatabaseManager;
import com.example.Context_Monitoring_Application.processing.ProcessCSV;
import com.example.Context_Monitoring_Application.processing.ProcessVideo;


public class MainActivity extends AppCompatActivity {

    public DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar spinnerRespiratoryRate = findViewById(R.id.spinner);
        TextView textViewRespiratoryRate = findViewById(R.id.textViewRespiratoryRateResult);

        ProgressBar spinnerHeartRate = findViewById(R.id.heartRateSpinner);
        TextView textViewHeartRate = findViewById(R.id.textViewHeartRateResult);

        databaseManager = new DatabaseManager(this);

        findViewById(R.id.buttonMeasureRespiratoryRate).setOnClickListener(view -> {
            uploadCSVFile(spinnerRespiratoryRate, textViewRespiratoryRate);
        });

        findViewById(R.id.buttonMeasureHeartRate).setOnClickListener(view -> {
            Double result = ProcessVideo.uploadRecordingAndCalculateHearRate(this);

            if (result == -1) {
                spinnerHeartRate.setVisibility(View.VISIBLE);
            } else {
                symptomRatings.put("Heart Rate", result);
                textViewHeartRate.setVisibility(View.VISIBLE);
                textViewHeartRate.setText("Heart Rate: " + result);
            }
        });

        findViewById(R.id.buttonUploadSigns).setOnClickListener(view -> {
            if (isHeartRateAndRespiratoryRatedataStored() != -1)
                Toast.makeText(MainActivity.this, "Data stored", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Data is not stored", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.buttonSymptoms).setOnClickListener(view -> {
            if (symptomRatings.containsKey("Heart Rate") || symptomRatings.containsKey("Respiratory Rate")) {
                Intent intent = new Intent(view.getContext(), SymptomsActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(view.getContext(), "Please log values for Heart Rate and Respiratory Rate first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadCSVFile(ProgressBar spinner, TextView resultTextView) {
        spinner.setVisibility(View.VISIBLE);
        resultTextView.setVisibility(View.GONE);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, REQUEST_CODE_CSV);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        ProcessCSV.processCSV(this, requestCode, resultCode, resultData);
    }

    private int isHeartRateAndRespiratoryRatedataStored(){
        CalculateRespiratoryRate respiratoryRate = new CalculateRespiratoryRate();
        CalculateHeartRate heartRate = new CalculateHeartRate();

        heartRate.setHeartRate(symptomRatings.get("Heart Rate"));
        respiratoryRate.setRespiratoryRate(symptomRatings.get("Respiratory Rate"));

        ROW_ID = (int) databaseManager.insertHeartRateAndRespiratoryRateValues(heartRate, respiratoryRate);
        return ROW_ID;
    }
}
