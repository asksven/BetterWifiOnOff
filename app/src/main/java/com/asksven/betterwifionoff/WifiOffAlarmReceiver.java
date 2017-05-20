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

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.asksven.betterwifionoff.data.Constants;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.AppUtil;
import com.asksven.betterwifionoff.utils.WifiControl;

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
		EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_alarm));
		try
		{
			//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			
			SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.name, Context.MODE_MULTI_PROCESS);
			
			// if diabled do nothing
			if (prefs.getBoolean("disable_control", false))
			{
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_disabled));
				return;
			}

			// if in call do nothing
			Log.d(TAG, "Checking if in call");
			TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		    if ((telephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
		                || (telephony.getCallState() == TelephonyManager.CALL_STATE_RINGING))
		    {
		    	Log.w(TAG, "Phone is ringing or in a phone call, leave wifi on");
		    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_in_call));
		    	return;
		    }

			boolean bProcess = prefs.getBoolean("wifi_on_if_activity", false);
			
			if (bProcess)
			{
				Log.d(TAG, "Checking if there is network activity");

				// is there any network activity?
				if (WifiControl.isTransferring(context) || isDownloading(context))				{
			    	Log.i(TAG, "Network activity detected,  leave wifi on");
			    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_network_active));
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
				}
				else
				{
					Log.i(TAG, "No network activity detected");
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_network_inactive));
				}
			}
			
			// tethering always prevent Wifi to go off
			Log.d(TAG, "Checking if tethering is active");

			if (WifiControl.isWifiTethering(context))
			{
		    	Log.i(TAG, "Wifi tethering,  leave wifi on");
		    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_tethering_active));
		    	return;
			}
			else
			{
				Log.i(TAG, "No tethering detected");
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_tethering_inactive));
			}
			

			bProcess = prefs.getBoolean("wifi_on_when_screen_off_but_whitelisted", false);
			
			if (bProcess)
			{
				String whitelist = prefs.getString("wifi_whitelist", "");
				Log.i(TAG, "Checking against SSID whitelist: " + whitelist);
				if (WifiControl.isWhitelistedWifiConnected(context, whitelist))
				{
			    	Log.i(TAG, "Access point is whitelisted,  leave wifi on");
			    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_wl, WifiControl.connectedSsid(context)));
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
				}
				else
				{
					Log.d(TAG, "Access Point not whitelisted: turning  Wifi off");
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_access_point_not_wl, WifiControl.connectedSsid(context))); 
				}
			}
			
			bProcess = prefs.getBoolean("wifi_on_if_whitelisted_app_running", false);
			
			if (bProcess)
			{
				Log.i(TAG, "Checking if a whitelisted app is running");
				String packageName = AppUtil.isWhitelistedAppRunning(context);
				if (!packageName.equals(""))
				{
			    	Log.i(TAG, "Whitelisted app is running,  leave wifi on");
			    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_app_wl, packageName));
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
				}
				else
				{
					Log.d(TAG, "No whitelisted app running: turning  Wifi off");
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_app_not_wl)); 
				}
			}

			// start service to turn off wifi
			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_off)); 
			Intent serviceIntent = new Intent(context, SetWifiStateService.class);
			serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
			serviceIntent.putExtra(SetWifiStateService.EXTRA_REASON_OFF, true);
			context.startService(serviceIntent);
		}
		catch (Exception e)
		{
			EventLogger.getInstance(context).addSystemEvent("An error occured receiving the alarm: " + e.getMessage());
			Log.e(TAG, "An error occured receiving the alarm: " + Log.getStackTraceString(e));
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
	    	Log.i(TAG, query.getCount() + " downloads are running or pending");
	    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
	    	return true;
		}
		else
		{
			Log.i(TAG, query.getCount() + " downloads are running or pending");
			return false;
		}
		 
	}
}
