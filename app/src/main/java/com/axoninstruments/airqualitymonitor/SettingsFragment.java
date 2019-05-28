package com.axoninstruments.airqualitymonitor;


import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private Switch outsideOnOff;

    private TextView wakeTimeTextView;
    private TextView sleepTimeTextView;

    private String wakeTime;
    private String sleepTime;

    private boolean externalSensorEnabled;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        wakeTimeTextView = view.findViewById(R.id.wakeTimeTextView);
        sleepTimeTextView = view.findViewById(R.id.sleepTimeTextView);

        outsideOnOff = view.findViewById(R.id.external_sensor_switch);

        wakeTime = getArguments().getString("waketime");
        sleepTime = getArguments().getString("sleeptime");

        if(wakeTime == null)
        {
            wakeTime = "07:00";
        }
        if(sleepTime == null)
        {
            sleepTime = "23:00";
        }
        wakeTimeTextView.setText(wakeTime);
        sleepTimeTextView.setText(sleepTime);

        externalSensorEnabled = getArguments().getBoolean("external", false);
        outsideOnOff.setChecked(externalSensorEnabled);

        wakeTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Integer.parseInt(wakeTime.substring(0, 2));
                int minute = Integer.parseInt(wakeTime.substring(3, 5));
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        wakeTime = String.format(Locale.UK,"%02d", selectedHour) + ":" + String.format(Locale.UK,"%02d", selectedMinute);
                        wakeTimeTextView.setText(wakeTime);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        sleepTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Integer.parseInt(sleepTime.substring(0, 2));
                int minute = Integer.parseInt(sleepTime.substring(3, 5));
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        sleepTime = String.format(Locale.UK,"%02d", selectedHour) + ":" + String.format(Locale.UK,"%02d", selectedMinute);
                        sleepTimeTextView.setText(sleepTime);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                // Save all of the settings and then switch back to the main view
                //
                OutdoorSensorData.getInstance().outsideSensorEnabled = outsideOnOff.isChecked();

                Intent bcmsg = new Intent("com.axoninstruments.main")
                        .putExtra("request", "SWITCHVIEW")
                        .putExtra("sleeptime", sleepTimeTextView.getText())
                        .putExtra("waketime", wakeTimeTextView.getText())
                        .putExtra("external", outsideOnOff.isChecked());

                getActivity().getApplicationContext().sendBroadcast(bcmsg);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                // Tell the main activity that we need to switch back to the air quality view
                //
                Intent bcmsg = new Intent("com.axoninstruments.main")
                        .putExtra("request", "CANCELVIEW");

                getActivity().getApplicationContext().sendBroadcast(bcmsg);
            }
        });
        return view;
    }

}
