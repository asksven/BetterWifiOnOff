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


import com.asksven.betterwifionoff.MyWidgetProvider;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.utils.WifiControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * General broadcast handler: handles event as registered on Manifest
 * @author sven
 *
 */
public class ConnectionStatusHandler extends BroadcastReceiver
{	
	private static final String TAG = "BetterWifiOnOff.ConnectionStatusHandler";
	
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
		
		// detect if Wifi is going off
		if (state == WifiManager.WIFI_STATE_DISABLING) // (info.getState().equals(NetworkInfo.State.DISCONNECTED))
		{
			Log.d(TAG, "Wifi was turned off");
			boolean disableWhenOff = sharedPrefs.getBoolean("disable_on_user_off", false);
			String lastAction = sharedPrefs.getString("last_action", ""); 		
			if (disableWhenOff && lastAction.equals("on"))
			{
				// User turned Wifi off. Respect that and disable processing
				Log.d(TAG, "User turned Wifi off: disable processing");
	            SharedPreferences.Editor editor = sharedPrefs.edit();
	            editor.putString("last_action", "off");
	            editor.commit();

				EventLogger.getInstance(context).addStatusChangedEvent(
						context.getString(R.string.event_disable_due_to_user_interaction));
				Intent intent2 = new Intent(context.getApplicationContext(), MyWidgetProvider.class);
				intent2.setAction(MyWidgetProvider.ACTION_DISABLE);
				context.sendBroadcast(intent2);
			}
			else
			{
				Log.d(TAG, "User turned Wifi off but previous action was off as well: no nothing");
			}
		}
		else if (state == WifiManager.WIFI_STATE_ENABLING)
		{
			Log.d(TAG, "Wifi was turned on");
			
			boolean enableWhenOn = sharedPrefs.getBoolean("enable_on_user_on", false);
			String lastAction = sharedPrefs.getString("last_action", ""); 		
			if (enableWhenOn && lastAction.equals("off"))
			{
				// User turned Wifi off. Respect that and disable processing
				Log.d(TAG, "User turned Wifi on: enable processing");
	            SharedPreferences.Editor editor = sharedPrefs.edit();
	            editor.putString("last_action", "on");
	            editor.commit();

				EventLogger.getInstance(context).addStatusChangedEvent(
						context.getString(R.string.event_enable_due_to_user_interaction));
				Intent intent2 = new Intent(context.getApplicationContext(), MyWidgetProvider.class);
				intent2.setAction(MyWidgetProvider.ACTION_ENABLE);
				context.sendBroadcast(intent2);
			}

			// scan for strongest AP if prefs are set so 
			if (sharedPrefs.getBoolean("connect_to_strongest_ap", false))
			{
				String whitelist = "";
				if (sharedPrefs.getBoolean("wifi_on_if_whitelisted", false))
				{
					whitelist = sharedPrefs.getString("wifi_whitelist", "");
					
				}
				String ssid = WifiControl.connectToBestNetwork(context, whitelist);
				if ((ssid != null) && (!ssid.equals("")))
				{
					Log.i(TAG, "No ssid connected to");
				}
				else
				{
					Log.i(TAG, "Connected to " + ssid);
				}
			}

			// scan for strongest AP if prefs are set so 
			if (sharedPrefs.getBoolean("check_for_cage", false))
			{
				// start thread for cage check
				WifiControl.doCageCheck(context);
			}

		}
	}
}
