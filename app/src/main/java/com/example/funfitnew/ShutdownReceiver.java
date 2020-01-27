package com.example.funfitnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class ShutdownReceiver  extends BroadcastReceiver {

    public static final String SHARED_PREFS = "funfit_prefs";
    public static final String RESET_FLAG = "resetFlag";

    @Override
    public void onReceive(Context context, Intent intent) {

        MainActivity main = MainActivity.getInstance();
        main.saveDataBeforeShutdown();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(RESET_FLAG, true);

        editor.apply();

        Log.d("DEBUG================", "SHUTTING DOWN!!!!!!!!!!!!");
    }
}
