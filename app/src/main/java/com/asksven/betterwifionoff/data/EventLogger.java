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
package com.asksven.betterwifionoff.data;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Singleton wrapper for an event collection
 * @author sven
 *
 */
public class EventLogger
{
	private Context m_ctx;
	private static EventLogger m_logger;
	
	public static EventLogger getInstance(Context context)
	{
		if (m_logger == null)
		{
			m_logger = new EventLogger(context);
		}
		return m_logger;
	}
	private EventLogger(Context ctx)
	{
		m_ctx = ctx;
	}

	public void addEvent(int type, String event)
	{
		Event entry = new Event(type, event);
		EventDBHelper db = new EventDBHelper(m_ctx);
		db.addEvent(entry);
		db.close();
	}
	
	public void clear()
	{
		EventDBHelper db = new EventDBHelper(m_ctx);
		db.purgeEvents();
		db.close();
	}

	public void addUserEvent(String event)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_ctx);
		boolean active = sharedPrefs.getBoolean("show_user", true);
		if (active)
		{
			addEvent(Event.USER_INTERACTION, event);
		}
	}
	
	public void addSystemEvent(String event)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_ctx);
		boolean active = sharedPrefs.getBoolean("show_system", true);
		if (active)
		{
			addEvent(Event.SYSTEM_EVENT, event);
		}
	}

	public void addStatusChangedEvent(String event)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_ctx);
		boolean active = sharedPrefs.getBoolean("show_status", true);
		if (active)
		{
			addEvent(Event.STATUS_CHANGE, event);
		}
	}

	
	public List<Event> getEvents()
	{
		EventDBHelper db = new EventDBHelper(m_ctx);
		List<Event> list = db .fetchAllRows();
		db.close();
		return list;
	}
}
