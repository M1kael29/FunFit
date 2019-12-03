package com.example.funfit;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.widget.Toast;

public class FunFitBackupAgent extends BackupAgentHelper {

    static final String PREFS = "user_prefs";

    static final String PREFS_BACKUP_KEY = "prefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, PREFS);

        addHelper(PREFS_BACKUP_KEY, helper);
        Toast.makeText(this, "backup completed", Toast.LENGTH_LONG).show();
    }
}