package com.example.funfitnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class ResetStepsReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {



        Log.d("DEBUG================", "onreceive reached");

        MainActivity main = MainActivity.getInstance();
        main.callServiceReset();

    }
}
