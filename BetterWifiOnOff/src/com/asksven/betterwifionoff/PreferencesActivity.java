/*
 * Copyright (C) 2012 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asksven.betterwifionoff;

import com.asksven.android.common.wifi.WifiManagerProxy;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.ChargerUtil;
import com.asksven.betterwifionoff.utils.Configuration;
import com.asksven.betterwifionoff.utils.Logger;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity for managing preferences using Android's preferences framework
 * @see http://www.javacodegeeks.com/2011/01/android-quick-preferences-tutorial.html
 * 
 * Access prefs goes like this:
 *   SharedPreferences sharedPrefs = 
 *   	PreferenceManager.getDefaultSharedPreferences(this);
 *   sharedPrefs.getBoolean("perform_updates", false));
 *   
 * @author sven
 *
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private static String TAG = "PreferencesActivity";
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// disable all LocationListener prefs in free version
		if (!Configuration.isFullVersion(this))
		{
			try
			{
				// sample action: findPreference("my-pref-here").setEnabled(false);
			}
			catch (Exception e)
			{
				Logger.e(TAG, "An error occured while loading the preferences.");
			}
		}
        // Set up a listener whenever a key changes
    	PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

	}
	
	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
        // Unregister the listener whenever a key changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

	}
	
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
    	
    	// handle wifi_on_when_screen_unlock and wifi_on_when_screen_on as a toggle
    	if (key.equals("wifi_on_when_screen_unlock"))
    	{
    		// if this value was just turned on make sure "wen_screen_off" gets unchecked
    		if (prefs.getBoolean("wifi_on_when_screen_unlock", true))
    		{
				if (prefs.getBoolean("wifi_on_when_screen_on", true))
				{
	    	        SharedPreferences.Editor editor = prefs.edit();
	    	        editor.putBoolean("wifi_on_when_screen_on", false);
	    	        editor.commit();
	    	        CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("wifi_on_when_screen_on");
	    	        checkboxPref.setChecked(false);
	    	        
	    		}
    		}
    	}

    	if (key.equals("wifi_on_when_screen_on"))
    	{
    		if (prefs.getBoolean("wifi_on_when_screen_on", true))
    		{
    			if (prefs.getBoolean("wifi_on_when_screen_unlock", true))
	    		{
	    	        SharedPreferences.Editor editor = prefs.edit();
	    	        editor.putBoolean("wifi_on_when_screen_unlock", false);
	    	        editor.commit();
	    	        CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("wifi_on_when_screen_unlock");
	    	        checkboxPref.setChecked(false);
	    		}
    		}
    	}

    	// handle wifilock_while_power_plugged and wifilock_full_power_while_power_plugged as a toggle
    	if (key.equals("wifilock_while_power_plugged"))
    	{
    		if (prefs.getBoolean("wifilock_while_power_plugged", true))
    		{
    			// turn off the other two
    	        SharedPreferences.Editor editor = prefs.edit();
    	        editor.putBoolean("wifilock_high_perf_while_power_plugged", false);
    	        editor.commit();
    	        CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("wifilock_high_perf_while_power_plugged");
    	        checkboxPref.setChecked(false);
    		}
    		
    	}
    	if (key.equals("wifilock_high_perf_while_power_plugged"))
    	{
    		if (prefs.getBoolean("wifilock_high_perf_while_power_plugged", true))
    		{
    			// turn off the other two
    	        SharedPreferences.Editor editor = prefs.edit();
    	        editor.putBoolean("wifilock_while_power_plugged", false);
    	        editor.commit();
    	        CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("wifilock_while_power_plugged");
    	        checkboxPref.setChecked(false);
    		}
    		
    	}
    	if (key.equals("wakelock_while_power_plugged"))
    	{
    		if (!prefs.getBoolean("wakelock_while_power_plugged", false))
    		{
    			// option was uncheck: make sure to release wakelock straight away
    			PluggedWakelock.releaseWakelock();
    		}
    		else
    		{
    			// if we are plugged to power apply
    			if (ChargerUtil.isConnected(this))
    			{
    				PluggedWakelock.acquireWakelock(this);
    			}
    		}
    	}
    	if (key.equals("wifilock_while_power_plugged"))
    	{
    		if (!prefs.getBoolean("wifilock_while_power_plugged", false))
    		{
    			// option was uncheck: make sure to release wakelock straight away
    			PluggedWakelock.releaseWifilock();
    		}
    		else
    		{
    			if (ChargerUtil.isConnected(this))
    			{
    				PluggedWakelock.acquireWifiLock(this);
    			}
    		}

    	}
    	if (key.equals("wifilock_high_perf_while_power_plugged"))
    	{
    		if (!prefs.getBoolean("wifilock_while_power_plugged", false))
    		{
    			// option was uncheck: make sure to release wakelock straight away
    			PluggedWakelock.releaseWifilock();
    		}
    		else
    		{
    			if (ChargerUtil.isConnected(this))
    			{
    				PluggedWakelock.acquireHighPerfWifiLock(this);
    			}
    		}
    	}

    	if (key.equals("wifi_whitelist"))
    	{
    		if (prefs.getBoolean("wifi_on_if_whitelisted", false))
    		{
    			// whitelist has changed, retest if connection should be kept active
    			SetWifiStateService.scheduleWifiConnectedAlarm(PreferencesActivity.this);
    		}
    	}
//    	if (key.equals("wifilock"))
//    	{
//    		if (!prefs.getBoolean("wifilock", false))
//    		{
//    			// option was uncheck: make sure to release wakelock straight away
//    			Wakelock.releaseWifilock();
//    			Toast.makeText(this, WifiManagerProxy.getWifiLocks(this) + " Wifilocks detected using API", Toast.LENGTH_LONG).show();
//    			
//    		}
//    		else
//    		{
//    			// get Wifilock
//    			Wakelock.acquireWifiLock(this);
//    			Toast.makeText(this, WifiManagerProxy.getWifiLocks(this) + " Wifilocks detected using API", Toast.LENGTH_LONG).show();
//    		}
//    	}

    }
}
