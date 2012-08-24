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

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.asksven.betterwifionoff.data.Event;
import com.asksven.betterwifionoff.data.EventLogger;

public class EventAdapter extends BaseAdapter
{
    private Context context;

    private List<Event> m_listData;
    private static final String TAG = "EventAdapter";


    public EventAdapter(Context context, EventLogger data)
    {
        this.context = context;
        this.m_listData = data.getEvents();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        boolean bKbEnabled = sharedPrefs.getBoolean("enable_kb", true);
    }

    public int getCount()
    {
        return m_listData.size();
    }

    public Object getItem(int position)
    {
        return m_listData.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
    	Event entry = m_listData.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_row, null);
        }
        TextView tvTime = (TextView) convertView.findViewById(R.id.TextViewEntry);
       	tvTime.setText(entry.getTime() + " " + entry.getType() +  "/" + entry.getEvent());
        
        
        return convertView;
    }
    

}

