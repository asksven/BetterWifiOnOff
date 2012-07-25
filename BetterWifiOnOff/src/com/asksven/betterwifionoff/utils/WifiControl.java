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

package com.asksven.betterwifionoff.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @author sven
 *
 */
public class WifiControl
{
	private static String TAG = "WifiControl";
	/**
	 * Returns whether wifi is on or not
	 * @param ctx a Context
	 * @return true if wifi is on
	 */
	public static final boolean isWifiOn(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		
		return wifiManager.isWifiEnabled();
	}
	
	/**
	 * Turns Wifi on or off
	 * @param ctx a Context
	 * @param state on=true or off=false
	 */
	public static final void setWifi(Context ctx, boolean state)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		
		if ( (state && !isWifiOn(ctx)) || (!state && isWifiOn(ctx)) )
		{
			wifiManager.setWifiEnabled(state);
		}
	}
	
	/**
	 * Returns true if a Wifi connection is established 
	 * @param ctx a Context
	 * @return true if a Wifi connection is established
	 */
	public static final boolean isWifiConnected(Context ctx)
	{
		ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
//		return (networkInfo != null && networkInfo.isConnected());
	
		if (networkInfo != null && networkInfo.isConnected())
		{
			Log.d(TAG, "A connection was detected, testing if an IP was assigned");
			WifiManager wifi;
			wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifi.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			
			if (ipAddress == 0)
			{
				Log.d(TAG, "No IP address assigned: " + ipAddress + ". Wifi is not connected");
				return false;
			}
			else
			{
				Log.d(TAG, "IP address assigned: " + ipAddress + ". Wifi is connected");
				return true;
			}
		}
		else
		{
			Log.d(TAG, "No active connection detected");
			return false;
		}
	}
	
	/**
	 * Return true if the connected access point is in the given whitelist
	 * @param ctx a Context
	 * @param whiteList the white list as separated string
	 * @return true if the currently connected AP is whitelisted
	 */
	public static final boolean isWhitelistedWifiConnected(Context ctx, String whiteList)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		String ssid = wifiManager.getConnectionInfo().getSSID();
		return (whiteList.indexOf(ssid) != -1);
	}
	
	
	/** 
	 * Returns the list of access points that were added to the Wifi configuration
	 * @param ctx a Context
	 * @return the list as List<String>
	 */
	public static final List<String> getConfiguredAccessPoints(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

		ArrayList<String> myList = new ArrayList<String>();
		
		List<WifiConfiguration> myConfiguredAccessPoints = wifiManager.getConfiguredNetworks();
		for (int i = 0; i < myConfiguredAccessPoints.size(); i++)
		{
			myList.add(myConfiguredAccessPoints.get(i).SSID);
		}
		
		return myList;
	}
	
	/**
	 * Returns the list of access points in range, disregarded if they can be connected or not
	 * @param ctx a Context
	 * @return the list as List<String>
	 */
	public static final List<String> getAvailableAccessPoints(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

		ArrayList<String> myList = new ArrayList<String>();
		
		List<ScanResult> myConfiguredAccessPoints = wifiManager.getScanResults();
		for (int i = 0; i < myConfiguredAccessPoints.size(); i++)
		{
			myList.add(myConfiguredAccessPoints.get(i).SSID);
		}
		
		return myList;
	}

	

}
