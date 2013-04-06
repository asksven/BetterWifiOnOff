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

/**
 * A value holder for cell info
 * @author sven
 *
 */
public class CellInfo
{

	int m_cid 		= 0;
	int m_lac 		= 0;
	int m_psc 		= 0;
	String m_tags 	= "";
	
	public CellInfo(int cid, int lac, int psc, String tags)
	{
		m_cid 	= cid;
		m_lac 	= lac;
		m_psc 	= psc;
		m_tags 	= tags;
	}
	
	public int getCid()
	{
		return m_cid;
	}
	
	public int getLac()
	{
		return m_lac;
	}

	public int getPsc()
	{
		return m_psc;
	}

	public String getTags()
	{
		return m_tags;
	}

}
