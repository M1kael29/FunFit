package com.example.funfit;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.logging.Logger;



public class StepsDatabase extends SQLiteOpenHelper {

    Context thisContext;

    public static final String DATABASE_NAME = "FunFit";
    public static final String TABLE_NAME = "Steps";
    public static final String ENTRY_ID = "EntryID";
    public static final String DAY = "Day";
    public static final String MONTH = "Month";
    public static final String YEAR = "Year";
    public static final String WEEK = "Week";
    public static final String STEPCOUNT = "StepCount";
    MainActivity main;
    int steps;
    SharedPreferences sharedPreferences;


    public StepsDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);

        sharedPreferences = context.getSharedPreferences(main.SHARED_PREFS, context.MODE_PRIVATE);
        thisContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ENTRY_ID + " INTEGER PRIMARY KEY" +
                " AUTOINCREMENT, " + DAY + " TEXT, " + MONTH + " TEXT, "
                + YEAR + " TEXT, " + WEEK + " TEXT, "+ STEPCOUNT + " INTEGER);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertDataSpecific(String day, String week, String month, String year, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DAY, day);
        contentValues.put(WEEK, week);
        contentValues.put(MONTH, month);
        contentValues.put(YEAR, year);
        contentValues.put(STEPCOUNT, steps);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public void addData() {
//        Log.d("DEBUG================", "got here");
//
//        if(checkIfDayExists()) {
//            Log.d("DEBUG================", "true");
//            updateData();
//        }
//        else {
//            Log.d("DEBUG================", "false");
            insertData();
        //}
    }

    private boolean checkIfDayExists() {
        // gets todays date and converts it to required format and a string
        SQLiteDatabase sqldb = this.getWritableDatabase();
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDay = sdf.format(currentDate);
        // Month
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        //Week
        sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String currentWeek = sdf.format(currentDate);


        //query db and look for any entries that match stringDate
        Cursor res = sqldb.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE Day = ?" +
                        " AND Month = ? AND Year = ? AND Week = ?",
                new String[]{currentDay, currentMonth, currentYear, currentWeek});

        if(res.getCount() == 0) {
            //there are no entries
            return false;
        }
        else {
            return true;
        }
    }

    // ADD A GETDAY METHOD TO ELIMINATE DUPLICATION

    public boolean insertData() {
        //create database instance
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDay = sdf.format(currentDate);
        contentValues.put(DAY, currentDay);
        // Week
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        contentValues.put(MONTH, currentMonth);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        contentValues.put(YEAR, currentYear);
        //Week
        sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String currentWeek = sdf.format(currentDate);
        contentValues.put(WEEK, currentWeek);
        //Steps
        float stepCount = sharedPreferences.getFloat(main.STEPS, 0);
        contentValues.put(STEPCOUNT, stepCount);

        long result = db.insert(TABLE_NAME, null, contentValues);
        Log.d("DEBUG================", "insertData called");
        if(result == -1)
            return false;
        else
            return true;
    }



    public boolean updateData() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String day = sdf.format(currentDate);
        // Week
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String month = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String year = sdf.format(currentDate);
        //Week
        sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String week = sdf.format(currentDate);

        float steps = sharedPreferences.getFloat(main.STEPS, 0);
        contentValues.put(STEPCOUNT, steps);
        db.update(TABLE_NAME, contentValues, "Day = ? AND Month = ? AND Year = ? " +
                "AND Week = ?",new String[] { day, month, year, week });
        Log.d("DEBUG================", "Number of steps added to db:" + steps);
        return true;
    }

    public Cursor getAllData () {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }

    public Cursor getToday (String day, String month, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Steps WHERE Day = ? AND Month = ? AND Year = ?",
                new String[] { day, month, year});
        return res;
    }

    public Cursor getWeek (String week, String month, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Steps WHERE Week = ? AND Month = ? AND Year = ?",
                new String[] { week, month, year});
        return res;
    }

    public Cursor getMonth (String month, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Steps WHERE Month = ? AND Year = ?",
                new String[] {month, year});
        return res;
    }


}
