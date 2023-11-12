package com.example.Context_Monitoring_Application.database;

import static com.example.Context_Monitoring_Application.Constants.BREATH_DIFFICULTY;
import static com.example.Context_Monitoring_Application.Constants.COUGH;
import static com.example.Context_Monitoring_Application.Constants.DIARRHEA;
import static com.example.Context_Monitoring_Application.Constants.FEELING_TIRED;
import static com.example.Context_Monitoring_Application.Constants.FEVER;
import static com.example.Context_Monitoring_Application.Constants.HEADACHE;
import static com.example.Context_Monitoring_Application.Constants.HEART_RATE;
import static com.example.Context_Monitoring_Application.Constants.LOSS_OF_SMELL_TASTE;
import static com.example.Context_Monitoring_Application.Constants.MUSCLE_ACHE;
import static com.example.Context_Monitoring_Application.Constants.NAUSEA;
import static com.example.Context_Monitoring_Application.Constants.RESPIRATORY_RATE;
import static com.example.Context_Monitoring_Application.Constants.ROW_ID;
import static com.example.Context_Monitoring_Application.Constants.SORE_THROAT;
import static com.example.Context_Monitoring_Application.Constants.DATABASE_NAME;
import static com.example.Context_Monitoring_Application.Constants.DATABASE_VERSION;
import static com.example.Context_Monitoring_Application.Constants.TABLE_NAME;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.Context_Monitoring_Application.calculations.CalculateHeartRate;
import com.example.Context_Monitoring_Application.calculations.CalculateRespiratoryRate;

public class DatabaseManager extends SQLiteOpenHelper {
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HEART_RATE + " REAL, " + RESPIRATORY_RATE + " REAL, " +
                NAUSEA + " REAL, " + HEADACHE + " REAL, " +
                DIARRHEA + " REAL, " + SORE_THROAT + " REAL, " +
                FEVER + " REAL, " + MUSCLE_ACHE + " REAL, " +
                LOSS_OF_SMELL_TASTE + " REAL, " + COUGH + " REAL, " +
                BREATH_DIFFICULTY + " REAL, " + FEELING_TIRED + " REAL)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean updateSymptomRating(RatingOfSymptom symptomRating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues symptomValues = new ContentValues();

        symptomValues.put(HEADACHE, symptomRating.getHeadache());
        symptomValues.put(NAUSEA, symptomRating.getNausea());
        symptomValues.put(MUSCLE_ACHE, symptomRating.getMuscleAche());
        symptomValues.put(LOSS_OF_SMELL_TASTE, symptomRating.getLossOfSmellTaste());
        symptomValues.put(DIARRHEA, symptomRating.getDiarrhea());
        symptomValues.put(COUGH, symptomRating.getCough());
        symptomValues.put(FEVER, symptomRating.getFever());
        symptomValues.put(BREATH_DIFFICULTY, symptomRating.getBreathDifficulty());
        symptomValues.put(SORE_THROAT, symptomRating.getSoreThroat());
        symptomValues.put(FEELING_TIRED, symptomRating.getFeelingTired());

        String whereClause = "ID = ?";
        String[] whereArgs = { String.valueOf(ROW_ID) };// rowId is the identifier for the row to update

        long result = db.update(TABLE_NAME, symptomValues, whereClause, whereArgs);
        return result != -1;
        //return db.update(TABLE_NAME, symptomValues, "ID = ?", new String[]{String.valueOf(rowID)}) > 0;
    }

    public void deleteAllRows() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertHeartRateAndRespiratoryRateValues(CalculateHeartRate heartRate, CalculateRespiratoryRate respiratoryRate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(RESPIRATORY_RATE, respiratoryRate.getRespiratoryRate());
        values.put(HEART_RATE, heartRate.getHeartRate());

        return db.insert(TABLE_NAME, null, values);
    }
}
