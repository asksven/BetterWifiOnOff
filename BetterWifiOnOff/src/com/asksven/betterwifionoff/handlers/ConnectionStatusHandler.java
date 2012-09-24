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


import com.asksven.android.common.wifi.WifiManagerProxy;
import com.asksven.betterwifionoff.PluggedWakelock;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.EventWatcherServiceBinder;
import com.asksven.betterwifionoff.services.SetWifiStateService;
import com.asksven.betterwifionoff.utils.WifiControl;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * General broadcast handler: handles event as registered on Manifest
 * @author sven
 *
 */
public class ConnectionStatusHandler extends BroadcastReceiver
{	
	private static final String TAG = "BetterWifiOnOff.ConnectionStatusHandler";
	
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
			Log.d(TAG, "WifiManager.NETWORK_STATE_CHANGED_ACTION received");
//			Log.d(TAG, "Wifi status: " + WifiControl.isWifiConnected(context));
//			Log.d(TAG, "Own Wifilock status: " + PluggedWakelock.holdsWifiLock());
//			Log.d(TAG, "Android Wifilock status: " + WifiManagerProxy.hasWifiLock(context));
		}
		if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
		{
			Log.d(TAG, "WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION received");
//			Log.d(TAG, "Wifi status: " + WifiControl.isWifiConnected(context));
//			Log.d(TAG, "Own Wifilock status: " + PluggedWakelock.holdsWifiLock());
//			Log.d(TAG, "Android Wifilock status: " + WifiManagerProxy.hasWifiLock(context));
		}
		if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
		{
			Log.d(TAG, "WifiManager.WIFI_STATE_CHANGED_ACTION received");
//			Log.d(TAG, "Wifi status: " + WifiControl.isWifiConnected(context));
//			Log.d(TAG, "Own Wifilock status: " + PluggedWakelock.holdsWifiLock());
//			Log.d(TAG, "Android Wifilock status: " + WifiManagerProxy.hasWifiLock(context));
		}
		if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
		{
			Log.d(TAG, "WifiManager.SUPPLICANT_STATE_CHANGED_ACTION received");
//			Log.d(TAG, "Wifi status: " + WifiControl.isWifiConnected(context));
//			Log.d(TAG, "Own Wifilock status: " + PluggedWakelock.holdsWifiLock());
//			Log.d(TAG, "Android Wifilock status: " + WifiManagerProxy.hasWifiLock(context));
		}

	}
}
