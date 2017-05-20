/*
 * Copyright (C) 2011-2013 asksven
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

import com.actionbarsherlock.app.SherlockListActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class CreditsActivity extends SherlockListActivity
{

    private static final String TAG = "CreditsActivity";
    
    
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
        setContentView(R.layout.credits);
        CreditsAdapter adapter = new CreditsAdapter(this);
        setListAdapter(adapter);
        setTitle("Credits");
        

    }   
    
}