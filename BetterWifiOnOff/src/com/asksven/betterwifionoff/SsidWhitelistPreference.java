/**
 * 
 */
package com.asksven.betterwifionoff;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.asksven.android.common.utils.StringUtils;
import com.asksven.betterwifionoff.data.Constants;
import com.asksven.betterwifionoff.utils.WifiControl;
import com.google.ads.e;

/**
 * @author sven
 *
 */
public class SsidWhitelistPreference extends MultiSelectListPreference 
{

	private static final String SEPARATOR=",";
	private Context m_context = null;
	private final static String TAG = "SsidWhitelistPreference";
	
    @TargetApi(11)
	public SsidWhitelistPreference(Context context, AttributeSet attrs)
    {
    	super(context, attrs);
        m_context = context;
        // retrieve the list of configured access points
        List<String> whitelistedSsids = new ArrayList<String>();
        
        List<String> ssids = WifiControl.getConfiguredAccessPoints(context);
        
        // retrieve the list of witelisted accesspoints
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String whitelist = sharedPrefs.getString("wifi_whitelist", "");
		
		String[] wl = whitelist.split(",");
		
		// start adding the whitelisted enstries
		for (int i = 0; i < wl.length; i++)
		{
			if ((!wl[i].equals("")) && (wl[i] != null))
			{
				String entry = StringUtils.stripLeadingAndTrailingQuotes(wl[i]).trim();
				whitelistedSsids.add(entry);
				Log.i(TAG, "Added whitelist entry: **" + entry + "**");
			}
		}

		// next add the available ssid not yet listed
        for (int i = 0; i < ssids.size(); i++)
        {
        	if (ssids.get(i) != null)
        	{
	        	String availableSSID = StringUtils.stripLeadingAndTrailingQuotes(ssids.get(i).trim());
	        	
	        	if ((!ssids.get(i).equals("")) && (!whitelistedSsids.contains(availableSSID)))
	        	{
	        		whitelistedSsids.add(availableSSID);
					Log.i(TAG, "Added configured SSID entry: **" + availableSSID + "**");
	        		
	        	}
        	}
        	else
        	{
        		Log.e(TAG, "ssids contain null object. That is not supposed to happen: " + ssids.toString());
        	}
        }
        
        CharSequence[] entries = new CharSequence[whitelistedSsids.size()];
        CharSequence[] entryValues = new CharSequence[whitelistedSsids.size()];

        for (int i = 0; i < whitelistedSsids.size(); i++)
        {
            entries[i] = whitelistedSsids.get(i);
            entryValues[i] = whitelistedSsids.get(i);
        }
        setEntries(entries);
        setEntryValues(entryValues);
        
        
    }

    public SsidWhitelistPreference(Context context)
    {
        this(context, null);
        m_context = context;
    }
    
    @TargetApi(11)
	@Override
    protected void onDialogClosed(boolean positiveResult)
    {
    	super.onDialogClosed(positiveResult);
    	
    	if (!positiveResult)
    	{
    		// dialog was canceled: do nothing
    		return;
    	}
    	
        CharSequence[] entryValues = getEntryValues();
        Set selectedValues = getValues();
        if (positiveResult && entryValues != null)
        {
            StringBuffer value = new StringBuffer();
            Iterator<String> it = selectedValues.iterator();
            while (it.hasNext())
            {
            	String val = it.next();
            	value.append(val).append(SEPARATOR);
            }
//            for ( int i=0; i<entryValues.length; i++ )
//            {
//                value.append(entryValues[i]).append(SEPARATOR);
//            }
            
            String pref = value.toString();
            if ( pref.length() > 0 )
            {
                pref = pref.substring(0, pref.length()-SEPARATOR.length());   
            }
            
			SharedPreferences prefs = m_context.getSharedPreferences(Constants.Preferences.name, Context.MODE_MULTI_PROCESS);

	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("wifi_whitelist", pref);
	        editor.commit();

        }
    }


}