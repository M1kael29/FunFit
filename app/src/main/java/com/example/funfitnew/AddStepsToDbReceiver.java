package com.example.funfitnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class AddStepsToDbReceiver extends BroadcastReceiver {

    StepsDatabase db;


    @Override
    public void onReceive(final Context context, Intent intent) {
        // add steps to database

        // wait for a couple of seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                db = new StepsDatabase(context);
                db.addData();

                // reset steps
                MainActivity main = MainActivity.getInstance();
                main.callServiceReset();
            }
        }, 5000);   //5 seconds






    }
}
