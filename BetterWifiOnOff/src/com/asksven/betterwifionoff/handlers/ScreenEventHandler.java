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
			boolean bProcess = sharedPrefs.getBoolean("wifi_off_when_screen_off", false);
			
			if (bProcess)
			{
				// start service to turn off wifi
				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, false);
				context.startService(serviceIntent);
			}
		}

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			Log.i(TAG, "Received Broadcast ACTION_SCREEN_ON");
			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_on", false);
			
			if (bProcess)
			{
				// start service to turn off wifi
				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
				context.startService(serviceIntent);
			}



			
		}
        
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
		{
			Log.i(TAG, "Received Broadcast ACTION_USER_PRESENT");
			boolean bProcess = sharedPrefs.getBoolean("wifi_on_when_screen_unlock", false);
			
			if (bProcess)
			{
				// start service to turn off wifi
				Intent serviceIntent = new Intent(context, SetWifiStateService.class);
				serviceIntent.putExtra(SetWifiStateService.EXTRA_STATE, true);
				context.startService(serviceIntent);
			}
			

		}

        Intent i = new Intent(context, EventWatcherService.class);
        context.startService(i);
        
//		// Build the intent to update widgets
//		Intent intentRefreshWidgets = new Intent(LargeWidgetProvider.WIDGET_UPDATE);
//		context.sendBroadcast(intentRefreshWidgets);

    }
    
}