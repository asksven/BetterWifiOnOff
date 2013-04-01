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
import java.util.HashMap;
import com.asksven.betterwifionoff.data.CellDBHelper;
import com.asksven.betterwifionoff.data.CellLogEntry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

// import com.android.phone.INetworkQueryService;

public class CellLogActivity extends ListActivity
{
    private static final String TAG = "CellLogActivity";

	/** the currently selected position */
	int m_cursorPosition = 0;
	
	ArrayList<CellLogEntry> m_data = null;
	CellLogAdapter m_adapter = null;

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
        setContentView(R.layout.cell_log);

        CellDBHelper db = new CellDBHelper(this);
        m_data = db.getCellLog();
        db.close();
        m_adapter = new CellLogAdapter(this, m_data);
        
        
        setListAdapter(m_adapter);
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
    	CellLogEntry myCell = m_data.get(position);
    	getTagsDialog(myCell.getCid()).show();
    }

	public Dialog getTagsDialog(final int cid)
	{
		
		CellDBHelper db = new CellDBHelper(this);
		final ArrayList<String> tags = db.getTags(); 
		ArrayList<String> cellTags = db.getCellTags(cid); 
		db.close();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		
		final CharSequence[] labels = new CharSequence[tags.size()];
		final boolean[] checks = new boolean[tags.size()];
		
		for (int i=0; i < tags.size(); i++)
		{
			labels[i] = tags.get(i);
			if (cellTags.contains(tags.get(i)))
			{
				checks[i] = true;
			}
			else
			{
				checks[i] = false;
			}
		}
		
		builder.setTitle("Tags")
				.setMultiChoiceItems(labels, checks, new DialogInterface.OnMultiChoiceClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked)
					{
						if (isChecked)
						{
							// If the user checked the item, add it to the
							// selected items
							checks[which] = true;
						}
						else
						{
							// Else, if the item is already in the array,
							// remove it
							checks[which] = false;
						}
					}
				})
				// Set the action buttons
				.setPositiveButton("Save", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						// update tags
						ArrayList<String> tags = new ArrayList<String>();
						for (int i=0; i < labels.length; i++)
						{
							if (checks[i])
							{
								tags.add(labels[i].toString());
							}
						}
						CellDBHelper db = new CellDBHelper(CellLogActivity.this);
						db.setCellTags(cid, tags); 
						m_adapter.notifyDataSetChanged();
						db.close();
						
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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


