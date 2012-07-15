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
import com.asksven.betterwifionoff.services.SetWifiStateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * General broadcast handler: handles event as registered on Manifest
 * @author sven
 *
 */
public class BroadcastHandler extends BroadcastReceiver
{	
	private static final String TAG = "BroadcastHandler";
	
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
        	
		}


        if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
		{
			Log.i(TAG, "Received Broadcast ACTION_POWER_DISCONNECTED, seralizing 'since unplugged'");
			
			boolean bProcess = sharedPrefs.getBoolean("wifi_off_when_power_ununplug", false);
			
			if (!bProcess)
			{
				// start service to turn off wifi
				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
				context.startService(serviceIntent);
			}
		}


	}
}
