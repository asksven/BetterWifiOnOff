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


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CreditsAdapter extends ArrayAdapter<String>
{
	private final Context m_context;
	private final String[] m_libs = new String[]
	{ "ActionBarSherlock"};
	private final String[] m_authors = new String[]
	{ "Jake Wharton"};
	private final String[] m_licenses = new String[]
	{ "Apache 2.0"};
	private final String[] m_urls = new String[]
	{ "Apache 2.0"};

	public CreditsAdapter(Context context)
	{
		super(context, R.layout.credits_row);
		this.m_context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.credits_row, parent, false);
		TextView textViewName = (TextView) rowView.findViewById(R.id.textViewName);
		TextView textViewAuthor = (TextView) rowView.findViewById(R.id.textViewAuthor);
		TextView textViewLicense = (TextView) rowView.findViewById(R.id.textViewLicense);

		textViewName.setText(m_libs[position]);
		textViewAuthor.setText(m_authors[position]);
		textViewLicense.setText(m_licenses[position]);

		return rowView;
	}
	
	public int getCount()
    {
    	if (m_libs != null)
    	{
    		return m_libs.length;
    	}
    	else
    	{
    		return 0;
    	}
    }

    public String getItem(int position)
    {
        return m_libs[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

	
}