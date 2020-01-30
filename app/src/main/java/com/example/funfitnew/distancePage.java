package com.example.funfitnew;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class distancePage extends AppCompatActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_page);

        barChart = (BarChart) findViewById(R.id.stepBarGraph);



        ArrayList<String> theDates = new ArrayList<>();
        theDates.add("April");
        theDates.add("May");
        theDates.add("June");
        theDates.add("July");
        theDates.add("August");
        theDates.add("September");

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(22.1f, 5541, "yes"));
        barEntries.add(new BarEntry(23.1f,2502));
        barEntries.add(new BarEntry(24.1f,3201));
        barEntries.add(new BarEntry(25.1f,4230));
        barEntries.add(new BarEntry(26.1f,1110));
        barEntries.add(new BarEntry(27.1f,1500));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Distance Travelled");


        BarData theData = new BarData(barDataSet);
        barChart.setData(theData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

    }
}
