package com.axoninstruments.airqualitymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private ActionBar toolbar;

    private BottomNavigationView navigation;

    private BroadcastReceiver mServiceReceiver = null;

    private PowerManager.WakeLock wakeLock;

    private String sleepTime = "23:00";
    private String wakeTime = "07:30";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    toolbar.setTitle("Air Quality Monitor - Sensors");
                    fragment = new AirQualityFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_graphs:
                    toolbar.setTitle("Air Quality Monitor - Graphs");
                    fragment = new GraphsFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_weather:
                    toolbar.setTitle("Air Quality Monitor - Weather Forecast");
                    fragment = new WeatherFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_settings:
                    toolbar.setTitle("Air Quality Monitor - Settings");
                    Bundle bundle = new Bundle();
                    bundle.putString("waketime", wakeTime);
                    bundle.putString("sleeptime", sleepTime);
                    bundle.putBoolean("external", OutdoorSensorData.getInstance().outsideSensorEnabled);
                    fragment = new SettingsFragment();
                    fragment.setArguments(bundle);
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AirQuality:Tag");

        toolbar = getSupportActionBar();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        toolbar.setTitle("Air Quality Monitor - Sensors");
        loadFragment(new AirQualityFragment());

        RegisterReceivers();
        //
        // Create and start the background service. This will read all the sensors, process the
        // data and make it available for the foreground processes for display
        //
        Intent serviceIntent = new Intent(this, DataProcService.class);
        startService(serviceIntent);
        //
        // Setup an alarm manager to put us to sleep at the preset time
        //
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        // Release our wakelock as the user may have closed us down
        //
        wakeLock.release();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //
        // Grab a wake lock so the LCD does not go to sleep
        //
        wakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent bcmsg = new Intent("com.axoninstruments.service.main")
                .putExtra("request", "SHUTDOWN");

        sendBroadcast(bcmsg);

        this.unregisterReceiver(mServiceReceiver);
    }

    //***********************************************************************
    // Registers the broadcast receivers
    //***********************************************************************

    private void RegisterReceivers() {
        //
        // This receiver handles the SERVICE messages from the other activities
        //
        IntentFilter ServiceFilter = new IntentFilter("com.axoninstruments.main");

        mServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String msg = intent.getStringExtra("request");

                    if (msg.equals("SWITCHVIEW")) {
                        sleepTime = intent.getStringExtra("sleeptime");
                        wakeTime = intent.getStringExtra("waketime");
                        OutdoorSensorData.getInstance().outsideSensorEnabled = intent.getBooleanExtra("external", false);
                        //
                        // Need to reset the timer if these times have changed
                        //
                        navigation.setSelectedItemId(R.id.navigation_home);
                    } else if (msg.equals("CANCELVIEW")) {
                        navigation.setSelectedItemId(R.id.navigation_home);
                    }
                } catch (Exception error){
                    Log.d("DataProcService", "Service Receiver decode error");
                }
            }
        };
        Log.d("DataProcService", "Registering mServiceReceiver");
        this.registerReceiver(mServiceReceiver, ServiceFilter);
    }
}

