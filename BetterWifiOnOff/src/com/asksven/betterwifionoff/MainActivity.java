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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.asksven.betterwifionoff.ReadmeActivity;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.EventWatcherServiceBinder;
import com.asksven.betterwifionoff.utils.Configuration;
import com.asksven.betterwifionoff.utils.Logger;
import com.google.ads.*;

public class MainActivity extends ListActivity 

{
	/**
	 * The logging TAG
	 */
	private static final String TAG = "MainActivity";

	public static final String MARKET_LINK ="market://details?id=com.asksven.betterwifionoff";
    public static final String TWITTER_LINK ="https://twitter.com/#!/asksven";
    
	private EventAdapter m_listViewAdapter;
    CheckBox m_checkboxDisabled;
    CheckBox m_checkboxWifilock;
    CheckBox m_checkboxHighPerfWifilock;
    OnClickListener m_checkBoxListener;

	
	/**
	 * a progess dialog to be used for long running tasks
	 */
	ProgressDialog m_progressDialog;
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
	
	    m_checkboxDisabled 			= (CheckBox) findViewById(R.id.checkBoxDisable);
	    m_checkboxWifilock 			= (CheckBox) findViewById(R.id.checkBoxWifilock);
	    m_checkboxHighPerfWifilock 	= (CheckBox) findViewById(R.id.checkBoxHighPerfWifilock);
	    
	    m_checkBoxListener = new OnClickListener()
	    {
	    	 @Override
	    	 public void onClick(View v)
	    	 {
	    			switch (v.getId())
	    			{
	    				case R.id.checkBoxDisable:
	    				{
	    					break;
	    				}
	    				case R.id.checkBoxWifilock:
	    				{
	    					if (m_checkboxWifilock.isChecked())
	    					{
	    						WifiLock.acquireWifiLock(MainActivity.this);
	    					}
	    					else
	    					{
	    						WifiLock.releaseWifilock();
	    					}
	    					break;
	    				}
	    				case R.id.checkBoxHighPerfWifilock:
	    				{
	    					if (m_checkboxHighPerfWifilock.isChecked())
	    					{
	    						WifiLock.acquireHighPerfWifiLock(MainActivity.this);
	    					}
	    					else
	    					{
	    						WifiLock.releaseWifilock();
	    					}
	    					break;
	    				}
	    			}
	    			savePrefs();

	       	 }
	    };

	    m_checkboxDisabled.setOnClickListener(m_checkBoxListener);
	    m_checkboxWifilock.setOnClickListener(m_checkBoxListener);
	    m_checkboxHighPerfWifilock.setOnClickListener(m_checkBoxListener);
	    
	    readPrefs();
	    
		// detect free/full version and enable/disable ads
		if (!Configuration.isFullVersion(this))
		{
			AdView adView = (AdView)this.findViewById(R.id.adView);
		    adView.loadAd(new AdRequest());
		}
		

	    // Initiate a generic request to load it with an ad
        // retrieve the version name and display it
		
		
        try
        {
        	PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        	TextView versionTextView = (TextView) findViewById(R.id.textViewVersion);
        	TextView nameTextView = (TextView) findViewById(R.id.textViewName);
        	TextView hintTextView = (TextView) findViewById(R.id.textViewHint);
        	
        	if (Configuration.isFullVersion(this))
    		{
        		nameTextView.setText("Support the devs Version");
        		hintTextView.setText("");
        		Log.i(TAG, "full version was detected");
    		}
        	else
        	{
        		nameTextView.setText("Free Version");
        		hintTextView.setText(getString(R.string.full_version_available));
        		Log.i(TAG, "free version was detected");
        	}
        	
        	versionTextView.setText(pinfo.versionName);
        }
        catch (Exception e)
        {
        	Logger.e(TAG, "An error occured retrieveing the version info: " + e.getMessage());
        }
        
        	

        // Show release notes when first starting a new version
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String strLastRelease	= sharedPrefs.getString("last_release", "0");
		String strCurrentRelease = "";

		try
		{
			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
	    	strCurrentRelease = Integer.toString(pinfo.versionCode);
		}
		catch (Exception e)
		{
			// nop strCurrentRelease is set to ""
		}

		if (!strLastRelease.equals(strCurrentRelease))
    	{
    		// show the readme
	    	Intent intentReleaseNotes = new Intent(this, ReadmeActivity.class);
	    	intentReleaseNotes.putExtra("filename", "readme.html");
	        this.startActivity(intentReleaseNotes);
	        
	        // save the current release to properties so that the dialog won't be shown till next version
	        SharedPreferences.Editor editor = sharedPrefs.edit();
	        editor.putString("last_release", strCurrentRelease);
	        editor.commit();
    	}

        // start the service
    	startService(new Intent(this, EventWatcherService.class));
    	
    	// add listview adapter
    	this.setListViewAdapter();

  	}

    /**
     * Save state, the application is going to get moved out of memory
     * @see http://stackoverflow.com/questions/151777/how-do-i-save-an-android-applications-state
     */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	super.onSaveInstanceState(savedInstanceState);
        
//    	savedInstanceState.putSerializable("logged_on", m_bLoggedOn); 
    }

	/* Request updates at startup */
	@Override
	protected void onResume()
	{
		super.onResume();

		// add listview adapter
    	this.setListViewAdapter();
    	
    	if (m_listViewAdapter != null)
    	{
    		m_listViewAdapter.notifyDataSetChanged();
    	}
		// update the status
		this.updateStatus();
	}


	/** 
     * Add menu items
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu)
    {  
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    @Override
	public boolean onPrepareOptionsMenu(Menu menu)
    {
    	
    	return true;
    }
    
    /** 
     * Define menu action
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {  
        switch (item.getItemId())
        {
	        case R.id.clear_events:
				EventWatcherService myService = EventWatcherServiceBinder.getInstance(this).getService();
				if (myService != null)
				{
					myService.clearEvents();
			    	if (m_listViewAdapter != null)
			    	{
			    		m_listViewAdapter.notifyDataSetChanged();
			    	}

				}
	        	break;
	        case R.id.refresh_events:
		    	if (m_listViewAdapter != null)
		    	{
		    		m_listViewAdapter.notifyDataSetChanged();
		    	}
	        	break;	
	
	        case R.id.preferences:  
	        	Intent intentPrefs = new Intent(this, PreferencesActivity.class);
	            this.startActivity(intentPrefs);
	        	break;	
	        case R.id.release_notes:
            	// Release notes
            	Intent intentReleaseNotes = new Intent(this, ReadmeActivity.class);
            	intentReleaseNotes.putExtra("filename", "readme.html");
                this.startActivity(intentReleaseNotes);
            	break;	
        }
        
        return true;
    }

    private void updateStatus()
    {
	    // Set the wifi state
    }
    
	private void setListViewAdapter()
	{
		// make sure we only instanciate when the reference does not exist
		if (m_listViewAdapter == null)
		{
			EventWatcherService myService = EventWatcherServiceBinder.getInstance(this).getService();
			if (myService != null)
			{
				m_listViewAdapter = new EventAdapter(this, myService.getEventLogger());
			}
		
			setListAdapter(m_listViewAdapter);
		}
	}
	
	/**
	 * save the preferences 
	 */
	void savePrefs()		
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("disable_control", m_checkboxDisabled.isChecked());
        editor.putBoolean("wfilock", m_checkboxWifilock.isChecked());
        editor.putBoolean("highperf_wifilock", m_checkboxHighPerfWifilock.isChecked());
        editor.commit();
	}

	void readPrefs()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        m_checkboxDisabled.setChecked(prefs.getBoolean("disable_control", false));
        m_checkboxWifilock.setChecked(prefs.getBoolean("wifilock", false));
        m_checkboxHighPerfWifilock.setChecked(prefs.getBoolean("highperf_wifilock", false));
	}
	

//    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
//    {
//    	if (key.equals("disable_control"))
//    	{
//    		// if this value was just turned on make sure "wen_screen_off" gets unchecked
//    		if (prefs.getBoolean("disable_control", false))
//    	
//    	        SharedPreferences.Editor editor = prefs.edit();
//    	        editor.putBoolean("disable_control", true);
//    	        editor.commit();
//    		}
//    		else
//    		{
//    	        SharedPreferences.Editor editor = prefs.edit();
//    	        editor.putBoolean("disable_control", false);
//    	        editor.commit();
//
//    		}
//    	}
//    	
//    	if (key.equals("wifilock"))
//    	{
//    		// if this value was just turned on make sure "wen_screen_off" gets unchecked
//    		if (prefs.getBoolean("wifilock", false))
//    		{
//    		}
//    		else
//    		{
//    		}
//    	}
//    	if (key.equals("highperf_wifilock"))
//    	{
//    		// if this value was just turned on make sure "wen_screen_off" gets unchecked
//    		if (prefs.getBoolean("highperf_wifilock", false))
//    		{
//    		}
//    		else
//    		{	
//    		}
//    	}
//    }
}