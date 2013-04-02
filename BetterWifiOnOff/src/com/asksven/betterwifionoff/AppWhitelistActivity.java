/*
 * Copyright (C) 2011 asksven
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asksven.betterwifionoff.data.ApplicationInfo;
import com.asksven.betterwifionoff.data.AppWhitelistDBHelper;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

// import com.android.phone.INetworkQueryService;

public class AppWhitelistActivity extends ListActivity
{
    private static final String TAG = "AppWhitelistActivity";

	/** the currently selected position */
	int m_cursorPosition = 0;
	
	ArrayList<ApplicationInfo> m_applications = null;

    /** Called when the activity is first created. */
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

        if (savedInstanceState != null)
        {
            // Restore last state for checked position.
        	m_cursorPosition = savedInstanceState.getInt("cur" + TAG, 0);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_whitelist);

        m_applications = getAppList();
        AppWhitelistAdapter adapter = new AppWhitelistAdapter(this, m_applications);
        
        
        setListAdapter(adapter);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("cur" + TAG, m_cursorPosition);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
    	ApplicationInfo myApp = m_applications.get(position);
        CheckBox cb = (CheckBox) v.findViewById(R.id.CheckBoxAllowed);
        
        AppWhitelistDBHelper myDB = new AppWhitelistDBHelper(this);
        try
        {
	        if ( (myApp != null) &&(cb != null) )
	        {
	        	if (cb.isChecked() )        	
				{
		        	cb.setChecked(false);
		        	myDB.deleteApp(myApp);
				}
	        	else
	        	{
		        	cb.setChecked(true);
		        	myDB.addApp(myApp);
	        	}
	        }
        }
        catch (Exception e)
        {
        	Log.e(TAG, "An error occured:" + e.getMessage());
        }
        finally
        {
        	myDB.close();
        }
    }

    ArrayList<ApplicationInfo> getAppList()
    {
    	ArrayList<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
		
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null)
        {
            final int count = apps.size();


            for (int i = 0; i < count; i++)
            {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.package_name = info.activityInfo.applicationInfo.packageName;
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);

                applications.add(application);
            }
        }

    	return applications;
    	
    }    
}


