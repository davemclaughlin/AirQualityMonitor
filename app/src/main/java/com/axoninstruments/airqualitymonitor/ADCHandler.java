package com.axoninstruments.airqualitymonitor;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gpioserial.gpio.axonI2C;

import java.io.IOException;

public class ADCHandler {

    private static final String TAG = "AX5000DGadc";
    private axonI2C i2c;
	private double SensorCurrent[] = {0, 0, 0, 0};
    private double GaugeCurrent;
    private int slaveAddr1 = 0x68;     // Sensor ADC
    private int channel = 0;
	private boolean Paused;

    private long Samples = 0;
    private int SampleCount;
    private static final int NumberSamples = 1;

    private MovingAverage[] movingAverageSensor;

    public boolean sensorPressSelected = false;
    public boolean sensorTempSelected = false;

    private int rollingAverageCount = 5;

    private int fileHandle = 0;

    private Bmx280 atmosSensor;

    public float atmosPressure;
    public float atmosTemperature;
    public float atmosHumidity;

    public ADCHandler() {
	    SetFilters();
        //
        // Create I2C interface handler (Native code) for the surface sensors
        //
        i2c = new axonI2C();

        try
        {
            fileHandle = i2c.open("/dev/i2c-0");
        }
        catch(Exception e)
        {
            Log.w(TAG, "Could not open I2C interface");
        }
        Paused = false;
        //
        // Init the ADC
        //
        InitADC();
        //
        // Init the BME280
        //
        try {
            atmosSensor = new Bmx280();
        }
        catch (IOException error)
        {
            Log.w(TAG, "Could not open BME280 sensor");
        }
        if(atmosSensor.isOk())
        {
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
        // Thread to read the ADC
        //
	    new Thread() {
	    	@Override
	    	public void run() {
	    		int NewChannel = 0;
	    		while(true)
	    		{
	    			if(! Paused)
	    			{
		    			channel = NewChannel;
						NewChannel = ReadADC(slaveAddr1, channel);
	    			}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
	    			}
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
                            atmosTemperature = atmosSensor.readTemperature();
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
	    		}
	    	}
	    }.start();
	}    

	public void closeADC()
    {
        i2c.close(fileHandle);
    }

	private void SetFilters()
    {
        movingAverageSensor = new MovingAverage[4];

        movingAverageSensor[0] = new MovingAverage(rollingAverageCount, 0);
        movingAverageSensor[1] = new MovingAverage(rollingAverageCount, 0);
        movingAverageSensor[2] = new MovingAverage(rollingAverageCount, 0);
    }

    //*******************************************************************
    // Read the MCP3428 ADC used for the sensor interface
    //*******************************************************************

	private int ReadADC(int slaveAddr, int channel)
    {
        short Bits = 0;
        int[] buf = new int[4];

        i2c.read(fileHandle, slaveAddr, buf, 3);

        if((buf[2] & 0x80) == 0)			// Not doing a conversion?
        {
            Bits = (short) (((short) buf[0] << 8) + (short) buf[1]);
            SensorCurrent[channel] = movingAverageSensor[channel].NextAverage(EngUnits(Bits, 4, 20, 6400, 32000));
            //
            // Next channel (we only read 4 channels)
            //
            channel += 1;
            if(channel > 2) channel = 0;    // Only 3 channels used
            //
            // Start a conversion going
            //
            buf[0] = 0x88 | (channel << 5);

            i2c.write(fileHandle, slaveAddr, 0, buf, 1);
        }
		return channel;
    }

    private void InitADC()
    {
        short Bits = 0;
        int[] buf = new int[4];
/*
        try
        {
            fileHandle = i2c.open("/dev/i2c-0");
        }
        catch(Exception e)
        {
            Log.w(TAG, "Could not open I2C interface");
        }
*/
        //
        // Start the conversion going. We only ever use channel 1
        //
        buf[0] = 0x88;

        i2c.write(fileHandle, slaveAddr1, 0, buf, 1);

//        i2c.close(fileHandle);
    }

    //*******************************************************************
    // Convert raw value into engineering value
    //*******************************************************************

    private double EngUnits(double Raw, double CalLoReading,
                            double CalHiReading, double CalLoCounts,
                            double CalHiCounts) {
        double SlopeCoeff, OffsetCoeff;

        SlopeCoeff = ((double) CalHiReading - (double) CalLoReading) /
                ((double) CalHiCounts - (double) CalLoCounts);
        OffsetCoeff = (double) CalHiReading - (SlopeCoeff * (double) CalHiCounts);

        return (((double) Raw * SlopeCoeff) + OffsetCoeff);
    }

	//*******************************************************************
    // Returns the ADC reading for the selected channel
    //*******************************************************************

    public double GetADCReading(int channel)
    {
    	return(SensorCurrent[channel]);
    }

    //*******************************************************************
    // Returns the ADC reading for gauge reading
    //*******************************************************************

    public double GetGaugeReading()
    {
        return(GaugeCurrent);
    }

    //*******************************************************************
    // Freezes updates
    //*******************************************************************

    public void SetPausedState()
    {
    	Paused = true;
    }

    //*******************************************************************
    // Restarts the reading
    //*******************************************************************

    public void SetRunState()
    {
    	Paused = false;
    }

    public void SetAverageFilter(int count)
    {
        rollingAverageCount = count;
    }
}
