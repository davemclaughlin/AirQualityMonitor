package com.axoninstruments.airqualitymonitor;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.CompletionService;

/**
 * A simple {@link Fragment} subclass.
 */
public class AirQualityFragment extends Fragment {

    private TextView inTempDisplay;
    private TextView inTempDecimalDisplay;
    private TextView inHumDisplay;
    private TextView inPressDisplay;

    private TextView PM25Display;
    private TextView PM10Display;

    private TextView co2Display;
    private TextView co2StatusDisplay;

    private ImageView aqiStateImage;
    private TextView aqiStateDisplay;

    private LinearLayout outsideLayout;

    private TextView inAirDisplay;

    private int stateLEDprev = -1;

    private boolean outsideSensorsPresent = false;

    private IndoorSensorData indoorSensorData = IndoorSensorData.getInstance();
    private AirSensorData airSensorData = AirSensorData.getInstance();
    private CO2SensorData co2SensorData = CO2SensorData.getInstance();
    //
    // Timer to handle the data updating to the screen
    //
    private Handler mDisplayHandler = new Handler();

    public AirQualityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_quality, container, false);

        outsideSensorsPresent = OutdoorSensorData.getInstance().outsideSensorEnabled;

        inTempDisplay = view.findViewById(R.id.inTempTextView);
        inTempDecimalDisplay = view.findViewById(R.id.inTempDecimalTextView);

        inHumDisplay = view.findViewById(R.id.inHumTextView);
        inPressDisplay = view.findViewById(R.id.inPressTextView);

        inAirDisplay = view.findViewById(R.id.inAirTextView);

        aqiStateImage = view.findViewById(R.id.stateImageView);
        aqiStateDisplay = view.findViewById(R.id.stateTextView);

        PM25Display = view.findViewById(R.id.PM25TextView);
        PM10Display = view.findViewById(R.id.PM10TextView);

        co2Display = view.findViewById(R.id.co2TextView);
        co2StatusDisplay = view.findViewById(R.id.co2StatusTextView);

        outsideLayout = view.findViewById(R.id.outside_sensors_display);

        if(! outsideSensorsPresent)
        {
            outsideLayout.setVisibility(View.GONE);
        }
        if(! IndoorSensorData.getInstance().hasHumidity) {
            inHumDisplay.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        // Create a handler to update the graph when we
        //
        mDisplayHandler.removeCallbacks(mUpdateDisplayTask);
        mDisplayHandler.postDelayed(mUpdateDisplayTask, 500);
    }

    //*******************************************************************
    // This handler updates the screen values
    //*******************************************************************

    private Runnable mUpdateDisplayTask = new Runnable() {
        public void run() {
            int co2Level;
            try {
                if (isVisible()) {
                    //
                    // Temperature/Pressure/Humidity
                    //
                    inTempDisplay.setText(String.format(Locale.UK, "%.0f", indoorSensorData.atmosTemperature));
                    String temp = String.format(Locale.UK, "%.1f", indoorSensorData.atmosTemperature);
                    String decimal = temp.substring(temp.length() - 2, temp.length());
                    inTempDecimalDisplay.setText(decimal);
                    inPressDisplay.setText(String.format(Locale.UK, "%.0f hPA", indoorSensorData.atmosPressure));
                    if (IndoorSensorData.getInstance().hasHumidity) {
                        inHumDisplay.setVisibility(View.VISIBLE);
                        inHumDisplay.setText(String.format(Locale.UK, "%.0f %%", indoorSensorData.atmosHumidity));
                    }
                    //
                    // Air quality
                    //
                    PM25Display.setText(String.format(Locale.UK, "%d", airSensorData.pm25concentration));
                    PM10Display.setText(String.format(Locale.UK, "%d", airSensorData.pm10concentration));

                    inAirDisplay.setText(String.format(Locale.UK, "%d", airSensorData.aqiReading));

                    co2Display.setText(String.format(Locale.UK, "%d", co2SensorData.co2Concentration));
                    co2Level = CO2SensorData.getInstance().co2Concentration;

                    if(co2Level > 0 && co2Level < 351) {
                        co2StatusDisplay.setText("Normal, outdoor level");
                    }
                    else if(co2Level > 350 && co2Level < 1001) {
                        co2StatusDisplay.setText("Normal, indoor level");
                    }
                    else if(co2Level > 1000 && co2Level < 2001) {
                        co2StatusDisplay.setText("Drowsiness? and poor air");
                    }
                    else if(co2Level > 2000 && co2Level < 5001) {
                        co2StatusDisplay.setText("Headache, sleepiness, stale");
                    }
                    else if(co2Level > 5000 && co2Level < 40001) {
                        co2StatusDisplay.setText("Workplace exposure limit");
                    } else {
                        co2StatusDisplay.setText("Hazardous, leave area");
                    }

                    if (airSensorData.stateLED != stateLEDprev) {
                        stateLEDprev = airSensorData.stateLED;
                        switch (airSensorData.stateLED) {
                            case 0:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.green_state));
                                break;
                            case 1:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.yellow_state));
                                break;
                            case 2:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.orange_state));
                                break;
                            case 3:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.red_state));
                                break;
                            case 4:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.maroon_state));
                                break;
                            case 5:
                                aqiStateImage.setImageDrawable(getResources().getDrawable(R.drawable.darkred_state));
                                break;
                        }
                        aqiStateDisplay.setText(airSensorData.stateText);
                    }
                }
            }
            catch(Exception error)
            {
                Log.d("AirQualityFragment", "Error - " + error.getMessage());
            }
            mDisplayHandler.postDelayed(this, 500);    // 2 times per second
        }
    };
}
