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

import com.asksven.android.common.utils.DateUtils;


/**
 * Value holder class for events of all kinds
 * @author sven
 *
 */
public class Event
{
	public static final int USER_INTERACTION = 1;
	public static final int STATUS_CHANGE	 = 2;
	public static final int ERROR_CONDITION	 = 3;
	public static final int SYSTEM_EVENT	 = 4;
	
	int m_type;
	String m_text;
	long m_timestamp;
	
	Event(int type, String event)
	{
		m_type 		= type;
		m_text 		= event;
		m_timestamp	= System.currentTimeMillis();
	}
	
	public String getTime()
	{
		return DateUtils.formatShort(m_timestamp);
	}
	
	public String getType()
	{
		switch (m_type)
		{
			case USER_INTERACTION:
				return "U";
			case STATUS_CHANGE:
				return "S";
			case SYSTEM_EVENT:
				return "S";
			case ERROR_CONDITION:
				return "!";
			default:
				return "Unknown";
		}
	}
	
	public String getEvent()
	{
		return m_text;
	}	
}
