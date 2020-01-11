package com.example.funfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ResetStepsReceiver extends BroadcastReceiver {




    @Override
    public void onReceive(Context context, Intent intent) {



        StepCounterService.resetStepCounter();

    }
}
