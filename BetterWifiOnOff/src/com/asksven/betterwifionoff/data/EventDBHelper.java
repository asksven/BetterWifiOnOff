/*
 * Copyright (C) 2011-2012 asksven
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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * DBHelper sigleton class.  
 * 
 * Database layer for cell log data
 */

public class EventDBHelper
{
	private static final String DATABASE_NAME	= "betterwifionoff";
    private static final String TABLE_DBVERSION = "dbversion";
    private static final String TABLE_NAME 		= "events";
    private static final int DATABASE_VERSION 	= 6;
    private static final String TAG 			= "EventDBHelper";
    private static final String[] COLS 			= new String[] {
    		"id", "event_type", "time_st", "event"};

    Context m_context;
    static EventDBHelper m_helper;

    private static final String DBVERSION_CREATE = 
    	"create table " + TABLE_DBVERSION + " ("
    		+ "version integer not null);";
    
    private static final String DBVERSION_DROP =
    	" drop table " + TABLE_DBVERSION + ";";

    private static final String PURGE_EVENTS =
        	" delete from " + TABLE_NAME + ";";

    private static final String TABLE_CREATE =
        "create table " + TABLE_NAME + " ("
    	    + "id integer primary key autoincrement, "
            + "event_type integer, "
            + "time_st integer, "
            + "event text"
            + ");";
    

    private static final String TABLE_DROP =
    	"drop table " + TABLE_NAME + ";";

    private SQLiteDatabase db;

    protected static EventDBHelper getInstance(Context context)
    {
    	if (m_helper == null)
    	{
    		m_helper = new EventDBHelper(context); 
    	}
    	return m_helper;
    }
    
    /**
     * Hidden constructor, use as singleton
     * @param ctx
     */
    private EventDBHelper(Context ctx)
    {
    	m_context = ctx;
		try
		{
			db = m_context.openOrCreateDatabase(DATABASE_NAME, 0,null);

			// Check for the existence of the DBVERSION table
			// If it doesn't exist than create the overall data,
			// otherwise double check the version
			Cursor c =
				db.query("sqlite_master", new String[] { "name" },
						"type='table' and name='"+TABLE_DBVERSION+"'", null, null, null, null);
			int numRows = c.getCount();
			if (numRows < 1)
			{
				createDatabase(db);
			}
			else
			{
				int version=0;
				Cursor vc = db.query(true, TABLE_DBVERSION, new String[] {"version"},
						null, null, null, null, null,null);
				if(vc.getCount() > 0)
				{
				    vc.moveToLast();
				    version=vc.getInt(0);
				}
				vc.close();
				if (version!=DATABASE_VERSION)
				{
					Log.e(TAG,"database version mismatch");
					migrateDatabase(db, version, DATABASE_VERSION);
				}

			}
			c.close();
			

		}
		catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
    }

    private void createDatabase(SQLiteDatabase db)
    {
		try
		{
			db.execSQL(DBVERSION_CREATE);
			ContentValues args = new ContentValues();
			args.put("version", DATABASE_VERSION);
			db.insert(TABLE_DBVERSION, null, args);

			db.execSQL(TABLE_CREATE);
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
			db = m_context.openOrCreateDatabase(DATABASE_NAME, 0,null);
			db.execSQL(TABLE_DROP);
			db.execSQL(DBVERSION_DROP);
        }
        catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
        finally 
		{
			db.close();
		}    	
    }
    
    protected void purgeEvents()
    {
        try
        {
			db = m_context.openOrCreateDatabase(DATABASE_NAME, 0,null);
			db.execSQL(PURGE_EVENTS);
        }
        catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
        finally 
		{
			db.close();
		}    	
    }

	/**
	 * 
	 * @param entry
	 */
	protected void addEvent(Event entry)
	{
	
		ContentValues val = new ContentValues();
    	val.put("event_type", entry.m_type);
    	val.put("time_st", entry.m_timestamp);
    	val.put("event", entry.m_text);

	    try
	    {
			db = m_context.openOrCreateDatabase(DATABASE_NAME, 0,null);
	        long lRes =db.insert(TABLE_NAME, null, val);
	        if (lRes == -1)
	        {
	        	Log.d(TAG,"Error inserting row");
	        }
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
		finally 
		{
			db.close();
		}
	}
	

	/**
	 * 
	 * @return
	 */
	protected List<Event> fetchAllRows()
	{
	    ArrayList<Event> ret = new ArrayList<Event>();
	    try
	    {
			db = m_context.openOrCreateDatabase(DATABASE_NAME, 0,null);
	        Cursor c;
	        c = db.query(TABLE_NAME, COLS, null, null, null, null, null);
	        int numRows = c.getCount();
	        c.moveToFirst();
	        for (int i = 0; i < numRows; ++i)
	        {

	        	// cctor with id, name, command, command_status
	            Event row = createEventFromRow(c);
	           
	            ret.add(row);
	            c.moveToNext();
	        }
	        c.close();
		}
	    catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		}
	    finally 
		{
			db.close();
		}
	    return ret;
	}

	private Event createEventFromRow(Cursor c)
	{
        Event myRet = new Event(
				c.getInt(c.getColumnIndex("event_type")),
				c.getString(c.getColumnIndex("event")),
				c.getLong(c.getColumnIndex("time_st")));
        return myRet;
	}

    private void migrateDatabase(SQLiteDatabase db, int fromVersion, int toVersion)
    {
		try
		{
//			if ((fromVersion == 1)&&(toVersion == 2))
//			{
				deleteDatabase();
				createDatabase(db);
//			}
		}
		catch (SQLException e)
		{
			Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
		} 
    }


}
	
