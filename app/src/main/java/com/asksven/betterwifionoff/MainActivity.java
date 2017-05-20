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
import java.util.ArrayList;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.asksven.android.common.AppRater;
import com.asksven.andoid.common.contrib.Util;
import com.asksven.android.common.utils.DataStorage;
import com.asksven.android.common.utils.DateUtils;
import com.asksven.betterwifionoff.R;
import com.asksven.betterwifionoff.data.EventLogger;
import com.asksven.betterwifionoff.services.EventWatcherService;
import com.asksven.betterwifionoff.utils.Configuration;
import com.google.ads.*;

public class MainActivity extends SherlockListActivity 

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
    private Intent broadcastIntent;
    
    

	
	/**
	 * a progess dialog to be used for long running tasks
	 */
	ProgressDialog m_progressDialog;
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String theme = sharedPrefs.getString("theme", "1");
		if (theme.equals("1"))
		{
			this.setTheme(R.style.Theme_Sherlock);
		}
		else
		{
			this.setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		}
	
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

        	Button buttonFree = (Button) findViewById(R.id.buttonFullVersion);

        	if (Configuration.isFullVersion(this))
    		{

        		nameTextView.setText(getString(R.string.support_version));
        		buttonFree.setVisibility(View.GONE);
        		Log.i(TAG, "full version was detected");
    		}
        	else
        	{
        		nameTextView.setText(R.string.free_version);
        		Log.i(TAG, "free version was detected");
            	buttonFree.setOnClickListener(new View.OnClickListener()
    	        {
    	            public void onClick(View v)
    	            {
    	                Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("https://play.google.com/store/apps/details?id=com.asksven.betterwifionoff_donate") );
    	                startActivity( browse );
    	            }
    	        });

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
	@Override
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
	        case R.id.refresh_events:
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
            	getShareDialog().show();
            	break;
	        case R.id.credits:
            	// Release notes
            	Intent intentCredits = new Intent(this, CreditsActivity.class);
                this.startActivity(intentCredits);
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
			m_listViewAdapter = new EventAdapter(this);
		}
		setListAdapter(m_listViewAdapter);
		m_listViewAdapter.update();
	}
	
		
	public Uri writeLoggingInfoToFile(Context context)
	{
		String fileName = "";
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
				fileName = "betterwifionoff-"
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

				Util.run("logcat -d -v time >> " + path + "/" + fileName);
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
		File dumpFile = new File(Environment.getExternalStorageDirectory(), fileName);
		return Uri.fromFile(dumpFile);
	}
	
	public Dialog getShareDialog()
	{
	
		final ArrayList<Integer> selectedSaveActions = new ArrayList<Integer>();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    	final ArrayList<Uri> attachements = new ArrayList<Uri>();
		
		// Set the dialog title
		builder.setTitle(R.string.title_share_dialog)
				.setPositiveButton(R.string.label_button_share, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						attachements.add(MainActivity.this.writeLoggingInfoToFile(MainActivity.this));


						if (!attachements.isEmpty())
						{
							Intent shareIntent = new Intent();
							shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
							shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachements);
							shareIntent.setType("text/text");
							startActivity(Intent.createChooser(shareIntent, "Share info to.."));
						}
					}
				})
				.setNeutralButton(R.string.label_button_save, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{

						attachements.add(MainActivity.this.writeLoggingInfoToFile(MainActivity.this));
						
					}
				}).setNegativeButton(R.string.label_button_cancel, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int id)
						{
							// do nothing
						}
					});
	
		return builder.create();
	}



}