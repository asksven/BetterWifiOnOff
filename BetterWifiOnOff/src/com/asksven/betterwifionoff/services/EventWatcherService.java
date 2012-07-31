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

import com.asksven.betterwifionoff.MainActivity;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.handlers.ScreenEventHandler;
import com.asksven.betterwifionoff.utils.ChargerUtil;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sven
 * 
 */
public class EventWatcherService extends Service implements
		OnSharedPreferenceChangeListener
{

	static final String TAG = "EventWatcherService";
	public static String SERVICE_NAME = "com.asksven.betterwifionoff.services.EventWatcherService";
	public static final int NOTFICATION_ID = 1002;
	static final String WAKELOCK = "OPTION_WAKELOCK_WHILE_CHARGING";
	private WakeLock m_wakelock = null;
	
	private static EventWatcherService m_instance = null;

	private EventLogger m_events;
	BroadcastReceiver m_receiver = null;

	Notification m_stickyNotification = null;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder
	{
		public EventWatcherService getService()
		{
			Log.i(TAG, "getService called");
			return EventWatcherService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		// register receiver that handles screen on and screen off logic
		IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		m_receiver = new ScreenEventHandler();
		registerReceiver(m_receiver, filter);

		m_events = new EventLogger(this);
		m_instance = this;
		
        // Set up a listener whenever a key changes
    	PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
	}

	static EventWatcherService getInstance()
	{
		return m_instance;
	}

	public EventLogger getEventLogger()
	{
		return m_events;
	}

	public void clearEvents()
	{
		m_events.clear();
	}

	/**
	 * Called when service is started
	 */
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(getClass().getSimpleName(), "Received start id " + startId + ": "
				+ intent);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean bForegroundService = prefs.getBoolean("foreground_service", false);
		if (bForegroundService)
		{
			setupAsForeground("Foreground Service Started");
		}

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return Service.START_STICKY;
	}

	void setupAsForeground(String strNotification)
	{
		m_stickyNotification = new Notification(R.drawable.ic_launcher,
				"Foreground Service", System.currentTimeMillis());
		Intent i = new Intent(this, MainActivity.class);

		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		m_stickyNotification.setLatestEventInfo(this, "BetterWifiOnOff",
				strNotification, pi);
		m_stickyNotification.flags |= Notification.FLAG_NO_CLEAR;

		if (isServiceRunning(this))
		{
			Log.d(TAG,
					"setupAsForeground was called to update the notification");
		} else
		{
			Log.d(TAG, "setupAsForeground was called and started the service");
		}

		startForeground(12245, m_stickyNotification);

	}

    /**
     * Called when Service is terminated
     */
    public void onDestroy()
    {        
     
		// hack: there is no way to test whether a receiver is registered so we have to try and ignore the exception
		try
		{
			unregisterReceiver(m_receiver);
		}
		catch (IllegalArgumentException e)
		{
			// do nothing
		}
		finally
		{
			releaseWakelock();
		}
		
        // Unregister the listener whenever a key changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

    }
    
    public void aquireWakelock()
    {
    	PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
    	releaseWakelock();
    	m_wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);
    	m_wakelock.acquire();
    	Log.d(TAG, "Wakelock " + WAKELOCK + " aquired");
    }
    
    public void releaseWakelock()
    {
    	if ((m_wakelock != null) && (m_wakelock.isHeld()))
    	{
    		m_wakelock.release();
    		Log.d(TAG, "Wakelock " + WAKELOCK + " released");
    	}
    }
	
	public static boolean isServiceRunning(Context context)
	{
		ActivityManager manager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE))
		{
			if (EventWatcherService.SERVICE_NAME.equals(service.service
					.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (key.equals("wakelock_while_power_plugged"))
		{
			if (prefs.getBoolean("wakelock_while_power_plugged", false))
			{
				// if powered apply wakelock immediately
				if (ChargerUtil.isConnected(this))
				{
					this.aquireWakelock();
				}
			}
			else
			{
				this.releaseWakelock();
			}
		}

		
		if (key.equals("foreground_service"))
		{
			// stop and start the service, starting it will lead to prefs being
			// read

			Intent i = new Intent();
			i.setClass( this, EventWatcherService.class );

			if (isServiceRunning(this))
			{
				try
				{
					if (!stopService(i))
					{
						stopForeground(true);

					}
					else
					{
						Log.i(TAG, "Service was stopped");
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, "An error occured while trying to stop teh service: " + e.getMessage());
				}
			}
			startService(i);
		}

	}

}