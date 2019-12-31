package com.example.funfit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import java.util.Calendar;

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
    Button resetButton, addEntryButton, viewAllButton;
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

    private View.OnClickListener viewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewAll();
        }
    };

//    private View.OnClickListener updateClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            updateSteps();
//        }
//    };


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

        setupAlarmManager(1, 25);


        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);
        addEntryButton = findViewById(R.id.btnAddEntry);
        viewAllButton = findViewById(R.id.btnViewAll);

        resetButton.setOnClickListener(resetClickListener);
        addEntryButton.setOnClickListener(addEntryClickListener);
        viewAllButton.setOnClickListener(viewAllClickListener);

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

    private void viewAll() {
        Cursor res = db.getAllData();
        if(res.getCount() == 0) {
            showMessage("Error","Nothing found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("Id: " + res.getString(0)+ "\n");
            buffer.append("Date: " + res.getString(1)+ "\n");
            buffer.append("Steps: " + res.getString(2)+ "\n\n");
        }

        showMessage("Data",buffer.toString());
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

//    public void updateSteps() {
//        db.updateData(steps);
//    }

    private void setupAlarmManager(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, FunFitBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public int getSteps() {
        return steps;
    }
}
