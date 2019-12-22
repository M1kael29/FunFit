package com.example.funfit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import static com.example.funfit.MainActivity.CHANNEL_ID;

public class StepCounterService extends Service implements SensorEventListener {

    private static int steps;
    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String STEPS = "steps";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "service oncreate called", Toast.LENGTH_SHORT).show();


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

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "FunFit Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();



    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Toast.makeText(this, "service onstart called", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        steps = (int) event.values[0];
        saveData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    // saves user data in shared preferences
    public void saveData() {

        Toast.makeText(this, "save data called", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putInt(STEPS, steps);

        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service ondestroy called", Toast.LENGTH_SHORT).show();
    }
}
