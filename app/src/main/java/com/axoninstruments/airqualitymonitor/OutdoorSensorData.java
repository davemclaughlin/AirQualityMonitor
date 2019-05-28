package com.axoninstruments.airqualitymonitor;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import static android.support.constraint.Constraints.TAG;

public class OutdoorSensorData {
    private static OutdoorSensorData outdoorSensorData = new OutdoorSensorData();
    public Context context;

    public boolean Started = false;

    public volatile boolean notTerminated = true;

    public boolean outsideSensorEnabled = false;

    public static OutdoorSensorData getInstance() {
        return outdoorSensorData;
    }

    public void Start(Context _context) throws InterruptedException {
        context = _context;

        Started = true;
        //
        // Create the thread that will handle the data from the Dust Sensor, ZH03
        //
        new Thread() {
            @Override
            public void run() {
                super.run();

                while (notTerminated) {
                    try {
                        Thread.sleep(1000);          // Do it every 1 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
