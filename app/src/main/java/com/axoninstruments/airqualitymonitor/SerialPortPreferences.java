/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.axoninstruments.airqualitymonitor;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class SerialPortPreferences extends PreferenceActivity {

//	private SerialPortFinder mSerialPortFinder;

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.serial_port_preferences1);
        addPreferencesFromResource(R.xml.serial_port_preferences2);
        addPreferencesFromResource(R.xml.serial_port_preferences3);
		//
		// VSD port
		//
        Populate(0, "BAUDRATE1", "DATABITS1", "PARITY1", "STOPBITS1");
        //
        // Gauge Serial Port
        //
        Populate(1, "BAUDRATE2", "DATABITS2", "PARITY2", "STOPBITS2");
        //
        // Modbus Slave Serial Port
        //
        Populate(2, "BAUDRATE3", "DATABITS3", "PARITY3", "STOPBITS3");
        //
        // Setup the Modbus Slave ID preference
        //
        ListPreference mbslave = (ListPreference) findPreference("MBSLAVEID");
        mbslave.setSummary(mbslave.getValue());
        mbslave.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });

    }

    protected void Populate(int Device, String Baudrate, String DataBits, String Parity,
                            String Stopbits) {
        //
        // Baud rates
        //
        ListPreference baudrates = (ListPreference) findPreference(Baudrate);
        baudrates.setSummary(baudrates.getValue());
        baudrates.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
        //
        // Data bits
        //
        ListPreference databits = (ListPreference) findPreference(DataBits);
        databits.setSummary(databits.getEntry());
        databits.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
        //
        // Parity
        //
        final ListPreference parity = (ListPreference) findPreference(Parity);
        parity.setSummary(parity.getEntry());
        parity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(parity.getEntry());
                return true;
            }
        });
        //
        // Stop bits
        //
        ListPreference stopbits = (ListPreference) findPreference(Stopbits);
        stopbits.setSummary(stopbits.getEntry());
        stopbits.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
    }
}
