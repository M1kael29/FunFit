package com.example.funfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class AddStepsToDbReceiver extends BroadcastReceiver {

    StepsDatabase db;
    MainActivity main;


    @Override
    public void onReceive(Context context, Intent intent) {

        db = new StepsDatabase(context);

        db.addData();



    }
}
