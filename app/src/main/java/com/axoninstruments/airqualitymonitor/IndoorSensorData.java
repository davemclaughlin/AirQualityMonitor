package com.axoninstruments.airqualitymonitor;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import static android.support.constraint.Constraints.TAG;

public class IndoorSensorData {
    private static IndoorSensorData indoorSensorData = new IndoorSensorData();
    public Context context;
    public boolean Started = false;

    Bmx280 atmosSensor;
    //
    // Data that can be accessed externally
    //
    public volatile float atmosPressure;
    public volatile float atmosTemperature;
    public volatile float atmosHumidity;
    public volatile boolean hasHumidity = false;

    public volatile boolean notTerminated = true;

    public volatile float tempOffset = 4.0F;

    public static IndoorSensorData getInstance() {
        return indoorSensorData;
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

                try {
                    atmosSensor = new Bmx280();
                }
                catch (IOException error)
                {
                    Log.w(TAG, "Could not open BME280 sensor");
                }
                if(atmosSensor.isOk())
                {
                    //
                    // Init the sensors settings
                    //
                    try {
                        atmosSensor.setPressureOversampling(Bmx280.OVERSAMPLING_1X);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        atmosSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(atmosSensor.hasHumiditySensor()) {
                            atmosSensor.setHumidityOversampling(Bmx280.OVERSAMPLING_1X);
                            hasHumidity = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        atmosSensor.setMode(Bmx280.MODE_NORMAL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //
                // Now we loop around getting the data from the I2C bus
                //
                while (notTerminated) {
                    if(atmosSensor.isOk())
                    {
                        try {
                            atmosPressure = atmosSensor.readPressure();
                        }
                        catch(IOException error)
                        {

                        }
                        catch (IllegalStateException error)
                        {

                        }
                        try {
                            atmosTemperature = atmosSensor.readTemperature() - tempOffset;
                        }
                        catch(IOException error)
                        {

                        }
                        catch (IllegalStateException error)
                        {

                        }
                        if(atmosSensor.hasHumiditySensor()) {
                            try {
                                atmosHumidity = atmosSensor.readHumidity();
                            } catch (IOException error) {

                            } catch (IllegalStateException error) {

                            }
                        }
                    }
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
