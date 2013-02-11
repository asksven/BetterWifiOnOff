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

import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.ChargerUtil;
import com.asksven.betterwifionoff.utils.WifiControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sven
 *
 */
public class ScreenEventHandler extends BroadcastReceiver
{

	private static final String TAG = "ScreenEventHandler";

    @Override
    public void onReceive(Context context, Intent intent)
    {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			Log.i(TAG, "Received Broadcast ACTION_SCREEN_OFF");
        	
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


    		EventLogger.getInstance(context).addUserEvent(context.getString(R.string.event_screen_off));
    		
			boolean bProcess = sharedPrefs.getBoolean("wifi_off_when_screen_off", false);
			boolean bCheckIfPowered = sharedPrefs.getBoolean("wifi_on_when_screen_off_but_power_plugged", true);
			
			
			if (bProcess)
			{
				if (bCheckIfPowered && ChargerUtil.isConnected(context))
				{
					EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_leave_on_charing));
					Log.i(TAG, "Currently connected to A/C and preference is true: leaving on");

				}
				else
				{					
					if (WifiControl.isWifiOn(context))
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

						SetWifiStateService.scheduleWifiOffAlarm(context);
					}
				}
			}
		}

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			Log.i(TAG, "Received Broadcast ACTION_SCREEN_ON");
			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			SetWifiStateService.cancelWifiOffAlarm(context);

			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_on", false);
			
			if (!bProcess)
			{
				return;
			}
			
			// make sure to cancel pendng alarms that may still be running from a previous screen off event
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

			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			SetWifiStateService.cancelWifiOffAlarm(context);

			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_screen_on));
			
			if (WifiControl.isWifiConnected(context))
			{
				EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_already_on));
				
			}
			else
			{
				if (bProcess)
				{				
					// start service to turn on wifi
					wifiOn(context);
				}
			}
		}
        
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
		{
			Log.i(TAG, "Received Broadcast ACTION_USER_PRESENT");
			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_unlock", false);

			if (!bProcess)
			{
				return;
			}

			// make sure to cancel pendng alarms that may still be running from a previous screen off event
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



			EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_screen_unlocked));

			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			if (bProcess)
			{
				// start service to turn off wifi
				wifiOn(context);
			}
		}

        Intent i = new Intent(context, EventWatcherService.class);
        context.startService(i);

    }
    
    public static void wifiOn(Context context)
    {
		// start service to turn off wifi
    	EventLogger.getInstance(context).addStatusChangedEvent(context.getString(R.string.event_wifi_on));

		Intent serviceIntent = new Intent(context, SetWifiStateService.class);
		serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
		context.startService(serviceIntent);
    }
}