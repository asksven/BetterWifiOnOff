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
package com.asksven.betterwifionoff.services;

import java.util.Random;

import com.asksven.android.common.privateapiproxies.R.drawable;
import com.asksven.betterwifionoff.MyWidgetProvider;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service
{
	private static final String TAG = "BetterWifiOnOff.UpdateWidgetService";

	@Override
	public void onStart(Intent intent, int startId)
	{
		Log.i(TAG, "Called");
		// Create some random data

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(), MyWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w(TAG, "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w(TAG, "Direct" + String.valueOf(allWidgetIds2.length));

		for (int widgetId : allWidgetIds)
		{
			// read the current status
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    		boolean bState = sharedPrefs.getBoolean("disable_control", false);

            // update widgets
			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_layout);
			
			// Set the icon
			if (bState)
			{
				//remoteViews.setTextViewText(R.id.status, "Off");
				remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_widget_disabled);
			}
			else
			{
				//remoteViews.setTextViewText(R.id.status, "On");
				remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_widget_enabled);
			}
			
			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(), MyWidgetProvider.class);

			clickIntent.setAction(MyWidgetProvider.ACTION_CLICK); //AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

			// make the widget clickable
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();

		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}