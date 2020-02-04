package com.example.funfitnew;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String ACHIEVEMENTS_CHANNEL = "achievementsChannel";

    @Override
    public void onCreate() {
        super.onCreate();

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
