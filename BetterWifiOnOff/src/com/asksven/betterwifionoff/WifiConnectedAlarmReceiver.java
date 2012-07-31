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
import android.util.Log;

import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.WifiControl;

/**
 * Handles alarms to turn off Wifi is a connection could not be established
 * @author sven
 *
 */
public class WifiConnectedAlarmReceiver extends BroadcastReceiver
{		 
	private static String TAG = "WifiConnectedAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Alarm received: checking connection");
		

		try
		{
			// start service to turn off wifi if connection was not established till now
			if (!WifiControl.isWifiConnected(context))
			{
				Log.d(TAG, "No connection: turning Wifi off");


				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_MESSAGE, "No Wifi connection could be established. Turning off Wifi");
				context.startService(serviceIntent);
			}
			else
			{
				Log.d(TAG, "Connection active: leaving Wifi on");
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured receiving the alarm" + e.getMessage());
		}
	}
}
