package com.example.funfitnew;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class App extends Application {
    public static final String ACHIEVEMENTS_CHANNEL = "achievementsChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("DEBUG================", "app OnCreate called");
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel achievementsChannel = new NotificationChannel(
                    ACHIEVEMENTS_CHANNEL,
                    "Achievement Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
        achievementsChannel.setDescription("This is the Achievement Channel");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(achievementsChannel);
        }
    }
}
