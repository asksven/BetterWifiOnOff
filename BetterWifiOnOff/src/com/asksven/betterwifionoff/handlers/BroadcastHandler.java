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

package com.asksven.betterwifionoff.handlers;


import com.asksven.betterwifionoff.PluggedWakelock;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.WifiControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * General broadcast handler: handles event as registered on Manifest
 * @author sven
 *
 */
public class BroadcastHandler extends BroadcastReceiver
{	
	private static final String TAG = "BetterWifiOnOff.BroadcastHandler";
	
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
        	// start the service
        	context.startService(new Intent(context, EventWatcherService.class));
        	
       		Log.d(TAG, "Boot completed, starting service");
		}


        if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
		{
			Log.i(TAG, "Received Broadcast ACTION_POWER_DISCONNECTED");
			
			// release any wakelocks / wifilocks
			PluggedWakelock.releaseWakelock();
			PluggedWakelock.releaseWifilock();
			
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

			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_power_disconnected));

			
			boolean bProcess = sharedPrefs.getBoolean("wifi_off_when_power_ununplug", false);
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			boolean screenOn = pm.isScreenOn();
			
			// turn on unplug only if screen is off. Else the screen off will take care of this later
			if (!screenOn && bProcess)
			{
		    	String strInterval = sharedPrefs.getString("wifi_off_delay", "30");
    	    	
				int delay = 30;
				try
		    	{
					delay = Integer.valueOf(strInterval);
		    	}
		    	catch (Exception e)
		    	{
		    	}
				
				
				if (delay > 0)
				{
					SetWifiStateService.scheduleWifiOffAlarm(context);

					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_scheduling_wifi_off_in, delay));


				}
				else
				{	
					// start service to turn off wifi
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_scheduling_wifi_off_now));

					Intent serviceIntent = new Intent(context, SetWifiStateService.class);
					serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
					context.startService(serviceIntent);
				}
			}
		}
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
		{
			Log.i(TAG, "Received Broadcast ACTION_POWER_CONNECTED");
			
			boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);
			if (bDisabled)
			{
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_disabled));
				Log.i(TAG, "Wifi handling is disabled: do nothing");
				return;
			}
			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_power_connected));

			boolean bWakelock = sharedPrefs.getBoolean("wakelock_while_power_plugged", false);
			boolean bWifilock = sharedPrefs.getBoolean("wifiock_while_power_plugged", false);
			boolean bWifilockHighPerf = sharedPrefs.getBoolean("wifilock_high_perf_while_power_plugged", false);

    		boolean bDisregard = sharedPrefs.getBoolean("disregard_airplane_mode", false);

    		// respect airplane mode
    		if (!bDisregard && (WifiControl.isAirplaneModeOn(context)))
    		{
    			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_airplane_mode));
    			Log.i(TAG, "Airplane Mode on: do nothing");
    			return;
    		}

			if (bWakelock)
			{
				// get a wakelock
				PluggedWakelock.acquireWakelock(context);
			}
			if (bWifilock)
			{
				// get a wakelock
				PluggedWakelock.acquireWifiLock(context);
			}
			if (bWifilockHighPerf)
			{
				// get a wakelock
				PluggedWakelock.acquireHighPerfWifiLock(context);
			}
			
        	Log.d(TAG, "Power was connected");
			
			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_power_plug", false);
			
			if (bProcess)
			{
				// start service to turn on wifi
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_on));

				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
				context.startService(serviceIntent);
			}
		}
	}
}
