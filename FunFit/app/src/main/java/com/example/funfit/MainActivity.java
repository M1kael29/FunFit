package com.example.funfit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String STEPS = "steps";

    boolean running = false;
    SensorManager sensorManager;
    TextView stepValue;
    Button resetButton;
    int steps;
    int savedSteps;

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

        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);

        resetButton.setOnClickListener(resetClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION}, 1);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

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
        sensorManager.unregisterListener(this);
        saveData();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
//        Toast.makeText(this, "onSensorChanged!!!", Toast.LENGTH_LONG).show();
////        if(running) {
////            stepValue.setText(String.valueOf(event.values[0]));
////            //steps = Integer.parseInt(stepValue.getText().toString());
            steps += 1;
            stepValue.setText(String.valueOf(steps));
        //}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(STEPS, steps);

        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        steps = sharedPreferences.getInt(STEPS, 0);
        stepValue.setText(String.valueOf(steps));
    }

    private void resetSteps() {
        steps = 0;
        stepValue.setText(String.valueOf(steps));
    }
}
