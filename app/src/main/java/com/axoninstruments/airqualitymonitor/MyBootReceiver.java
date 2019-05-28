package com.axoninstruments.airqualitymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyBootReceiver extends BroadcastReceiver {
    private Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = context;

        if(intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN"))
        {
            //
            // Signal system to close all files and connections to the server
            //
        }
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Log.d("MyBootReceiver", "Starting MainActivity");

            Intent mainActivity = new Intent(appContext, MainActivity.class);

            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            appContext.startActivity(mainActivity);
        }
    }
}
