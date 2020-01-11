package com.example.funfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FunFitBroadcastReceiver extends BroadcastReceiver {

    StepsDatabase db;
    MainActivity main;


    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(main.SHARED_PREFS, context.MODE_PRIVATE);


        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Toast.makeText(context, "BOOT COMPLETED", Toast.LENGTH_SHORT).show();
        }


        db = new StepsDatabase(context);

        db.addData();

        Toast.makeText(context, "RING RING", Toast.LENGTH_SHORT).show();

        StepCounterService.resetStepCounter();

    }
}
