/*
 * Copyright (C) 2013 asksven
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.asksven.android.common.AppRater;
import com.asksven.android.common.ReadmeActivity;
import com.asksven.andoid.common.contrib.Util;
import com.asksven.android.common.utils.DataStorage;
import com.asksven.android.common.utils.DateUtils;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.utils.Configuration;
import com.google.ads.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends SherlockListActivity implements OnSharedPreferenceChangeListener 

{
	/**
	 * The logging TAG
	 */
	private static final String TAG = "MainActivity";

	public static final String MARKET_LINK ="market://details?id=com.asksven.betterwifionoff";
    public static final String TWITTER_LINK ="https://twitter.com/#!/asksven";

    public static final String BROADCAST_ACTION = "com.asksven.betterwifionoff.displayevent";

    
	private EventAdapter m_listViewAdapter;
    OnClickListener m_checkBoxListener;
    boolean m_restartActivity = false;
    String m_theme = "0";
	
	/**
	 * a progess dialog to be used for long running tasks
	 */
	ProgressDialog m_progressDialog;
	
	PullToRefreshListView m_pullToRefreshView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// set the theme before calling super.onCreate
		setTheme();
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
	    
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
        		hintTextView.setVisibility(View.GONE);
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
		else
		{
	    	// show "rate" dialog
	    	// for testing: AppRater.showRateDialog(this, null);
	    	AppRater.app_launched(this);
			
		}

        // start the service
    	startService(new Intent(this, EventWatcherService.class));
    	
    	// add listview adapter
    	this.setListViewAdapter();
    	
    	m_pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
    	m_pullToRefreshView.setScrollingWhileRefreshingEnabled(false);
    	m_pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>()
    	{
    	    @Override
    	    public void onRefresh(PullToRefreshBase<ListView> refreshView)
    	    {
    	        // Do work to refresh the list here.
    	        new GetDataTask().execute();
    	    }
    	});
    	
        // Set up a listener whenever a key changes
    	PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


  	}



	/**
     * Save state, the application is going to get moved out of memory
     * @see http://stackoverflow.com/questions/151777/how-do-i-save-an-android-applications-state
     */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	super.onSaveInstanceState(savedInstanceState);
        
    }

	/* Request updates at startup */
	@Override
	protected void onResume()
	{
		super.onResume();

		// pref was changed: restart to reload theme
		if(m_restartActivity)
	    {
	        m_restartActivity = false;
	        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(i);
	    }

		// add listview adapter
    	this.setListViewAdapter();
    	
    	if (m_listViewAdapter != null)
    	{
    		m_listViewAdapter.notifyDataSetChanged();
    	}
	}


	/** 
     * Add menu items
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu)
    {  
    	MenuInflater inflater = getSupportMenuInflater();
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
				EventLogger.getInstance(this).clear();
		    	if (m_listViewAdapter != null)
		    	{
		    		m_listViewAdapter.update();
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
            case R.id.credits:
	        	Intent intentCredits = new Intent(this, CreditsActivity.class);
	            this.startActivity(intentCredits);
	        	break;	            	

        }
        
        return true;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences spref, String key)
    {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(key.equals("theme") && !sharedPrefs.getString(key, "1").equals(m_theme))
        {
            setTheme();
            m_restartActivity = true;
        }
    }
	private void setListViewAdapter()
	{
		// make sure we only instanciate when the reference does not exist
		if (m_listViewAdapter == null)
		{
			m_listViewAdapter = new EventAdapter(this);
		}
		setListAdapter(m_listViewAdapter);
		m_listViewAdapter.update();
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

				Util.run("logcat -d >> " + path + "/" + fileName);
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
	
	private class GetDataTask extends AsyncTask<Void, Void, Void>
	{
		@Override
	    protected Void doInBackground(Void... refresh)
	    {
			m_listViewAdapter.update();
    		
    		return null;
	    }

	    @Override
	    protected void onPostExecute(Void result)
	    {
	    	m_listViewAdapter.notifyDataSetChanged();
	        // Call onRefreshComplete when the list has been refreshed.
	    	
	        m_pullToRefreshView.onRefreshComplete();
	        super.onPostExecute(result);
	    }
	}
	
	private void setTheme()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		m_theme = sharedPrefs.getString("theme", "1");
		if (m_theme.equals("1"))
		{
			this.setTheme(R.style.Theme_Sherlock);
		}
		else
		{
			this.setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		}
	}


}