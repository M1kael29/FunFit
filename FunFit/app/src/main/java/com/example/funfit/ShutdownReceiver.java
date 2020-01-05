package com.example.funfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ShutdownReceiver  extends BroadcastReceiver {

    StepsDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = new StepsDatabase(context);
        db.insertData(99);
        Log.d("DEBUG================", "SHUTTING DOWN!!!!!!!!!!!!");
    }
}
