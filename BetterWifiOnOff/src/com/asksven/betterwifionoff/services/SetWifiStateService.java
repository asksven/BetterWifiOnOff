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

import java.util.Calendar;
import com.asksven.betterwifionoff.utils.Logger;
import com.asksven.betterwifionoff.AlarmReceiver;
import com.asksven.betterwifionoff.utils.WifiControl;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sven
 *
 */
public class SetWifiStateService extends Service
{
	private static final String TAG = "SetWifiStateService";
	public static final String EXTRA_STATE = "com.asksven.betterwifionoff.WifiState";
	
	private static final int ALARM = 12;

	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// cancel any running alarms
		cancelAlarm(this);
		
		boolean state = intent.getBooleanExtra(EXTRA_STATE, false);
		Log.i(TAG, "Called with extra " + state);
		try
		{	
			WifiControl.setWifi(this, state);	
		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured: " + e.getMessage());
		}
		
		stopSelf();
		return START_NOT_STICKY;
		
		

	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	/**
	 * Adds an alarm to schedule a wakeup to retrieve the current location
	 */
	public static boolean setAlarm(Context ctx)
	{
		Logger.i(TAG, "setAlarm called");
		
		// cancel any exiting alarms
		cancelAlarm(ctx);

		// create a new one starting to count NOW
		Calendar cal = Calendar.getInstance();
		
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	String strInterval = prefs.getString("wifi_off_delay", "30");
    	    	
		int iInterval = 30;
		try
    	{
			iInterval = Integer.valueOf(strInterval);
    	}
    	catch (Exception e)
    	{
    	}

		long fireAt = System.currentTimeMillis() + (iInterval * 1000);

		Intent intent = new Intent(ctx, AlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);

		return true;
	}
	
	/**
	 * Cancels the current alarm (if existing)
	 */
	public static void cancelAlarm(Context ctx)
	{
		Logger.i(TAG, "cancelAlarm");
		// check if there is an intent pending
		Intent intent = new Intent(ctx, AlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (sender != null)
		{
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
			am.cancel(sender);
		}
	}

}