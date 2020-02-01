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


import static com.example.funfitnew.MainActivity.CHANNEL_ID;


public class StepCounterService extends Service implements SensorEventListener {

    private float currentSteps;
    private float stepsAtStartOfDay;
    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS_TODAY = "steps";
    public static final String STEPS_TOTAL = "stepsTotal";
    public static final String RESET_FLAG = "resetFlag";
    public static final String STEPS_START_OF_DAY = "stepsAtStartOfDay";
    private static float stepsAtLastSave;
    private static final int STEPS_OFFSET = 10;
    private static float totalSteps;
    private static boolean firstCheck = true;
    private float initialSteps;
    private float nextSteps;
    private boolean getInitialSteps;
    private float stepDiff;




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

        if(totalSteps >= 5000) {
            checkAchievements();
        }



    }

    private void checkAchievements() {
        todayStepsGoal();

        // today progress
        // update progress bar
        //      give congrats message

        // week all > 5000
        //      update today node
        // if 7 days in a row congrats (at endof day reset node display and number)
        //

        // highest steps
        // stepsToday > highest ever
        //      congratulate
        //      switch bool to not check this for the day anymore
        // at end of day save that value

    }

    private void todayStepsGoal() {
        // update progress bar
        // if(totalSteps >= 5000) {
        //      give congrats message
    }

    private void weekStepsGoal() {
        // if(totalSteps >= 5000) {
        //      if(stepsInRow = 7)
                    // update display nodes
                    // congrats message
        //                    // (at end of day reset this and reset node display)
        //        // }
    }

    private void highestSteps() {
        // if(totalSteps > highestStepsvalue)
        //  switch bool to false check for rest of day
        //  congrats
        //  (if shared pref bool is false set total Steps to highest steps value, switch bool to true
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
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        totalSteps = 0;

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
}
