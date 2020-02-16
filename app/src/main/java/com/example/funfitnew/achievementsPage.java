package com.example.funfitnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class achievementsPage extends AppCompatActivity {


    ProgressBar dailyProgressBar, weeklyProgressBar, distanceProgressBar;
    StepsDatabase db;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView distanceToday, stepsToday, stepsWeekly, stepsHighest, calsHighest, distanceHighest;
    public static final String todaySteps = "steps";
    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String highestSteps = "highestStepsValue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements_page);


        db = new StepsDatabase(this);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        dailyProgressBar = findViewById(R.id.todayProgBar);
        stepsToday = findViewById(R.id.todayText);

        weeklyProgressBar = findViewById(R.id.weeklyProgBar);
        stepsWeekly = findViewById(R.id.weeklyText);

        distanceProgressBar = findViewById(R.id.distanceProgBar);
        distanceToday = findViewById(R.id.distanceText);

        stepsHighest = findViewById(R.id.highestStepsText);
        calsHighest = findViewById(R.id.highestCalsText);
        distanceHighest = findViewById(R.id.highestDistanceText);

        float highest, today, distance, highestCals, highestDistance;
        highest = sharedPreferences.getFloat(highestSteps, 0);
        today = sharedPreferences.getFloat(todaySteps, 0);
        distance = today * .7f;
        highestCals = highest * .04f;
        highestDistance = highest * .7f;

        String strToday = String.format("%.0f", today);
        String strWeek = String.format("%.0f", today);
        String strHighestStep = String.format("%.0f", highest);
        String strHighestCals = String.format("%.0f", highestCals);
        String strHighestDistance = String.format("%.0f", highestDistance);
        String strDistance = String.format("%.2f", distance);


        stepsToday.setText(strToday +"/5000");
        distanceToday.setText(strDistance + "m/5km");
        stepsHighest.setText(strHighestStep);
        calsHighest.setText(strHighestCals + "Cals");
        distanceHighest.setText(strHighestDistance + "m");


        dailyProgressBar.setProgress((int) sharedPreferences.getFloat(todaySteps, 0));
        distanceProgressBar.setProgress((int) distance);
    }
}
