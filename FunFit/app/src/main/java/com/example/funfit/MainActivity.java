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
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@TargetApi(28)
public class MainActivity extends Activity implements SensorEventListener {

    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS = "steps";
    public static final String CHANNEL_ID = "funfit";

    SensorManager sensorManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView stepValue;
    Button resetButton, addEntryButton, viewAllButton, currentDayButton;
    int steps;
    StepsDatabase db;
    FunFitBroadcastReceiver broadcastReceiver = new FunFitBroadcastReceiver();
    ShutdownReceiver shutdownReceiver = new ShutdownReceiver();

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

    private View.OnClickListener checkForCurrentDayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addSteps();
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



        setupAlarmManager(18, 34);


        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);
        addEntryButton = findViewById(R.id.btnAddEntry);
        viewAllButton = findViewById(R.id.btnViewAll);
        currentDayButton = findViewById(R.id.btnCurrentDay);

        resetButton.setOnClickListener(resetClickListener);
        addEntryButton.setOnClickListener(addEntryClickListener);
        viewAllButton.setOnClickListener(viewAllClickListener);
        currentDayButton.setOnClickListener(checkForCurrentDayClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // request user permission to use activity sensor
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION}, 1);




//        String aDate = "1992_12_28";
//        SimpleDateFormat newSdf = new SimpleDateFormat("yyyy_MM_dd");
//        try {
//            Date bloop = newSdf.parse(aDate);
//            Log.d("DEBUG================", bloop.toString());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//
//        sensorManager.registerListener(this, stepSensor, SensorManager.
//                    SENSOR_DELAY_FASTEST);
//        loadData();

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
//        steps++;
//        saveData();
//        addSteps();
//        stepValue.setText(String.valueOf(steps));

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

    public void updateEntry() {
        Date todaysDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        String stringDate = sdf.format(todaysDate);
        db.updateData(stringDate, steps);
        Toast.makeText(this, "Data updated", Toast.LENGTH_LONG).show();
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

    private boolean checkIfDayExists() {
        // gets todays date and converts it to required format and a string
        SQLiteDatabase sqldb = db.getWritableDatabase();
        Date todaysDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        String stringDate = sdf.format(todaysDate);
        Log.d("DEBUG================", stringDate);


        //query db and look for any entries that match stringDate
        Cursor res = sqldb.rawQuery("SELECT * FROM " + db.TABLE_NAME + " WHERE Date = ?",
                new String[]{stringDate});
        


        if(res.getCount() == 0) {
            //there are no entries
            return false;
        }
        else {
            return true;
        }
    }

    public void addSteps() {
        if(checkIfDayExists()) {
            Log.d("DEBUG================", "true");
            updateEntry();
        }
        else {
            Log.d("DEBUG================", "false");
            addEntry();
        }
    }
}
