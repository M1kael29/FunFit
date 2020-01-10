package com.example.funfit;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    Button resetButton, todayButton, updateEntryButton;
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

    private View.OnClickListener todayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewDay();
        }
    };

    private View.OnClickListener updateEntryClickListener = new View.OnClickListener() {
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



        setupAlarmManager(0, 0);


        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tv_steps);
        todayButton = findViewById(R.id.btnDay);
        updateEntryButton = findViewById(R.id.btnUpdateEntry);

        resetButton.setOnClickListener(resetClickListener);
        todayButton.setOnClickListener(todayClickListener);
        updateEntryButton.setOnClickListener(updateEntryClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // request user permission to use activity sensor
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION}, 1);


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this, stepSensor, SensorManager.
                    SENSOR_DELAY_FASTEST);
        loadData();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        steps++;
        saveData();
        //addSteps();
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
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDay = sdf.format(currentDate);
        // Week
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        //Week
        sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String currentWeek = sdf.format(currentDate);
        db.updateData(currentDay, currentMonth, currentYear, currentWeek, steps);
        Toast.makeText(this, "Data updated", Toast.LENGTH_LONG).show();
    }

    private void viewDay() {
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDay = sdf.format(currentDate);
        // Week
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        Cursor res = db.getToday(currentDay, currentMonth, currentYear);
        if(res.getCount() == 0) {
            showMessage("Error","Nothing found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("Id: " + res.getString(0)+ "\n");
            buffer.append("Day: " + res.getString(1)+ "\n");
            buffer.append("Month: " + res.getString(2)+ "\n");
            buffer.append("Year: " + res.getString(3)+ "\n");
            buffer.append("Week: " + res.getString(4)+ "\n");
            buffer.append("Steps: " + res.getString(5)+ "\n\n");
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



    public void addSteps() {
        Log.d("DEBUG================", "got here");
        if(checkIfDayExists()) {
            Log.d("DEBUG================", "true");
            updateEntry();
        }
        else {
            Log.d("DEBUG================", "false");
            addEntry();
        }
    }

    private boolean checkIfDayExists() {
        // gets todays date and converts it to required format and a string
        SQLiteDatabase sqldb = db.getWritableDatabase();
        Date currentDate = new Date();
        // Day
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDay = sdf.format(currentDate);
        // Month
        sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);
        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        //Week
        sdf = new SimpleDateFormat("ww", Locale.getDefault());
        String currentWeek = sdf.format(currentDate);


        //query db and look for any entries that match stringDate
        Cursor res = sqldb.rawQuery("SELECT * FROM " + db.TABLE_NAME + " WHERE Day = ?" +
                        " AND Month = ? AND Year = ? AND Week = ?",
                new String[]{currentDay, currentMonth, currentYear, currentWeek});

        if(res.getCount() == 0) {
            //there are no entries
            return false;
        }
        else {
            return true;
        }
    }
}
