/*
 * Copyright (C) 2011 asksven
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
package com.asksven.betterwifionoff.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asksven.betterwifionoff.data.AppWhitelistDBHelper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Helper to list running apps
 * 
 * @author sven
 * 
 */
public class AppUtil
{
	
	static String TAG = "AppUtil";
	/**
	 * Returns true if the charger is currently connected
	 * 
	 * @param context
	 * @return true if the charger is connected
	 */
	public static String isWhitelistedAppRunning(Context context)
	{
		String ret = "";
		AppWhitelistDBHelper myDb = new AppWhitelistDBHelper(context);
		HashMap<String, Integer> whitelist = myDb.getWitelist();
		myDb.close();
		
		ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
		
		if (!whitelist.isEmpty())
		{
			Log.i(TAG, "Testing running apps against whitelist: " + whitelist.keySet().toString());
			
			for (int i = 0; i < procInfos.size(); i++)
			{
				String processName = procInfos.get(i).processName;
				int state = procInfos.get(i).importance;
				// check for important states (@see http://developer.android.com/reference/android/app/ActivityManager.RunningAppProcessInfo.html)
				// we consider a process active if it's importance is FOREGROUND, PERCEPTIBLE, VISIBLE
				if ((state == RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
						|| (state == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE)
						|| (state == RunningAppProcessInfo.IMPORTANCE_VISIBLE))
				{
					if (whitelist.containsKey(processName))
					{
						Log.i(TAG, "Found " + processName + ", " + state + " in whitelist");
	
						ret = processName;
						break;
					}
					else
					{
						Log.i(TAG, processName + ", " + state + " is not in whitelist");
	
					}
				}
				else
				{
					Log.i(TAG, "State " + state + " is not considered active");
				}
			}
		}
		else
		{
			Log.i(TAG, "Whitelist is empty: no whitelisted app can run");			
		}
		return ret;
	}
}
