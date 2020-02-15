package com.example.funfitnew;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@TargetApi(28)
public class MainActivity extends Activity implements SensorEventListener {

    public static final int RESULT_LOAD_IMAGE = 3;

    RelativeLayout rellay1, rellay2, rellay3, testBg;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
        }
    };

    TextView Welcome;
    Handler handler2 = new Handler();
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            testBg.setVisibility(View.VISIBLE);
            rellay2.setVisibility(View.VISIBLE);
            rellay3.setVisibility(View.VISIBLE);
            Welcome.setVisibility(View.INVISIBLE);;
        }
    };



    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS_TODAY = "steps";

    public static final String CALORIES = "calories";
    public static final String DISTANCE = "distance";
    public static final String CHANNEL_ID = "funfit";

    SensorManager sensorManager;
    SharedPreferences sharedPreferences;
    AlarmManager alarmManager;
    PendingIntent addToDbIntent;
    PendingIntent wakeServiceIntent;
    SharedPreferences.Editor editor;
    TextView stepValue, caloriesValue, distanceValue;
    Button resetButton, todayButton, weekButton, monthButton, allTimeButton, updateEntryButton;
    ProgressBar stepProgressBar, calorieProgressBar, distanceProgressBar;
    ImageButton imgButton;
    Bitmap bitmapTest = null;
    float currentDaySteps;
    float stepDistance = 0.7f;
    float caloriesPerStep = 0.04f;
    StepsDatabase db;
    private static MainActivity instance;
    public static final String STEPS_TOTAL = "stepsTotal";

    boolean mBounded;
    StepCounterService stepCounterService;

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
            moveToStepPage();
        }
    };

    private View.OnClickListener weekClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            moveToCaloriePage();
        }
    };

    private View.OnClickListener monthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            moveToDistance();
        }
    };

    private View.OnClickListener allTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            moveToAchievements();
        }
    };

    private View.OnClickListener updateEntryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addSteps();
        }
    };


    private View.OnClickListener profileSelect = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    };

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected",
                    Toast.LENGTH_SHORT);
            mBounded = false;
            stepCounterService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected",
                    Toast.LENGTH_SHORT);
            mBounded = true;
            StepCounterService.LocalBinder mLocalBinder = (StepCounterService.LocalBinder)service;
            stepCounterService = mLocalBinder.getServerInstance();
        }
    };
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.
                        FOREGROUND_SERVICE}, 1);

        setContentView(R.layout.activity_main);

        db = new StepsDatabase(this);

        // request user permission to use activity sensor



        rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
        rellay2 = (RelativeLayout) findViewById(R.id.rellay2);
        rellay3 = (RelativeLayout) findViewById(R.id.rellay3);
        Welcome = (TextView) findViewById(R.id.Welcome);
        testBg = (RelativeLayout) findViewById(R.id.testBg);


        handler.postDelayed(runnable, 2000);
        handler2.postDelayed(runnable2, 4000);


        ImageButton profilePic = (ImageButton) findViewById(R.id.imgView_logo);

        profilePic.setOnClickListener(profileSelect);

        saveStepToDb(23, 59, 59);
        //resetStepToZero(00, 00, 10);


        stepValue = findViewById(R.id.tvStepsValue);
        caloriesValue = findViewById(R.id.tvCaloriesValue);
        distanceValue = findViewById(R.id.tvDistanceValue);
        todayButton = findViewById(R.id.btnDay);
        weekButton = findViewById(R.id.btnWeek);
        monthButton = findViewById(R.id.btnMonth);
        allTimeButton = findViewById(R.id.btnAllTime);
        stepProgressBar = findViewById(R.id.stepProgressBar);
        calorieProgressBar = findViewById(R.id.CaloriesProgressBar);
        distanceProgressBar = findViewById(R.id.distanceProgressBar);
        imgButton = findViewById(R.id.imgView_logo);

        todayButton.setOnClickListener(todayClickListener);
        weekButton.setOnClickListener(weekClickListener);
        monthButton.setOnClickListener(monthClickListener);
        allTimeButton.setOnClickListener(allTimeClickListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        setServiceAlarm();

        //addDummyData();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try {
                bitmapTest = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            }catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();}

            imgButton.setImageBitmap(bitmapTest);
            db.PROFILEIMAGE = bitmapTest;

        }}

    @Override
    protected void onStart() {
        super.onStart();
        startService();
    }

    public void startService() {
        Intent mIntent = new Intent(this, StepCounterService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        this.startForegroundService(new Intent(getApplicationContext(), StepCounterService.
                class));
        instance = this;


        if (db.PROFILEIMAGE != null ){
            imgButton.setImageBitmap(db.PROFILEIMAGE);
        }
    }

    public static MainActivity getInstance(){
        return instance;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
//                == PackageManager.PERMISSION_GRANTED) {
//            this.startForegroundService(new Intent(getApplicationContext(), StepCounterService.
//                    class));
//            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
//        }

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this, stepSensor, SensorManager.
                SENSOR_DELAY_NORMAL, 10000000);


    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentDaySteps = sharedPreferences.getFloat(STEPS_TODAY, 0);
        updateScreen();
    }

    private void updateScreen() {


        float calories = calculateCalories();
        float distance = calculateDistance();
        String strCalories = String.format("%.2f", calories);
        String strDistance = String.format("%.2f", distance);
        String strSteps = String.format("%.0f", currentDaySteps);
        stepValue.setText(strSteps);
        caloriesValue.setText(strCalories);
        distanceValue.setText(strDistance);
        stepProgressBar.setProgress((int) currentDaySteps);
        calorieProgressBar.setProgress((int) calories);
        distanceProgressBar.setProgress((int) distance*1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // saves user data in shared preferences
    public void saveData(float caloriesValue, float distanceValue) {
        editor = sharedPreferences.edit();
        editor.putFloat(STEPS_TODAY, currentDaySteps);
        editor.putFloat(CALORIES, caloriesValue);
        editor.putFloat(DISTANCE, distanceValue);
        editor.apply();
    }

    //loads user data from shared preferences
//    public float loadData() {
//        float stepsToReturn = sharedPreferences.getFloat(STEP_TODAY, 0);
//        return stepsToReturn;
//    }

    // resets currentDaySteps and applies to text view
    private void resetSteps() {

    }

//    public void addEntry() {
//        boolean isInserted = db.insertData(currentDaySteps);
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
//        db.updateData(currentDay, currentMonth, currentYear, currentWeek, currentDaySteps);
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
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AddStepsToDbReceiver.class);
        addToDbIntent = PendingIntent.getBroadcast(this, 3, intent
                , 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                addToDbIntent);
        Log.d("DEBUG================", "saveStepToDb done");
    }

    private void setServiceAlarm() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WakeServiceReceiver.class);
        wakeServiceIntent = PendingIntent.getBroadcast(this, 5, intent
                , 0);
        Calendar calendar = Calendar.getInstance();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000 * 10,
                1000 * 10, wakeServiceIntent);
        Log.d("DEBUG================", "setServiceAlarm called");
    }

//    private void resetStepToZero(int hour, int minute, int second) {
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.HOUR_OF_DAY, hour);
//        c.set(Calendar.MINUTE, minute);
//        c.set(Calendar.SECOND, second);
//        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context
//                .ALARM_SERVICE);
//        Intent intent = new Intent(this, ResetStepsReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, intent
//                , 0);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
//                pendingIntent);
//        Log.d("DEBUG================", "resetStepToZero done");
//    }

    public float getSteps() {
        return currentDaySteps;
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
        float calories = currentDaySteps * caloriesPerStep;
        return calories;
    }

    private float calculateDistance() {
        // 0.7m per step
        float distance = (currentDaySteps * stepDistance)/1000;
        return distance;
    }

    public void callServiceReset() {
        stepCounterService.resetAtStartOfDay();
    }

    public void saveDataBeforeShutdown() {
        stepCounterService.rebootSetup();
        Log.d("DEBUG================", "saveDataBeforeShutdown called");
    }

    private void moveToStepPage(){

        Intent moveToSteps = new Intent(MainActivity.this, stepPage.class);
        startActivity(moveToSteps);
    }

    private void moveToCaloriePage(){

        Intent moveToCalories = new Intent(MainActivity.this, caloriesPage.class);
        startActivity(moveToCalories);
    }

    private void moveToAchievements(){

        Intent moveToAchievements = new Intent(MainActivity.this, achievementsPage.class);
        startActivity(moveToAchievements);
    }

    private void moveToDistance(){

        Intent moveToDistance = new Intent(MainActivity.this, distancePage.class);
        startActivity(moveToDistance);
    }


//    public void setupStepsFromReboot() {
//        stepCounterService.rebootSetup();
//    }
}
