package com.example.funfitnew;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import static com.example.funfitnew.App.ACHIEVEMENTS_CHANNEL;
import static com.example.funfitnew.MainActivity.CHANNEL_ID;


public class StepCounterService extends Service implements SensorEventListener {

    private float currentSteps;
    private float stepsAtStartOfDay;
    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS_TODAY = "steps";
    public static final String STEPS_TOTAL = "stepsTotal";
    public static final String RESET_FLAG = "resetFlag";
    public static final String STEPS_START_OF_DAY = "stepsAtStartOfDay";
    public static final String DAILY_STEPS_BOOL = "dailyStepsBool";
    public static final String WEEKLY_STEPS_BOOL = "weeklyStepsBool";
    public static final String HIGHEST_STEPS_BOOL = "highestStepsBool";
    public static final String FIVE_KM_BOOL = "fiveKmBool";
    public static final String STEPS_IN_ROW = "stepInRow";
    public static final String HIGHEST_STEPS_VALUE = "highestStepsValue";
    private static float stepsAtLastSave;
    private static final int STEPS_OFFSET = 10;
    private static float totalSteps;
    private static boolean firstCheck = true;
    private float initialSteps;
    private float nextSteps;
    private boolean getInitialSteps;
    private float stepDiff;
    private NotificationManagerCompat newNotificationManager;




    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    IBinder mBinder = new LocalBinder();

    public float getCurrentDaySteps() {
        return currentSteps;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public StepCounterService getServerInstance() {
            return StepCounterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "service oncreate called", Toast.LENGTH_SHORT).show();


        loadData();
        getInitialSteps = true;

        newNotificationManager = NotificationManagerCompat.from(this);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "FunFit Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FunFit")
                .setContentText("example string")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        this.startForeground(1, notification);



        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // if bools exist
        // do nothing
        // else
        if(sharedPreferences.contains(WEEKLY_STEPS_BOOL)) {
            // do nothing
            Log.d("DEBUG================", "bools already exist");

        } else {
            editor.putBoolean(DAILY_STEPS_BOOL, true);
            editor.putBoolean(WEEKLY_STEPS_BOOL, true);
            editor.putBoolean(HIGHEST_STEPS_BOOL, true);
            editor.putBoolean(FIVE_KM_BOOL, true);
            editor.apply();
            Log.d("DEBUG================", "bools added");
        }


        ShutdownReceiver shutdownReceiver = new ShutdownReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);



        Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();




    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //checkForResetFlag();


        getInitialSteps = true;

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor, SensorManager.
                SENSOR_DELAY_UI, 10000);
        Toast.makeText(this, "service onstart called", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    private boolean checkForResetFlag() {
        if(sharedPreferences.contains(RESET_FLAG)) {
            sharedPreferences.edit().remove(RESET_FLAG)
                    .commit();
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(getInitialSteps) {
            initialSteps = event.values[0];
            getInitialSteps = false;
        }

        currentSteps = event.values[0];

        if(currentSteps > initialSteps + STEPS_OFFSET) {
            stepDiff = currentSteps - initialSteps;
            totalSteps += stepDiff + 1;
            saveData();
        }

        if(totalSteps >= 50) {
            checkAchievements();
        }



    }

    private void checkAchievements() {
        // get shared prefs
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        //check that this hasn't already been triggered
        boolean boolValue = sharedPreferences.getBoolean(DAILY_STEPS_BOOL, true);
        if(boolValue) {
            //congrats message
            achievementDisplay("day");
            //set bool to false
            editor = sharedPreferences.edit();
            editor.putBoolean(DAILY_STEPS_BOOL, false);
            editor.apply();

            //      (at end of day set dailyStepsBool = true)
        }



        //check that this hasn't already been triggered
        boolValue = sharedPreferences.getBoolean(WEEKLY_STEPS_BOOL, true);
        if(boolValue) {
            weekStepsGoal();
        }

        boolValue = sharedPreferences.getBoolean(HIGHEST_STEPS_BOOL, true);
        if(boolValue) {
            //check if steps are greater than best steps
            if(totalSteps > sharedPreferences.getFloat(HIGHEST_STEPS_VALUE, 100)) {
                // if yes
                highestSteps();
            }

        }

        boolValue = sharedPreferences.getBoolean(FIVE_KM_BOOL, true);
        if(boolValue) {
            //check if steps are greater >= 7143
            if(totalSteps >= 7143) {
                fiveKm();
            }
        }
    }

    private void weekStepsGoal() {
        //      add one to count
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int currentValue = sharedPreferences.getInt(STEPS_IN_ROW, 0);

        editor = sharedPreferences.edit();
        editor.putInt(STEPS_IN_ROW, currentValue + 1);
        editor.putBoolean(WEEKLY_STEPS_BOOL, false);
        editor.apply();
        Log.d("DEBUG================", String.valueOf
                (sharedPreferences.getInt(STEPS_IN_ROW, 0)));

        if(sharedPreferences.getInt(STEPS_IN_ROW, 0) >= 7) {
            // congrats message
            achievementDisplay("week");
            // weeklyStepsBool = false;
            editor.putBoolean(WEEKLY_STEPS_BOOL, false);
            editor.apply();
            //                    // (at end of day reset count and progress bar and
            //                    weeklyStepsBool = true)
        }
    }

    private void highestSteps() {

        //  highestStepsBool = false
        editor.putBoolean(HIGHEST_STEPS_BOOL, false);
        editor.apply();
        achievementDisplay("highest");
        //   at end of day
        //   (if shared pref totalStepsBool is false set total Steps to highest steps value
        //   , switch bool to true)
    }

    private void fiveKm() {
        // congrats
        achievementDisplay("fiveKm");
        // fiveKmBool = false;
        editor.putBoolean(FIVE_KM_BOOL, false);
        editor.apply();
        // at end of day set FiveKmBool true
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // saves user data in shared preferences

//    public void refreshSteps() {
//        float stepDiff = currentSteps - initialSteps;
//        totalSteps += stepDiff;
//        saveData();
//    }

    public void saveData() {
        //stepsAtLastSave = currentSteps;

        //Toast.makeText(this, "save data called", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putFloat(STEPS_TODAY, totalSteps);

        editor.apply();
        initialSteps = currentSteps;
        Log.d("DEBUG================", "saveData called");
    }

    public void loadData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        totalSteps = sharedPreferences.getFloat(STEPS_TODAY, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service ondestroy called", Toast.LENGTH_SHORT).show();
    }

    public void resetAtStartOfDay() {
        // end of day challenge logic
        challengeTasks();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        // this should reset shared preference
        editor = sharedPreferences.edit();
        totalSteps = 0;
        editor.putFloat(STEPS_TODAY, totalSteps);


        editor.apply();


    }

    private void challengeTasks() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();






        //weekly steps
        if(sharedPreferences.getBoolean(WEEKLY_STEPS_BOOL, true) == false) {
            // reset weekly count and progress bar
            editor.putInt(STEPS_IN_ROW, 0);
            editor.apply();
        }


        //highest steps
        // if (HIGHEST_STEPS_BOOL = false)
        if(sharedPreferences.getBoolean(HIGHEST_STEPS_BOOL, true) == false) {
            // set new highest steps value to today's steps
            editor.putFloat(HIGHEST_STEPS_VALUE, totalSteps);
            editor.apply();
        }



        // reset booleans
        editor.putBoolean(DAILY_STEPS_BOOL, true);
        editor.putBoolean(WEEKLY_STEPS_BOOL, true);
        editor.putBoolean(HIGHEST_STEPS_BOOL, true);
        editor.putBoolean(FIVE_KM_BOOL, true);
        editor.apply();
    }



    public void rebootSetup() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        float negativeValue = sharedPreferences.getFloat(STEPS_TODAY, 1);
        negativeValue = -negativeValue;
        editor = sharedPreferences.edit();
        editor.putFloat(STEPS_START_OF_DAY, negativeValue);
        editor.putInt(RESET_FLAG, 1);
        editor.apply();



        Log.d("DEBUG================", "reboot setup called");
    }


    public void achievementDisplay(String achievement) {

        String title = "title";
        String text = "text";

        switch (achievement) {
            case "day":
                title = "Daily Step Achievement";
                text = "Congratulations! You took 5000 steps today. Way to go!";
                break;
            case "week":
                title = "7 Days in A Row Achievement";
                text = "Congratulations! You took 5000 steps, 7 days in a row. You're on a roll!";
                break;
            case "highest":
                title = "Highest Step Achievement";
                text = "Great Work! You have beaten your step high score!";
                break;
            case "fiveKm":
                title = "5km Distance Achievement";
                text = "Fantastic Job! You have travelled 5km today.";
                break;

        }
        Notification notification = new NotificationCompat.Builder(this, ACHIEVEMENTS_CHANNEL)
                .setSmallIcon(R.drawable.achievementicon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        newNotificationManager.notify(2, notification);
    }
}
