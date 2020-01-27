package com.example.funfitnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class FunFitBroadcastReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences;


    @Override
    public void onReceive(Context context, Intent intent) {
//        MainActivity main = MainActivity.getInstance();
//        main.setupStepsFromReboot();
        Log.d("DEBUG================", String.valueOf
                (sharedPreferences.getFloat("stepsAtStartOfDay", 0)));
    }

}
