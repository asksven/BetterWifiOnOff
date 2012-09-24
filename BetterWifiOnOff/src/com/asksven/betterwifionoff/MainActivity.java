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

import java.io.File;
import java.util.Map;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.asksven.andoid.common.contrib.Util;
import com.asksven.android.common.utils.DataStorage;
import com.asksven.android.common.utils.DateUtils;
import com.asksven.betterwifionoff.ReadmeActivity;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.services.EventWatcherServiceBinder;
import com.asksven.betterwifionoff.utils.Configuration;
import com.google.ads.*;

public class MainActivity extends ListActivity 

{
	/**
	 * The logging TAG
	 */
	private static final String TAG = "MainActivity";

	public static final String MARKET_LINK ="market://details?id=com.asksven.betterwifionoff";
    public static final String TWITTER_LINK ="https://twitter.com/#!/asksven";

    public static final String BROADCAST_ACTION = "com.asksven.betterwifionoff.displayevent";

    
	private EventAdapter m_listViewAdapter;
    CheckBox m_checkboxDisabled;
    CheckBox m_checkboxWifilock;
    CheckBox m_checkboxHighPerfWifilock;
    OnClickListener m_checkBoxListener;
    private Intent broadcastIntent;
    
    

	
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

        		nameTextView.setText(getString(R.string.support_version));
        		hintTextView.setText("");
        		Log.i(TAG, "full version was detected");
    		}
        	else
        	{
        		nameTextView.setText(R.string.free_version);
        		hintTextView.setText(getString(R.string.full_version_available));
        		Log.i(TAG, "free version was detected");
        	}
        	
        	versionTextView.setText(pinfo.versionName);
        }
        catch (Exception e)
        {
        	Log.e(TAG, "An error occured retrieveing the version info: " + e.getMessage());
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
            case R.id.logcat:
            	// Dump to File
            	new WriteLogcatFile().execute("");
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
		
			
		}
		setListAdapter(m_listViewAdapter);
	}
	
	/**
	 * save the preferences 
	 */
	void savePrefs()		
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		boolean disabled 	= m_checkboxDisabled.isChecked();
		boolean wifilock 	= m_checkboxWifilock.isChecked();
		boolean hpWifilock = m_checkboxHighPerfWifilock.isChecked();
        editor.putBoolean("disable_control", disabled);
        editor.putBoolean("wifilock", wifilock);
        editor.putBoolean("highperf_wifilock", hpWifilock);
        editor.commit();
	}

	void readPrefs()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean disabled 	= prefs.getBoolean("disable_control", false);
		boolean wifilock 	= prefs.getBoolean("wifilock", false);
		boolean hpWifilock = prefs.getBoolean("highperf_wifilock", false);

        m_checkboxDisabled.setChecked(disabled);
        m_checkboxWifilock.setChecked(wifilock);
        m_checkboxHighPerfWifilock.setChecked(hpWifilock);
	}
	
	private class WriteLogcatFile extends AsyncTask
	{
		@Override
	    protected Object doInBackground(Object... params)
	    {
			MainActivity.this.writeLoggingInfoToFile(MainActivity.this);
	    	return true;
	    }

		@Override
		protected void onPostExecute(Object o)
	    {
			super.onPostExecute(o);
	        // update hourglass
	    }
	 }
	
	public void writeLoggingInfoToFile(Context context)
	{
		if (!DataStorage.isExternalStorageWritable())
		{
			Log.e(TAG, "External storage can not be written");
			Toast.makeText(this, "External Storage can not be written",
					Toast.LENGTH_SHORT).show();
		}
		try
		{
			// open file for writing
			File root = Environment.getExternalStorageDirectory();
			String path = root.getAbsolutePath();
			// check if file can be written
			if (root.canWrite())
			{
				String fileName = "betterwifionoff-"
						+ DateUtils.now("yyyy-MM-dd_HHmmssSSS") + ".txt";

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				DataStorage.LogToFile(fileName, "===========================");
		        DataStorage.LogToFile(fileName, "BetterWifiOnOff preferences");
		        DataStorage.LogToFile(fileName, "===========================");
		        
		        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet())
		        {
		            Object val = entry.getValue();
		            if (val == null)
		            {
		            	DataStorage.LogToFile(fileName, String.format("%s = <null>%n", entry.getKey()));
		            }
		            else
		            {
		            	DataStorage.LogToFile(fileName, String.format("%s = %s (%s)%n", entry.getKey(), String.valueOf(val), val.getClass()
		                        .getSimpleName()));
		            }
		        }
		        
		        DataStorage.LogToFile(fileName, "======================");
		        DataStorage.LogToFile(fileName, "BetterWifiOnOff logcat");
		        DataStorage.LogToFile(fileName, "======================");

				Util.run("logcat -d | grep \"BetterWifiOnOff\\.\" >> " + path + "/" + fileName);
			} else
			{
				Log.i(TAG,
						"Write error. "
								+ Environment.getExternalStorageDirectory()
								+ " couldn't be written");
			}
		} catch (Exception e)
		{
			Log.e(TAG, "Exception: " + e.getMessage());
		}
	}


}