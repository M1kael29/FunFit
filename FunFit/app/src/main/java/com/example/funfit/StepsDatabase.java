package com.example.funfit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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



    public StepsDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);


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

    public boolean insertData(int stepCount) {
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
        contentValues.put(STEPCOUNT, stepCount);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }



   public boolean updateData(String day, String month, String year, String week, int stepCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STEPCOUNT, stepCount);
        db.update(TABLE_NAME, contentValues, "Day = ? AND Month = ? AND Year = ? " +
                "AND Week = ?",new String[] { day, month, year, week });
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
