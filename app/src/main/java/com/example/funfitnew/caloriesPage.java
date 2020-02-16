package com.example.funfitnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class caloriesPage extends AppCompatActivity {



    BarChart barChart;
    ProgressBar progressBar;
    StepsDatabase db;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView distanceToday, distanceDate;
    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS_1 = "steps";
    public static final String STEPS_2 = "steps";
    public static final String STEPS_3 = "steps";
    public static final String STEPS_4 = "steps";
    public static final String STEPS_5 = "steps";
    public static final String STEPS_6 = "steps";
    public static final String STEPS_7 = "steps";
    String date_n = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());
    float caloriesPerStep = 0.04f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories_page);


        barChart = (BarChart) findViewById(R.id.stepBarGraph);
        db = new StepsDatabase(this);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        progressBar = findViewById(R.id.stepProgressBar);
        distanceToday = findViewById(R.id.tvStepsValue);
        distanceDate = findViewById(R.id.tvStepsDate);


        int day1, day2, day3, day4, day5, day6, day7;
        float currentDay;

        day1 = 0;
        day2 = 0;
        day3 = 0;
        day4 = 0;
        day5 = 0;
        day6 = 0;
        day7 = 0;
        currentDay = sharedPreferences.getFloat(MainActivity.STEPS_TODAY, 0);

        Date currentDate = new Date();
        // Week
        SimpleDateFormat sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String currentWeek = sdf.format(currentDate);
        // Month
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        Cursor res = db.getWeek(currentWeek, currentMonth, currentYear);

        if(res.getCount() == 0) {
            Log.d("DEBUG================", "no data");
            return;
        }

        else {
            while (res.moveToNext()) {
                if(res.getString(5).equals("Sunday")) {
                    day1 = res.getInt(6);
                }
                if(res.getString(5).equals("Monday")) {
                    day2 = res.getInt(6);
                }
                if(res.getString(5).equals("Tuesday")) {
                    day3 = res.getInt(6);
                }
                if(res.getString(5).equals("Wednesday")) {
                    day4 = res.getInt(6);
                }
                if(res.getString(5).equals("Thursday")) {
                    day5 = res.getInt(6);
                }
                if(res.getString(5).equals("Friday")) {
                    day6 = res.getInt(6);
                }
                if(res.getString(5).equals("Saturday")) {
                    day7 = res.getInt(6);
                }
                Log.d("DEBUG================", "not a day");
            }
        }

        day1 *= caloriesPerStep;
        day2 *= caloriesPerStep;
        day3 *= caloriesPerStep;
        day4 *= caloriesPerStep;
        day5 *= caloriesPerStep;
        day6 *= caloriesPerStep;
        day7 *= caloriesPerStep;
        currentDay *= caloriesPerStep;

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                day1 = (int) currentDay;
                break;
            case Calendar.MONDAY:
                day2 = (int) currentDay;
                break;
            case Calendar.TUESDAY:
                day3 = (int) currentDay;
                break;
            case Calendar.WEDNESDAY:
                day4 = (int) currentDay;
                break;
            case Calendar.THURSDAY:
                day5 = (int) currentDay;
                break;
            case Calendar.FRIDAY:
                day6 = (int) currentDay;
                break;
            case Calendar.SATURDAY:
                day7 = (int) currentDay;
                break;
        }

        progressBar.setProgress((int) currentDay);

        String cals = String.format("%.2f", currentDay);

        String strSteps = String.format(cals + "/300Cal");
        distanceToday.setText(strSteps);
        distanceDate.setText(date_n);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(22.1f, day1));
        barEntries.add(new BarEntry(23.1f, day2));
        barEntries.add(new BarEntry(24.1f, day3));
        barEntries.add(new BarEntry(25.1f, day4));
        barEntries.add(new BarEntry(26.1f, day5));
        barEntries.add(new BarEntry(27.1f, day6));
        barEntries.add(new BarEntry(28.1f, day7));




        ArrayList<BarEntry> moreThan5000 = new ArrayList<>();

        BarDataSet barDataSet = new BarDataSet(barEntries, "Distance Travelled");

        int i=0;
        int o=0;
        o = barEntries.size();

        while (i<o){
            if (barEntries.indexOf(i) >= 2){

                barDataSet.setColor(i, Color.GREEN);

            }
            else{

            }
            i++;
        }

        BarData theData = new BarData(barDataSet);
        barChart.setData(theData);

        barChart.setTouchEnabled(true);
    }
}
