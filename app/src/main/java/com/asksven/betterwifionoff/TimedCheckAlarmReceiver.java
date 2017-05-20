/*

 * Copyright (C) 2013 asksven
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
import com.asksven.betterwifionoff.data.CellDBHelper;
import com.asksven.betterwifionoff.data.CellLogEntry;
import com.asksven.betterwifionoff.data.Constants;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.CellUtil;
import com.asksven.betterwifionoff.utils.WifiControl;

/**
 * Handles alarms to regularly check if a Wifi connection could be established
 * @author sven
 *
 */
public class TimedCheckAlarmReceiver extends BroadcastReceiver
{		 
	private static String TAG = "BetterWifiOnOff.TimedCheckAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Alarm received: turining Wifi on");
		SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.Preferences.name, Context.MODE_MULTI_PROCESS);
		
		// Schedule next alarm
		SetWifiStateService.scheduleTimerAlarm(context);
		
		try
		{
			// check if we should log cell info
			if (sharedPrefs.getBoolean("log_cells", false))
			{
				CellLogEntry cell = CellUtil.getCurrentCell(context);
				if (cell != null)
				{
					CellDBHelper db = new CellDBHelper(context);
					db.addCellLogEntry(cell);
					db.close();
				}
			}

			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			SetWifiStateService.cancelWifiOffAlarm(context);
			SetWifiStateService.cancelWifiOffAlarm(context);

			
        	boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);
    		if (bDisabled)
    		{
    			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_disabled));
    			Log.i(TAG, "Wifi handling is disabled: do nothing");
    			return;
    		}
    		
    		boolean bDisregard = sharedPrefs.getBoolean("disregard_airplane_mode", false);

    		// respect airplane mode
    		if (!bDisregard && (WifiControl.isAirplaneModeOn(context)))
    		{
    			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_airplane_mode));
    			Log.i(TAG, "Airplane Mode on: do nothing");
    			return;
    		}


			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_timed_check));
			
			if (WifiControl.isWifiConnected(context))
			{
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_already_on));
				
			}
			else
			{
				// start service to turn off wifi
		    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_on));

				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
				context.startService(serviceIntent);
			}

		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured receiving the alarm" + Log.getStackTraceString(e));
		}
	}
}
