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

import com.asksven.betterwifionoff.data.EventBroadcaster;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.WifiControl;

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
		EventBroadcaster.sendStatusEvent(context, "Alarm received: preparing to turn Wifi off");
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
			    	EventBroadcaster.sendStatusEvent(context, "Phone is ringing or in a phone call, leave wifi on");
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
			    }
			}

			bProcess = prefs.getBoolean("wifi_on_if_activity", false);
			
			if (bProcess)
			{
				Log.d(TAG, "Checking if there is network activity");

				// is there any network activity?
				if (WifiControl.isTransferring() || isDownloading(context))				{
			    	Log.i(TAG, "Network activity detected,  leave wifi on");
			    	EventBroadcaster.sendStatusEvent(context, "Network activity detected,  leave wifi on");
			    	SetWifiStateService.scheduleRetryWifiOffAlarm(context);
			    	return;
				}
				else
				{
					Log.i(TAG, "No network activity detected");
					EventBroadcaster.sendStatusEvent(context, "No network activity detected,  turining wifi off");
				}
			}

			// start service to turn off wifi
			Intent serviceIntent = new Intent(context, SetWifiStateService.class);
			serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
			serviceIntent.putExtra(SetWifiStateService.EXTRA_MESSAGE, "Timeout to turn Wifi off reached, turning Wifi off");
			context.startService(serviceIntent);
//			}
		}
		catch (Exception e)
		{
			EventBroadcaster.sendErrorEvent(context, "An error occured receiving the alarm: " + e.getMessage());
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
