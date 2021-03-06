package com.example.funfit;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.content.pm.PackageManager;
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
    public static final String CALORIES = "calories";
    public static final String DISTANCE = "distance";
    public static final String CHANNEL_ID = "funfit";

    SensorManager sensorManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView stepValue, caloriesValue, distanceValue;
    Button resetButton, todayButton, weekButton, monthButton, allTimeButton, updateEntryButton;
    float steps;
    float stepDistance = 0.7f;
    float caloriesPerStep = 0.04f;
    StepsDatabase db;



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

    private View.OnClickListener weekClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewWeek();
        }
    };

    private View.OnClickListener monthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewMonth();
        }
    };

    private View.OnClickListener allTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewAllTime();
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

        // request user permission to use activity sensor
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.
                        FOREGROUND_SERVICE}, 1);

        db = new StepsDatabase(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                == PackageManager.PERMISSION_GRANTED) {
            this.startForegroundService(new Intent(getApplicationContext(), StepCounterService.
                    class));
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }



        saveStepToDb(00, 13, 00);
        resetStepToZero(00, 13, 30);


        resetButton = findViewById(R.id.btnReset);
        stepValue = findViewById(R.id.tvStepsValue);
        caloriesValue = findViewById(R.id.tvCaloriesValue);
        distanceValue = findViewById(R.id.tvDistanceValue);
        todayButton = findViewById(R.id.btnDay);
        weekButton = findViewById(R.id.btnWeek);
        monthButton = findViewById(R.id.btnMonth);
        allTimeButton = findViewById(R.id.btnAllTime);
        updateEntryButton = findViewById(R.id.btnUpdateEntry);

        resetButton.setOnClickListener(resetClickListener);
        todayButton.setOnClickListener(todayClickListener);
        weekButton.setOnClickListener(weekClickListener);
        monthButton.setOnClickListener(monthClickListener);
        allTimeButton.setOnClickListener(allTimeClickListener);
        updateEntryButton.setOnClickListener(updateEntryClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);




        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        //registerReceiver(shutdownReceiver, filter);

        //addDummyData();
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
        loadData();
        float calories = calculateCalories();
        float distance = calculateDistance();
        stepValue.setText(String.valueOf(steps));
        caloriesValue.setText(String.valueOf(calories));
        distanceValue.setText(String.valueOf(distance));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // saves user data in shared preferences
    public void saveData(float caloriesValue, float distanceValue) {
        editor = sharedPreferences.edit();
        editor.putFloat(STEPS, steps);
        editor.putFloat(CALORIES, caloriesValue);
        editor.putFloat(DISTANCE, distanceValue);
        editor.apply();
    }

    // loads user data from shared preferences
    public void loadData() {
        steps = sharedPreferences.getFloat(STEPS, 0);
    }

    // resets steps and applies to text view
    private void resetSteps() {
        steps = 0;
        stepValue.setText(String.valueOf(steps));
    }

//    public void addEntry() {
//        boolean isInserted = db.insertData(steps);
//        if(isInserted)
//            Toast.makeText(this, "Data inserted", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(this, "Data not inserted", Toast.LENGTH_LONG).show();
//    }
//
//    public void updateEntry() {
//        Date currentDate = new Date();
//        // Day
//        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
//        String currentDay = sdf.format(currentDate);
//        // Week
//        sdf = new SimpleDateFormat("MM", Locale.getDefault());
//        String currentMonth = sdf.format(currentDate);
//        // Year
//        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
//        String currentYear = sdf.format(currentDate);
//        //Week
//        sdf = new SimpleDateFormat("ww", Locale.getDefault());
//        String currentWeek = sdf.format(currentDate);
//        db.updateData(currentDay, currentMonth, currentYear, currentWeek, steps);
//        Toast.makeText(this, "Data updated", Toast.LENGTH_LONG).show();
//    }



    private void viewAllTime() {

        Cursor res = db.getAllData();
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

    private void viewWeek() {
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

    private void viewMonth() {
        Date currentDate = new Date();
        // Month
        SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(currentDate);

        // Year
        sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf.format(currentDate);
        Cursor res = db.getMonth(currentMonth, currentYear);
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

    private void viewDummyDataDay() {

        Cursor res = db.getToday("01", "04", "2019");
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

    private void viewDummyDataWeek() {

        Cursor res = db.getWeek("09", "04", "2019");
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

    private void viewDummyDataMonth() {

        Cursor res = db.getMonth( "01", "1992");
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



    private void saveStepToDb(int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AddStepsToDbReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        Log.d("DEBUG================", "saveStepToDb done");
    }

    private void resetStepToZero(int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ResetStepsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        Log.d("DEBUG================", "resetStepToZero done");
    }

    public float getSteps() {
        return steps;
    }



    public void addSteps() {
        Log.d("DEBUG================", "got here");
        if(checkIfDayExists()) {
            Log.d("DEBUG================", "true");
            db.updateData();
        }
        else {
            Log.d("DEBUG================", "false");
            db.insertData();
        }
    }

    public boolean checkIfDayExists() {
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

    private void addDummyData() {
        db.insertDataSpecific("28", "09", "12", "2019", 100);
        db.insertDataSpecific("29", "09", "12", "2019", 100);
        db.insertDataSpecific("01", "09", "12", "2019", 100);
        db.insertDataSpecific("25", "09", "04", "2019", 100);
        db.insertDataSpecific("01", "09", "04", "2019", 100);
        db.insertDataSpecific("30", "09", "04", "2019", 100);
        db.insertDataSpecific("01", "20", "01", "1992", 100);
        db.insertDataSpecific("02", "20", "01", "1992", 100);
        db.insertDataSpecific("03", "20", "01", "1992", 100);
    }

    private float calculateCalories(){
        // 0.04 calories per step
        float calories = steps * caloriesPerStep;
        return calories;
    }

    private float calculateDistance() {
        // 0.7m per step
        float distance = (steps * stepDistance)/1000;
        return distance;
    }
}
