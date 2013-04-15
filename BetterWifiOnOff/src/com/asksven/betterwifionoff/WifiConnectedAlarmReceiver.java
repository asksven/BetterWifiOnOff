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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.asksven.android.common.kernelutils.Wakelocks;
import com.asksven.betterwifionoff.data.Constants;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.WifiControl;

/**
 * Handles alarms to turn off Wifi is a connection could not be established
 * @author sven
 *
 */
public class WifiConnectedAlarmReceiver extends BroadcastReceiver
{		 
	private static String TAG = "BetterWifiOnOff.WifiConnectedAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Alarm received: checking connection");
		SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.Preferences.name, Context.MODE_MULTI_PROCESS);
		

		try
		{
			// if diabled do nothing
			if (sharedPrefs.getBoolean("disable_control", false))
			{
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_disabled));
				return;
			}
			
			// start service to turn off wifi if connection was not established till now
			if (!WifiControl.isWifiConnected(context))
			{
				Log.d(TAG, "No connection: turning Wifi off");
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_no_wifi_connection));

				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_REASON_OFF, false);
				context.startService(serviceIntent);
				return;
			}
			else
			{
				// check if the SSID needs to be checked against whitelist

				boolean bCheckWhiteList 	= sharedPrefs.getBoolean("wifi_on_if_whitelisted", false);
				
				if (bCheckWhiteList)
				{
					String whitelist = sharedPrefs.getString("wifi_whitelist", "");
					if (!WifiControl.isWhitelistedWifiConnected(context, whitelist))
					{
						Log.d(TAG, "Access point is not whitelisted: turning Wifi off");
						EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_not_wl, WifiControl.connectedSsid(context))); 

						Intent serviceIntent = new Intent(context, SetWifiStateService.class);
						serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
						serviceIntent.putExtra(SetWifiStateService.EXTRA_REASON_OFF, false);
						context.startService(serviceIntent);
						return;
					}
					else
					{
						Log.d(TAG, "Access Point wihtelisted: leaving Wifi on");
						EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_wl, WifiControl.connectedSsid(context))); 
					}
				}
				else
				{
					Log.d(TAG, "Connection active: leaving Wifi on");
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_connection_active));
				}
				
				boolean bCheckCage 	= sharedPrefs.getBoolean("check_for_cage", false);
				if (bCheckCage)
				{
					if (WifiControl.isWifiCagedAlt(context))
					{
						Log.d(TAG, "Access point is caged: turning Wifi off");
						EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_caged)); 

						Intent serviceIntent = new Intent(context, SetWifiStateService.class);
						serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
						serviceIntent.putExtra(SetWifiStateService.EXTRA_REASON_OFF, false);
						context.startService(serviceIntent);
						return;
					}
					else
					{
						Log.d(TAG, "Connection not caged: leaving Wifi on");
						EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_not_caged));
						
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured receiving the alarm" + Log.getStackTraceString(e));
		}
	}
}
