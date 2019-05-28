package com.axoninstruments.airqualitymonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataProcService extends Service {
    PowerManager.WakeLock wakeLock;

    AirSensorData airSensorData = AirSensorData.getInstance();
    CO2SensorData co2SensorData = CO2SensorData.getInstance();
    IndoorSensorData indoorSensorData = IndoorSensorData.getInstance();

    private BroadcastReceiver mServiceReceiver = null;

    public DataProcService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        RegisterReceivers();

        Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_bmp),
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height),
                true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0); // Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("AX9100");
        builder.setContentText("Data Processing Service");
        builder.setNumber(102);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setAutoCancel(false);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setLargeIcon(bm);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        startForeground(101023, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            airSensorData.Start(getApplicationContext());
        }
        catch(Exception error)
        {

        }
        try {
            co2SensorData.Start(getApplicationContext());
        }
        catch(Exception error)
        {

        }
        try {
            indoorSensorData.Start(getApplicationContext());
        }
        catch (Exception error)
        {

        }
        return Service.START_STICKY;
    }

    private String GetSerialPortSetting(int Index, int Offset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String value = "";

        switch (Index) {
            case 0:                 // PM2.5 serial port
                switch (Offset) {
                    case 0:         // Baudrate
                        value = prefs.getString("BAUDRATE1", "19200");
                        break;
                    case 1:         // Data bits
                        value = prefs.getString("DATABITS1", "8");
                        break;
                    case 2:         // Parity
                        value = prefs.getString("PARITY1", "1");
                        break;
                    case 3:         // Stop bits
                        value = prefs.getString("STOPBITS1", "1");
                        break;
                }
                break;
            case 1:                 // CO2 serial port
                switch (Offset) {
                    case 0:         // Baudrate
                        value = prefs.getString("BAUDRATE2", "19200");
                        break;
                    case 1:         // Data bits
                        value = prefs.getString("DATABITS2", "8");
                        break;
                    case 2:         // Parity
                        value = prefs.getString("PARITY2", "1");
                        break;
                    case 3:         // Stop bits
                        value = prefs.getString("STOPBITS2", "1");
                        break;
                }
                break;
        }
        return (value);
    }

    private void ShutdownSystem()
    {
        try {
            Intent intent = new
                    Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getApplicationContext().startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ShutdownService()
    {
        //
        // Cause each of the threads handling data to shutdown
        //
        airSensorData.notTerminated = false;
        co2SensorData.notTerminated = false;
        indoorSensorData.notTerminated = false;
    }

    //***********************************************************************
    // Registers the broadcast receivers
    //***********************************************************************

    private void RegisterReceivers() {
        //
        // This receiver handles the SERVICE messages from the other activities
        //
        IntentFilter ServiceFilter = new IntentFilter("com.axoninstruments.service.main");

        mServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String msg = intent.getStringExtra("request");

                    if (msg.equals("SHUTDOWN")) {
                        ShutdownService();
                    }
                } catch (Exception error) {
                    Log.d("DataProcService", "Service Receiver decode error");
                }
            }
        };
        Log.d("DataProcService", "Registering mServiceReceiver");
        this.registerReceiver(mServiceReceiver, ServiceFilter);
    }
}

