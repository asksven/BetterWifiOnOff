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
public class WifiLock
{
	private static WifiManager.WifiLock wifilock;
	
	static final String WIFILOCK = "OPTION_PERM_WIFILOCK";
	static final String TAG = "BetterWifiOnOff.Wakelock";
	
    
    public static void acquireWifiLock(Context ctx)
    {
    	WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    	releaseWifilock();
    	wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFILOCK);
    	// make sure that the calls to acquire are not counted
    	wifilock.setReferenceCounted(true);
    	wifilock.acquire();
    	Log.d(TAG, "WifiLock " + WIFILOCK + " aquired (FULL_MODE)");
    	Log.d(TAG, "Checking if Wifilock is held:" + wifilock.isHeld()); 
    	Log.d(TAG, "Checking if Wifilock was applied:" + WifiManagerProxy.hasWifiLock(ctx));
    }
    
    public static void acquireHighPerfWifiLock(Context ctx)
    {
    	WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    	releaseWifilock();
    	wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, WIFILOCK);
    	wifilock.setReferenceCounted(false);
    	wifilock.acquire();
    	Log.d(TAG, "WifiLock " + WIFILOCK + " aquired (HIGH_PERF)");
    	Log.d(TAG, "Checking if Wifilock is held:" + wifilock.isHeld()); 
    	Log.d(TAG, "Checking if Wifilock was applied:" + WifiManagerProxy.hasWifiLock(ctx));

    }


    public static void releaseWifilock()
    {
    	Log.d(TAG, "releaseWifilock called");
    	if ((wifilock != null) && (wifilock.isHeld()))
    	{
    		wifilock.release();
    		Log.d(TAG, "Wifilock " + WIFILOCK + " released");
    	}
    }
    
    public static boolean holdsWifiLock()
    {
    	Log.d(TAG, "holdsWifilock called");
    	if (wifilock != null)
    	{
    		return (wifilock.isHeld());
    	}
    	return (false);
    }

}
