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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
    Button resetButton;
    int steps;

    // setup reset button click listener
    private View.OnClickListener resetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetSteps();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE},
                2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                == PackageManager.PERMISSION_GRANTED) {
            this.startForegroundService(new Intent(getApplicationContext(), StepCounterService.class));
        }
        else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }




        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);

        resetButton.setOnClickListener(resetClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // request user permission to use activity sensor
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION}, 1);

        // load step data (and other data eventually) on start
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set status to running (might not need this though)
        running = true;
        loadData();
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Will inform the users if there is a step counter present or not
        if (stepSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "There is a step counter sensor!", Toast.LENGTH_SHORT).show();
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        running = false;
        //sensorManager.unregisterListener(this);
        saveData(); // saves the users step data
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
        loadData();
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
        stepValue.setText(String.valueOf(steps));
    }

    // resets steps and applies to text view
    private void resetSteps() {
        steps = 0;
        stepValue.setText(String.valueOf(steps));
    }




}
