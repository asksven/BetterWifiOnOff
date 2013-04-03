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



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.asksven.android.common.utils.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * DBHelper class.  
 * 
 * Database layer for the launcher
 */
/**
 * @author sven
 *
 */
public class CellDBHelper
{

	private static final String DATABASE_NAME	= "betterwifionoff_cellinfo";
    private static final String TABLE_DBVERSION = "dbversion";
    private static final String TABLE_LOG_NAME	= "cell_log";
    private static final String TABLE_CELL_NAME	= "cells";
    private static final String TABLE_TAGS_NAME	= "tags";
    private static final int DATABASE_VERSION 	= 4;
    private static final String TAG 			= "CellDBHelper";
    private static final String SEPARATOR		= ",";
    
    private static final String[] LOG_COLS 			= new String[] {"id", "timestamp", "cid", "lac"};
    private static final String[] CELL_COLS 			= new String[] {"cid", "tags"};
    private static final String[] TAGS_COLS 			= new String[] {"id", "tag"};

    Context myCtx;

    private static final String DBVERSION_CREATE = 
    	"create table " + TABLE_DBVERSION + " ("
    		+ "version integer not null);";
    
    private static final String DBVERSION_DROP =
    	" drop table " + TABLE_DBVERSION + ";";

    private static final String TABLE_LOG_CREATE =
        "create table " + TABLE_LOG_NAME + " ("
    	    + "id integer primary key autoincrement, "
            + "timestamp text not null, "
            + "cid integer not null, "
            + "lac integer not null"
            + ");";

    private static final String TABLE_CELL_CREATE =
            "create table " + TABLE_CELL_NAME + " ("
                + "cid integer primary key, "
                + "tags text not null"
                + ");";

    private static final String TABLE_TAGS_CREATE =
            "create table " + TABLE_TAGS_NAME + " ("
        	    + "id integer primary key autoincrement, "
                + "tag text not null"
                + ");";


    private static final String TABLE_LOG_DROP =
    	"drop table " + TABLE_LOG_NAME + ";";

    private static final String TABLE_CELL_DROP =
        	"drop table " + TABLE_CELL_NAME + ";";

    private static final String TABLE_TAGS_DROP =
        	"drop table " + TABLE_TAGS_NAME + ";";

    private SQLiteDatabase m_db;

    /**
     * 
     * @param ctx
     */
    public CellDBHelper(Context ctx)
    {
    	myCtx = ctx;
		try
		{
			m_db = myCtx.openOrCreateDatabase(DATABASE_NAME, 0,null);

			// Check for the existence of the DBVERSION table
			// If it doesn't exist than create the overall data,
			// otherwise double check the version
			Cursor c =
				m_db.query("sqlite_master", new String[] { "name" },
						"type='table' and name='"+TABLE_DBVERSION+"'", null, null, null, null);
			int numRows = c.getCount();
			if (numRows < 1)
			{
				CreateDatabase(m_db);
			}
			else
			{
				int version=0;
				Cursor vc = m_db.query(true, TABLE_DBVERSION, new String[] {"version"},
						null, null, null, null, null,null);
				if(vc.getCount() > 0) {
				    vc.moveToLast();
				    version=vc.getInt(0);
				}
				vc.close();
				if (version!=DATABASE_VERSION)
				{
					Log.e(TAG,"database version mismatch");
//					MigrateDatabase(m_db, version, DATABASE_VERSION);
					deleteDatabase();
					CreateDatabase(m_db);
//					populateDatabase();
				}
			}
			c.close();
			

		}
		catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
    }

    public void close()
    {
		m_db.close();
    }
    
    private void CreateDatabase(SQLiteDatabase db)
    {
		try
		{
			db.execSQL(DBVERSION_CREATE);
			ContentValues args = new ContentValues();
			args.put("version", DATABASE_VERSION);
			db.insert(TABLE_DBVERSION, null, args);

			db.execSQL(TABLE_LOG_CREATE);
			db.execSQL(TABLE_CELL_CREATE);
			db.execSQL(TABLE_TAGS_CREATE);
		}
		catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		} 
    }
    
    private void MigrateDatabase(SQLiteDatabase db, int fromVersion, int toVersion)
    {
		try
		{
			if ((fromVersion == 1)&&(toVersion == 2))
			{
//				db.execSQL(TABLE_MIGRATE_1_2);
//				ContentValues args = new ContentValues();
//				args.put("version", DATABASE_VERSION);
//				db.insert(TABLE_DBVERSION, null, args);
			}
			if ((fromVersion == 3)&&(toVersion == 4))
			{
				deleteDatabase();
				CreateDatabase(db);
			}
		}
		catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		} 
    }

    private void deleteDatabase()
    {
        try
        {
        	m_db.execSQL(DBVERSION_DROP);
        }
        catch (SQLException e)
		{
		}
        try
        {

			m_db.execSQL(TABLE_LOG_DROP);
        }
        catch (SQLException e)
		{
		}

        try
        {
        	m_db.execSQL(TABLE_CELL_DROP);
        }
        catch (SQLException e)
		{
		}

        try
        {
        	m_db.execSQL(TABLE_TAGS_DROP);
        }
        catch (SQLException e)
		{
		}

    }
        
//	/**
//	 * 
//	 * @param ApplicationInfo record a value object
//	 */
//	public void addCell(CellInfo record)
//	{
//		ContentValues val = new ContentValues();
//		String name = record.package_name; //fullName();
//		val.put("packagename", name);
//
//	    try
//	    {
//	        long lRes =m_db.insert(TABLE_LOG_NAME, null, val);
//	        if (lRes == -1)
//	        {
//	        	Log.d(TAG,"Error inserting row");
//	        }
//		}
//	    catch (SQLException e)
//		{
//			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
//		}
//	}

	/**
	 * 
	 * @param ApplicationInfo record a value object
	 */
	public void addTag(String name)
	{
		ContentValues val = new ContentValues();
		val.put("tag", name);

	    try
	    {
	        long lRes =m_db.insert(TABLE_TAGS_NAME, null, val);
	        if (lRes == -1)
	        {
	        	Log.d(TAG,"Error inserting row");
	        }
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * @param ApplicationInfo record a value object
	 */
	public void addCellLogEntry(CellLogEntry log)
	{
		ContentValues val = new ContentValues();
		val.put("timestamp", log.getTime());
		val.put("cid", log.getCid());
		val.put("lac", log.getLac());

	    try
	    {
	        long lRes =m_db.insert(TABLE_LOG_NAME, null, val);
	        if (lRes == -1)
	        {
	        	Log.d(TAG,"Error inserting row");
	        }
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * @param a tag
	 */
	public void deleteTag(String tag)
	{
	    try
	    {
			m_db.delete(TABLE_TAGS_NAME, "tag=\"" + tag + "\"", null);
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * @return true if Application exists
	 */
	public boolean tagExists(String tag)
	{
	    boolean ret = false;
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_TAGS_NAME, TAGS_COLS, "tag=" + "\"" + tag + "\"", null, null, null, null);
	        int numRows = c.getCount();
	        c.moveToFirst();
	        if (numRows == 1)
	        {

	        	// cctor with id, name, command, command_status
	            ret = true;	           
	        }
	        c.close();
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	    return ret;
	}

	public ArrayList<CellLogEntry> getCellLog()
	{
		ArrayList<CellLogEntry> ret = new ArrayList<CellLogEntry>();
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_LOG_NAME, LOG_COLS, null, null, null, null, "ID DESC");

	        c.moveToFirst();
	        while (c.isAfterLast() == false) 
	        {
	        	CellLogEntry log = new CellLogEntry(c.getString(1), c.getInt(2), c.getInt(3));
	            ret.add(log);
	            Log.i(TAG, "Added " + log.toString() + " to cell log");
	            c.moveToNext();
	        }
	        c.close();
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	    return ret;

	}

	public ArrayList<String> getCellTags(int cid)
	{
		ArrayList<String> ret = new ArrayList<String>();
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_CELL_NAME, CELL_COLS, "cid=" + cid , null, null, null, null);

	        c.moveToFirst();
	        while (c.isAfterLast() == false) 
	        {
	        	String strTags = c.getString(1);
	        	StringUtils.splitLine(strTags, ret, SEPARATOR);
	            c.moveToNext();
	        }
	        c.close();
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	    Log.i(TAG, "Reading tags for cid " + cid + " :" + ret.toString());
	    return ret;

	}
	
	public String getCellTagsAsString(int cid)
	{
		ArrayList<String> tags = getCellTags(cid);
		String strTags = StringUtils.join(tags.toArray(new String[]{}), SEPARATOR, true);
		return strTags;
		
	}

	public void setCellTags(int cid, ArrayList<String> tags)
	{

		String strTags = StringUtils.join(tags.toArray(new String[]{}), ",", true);
	
		ContentValues val = new ContentValues();
		val.put("cid", cid);
		val.put("tags", strTags);

		try
		{
			long lRes = m_db.replace(TABLE_CELL_NAME, null, val);
			if (lRes == -1)
			{
				Log.e(TAG, "Error inserting or updating row");
			}
		}
		catch (SQLException e)
		{
			Log.d(TAG, "SQLite exception: " + e.getLocalizedMessage());
		}

	}

	public ArrayList<String> getTags()
	{
		ArrayList<String> ret = new ArrayList<String>();
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_TAGS_NAME, TAGS_COLS, null, null, null, null, null);

	        c.moveToFirst();
	        while (c.isAfterLast() == false) 
	        {
	            ret.add(c.getString(1));
	            Log.i(TAG, "Added " + c.getString(1) + " to tags list");
	            c.moveToNext();
	        }
	        c.close();
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	    return ret;

	}
	
    public void purgeLog()
    {
        try
        {
			m_db.execSQL("delete from " + TABLE_LOG_NAME + ";");
        }
        catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
    }


}
	
