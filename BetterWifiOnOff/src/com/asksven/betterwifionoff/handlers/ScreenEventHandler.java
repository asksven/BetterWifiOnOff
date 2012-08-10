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

import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.EventWatcherServiceBinder;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.ChargerUtil;
import com.asksven.betterwifionoff.utils.Logger;
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
			Logger.i(TAG, "Received Broadcast ACTION_SCREEN_OFF");
        	EventWatcherService myService = EventWatcherServiceBinder.getInstance(context).getService();
        	
    		boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);
    		if (bDisabled)
    		{
            	if (myService != null)
            	{
            		myService.getEventLogger().addSystemEvent("Disabled: do nothing");
            	}

    			Log.i(TAG, "Wifi handling is disabled: do nothing");
    			return;
    		}

        	if (myService != null)
        	{
        		myService.getEventLogger().addUserEvent("Screen was turned off");
        	}

			boolean bProcess = sharedPrefs.getBoolean("wifi_off_when_screen_off", false);
			boolean bCheckIfPowered = sharedPrefs.getBoolean("wifi_on_when_screen_off_but_power_plugged", true);
			
			
			if (bProcess)
			{
				if (bCheckIfPowered && ChargerUtil.isConnected(context))
				{
		        	if (myService != null)
		        	{
		        		myService.getEventLogger().addStatusChangedEvent("Leaving Wifi on because charger is connected");
		        	}
					Logger.i(TAG, "Currently connected to A/C and preference is true: leaving on");

				}
				else
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
					}
					else
					{	
						// start service to turn off wifi
			        	if (myService != null)
			        	{
			        		myService.getEventLogger().addStatusChangedEvent("Turning off Wifi");
			        	}
						Logger.i(TAG, "Turining Wifi off immediately");

	
						Intent serviceIntent = new Intent(context, SetWifiStateService.class);
						serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
						context.startService(serviceIntent);
					}
				}
			}
		}

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			Logger.i(TAG, "Received Broadcast ACTION_SCREEN_ON");
        	EventWatcherService myService = EventWatcherServiceBinder.getInstance(context).getService();

        	boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);
    		if (bDisabled)
    		{
            	if (myService != null)
            	{
            		myService.getEventLogger().addSystemEvent("Disabled: do nothing");
            	}

    			Log.i(TAG, "Wifi handling is disabled: do nothing");
    			return;
    		}

        	if (myService != null)
        	{
        		myService.getEventLogger().addUserEvent("Screen was turned on");
        	}

			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_on", false);
			
			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			if (bProcess)
			{
				SetWifiStateService.cancelWifiOffAlarm(context);
			}
			
			if (WifiControl.isWifiConnected(context))
			{
	        	if (myService != null)
	        	{
	        		myService.getEventLogger().addSystemEvent("Wifi is already on: nothing to do");
	        	}
				
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
			Logger.i(TAG, "Received Broadcast ACTION_USER_PRESENT");
        	EventWatcherService myService = EventWatcherServiceBinder.getInstance(context).getService();
        	
    		boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);
    		if (bDisabled)
    		{
            	if (myService != null)
            	{
            		myService.getEventLogger().addSystemEvent("Disabled: do nothing");
            	}

    			Log.i(TAG, "Wifi handling is disabled: do nothing");
    			return;
    		}

        	if (myService != null)
        	{
        		myService.getEventLogger().addUserEvent("Screen was unlocked");
        	}

			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_unlock", false);

			// make sure to cancel pendng alarms that may still be running from a previous screen off event
			if (bProcess)
			{
				SetWifiStateService.cancelWifiOffAlarm(context);
				// start service to turn off wifi
				wifiOn(context);
			}
		}

        Intent i = new Intent(context, EventWatcherService.class);
        context.startService(i);

    }
    
    public void wifiOn(Context context)
    {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		// start service to turn off wifi
    	EventWatcherService myService = EventWatcherServiceBinder.getInstance(context).getService();
    	if (myService != null)
    	{
    		myService.getEventLogger().addStatusChangedEvent("Turning Wifi on");
    	}

		Intent serviceIntent = new Intent(context, SetWifiStateService.class);
		serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
		context.startService(serviceIntent);
    }
}