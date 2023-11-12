package com.example.Context_Monitoring_Application;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.Context_Monitoring_Application.calculations.CalculateHeartRate;
import com.example.Context_Monitoring_Application.calculations.CalculateRespiratoryRate;
import com.example.Context_Monitoring_Application.database.DatabaseManager;
import com.example.Context_Monitoring_Application.database.RatingOfSymptom;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity {
    private Spinner symptomsSpinner;
    private RatingBar ratingBar;
    private DatabaseManager databaseHelper;
    public static HashMap<String, Double> symptomRatings = new HashMap<>();
    private String presentSelectedSymptom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        displaySymptomsSpinnerAndRatingBar();
        handleSymptomRatingChangesToHashMap();

        findViewById(R.id.buttonUploadSymptoms).setOnClickListener(view -> updateDataBaseWithSymptomRatings());

        findViewById(R.id.buttonDeleteAllRows).setOnClickListener(view -> deleteDataBaseRows());
    }

    private void deleteDataBaseRows() {
        try{
            databaseHelper.deleteAllRows();
        }catch (Exception e){
            Log.e("Exception occurred: ", "Exception", e);
        }finally {
            Toast.makeText(SymptomsActivity.this, "Data Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDataBaseWithSymptomRatings() {
        if (isDataUpdated())
            Toast.makeText(SymptomsActivity.this, "Data Stored", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(SymptomsActivity.this, "Data Not Stored", Toast.LENGTH_SHORT).show();
    }


    private boolean isDataUpdated(){
        RatingOfSymptom symptomRating = new RatingOfSymptom();

        symptomRating.setNausea(symptomRatings.get("Nausea"));
        symptomRating.setHeadache(symptomRatings.get("Headache"));
        symptomRating.setDiarrhea(symptomRatings.get("Diarrhea"));
        symptomRating.setSoreThroat(symptomRatings.get("Sore Throat"));
        symptomRating.setFever(symptomRatings.get("Fever"));
        symptomRating.setMuscleAche(symptomRatings.get("Muscle Ache"));
        symptomRating.setLossOfSmellTaste(symptomRatings.get("Loss of Smell or Taste"));
        symptomRating.setCough(symptomRatings.get("Cough"));
        symptomRating.setBreathDifficulty(symptomRatings.get("Difficulty Breathing"));
        symptomRating.setFeelingTired(symptomRatings.get("Feeling Tired"));

        //return databaseHelper.updateSymptomRating(symptomRating, heartRate, respiratoryRate);

        return databaseHelper.updateSymptomRating(symptomRating);
    }


    private void handleSymptomRatingChangesToHashMap() {
        String[] symptoms = getResources().getStringArray(R.array.symptom_options);
        for (String symptom : symptoms) {
            if (!symptomRatings.containsKey(symptom)) {
                symptomRatings.put(symptom, 0.0);
            }
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (presentSelectedSymptom != null) {
                symptomRatings.put(presentSelectedSymptom, (double) rating);
            }
        });

        symptomsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                presentSelectedSymptom = parentView.getItemAtPosition(position).toString();
                Double previousRating = symptomRatings.get(presentSelectedSymptom);
                if (previousRating != null) {
                    ratingBar.setRating(previousRating.floatValue());
                } else {
                    ratingBar.setRating(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void displaySymptomsSpinnerAndRatingBar() {
        symptomsSpinner = findViewById(R.id.spinnerSymptoms);
        ratingBar = findViewById(R.id.ratingBarSymptoms);
        databaseHelper = new DatabaseManager(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.symptom_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        symptomsSpinner.setAdapter(adapter);
    }
}