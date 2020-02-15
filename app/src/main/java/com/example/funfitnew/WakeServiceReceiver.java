package com.example.funfitnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class WakeServiceReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {

        MainActivity main = MainActivity.getInstance();
        main.startService();
        Log.d("DEBUG================", "WakeService onreceive called");
    }
}
