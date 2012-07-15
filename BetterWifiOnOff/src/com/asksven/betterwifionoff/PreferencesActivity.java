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

import com.asksven.betterwifionoff.utils.Configuration;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

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
public class PreferencesActivity extends PreferenceActivity
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
				findPreference("notify_errors").setEnabled(false);
				findPreference("start_on_boot").setEnabled(false);
				findPreference("update_interval").setEnabled(false);
				findPreference("update_interval").setSummary("Latitude update interval: 15 minutes");
				findPreference("update_accuracy").setEnabled(false);
				findPreference("update_accuracy").setSummary("Latitude update accuracy: 2 Km");
				findPreference("map_loc_provider").setEnabled(false);
				findPreference("map_loc_provider").setSummary("Map location provider: cell network");
				findPreference("map_update_interval").setEnabled(false);
				findPreference("map_update_interval").setSummary("Map update interval: 15 minutes");
				findPreference("map_update_accuracy").setEnabled(false);
				findPreference("map_update_accuracy").setSummary("Map update accuracy: 2 Km");
				findPreference("quick_update_interval").setEnabled(false);
				findPreference("quick_update_accuracy").setEnabled(false);
				findPreference("quick_update_duration").setEnabled(false);
				findPreference("use_account_manager").setEnabled(false);
				findPreference("update_on_wifi_only").setEnabled(false);
			}
			catch (Exception e)
			{
				Log.e(TAG, "An error occured while loading the preferences.");
			}
		}
	}
}
