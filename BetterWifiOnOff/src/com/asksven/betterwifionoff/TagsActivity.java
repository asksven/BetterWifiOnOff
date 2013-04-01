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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

// import com.android.phone.INetworkQueryService;

public class TagsActivity extends ListActivity
{
    private static final String TAG = "TagActivity";

	/** the currently selected position */
	int m_cursorPosition = 0;
	
	ArrayList<String> m_data = null;
	TagsAdapter m_adapter = null;

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
        setContentView(R.layout.tags);

        CellDBHelper db = new CellDBHelper(this);
        m_data = db.getTags();
        db.close();
        m_adapter = new TagsAdapter(this, m_data);
        
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
    	String tag = m_data.get(position);
    	deleteTagDialog(tag);
    }

    public void onAddTagClick(View view) 
    {
        EditText edNewTag = (EditText) findViewById(R.id.editTextNewTag);
        if (!edNewTag.getText().toString().equals(""))
        {
        	CellDBHelper db = new CellDBHelper(this);
        	if (!db.tagExists(edNewTag.getText().toString()))
        	{
        		db.addTag(edNewTag.getText().toString());
        		m_data.add(edNewTag.getText().toString());
        		edNewTag.setText("");
        		m_adapter.notifyDataSetChanged();
        		
        	}
        	db.close();
        }
    	
    }
    
    void deleteTagDialog(final String tag)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
            	CellDBHelper db = new CellDBHelper(TagsActivity.this);
            	db.deleteTag(tag);
            	m_data.remove(tag);
            	db.close();
                dialog.dismiss();
                m_adapter.notifyDataSetChanged();
            }

        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        Dialog alert = builder.create();
        alert.show();
    }
}


