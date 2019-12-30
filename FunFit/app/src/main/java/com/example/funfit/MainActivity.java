package com.example.funfit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(28)
public class MainActivity extends Activity implements SensorEventListener {

    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS = "steps";
    public static final String CHANNEL_ID = "funfit";

    boolean running = false;
    SensorManager sensorManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView stepValue;
    Button resetButton, addEntryButton;
    int steps;
    StepsDatabase db;

    // setup reset button click listener
    private View.OnClickListener resetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetSteps();
        }
    };

    private View.OnClickListener addEntryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addEntry();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new StepsDatabase(this);

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
//                        FOREGROUND_SERVICE},
//                2);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
//                == PackageManager.PERMISSION_GRANTED) {
//            this.startForegroundService(new Intent(getApplicationContext(), StepCounterService.
//                    class));
//            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
//        }




        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);
        addEntryButton = findViewById(R.id.btnAddEntry);

        resetButton.setOnClickListener(resetClickListener);
        addEntryButton.setOnClickListener(addEntryClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // request user permission to use activity sensor
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION}, 1);



    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this, stepSensor, SensorManager.
                    SENSOR_DELAY_FASTEST);

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
//        Toast.makeText(this, "onSensorChanged!!!", Toast.LENGTH_LONG).show();
////        if(running) {
////            stepValue.setText(String.valueOf(event.values[0]));
////            //steps = Integer.parseInt(stepValue.getText().toString());

            // increment steps and apply to textview on layout
//            steps += 1;
//            stepValue.setText(String.valueOf(steps));
        //}
        //db.createStepsEntry();
        //steps = sharedPreferences.getInt(STEPS, 0);
        steps = (int) event.values[0];
        stepValue.setText(String.valueOf(steps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // saves user data in shared preferences
    public void saveData() {


        editor = sharedPreferences.edit();

        editor.putInt(STEPS, steps);

        editor.apply();
    }

    // loads user data from shared preferences
    public void loadData() {

        steps = sharedPreferences.getInt(STEPS, 0);

//        stepValue.setText(String.valueOf(db.getStepsToday()));
        //stepValue.setText(String.valueOf(steps));
    }

    // resets steps and applies to text view
    private void resetSteps() {
        steps = 0;
        stepValue.setText(String.valueOf(steps));
    }

    private void addEntry() {
        boolean isInserted = db.insertData(steps);
        if(isInserted)
            Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Data not inserted", Toast.LENGTH_LONG).show();
    }




}
