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

import com.asksven.android.common.wifi.WifiManagerProxy;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * An implementation of a global store of static vars
 * @author sven
 *
 */
public class PluggedWakelock
{
	private static WakeLock screen_on_wakelock;
	private static WifiManager.WifiLock screen_on_wifilock;
	
	static final String WAKELOCK = "OPTION_WAKELOCK_WHILE_CHARGING";
	static final String WIFILOCK = "OPTION_WIFI_WHILE_CHARGING";
	static final String TAG = "BetterWifiOnOff.PluggedWakelock";
	
    public static void acquireWakelock(Context ctx)
    {
    	PowerManager powerManager = (PowerManager) ctx.getApplicationContext().getSystemService(Context.POWER_SERVICE);
    	releaseWakelock();
    	screen_on_wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);
    	screen_on_wakelock.acquire();
    	Log.d(TAG, "Wakelock " + WAKELOCK + " aquired");
    }
    
    public static void acquireWifiLock(Context ctx)
    {
    	WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    	releaseWifilock();
    	screen_on_wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFILOCK);
    	// make sure that the calls to acquire are not counted
    	screen_on_wifilock.setReferenceCounted(true);
    	screen_on_wifilock.acquire();
    	Log.d(TAG, "WifiLock " + WIFILOCK + " aquired (FULL_MODE)");
    	Log.d(TAG, "Checking if Wifilock is held:" + screen_on_wifilock.isHeld()); 
    	Log.d(TAG, "Checking if Wifilock was applied:" + WifiManagerProxy.hasWifiLock(ctx));
    }
    
    public static void acquireHighPerfWifiLock(Context ctx)
    {
    	WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    	releaseWifilock();
    	screen_on_wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, WIFILOCK);
    	screen_on_wifilock.setReferenceCounted(false);
    	screen_on_wifilock.acquire();
    	Log.d(TAG, "WifiLock " + WIFILOCK + " aquired (HIGH_PERF)");
    	Log.d(TAG, "Checking if Wifilock is held:" + screen_on_wifilock.isHeld()); 
    	Log.d(TAG, "Checking if Wifilock was applied:" + WifiManagerProxy.hasWifiLock(ctx));

    }

    public static void releaseWakelock()
    {
    	Log.d(TAG, "releaseWakelock called");
    	if ((screen_on_wakelock != null) && (screen_on_wakelock.isHeld()))
    	{
    		screen_on_wakelock.release();
    		screen_on_wakelock = null;
    		Log.d(TAG, "Wakelock " + WAKELOCK + " released");
    	}
    }

    public static void releaseWifilock()
    {
    	Log.d(TAG, "releaseWifilock called");
    	if ((screen_on_wifilock != null) && (screen_on_wifilock.isHeld()))
    	{
    		screen_on_wifilock.release();
    		Log.d(TAG, "Wifilock " + WIFILOCK + " released");
    	}
    }
    
    public static boolean holdsWifiLock()
    {
    	Log.d(TAG, "holdsWifilock called");
    	if (screen_on_wifilock != null)
    	{
    		return (screen_on_wifilock.isHeld());
    	}
    	return (false);
    }

}
