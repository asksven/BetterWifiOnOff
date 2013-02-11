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

import com.asksven.android.common.utils.DateUtils;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.handlers.ScreenEventHandler;
import com.asksven.betterwifionoff.services.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyWidgetProvider extends AppWidgetProvider
{

	public static final String ACTION_REFRESH = "ACTION_REFRESH";
	public static final String ACTION_ENABLE = "ACTION_DISABLE";
	public static final String ACTION_DISABLE = "ACTION_ENABLE";
	
	private static final String TAG = "BetterWifiOnOff.MyWidgetProvider";
	public static final String ACTION_CLICK = "ACTION_CLICK";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);

		Log.i(TAG, "onReceive method called, action = '" + intent.getAction() + "' at " + DateUtils.now());
		
		if ( (ACTION_CLICK.equals(intent.getAction())) )
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    		boolean bDisabled = sharedPrefs.getBoolean("disable_control", false);

    		// write back the new value
    		boolean bNewState = !bDisabled;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            if (bNewState)
            {
				EventLogger.getInstance(context).addStatusChangedEvent(
						context.getString(R.string.event_disable_due_to_user_interaction));

            }
            else
            {
				EventLogger.getInstance(context).addStatusChangedEvent(
						context.getString(R.string.event_enable_due_to_user_interaction));
    			// turn Wifi on
				ScreenEventHandler.wifiOn(context);


            }
            editor.putBoolean("disable_control", bNewState);
            editor.commit();

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(),
					this.getClass().getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);

			if (appWidgetIds.length > 0)
			{
				onUpdate(context, appWidgetManager, appWidgetIds);
			}
			else
			{
				Log.i(TAG, "No widget found to update");
			}
		}

		if ( (ACTION_ENABLE.equals(intent.getAction())) || (ACTION_DISABLE.equals(intent.getAction())) )
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    		boolean bDisabled = false;
    		
    		if (ACTION_ENABLE.equals(intent.getAction()))
    		{
    			bDisabled = false;
    		}
    		else
    		{
    			bDisabled = true;
    		}

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("disable_control", bDisabled);
            editor.commit();

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(),
					this.getClass().getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);

			if (appWidgetIds.length > 0)
			{
				onUpdate(context, appWidgetManager, appWidgetIds);
			}
			else
			{
				Log.i(TAG, "No widget found to update");
			}
		}

		if ( (ACTION_REFRESH.equals(intent.getAction())) )
		{

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(),
					this.getClass().getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);

			if (appWidgetIds.length > 0)
			{
				onUpdate(context, appWidgetManager, appWidgetIds);
			}
			else
			{
				Log.i(TAG, "No widget found to update");
			}
		}

	}

	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{

		Log.w(TAG, "onUpdate method called");
		// Get all ids
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);
	}
}