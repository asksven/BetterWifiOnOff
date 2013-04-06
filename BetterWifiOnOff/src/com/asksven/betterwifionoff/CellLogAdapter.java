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

import java.util.ArrayList;
import java.util.List;

import com.asksven.betterwifionoff.data.ApplicationInfo;
import com.asksven.betterwifionoff.data.CellDBHelper;
import com.asksven.betterwifionoff.data.CellInfo;
import com.asksven.betterwifionoff.data.CellLogEntry;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class CellLogAdapter extends BaseAdapter
{
	// override with specific typed list
	protected ArrayList<CellLogEntry> m_listData;
	private Context m_context;
	
    public CellLogAdapter(Context context, ArrayList<CellLogEntry> listData)
    {
        super();
        m_context = context;
        m_listData = listData;
    }


    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
    	CellLogEntry entry = m_listData.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) m_context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell_log_row, null);
        }

        TextView tvTime = (TextView) convertView.findViewById(R.id.TextViewTimestamp);
        TextView tvCid = (TextView) convertView.findViewById(R.id.TextViewCid);
//        TextView tvLac = (TextView) convertView.findViewById(R.id.TextViewLac);
        TextView tvTag = (TextView) convertView.findViewById(R.id.textViewTags);


        tvTime.setText(entry.getTime());
        tvCid.setText(String.valueOf(entry.getCid()));
//        tvLac.setText(String.valueOf(entry.getLac()));
        
		CellDBHelper db = new CellDBHelper(m_context);
		String tags = db.getCellTagsAsString(entry.getCid());
		tvTag.setText(tags); 
		db.close();

                
        return convertView;
    }
    
    public int getCount()
    {
    	int ret = 0;
    	if (m_listData != null)
    	{
    		ret = m_listData.size();
    	}
    	return ret;
    }

    public Object getItem(int position)
    {
        return m_listData.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public void notifyDataSetChanged()
    {
      super.notifyDataSetChanged();
    }
    
    public void clear()
    {
    	m_listData.clear();
    }
    protected class OnItemClickListener implements OnClickListener
    {           
        private int m_iPosition;
        OnItemClickListener(int position)
        {
                m_iPosition = position;
        }
        
        @Override
        public void onClick(View arg0)
        {
//        	ApplicationInfo entry = (ApplicationInfo) getItem(m_iPosition);
        }
    }

}

