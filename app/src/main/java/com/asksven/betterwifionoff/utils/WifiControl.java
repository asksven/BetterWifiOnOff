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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asksven.android.common.utils.DateUtils;
import com.asksven.android.common.utils.StringUtils;
import com.asksven.android.common.wifi.WifiManagerProxy;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * @author sven
 *
 */
public class WifiControl
{
	private static String TAG = "BetterWifiOnOff.WifiControl";
	private static boolean m_WifiCaged = false;
	private static boolean m_WifiCageTransactional = false;
	
	
//	private static long m_snapshotTotalBytes 		= 0;
//	private static long m_snapshotTotalTimestamp 	= 0;
	
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
			if (state)
			{
				Log.d(TAG, "Turning Wifi on");
			}
			else
			{
				Log.d(TAG, "Turning Wifi off");
			}
			
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
				Log.d(TAG, "Wifilock status: " + WifiManagerProxy.hasWifiLock(ctx));
				Log.d(TAG, "SSID:" + wifiInfo.getSSID());
				Log.d(TAG, "Supplicant state:" + wifiInfo.getSupplicantState());
				
				return false;
			}
			else
			{
				Log.d(TAG, "IP address assigned: " + ipAddress + ". Wifi is connected");
				Log.d(TAG, "Wifilock status: " + WifiManagerProxy.hasWifiLock(ctx));
				Log.d(TAG, "SSID:" + wifiInfo.getSSID());
				Log.d(TAG, "Supplicant state:" + wifiInfo.getSupplicantState());
				return true;
			}
		}
		else
		{
			Log.d(TAG, "No active connection detected");
			return false;
		}
	}

//	/**
//	 * Returns true if a Wifi connection is established but google DNS could not be reached 
//	 * @param ctx a Context
//	 * @return true if a Wifi connection is established but caged
//	 */
//	public static final boolean isWifiCaged(Context ctx)
//	{
//		try
//		{
//			if (!isWifiConnected(ctx))
//			{
//				Log.i(TAG, "isWifiCaged: no connection active, aborting");
//	
//				return false;
//			}
//			
//			ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
//	
//			final String googleDns = "8.8.8.8";
//			final String bingSearch = "131.253.13.32";
//			int ip = ipToInt(googleDns);  
//	
//			boolean ret = (connMgr.requestRouteToHost(ConnectivityManager.TYPE_WIFI, ip));
//			Log.i(TAG, "isWifiCaged: requestRouteToHost returned " + ret + " for IP " + googleDns);
//			
//			if (!ret)
//			{
//				// retry with bing.com
//				ip = ipToInt(bingSearch);  
//				
//				ret = (connMgr.requestRouteToHost(ConnectivityManager.TYPE_WIFI, ip));
//				Log.i(TAG, "isWifiCaged: requestRouteToHost returned " + ret + " for IP " + bingSearch);
//
//			}
//			return (!ret);
//		}
//		catch (Exception e)
//		{
//			Log.e(TAG, "isWifiCaged: An exception occured: " + e.fillInStackTrace());
//		}
//		return false;
//	}

	/**
	 * Returns true if a Wifi connection is established but google DNS could not be reached 
	 * @param ctx a Context
	 * @return true if a Wifi connection is established but caged
	 */
	public static boolean isWifiCaged()
	{
		Log.i(TAG, "Check for cage returned " + m_WifiCaged + ". Thread status was " +m_WifiCageTransactional);
		return m_WifiCaged;
	}
	/**
	 * Start a thread to check for caged Wifi. Restult is retrieved by isWifiCaged
	 * @param ctx
	 */
	public static final void doCageCheck(Context ctx)
	{
		//Body of your click handler
		m_WifiCaged = false;
		m_WifiCageTransactional = true;
		Thread trd = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					HttpURLConnection urlConnection = null;
					try
					{
					    URL url = new URL("http://clients3.google.com/generate_204");
					    urlConnection = (HttpURLConnection) url.openConnection();
					    urlConnection.setInstanceFollowRedirects(false);
					    urlConnection.setConnectTimeout(5000);
					    urlConnection.setReadTimeout(5000);
					    urlConnection.setUseCaches(false);
					    urlConnection.getInputStream();
						Log.i(TAG, "Caged connection check retuned: " + urlConnection.getResponseCode());
					
					    m_WifiCaged = urlConnection.getResponseCode() != 204;
					}
					catch (IOException e)
					{
					    Log.e(TAG, "Walled garden check - probably a cage: exception " + e);
					    m_WifiCaged = true;
					}
					finally
					{
					    if (urlConnection != null)
					    {
					    	urlConnection.disconnect();
					    }
					}
					m_WifiCageTransactional = false;
				}
				
			});
		trd.start();
	}
	
	public static int ipToInt(String addr)
	{
		String[] addrArray = addr.split("\\.");

		int num = 0;
		for (int i = 0; i < addrArray.length; i++)
		{
			int power = 3 - i;

			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
		}
		return num;
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
		String ssid = StringUtils.stripLeadingAndTrailingQuotes(wifiManager.getConnectionInfo().getSSID());
		Log.i(TAG, "Whitelist check: ssid: '" + ssid + "', whitelist: '" + whiteList + "', result: " + (whiteList.indexOf(ssid) != -1));
		List<String> whitelistedList = Arrays.asList(whiteList.split(","));
		return ( (whitelistedList.contains(ssid)) && (!ssid.equals("")) );
//		return ((whiteList.indexOf(ssid) != -1) && (!ssid.equals("")));
	}
	

	/**
	 * Return the ssid currently connected to
	 * @param ctx a Context
	 * @return the ssid of the connected AP
	 */
	public static final String connectedSsid(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		String ssid = StringUtils.stripLeadingAndTrailingQuotes(wifiManager.getConnectionInfo().getSSID());
		return ssid;
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
		if (myConfiguredAccessPoints != null)
		{
			for (int i = 0; i < myConfiguredAccessPoints.size(); i++)
			{
				myList.add(myConfiguredAccessPoints.get(i).SSID);
			}
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
		if (myConfiguredAccessPoints != null)
		{
			for (int i = 0; i < myConfiguredAccessPoints.size(); i++)
			{
				myList.add(myConfiguredAccessPoints.get(i).SSID);
			}
		}
		return myList;
	}


	public static String connectToBestNetwork(Context ctx, String whitelist)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

		List<ScanResult> myAvailableAccessPoints = wifiManager.getScanResults();
		List<WifiConfiguration> myConfiguredAccessPoints = wifiManager.getConfiguredNetworks();

		if (myAvailableAccessPoints == null || myConfiguredAccessPoints == null)
		{
			return null;
		}
		
		List<ScanResult> myConfiguredAvailableAccessPoints = new ArrayList<ScanResult>();
		// the best network is the avalable network from the configured ones with the stronges signal
		for (int i=0; i < myAvailableAccessPoints.size(); i++)
		{
			String ssid = myAvailableAccessPoints.get(i).SSID;
			boolean found = false;
			for (int j=0; j < myConfiguredAccessPoints.size(); j++)
			{
				if (StringUtils.stripLeadingAndTrailingQuotes(myConfiguredAccessPoints.get(j).SSID).equals(ssid))
				{
					if (whitelist != null)
					{
						if (whitelist.indexOf(ssid) != -1)
						{
							found = true;
						}
					}
					else
					{
						found = true;
					}
				}
				if (found)
				{
					myConfiguredAvailableAccessPoints.add(myAvailableAccessPoints.get(i));
				}
			}
		}
		
		// myConfiguredAvailableAccessPoints contains the whitelisted available configured access points
		if (myConfiguredAvailableAccessPoints.size() != 0)
		{
			ScanResult bestAP = getBestAccessPoint(myConfiguredAvailableAccessPoints);
			if (bestAP == null)
			{
				Log.e(TAG, "Best AP could not be deternimed");
				return null;
			}
			else
			{
				Log.i(TAG, "Best AP: " + bestAP.SSID);
			}
			// now find the id of the configured AP for that SSID
			int id = -1;
			String ssid = bestAP.SSID;
			Log.i(TAG, "Searching net id for best AP " + ssid);
			for (int j=0; j < myConfiguredAccessPoints.size(); j++)
			{
				if (StringUtils.stripLeadingAndTrailingQuotes(myConfiguredAccessPoints.get(j).SSID).equals(ssid))
				{
					id = myConfiguredAccessPoints.get(j).networkId;
					Log.i(TAG, "Found id " + id);
				}
			}
			
			boolean done = false;
			if (id != -1)
			{
				done = wifiManager.enableNetwork(id, true);
				if (!done)
				{
					Log.i(TAG, "Unable to connect to AP " + ssid);
					return null;
				}
				else
				{
					Log.i(TAG, "Connected to " + ssid);
					return ssid;
				}
			}
			else
			{
				Log.e(TAG, "Best AP could not be determined");
				return null;
			}
		}
		
		return null;
	}
	
	static ScanResult getBestAccessPoint(List<ScanResult> sResults)
	{
		ScanResult bestSignal = null;
		for (ScanResult result : sResults)
		{
			if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
				bestSignal = result;
		}

		String message = String.format("%s networks found. %s is the strongest. %s is the bsid", sResults.size(),
				bestSignal.SSID, bestSignal.BSSID);

		Log.d("sResult", message);
		return bestSignal;
	}
	/**
	 * Returns the speed of the current Wifi connection
	 * @param ctx a Context
	 * @return the speed in Mbps
	 */
	public static final int getConnectionSpeed(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		int speed = info.getLinkSpeed();
		Log.d(TAG, "Connection speed: " + speed);
		
		return speed;
	}
	
	// alternative: parse /proc/net/xt_qtaguid/iface_stat_all (entry wlan0) at two times and determine the rate 
	
	@TargetApi(8)
	public static void snapshot(Context ctx)
	{
		long snapshotTotalTimestamp = SystemClock.elapsedRealtime();
		long snapshotTotalBytes = 0;
		
		if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED)
		{
			snapshotTotalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
		}
		else
		{ 
			snapshotTotalBytes = 0;
		}	
		
		Log.i(TAG, "Snapshot at " + DateUtils.now() + ": " + snapshotTotalBytes + " Bytes");

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("snapshot_time", snapshotTotalTimestamp);
        editor.putLong("snapshot_bytes", snapshotTotalBytes);
        editor.commit();

	}

	@TargetApi(8)
	public static boolean isTransferring(Context ctx)
	{
		boolean ret = false;
		
		long totalBytes = 0;
		long totalTimestamp = SystemClock.elapsedRealtime();
		
		if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED)
		{
			totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
		}
		Log.i(TAG, "Sample at " + DateUtils.now() + ": " + totalBytes + " Bytes");

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		long snapshotTotalTimestamp	= sharedPrefs.getLong("snapshot_time", 0);
		long snapshotTotalBytes		= sharedPrefs.getLong("snapshot_bytes", 0);


		long bytes = totalBytes - snapshotTotalBytes;
		long time = (totalTimestamp - snapshotTotalTimestamp) / 1000;
		long throughput = bytes / time;
		Log.i(TAG, "Throughput: " + throughput + " Bytes/s" + "(" + bytes + "/" + time + ")");
		
		if (throughput >= 1024 )
		{
			Log.i(TAG, "Transferring (>= 1KB/s)");
			ret = true;
		}
		else
		{
			Log.i(TAG, "Not transferring (< 1KB/s)");
		}
	
		return ret;
	}
	
	public static boolean isWifiTethering(Context context)
	{
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		boolean ret = false;
		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		for(Method method: wmMethods)
		{
		
			if (method.getName().equals("isWifiApEnabled"))
			{
				try
				{
					ret = (Boolean) method.invoke(wifi);
				}
				catch (IllegalArgumentException e)
				{
					e.printStackTrace();
				} catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	/**
	* Gets the state of Airplane Mode.
	* 
	* @param context
	* @return true if enabled.
	*/
	public static boolean isAirplaneModeOn(Context context)
	{

	   return Settings.System.getInt(context.getContentResolver(),
	           Settings.System.AIRPLANE_MODE_ON, 0) != 0;

	}
}
