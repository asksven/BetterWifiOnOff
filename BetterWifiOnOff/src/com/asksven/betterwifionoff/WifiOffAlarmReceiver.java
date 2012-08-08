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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Handles alarms set to turn off Wifi with a delay 
 * @author sven
 *
 */
public class WifiOffAlarmReceiver extends BroadcastReceiver
{		 
	private static String TAG = "BetterWifiOnOff.WifiOffAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Alarm received: preparing to turn Wifi off");
		try
		{
			// see if we want to respect Wifilocks
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//			boolean bProcess = prefs.getBoolean("wifi_on_when_screen_off_but_wifilock", false);
//			if (bProcess)
//			{
//				Log.d(TAG, "Checking if Wifilock is present as preferences are set to respect Wifilocks");
//			}
//			else
//			{
//				Log.d(TAG, "Wifilock will not be respected");	
//			}
//			
//			boolean bHasWifilock = WifiManagerProxy.hasWifiLock(context);
//			Log.d(TAG, "BetterWifiOnOff Wifilock state: " + Wakelock.holdsWifiLock());
//			
//			if (bHasWifilock)
//			{
//				Log.d(TAG, "A Wifilock was detected");
//			}
//			else
//			{
//				Log.d(TAG, "No Wifilock was detected");
//			}
//			
//			
//			
//			
//			if (bProcess && bHasWifilock)
//			{
//				Log.d(TAG, "A Wifilock is help: rescheduling Wifi off");
//				SetWifiStateService.scheduleRetryWifiOffAlarm(context);
//			}
//			else
//			{
				// start service to turn off wifi
				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_MESSAGE, "Timeout to turn Wifi off reached, turning Wifi off");
				context.startService(serviceIntent);
//			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured receiving the alarm");
		}
	}
}
