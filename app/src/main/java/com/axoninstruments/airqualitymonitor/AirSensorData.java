package com.axoninstruments.airqualitymonitor;

import android.content.Context;

import com.gpioserial.gpio.SerialPort;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.messaging.StreamTransport;

import java.io.IOException;

public class AirSensorData {
    private static AirSensorData airSensorData = new AirSensorData();

    private SerialPort serialPortDust;

    public Context context;
    public boolean Started = false;
    private StreamTransport transport;
    //
    // Data that can be access externally
    //
    public volatile int pm25concentration;
    public volatile int pm10concentration;
    public volatile int aqiReading;
    public volatile String stateText = "Good";
    public volatile int stateLED = 0;
    public volatile boolean notTerminated = true;

    private long msTimeout;

    private final byte FIRST_BYTE = 0x42;
    private final byte SECOND_BYTE = 0x4D;

    private final int MAX_BUFFER = 300;
    private int[] dataTelegram = new int[MAX_BUFFER];

    public static AirSensorData getInstance() {
        return airSensorData;
    }

    public void Start(Context _context) throws InterruptedException {
        context = _context;

        Started = true;

        try {
            serialPortDust = new SerialPort();
            serialPortDust.Open("/dev/ttySAC3", 9600, 8, 1, 0, 0);
        } catch (Exception error) {

        }
        transport = new StreamTransport(serialPortDust.getInputStream(), serialPortDust.getOutputStream());
        //
        // Create the thread that will handle the data from the Dust Sensor, ZH03
        //
        new Thread() {
            @Override
            public void run() {
                super.run();

                initSensor();                   // Initialise the sensor for polling mode

                while (notTerminated) {
                    readSensor();

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void initSensor() {

    }

    private void readSensor() {
        int chIn;
        int crcRead;
        int crcCalc;
        int inPtr = 0;
        int length = 0;
        boolean crcOK;
        boolean foundStart = false;
        boolean foundSecond = false;
        boolean gotLength1 = false;
        boolean gotLength2 = false;

        try {
            msTimeout = System.currentTimeMillis() + 2000; // 2 seconds timeout

            while ((serialPortDust.getInputStream().available() > 0) && (System.currentTimeMillis() < msTimeout)) {

                if(serialPortDust.getInputStream().available() > 0) {
                    try {
                        chIn = serialPortDust.getInputStream().read();
                    } catch (IOException error) {
                        chIn = 0;
                    }
                    dataTelegram[inPtr++] = chIn & 0xFF;

                    if (!foundStart) {
                        if (chIn == FIRST_BYTE) {
                            foundStart = true;
                            foundSecond = false;
                            inPtr = 0;
                            dataTelegram[inPtr++] = chIn & 0xFF;
                        }
                    } else if (foundStart && !foundSecond) {
                        if (chIn == SECOND_BYTE) {
                            foundSecond = true;
                            gotLength1 = false;
                            gotLength2 = false;
                        }
                    }
                    else
                    {
                        if(! gotLength1)
                        {
                            gotLength1 = true;
                            length = (chIn << 8);
                        }
                        else if(! gotLength2)
                        {
                            gotLength2 = true;
                            length += chIn;
                        }
                        else
                        {
                            if((inPtr - 4) == length) {
                                crcRead = (dataTelegram[inPtr - 2] << 8) + dataTelegram[inPtr - 1];

                                crcCalc = 0;
                                for(int ptr = 0; ptr < (inPtr - 2); ptr++)
                                {
                                    crcCalc += dataTelegram[ptr] & 0xFF;
                                }
                                if(crcCalc == crcRead)
                                {
                                    //
                                    // Extract the data
                                    //
                                    pm25concentration = (dataTelegram[12] << 8) + dataTelegram[13];

                                    pm10concentration = (dataTelegram[14] << 8) + dataTelegram[15];

                                    aqiReading = toAQI(pm25concentration);

                                }
                                inPtr = 0;
                                foundStart = false;
                                foundSecond = false;
                                gotLength1 = false;
                                gotLength2 = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception error)
        {

        }
    }

    private int toAQI(int ugm3)
    {
        int aqi = 0;

        if (ugm3 >= 0 && ugm3 <= 12)
        {
            stateLED = 0;
            stateText = "Good";
            aqi=convert(ugm3, 0, 50, 0, 12);
        }
        else if(ugm3 >= 13 && ugm3 <= 35)
        {
            stateLED = 1;
            stateText = "Moderate";
            aqi=convert(ugm3, 51, 100, 13, 35);
        }
        else if(ugm3 >= 36 && ugm3 <= 55)
        {
            stateLED = 2;
            stateText = "Unhealthy for sensitive groups";
            aqi=convert(ugm3, 101, 150, 36, 55);}
        else if(ugm3 >= 56 && ugm3 <= 150)
        {
            stateLED = 3;
            stateText = "Unhealthy";
            aqi=convert(ugm3, 151, 200, 56, 150);
        }
        else if(ugm3 >= 151 && ugm3 <= 250)
        {
            stateLED = 4;
            stateText = "Very Unhealthy";
            aqi=convert(ugm3, 201, 300, 151, 250);
        }
        else
        {
            stateLED = 5;
            stateText = "Hazardous";
            aqi=convert(ugm3, 301, 500, 251, 500);
        }
        return aqi;
    }

    private int convert(float Raw, float MinEngineering, float MaxEngineering, float MinCalibrated, float MaxCalibrated)
    {
        float SlopeCoeff = 0;
        float OffsetCoeff = 0;

        SlopeCoeff = (MaxEngineering - MinEngineering) / (MaxCalibrated - MinCalibrated);
        OffsetCoeff = MaxEngineering - (SlopeCoeff * MaxCalibrated);

        return (int) (((Raw * SlopeCoeff) + OffsetCoeff));
    }
}
