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

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for an event collection
 * @author sven
 *
 */
public class EventLogger
{
	private List<Event> m_data;
	
	public EventLogger()
	{
		m_data = new ArrayList<Event>();
	}
	public void addEvent(int type, String event)
	{
		m_data.add(new Event(type, event));
	}
	
	public  void addUserEvent(String event)
	{
		m_data.add(new Event(Event.USER_INTERACTION, event));
	}
	
	public void addStatusChangedEvent(String event)
	{
		m_data.add(new Event(Event.STATUS_CHANGE, event));
	}

	
	public List<Event> getEvents()
	{
		return m_data;
	}
}
