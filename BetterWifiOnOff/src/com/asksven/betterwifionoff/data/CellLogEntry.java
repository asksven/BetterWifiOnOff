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
package com.asksven.betterwifionoff.data;

import com.asksven.android.common.utils.DateUtils;

/**
 * A value holder for cell info
 * @author sven
 *
 */
public class CellLogEntry
{

	String m_time;
	int m_cid;
	int m_lac;
	
	public CellLogEntry(int cid, int lac)
	{
		m_cid 	= cid;
		m_lac 	= lac;
		m_time	= DateUtils.now();
	}

	public CellLogEntry(String timestamp, int cid, int lac)
	{
		m_cid 	= cid;
		m_lac 	= lac;
		m_time	= timestamp;
	}

	public int getCid()
	{
		return m_cid;
	}

	public int getLac()
	{
		return m_lac;
	}

	public String getTime()
	{
		return m_time;
	}
}
