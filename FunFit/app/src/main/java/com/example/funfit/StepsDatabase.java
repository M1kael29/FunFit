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
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class StepsDatabase extends SQLiteOpenHelper {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd_hhmmss", Locale.getDefault());

    public static final String DATABASE_NAME = "FunFit";
    public static final String TABLE_NAME = "Steps";
    public static final String ENTRY_ID = "EntryID";
    public static final String DATE = "Date";
    public static final String STEPCOUNT = "StepCount";
    MainActivity main;
    int steps;



    public StepsDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ENTRY_ID + " INTEGER PRIMARY KEY" +
                " AUTOINCREMENT, " + DATE + " TEXT," + STEPCOUNT + " INTEGER);");

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
        String currentDateAndTime = sdf.format(new Date());
        contentValues.put(DATE, currentDateAndTime);
        contentValues.put(STEPCOUNT, stepCount);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

}
