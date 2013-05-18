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

import com.asksven.android.common.kernelutils.Wakelocks;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.TimedCheckAlarmReceiver;
import com.asksven.betterwifionoff.WifiConnectedAlarmReceiver;
import com.asksven.betterwifionoff.WifiOffAlarmReceiver;
import com.asksven.betterwifionoff.data.CellDBHelper;
import com.asksven.betterwifionoff.data.CellInfo;
import com.asksven.betterwifionoff.data.CellLogEntry;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.utils.CellUtil;
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
	private static final String TAG = "BetterWifiOnOff.SetWifiStateService";
	public static final String EXTRA_STATE = "com.asksven.betterwifionoff.WifiState";
	public static final String EXTRA_REASON_OFF = "com.asksven.betterwifionoff.WifiReasonOff";
	
	private static final int ALARM_WIFI_OFF 		= 12;
	private static final int ALARM_WIFI_CONNECTED 	= 13;
	private static final int ALARM_TIMER		 	= 14;

	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	
		boolean state = intent.getBooleanExtra(EXTRA_STATE, false);
		
		// tells whether Wifi is turned off because it was scheduled to go off
		boolean reasonOff = intent.getBooleanExtra(EXTRA_REASON_OFF, false);
		Log.i(TAG, "Called with extra " + state + ", " + reasonOff);
		
		boolean bCheckWakelocks 	= sharedPrefs.getBoolean("wifi_on_if_wakelock", false);
		boolean bCheckKnownCid	 	= sharedPrefs.getBoolean("wifi_on_only_when_known_cid", false);
		
		// if location awareness is on only turn on wifi when the cell id is known (tagged)
		if (state && bCheckKnownCid)
		{
			CellLogEntry cellInfo = CellUtil.getCurrentCell(this);
			if (cellInfo != null)
			{
				int cid = cellInfo.getCid();
				CellDBHelper db = new CellDBHelper(this);
				String tags = db.getCellTagsAsString(cid);
				db.close();
			
				if (tags.equals(""))
				{
					// unknown cell: do nothing
					Log.d(TAG, "Cell " + cid + " has no tags: do not turn wifi on");
					EventLogger.getInstance(this).addStatusChangedEvent(this.getString(R.string.event_unknown_cell)); 
					stopSelf();
					return START_NOT_STICKY;				
				}
				else
				{
					// known cell: go on
					Log.d(TAG, "Cell " + cid + " has tags " + tags + ": turn wifi on");
					EventLogger.getInstance(this).addStatusChangedEvent(this.getString(R.string.event_known_cell)); 
				}
			}
		}
		// if Wifi is going to be tured off (reasonOff distinguishes the case) we may want to respect Wakelocks
		// This must be done here (instead of the Alarm receiver as there is a wakelock being held while alarms are processed
		if (reasonOff && !state && (bCheckWakelocks))
		{
			if (Wakelocks.hasWakelocks(this))
			{
				Log.d(TAG, "Wakelock detected: postponing Wifi off");
				EventLogger.getInstance(this).addStatusChangedEvent(this.getString(R.string.event_wakelock_detected)); 

		    	SetWifiStateService.scheduleRetryWifiOffAlarm(this);
				stopSelf();
				return START_NOT_STICKY;

			}
			else
			{
				Log.d(TAG, "No wakelocks detected: turning Wifi off");
				EventLogger.getInstance(this).addStatusChangedEvent(this.getString(R.string.event_no_wakelock_detected)); 
			}
		}

		
		try
		{	
			WifiControl.setWifi(this, state);
			
			// write the last action 
            SharedPreferences.Editor editor = sharedPrefs.edit();
            if (state)
            {
            	editor.putString("last_action", "on");
            }
            else
            {
            	editor.putString("last_action", "off");
            }
            
            editor.commit();

			
			// cancel pending alarms planned to turn wifi on or off
			if (state)
			{
				cancelWifiOffAlarm(this);
			}
			else
			{
				cancelWifiConnectedAlarm(this);
			}
				        
	        // check if we need to schedule and alarm for delayed check if a connection could be established
			boolean bProcess = sharedPrefs.getBoolean("wifi_on_if_connected", true);
			
			if (state && bProcess)
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
				
				// start thread for cage check
				WifiControl.doCageCheck(this);

				SetWifiStateService.scheduleWifiConnectedAlarm(this);
				
			}
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
	public static boolean scheduleWifiOffAlarm(Context ctx)
	{
		Log.i(TAG, "scheduleOffAlarm called");

		// reset the retry counter
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("wifi_off_retries", 0);
        editor.commit();

		// store a reference for throughput measurement
        if (sharedPrefs.getBoolean("wifi_on_if_activity", false))
        {
        	WifiControl.snapshot(ctx);
        }

		// cancel any exiting alarms
		cancelWifiOffAlarm(ctx);

		// create a new one starting to count NOW
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

		EventLogger.getInstance(ctx).addStatusChangedEvent(ctx.getString(R.string.event_scheduling_wifi_off_in, iInterval));

		long fireAt = System.currentTimeMillis() + (iInterval * 1000);

		Intent intent = new Intent(ctx, WifiOffAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_WIFI_OFF,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);

		return true;
	}
	
	/**
	 * Adds an alarm to schedule a wakeup to retrieve the current location
	 */
	public static boolean scheduleRetryWifiOffAlarm(Context ctx)
	{
		Log.i(TAG, "scheduleOffAlarm called");
		
		// cancel any exiting alarms
		cancelWifiOffAlarm(ctx);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int retries = sharedPrefs.getInt("wifi_off_retries", 0) + 1;
		
		if (retries <= 5)
		{
	        SharedPreferences.Editor editor = sharedPrefs.edit();
	        editor.putInt("wifi_off_retries", retries);
	        editor.commit();
		}

		// store a reference for throughput measurement
        if (sharedPrefs.getBoolean("wifi_on_if_activity", false))
        {
        	WifiControl.snapshot(ctx);
        }

		// create a new one starting to count NOW
		
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	String strInterval = prefs.getString("wifi_off_delay", "30");
    	    	
		int iInterval = 30;
		try
    	{
			iInterval = Integer.valueOf(strInterval);
    	}
    	catch (Exception e)
    	{
    		iInterval = 30;
    	}

		if (retries >= 5)
		{
			// set timeout to 15 minutes
			iInterval = 15 * 60;
		}
		else
		{
			// increase interval depending on retries
			iInterval = iInterval + (iInterval * retries);
		}
		
		EventLogger.getInstance(ctx).addStatusChangedEvent(ctx.getString(R.string.event_scheduling_wifi_off_in, iInterval));
		
		long fireAt = System.currentTimeMillis() + (iInterval * 1000);

		Intent intent = new Intent(ctx, WifiOffAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_WIFI_OFF,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);

		return true;
	}

	/**
	 * Cancels the current alarm (if existing)
	 */
	public static void cancelWifiOffAlarm(Context ctx)
	{
		// check if there is an intent pending
		Intent intent = new Intent(ctx, WifiOffAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_WIFI_OFF,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (sender != null)
		{

	    	// Get the AlarmManager service
			AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
			am.cancel(sender);
		}
	}

	/**
	 * Adds an alarm to schedule a wakeup to retrieve the current location
	 */
	public static boolean scheduleWifiConnectedAlarm(Context ctx)
	{
		Log.i(TAG, "scheduleWifiConnectedAlarm called");
		
		// create a new one starting to count NOW
		
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
		
		EventLogger.getInstance(ctx).addStatusChangedEvent(ctx.getString(R.string.event_scheduling_wifi_off_in, iInterval));
		long fireAt = System.currentTimeMillis() + (iInterval * 1000);

		Intent intent = new Intent(ctx, WifiConnectedAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_WIFI_CONNECTED,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);

		return true;
	}
	
	/**
	 * Cancels the current alarm (if existing)
	 */
	public static void cancelWifiConnectedAlarm(Context ctx)
	{
		// check if there is an intent pending
		Intent intent = new Intent(ctx, WifiConnectedAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_WIFI_CONNECTED,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (sender != null)
		{

			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
			am.cancel(sender);
		}
	}

	/**
	 * Adds an alarm to schedule a wakeup to retrieve the current location
	 */
	public static boolean scheduleTimerAlarm(Context ctx)
	{
		Log.i(TAG, "scheduleTimerAlarm called");
		
		// create a new one starting to count NOW
		
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	
    	if (prefs.getBoolean("timed_check", false))
    	{
	    	String strInterval = prefs.getString("timed_check_delay", "0");
	    	    	
			int iInterval = 0;
			try
	    	{
				iInterval = Integer.valueOf(strInterval);
	    	}
	    	catch (Exception e)
	    	{
	    	}

			if (iInterval != 0)
			{
				long fireAt = System.currentTimeMillis() + (iInterval * 1000);
		
				Intent intent = new Intent(ctx, TimedCheckAlarmReceiver.class);
		
				PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_TIMER,
						intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
				// Get the AlarmManager service
				AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, fireAt, sender);
			}
    	}
		return true;
	}
	
	/**
	 * Cancels the current alarm (if existing)
	 */
	public static void cancelTimerAlarm(Context ctx)
	{
		// check if there is an intent pending
		Intent intent = new Intent(ctx, WifiConnectedAlarmReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_TIMER,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (sender != null)
		{

			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
			am.cancel(sender);
		}
	}

}