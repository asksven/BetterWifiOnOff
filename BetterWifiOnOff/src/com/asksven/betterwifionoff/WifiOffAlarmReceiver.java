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

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean bProcess = prefs.getBoolean("wifi_on_if_in_call", false);
			if (bProcess)
			{
				Log.d(TAG, "Checking if in call");
				TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			    if ((telephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
			                || (telephony.getCallState() == TelephonyManager.CALL_STATE_RINGING))
			    {
			    	Log.w(TAG, "Phone is ringing or in a phone call, leave wifi on");
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
			    }
			}

			bProcess = prefs.getBoolean("wifi_on_if_downloading", false);
			
			if (bProcess)
			{
				Log.d(TAG, "Checking if downloads are active or pending");

				// are download going on?
				if (isDownloading(context))
				{
			    	Log.w(TAG, "Downloads are running or pending,  leave wifi on");
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
				}
					
			}
//			boolean bHasWifilock = WifiManagerProxy.hasWifiLock(context);

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
	
	@TargetApi(9)
	boolean isDownloading(Context context)
	{
		DownloadManager dl = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor query = dl.query(new DownloadManager.Query()
				.setFilterByStatus(DownloadManager.STATUS_PENDING
						| DownloadManager.STATUS_RUNNING));
		
		// are download going on?
		if (query.getCount() > 0)
		{
	    	Log.w(TAG, "Downloads are running or pending,  leave wifi on");
	    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
	    	return true;
		}
		else
		{
			return false;
		}
		 
	}
}
