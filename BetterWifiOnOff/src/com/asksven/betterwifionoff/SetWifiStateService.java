/*
 * Copyright (C) 2011-12 asksven
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

import com.asksven.betterwifionoff.utils.WifiControl;

import android.app.Service;
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
	

	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean state = intent.getBooleanExtra(this.EXTRA_STATE, false);
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
}