package com.axoninstruments.airqualitymonitor;

import android.content.Context;
import android.util.Log;

import com.gpioserial.gpio.SerialPort;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.messaging.StreamTransport;

import java.io.IOException;

import static android.support.constraint.Constraints.TAG;

public class CO2SensorData {

    private static CO2SensorData co2SensorData = new CO2SensorData();

    private SerialPort serialPortDust;

    private Context context;
    private StreamTransport transport;

    private long msTimeout;

    private final int FIRST_BYTE = 0xFF;

    private final int MAX_BUFFER = 300;
    private int[] dataTelegram = new int[MAX_BUFFER];
    //
    // Data that can be access externally
    //
    public volatile int co2Concentration;
    public volatile boolean notTerminated = true;
    public volatile boolean Started = false;

    public static CO2SensorData getInstance() {
        return co2SensorData;
    }

    public void Start(Context _context) throws InterruptedException {
        context = _context;

        Started = true;

        try {
            serialPortDust = new SerialPort();
            serialPortDust.Open("/dev/ttySAC4", 9600, 8, 1, 0, 0);
        } catch (Exception error) {

        }
        transport = new StreamTransport(serialPortDust.getInputStream(), serialPortDust.getOutputStream());
        //
        // Create the thread that will handle the data from the CO2 Sensor, MH-Z19
        //
        new Thread() {
            @Override
            public void run() {
                super.run();

                while (notTerminated) {
                    pollSensor();
                    readSensor();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void pollSensor() {
        byte[] buffer_RTT = new byte[10];

        buffer_RTT[0] = (byte) 0xFF;
        buffer_RTT[1] = (byte) 0x01;
        buffer_RTT[2] = (byte) 0x86;       // Poll for gas concentration
        buffer_RTT[3] = (byte) 0x00;
        buffer_RTT[4] = (byte) 0x00;
        buffer_RTT[5] = (byte) 0x00;
        buffer_RTT[6] = (byte) 0x00;
        buffer_RTT[7] = (byte) 0x00;
        buffer_RTT[8] = (byte) 0x79;

        try
        {
            serialPortDust.getOutputStream().write(buffer_RTT, 0, 9);
        }
        catch (IOException error)
        {
            Log.d(TAG, error.getMessage());
        }
    }

    private void readSensor() {
        int chIn;
        int crcRead;
        int crcCalc;
        int inPtr = 0;
        int length = 0;
        boolean foundStart = false;

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
                            inPtr = 0;
                            dataTelegram[inPtr++] = chIn;
                        }
                    }
                    else
                    {
                        if(inPtr == 9) {
                            crcRead = dataTelegram[inPtr - 1];

                            crcCalc = 0;
                            for (int ptr = 1; ptr < (inPtr - 1); ptr++) {
                                crcCalc += dataTelegram[ptr] & 0xFF;
                            }
                            crcCalc = 0xFF - (crcCalc & 0xFF);
                            crcCalc += 1;

                            if (crcCalc == crcRead) {
                                //
                                // Extract the data
                                //
                                co2Concentration = (dataTelegram[2] << 8) + dataTelegram[3];
                            }
                            inPtr = 0;
                            foundStart = false;
                        }
                    }
                }
            }
        } catch (Exception error)
        {

        }
    }
}
