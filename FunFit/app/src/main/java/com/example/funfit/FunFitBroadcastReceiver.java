package com.example.funfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FunFitBroadcastReceiver extends BroadcastReceiver {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    public static final String TEST_VALUE = "testing";
    StepsDatabase db;
    MainActivity main;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Toast.makeText(context, "SHUTTING DOWN", Toast.LENGTH_SHORT).show();
            editor = preferences.edit();

            editor.putInt(TEST_VALUE, 998877);

            editor.apply();
        }

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "BOOT COMPLETED", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(context, "RING RING", Toast.LENGTH_SHORT).show();
        db = new StepsDatabase(context);
        db.insertData(69);

    }
}
