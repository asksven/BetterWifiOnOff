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



import java.util.HashMap;
import java.util.Map;

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
public class AppWhitelistDBHelper
{

	private static final String DATABASE_NAME	= "betterwifionoff_appwhitelist";
    private static final String TABLE_DBVERSION = "dbversion";
    private static final String TABLE_NAME 		= "app_whitelist";
    private static final int DATABASE_VERSION 	= 2;
    private static final String TAG 			= "AppWhitelistDBHelper";
    private static final String[] COLS 			= new String[] {"id", "packagename"};

    Context myCtx;

    private static final String DBVERSION_CREATE = 
    	"create table " + TABLE_DBVERSION + " ("
    		+ "version integer not null);";
    
    private static final String DBVERSION_DROP =
    	" drop table " + TABLE_DBVERSION + ";";

    private static final String TABLE_CREATE =
        "create table " + TABLE_NAME + " ("
    	    + "id integer primary key autoincrement, "
            + "packagename text not null"
            + ");";
    
    private static final String TABLE_MIGRATE_1_2 =
    	"alter table " + TABLE_NAME + " add column processresult int";

    private static final String TABLE_DROP =
    	"drop table " + TABLE_NAME + ";";

    private SQLiteDatabase m_db;

    /**
     * 
     * @param ctx
     */
    public AppWhitelistDBHelper(Context ctx)
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

			db.execSQL(TABLE_CREATE);
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
				db.execSQL(TABLE_MIGRATE_1_2);
				ContentValues args = new ContentValues();
				args.put("version", DATABASE_VERSION);
				db.insert(TABLE_DBVERSION, null, args);
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
			m_db.execSQL(TABLE_DROP);
			
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
	public void addApp(ApplicationInfo record)
	{
		ContentValues val = new ContentValues();
		String name = record.package_name; //fullName();
		val.put("packagename", name);
		Log.d(TAG, "Added '" + name + "' to app whitelist");
	    try
	    {
	        long lRes =m_db.insert(TABLE_NAME, null, val);
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
	 * @param ApplicationInfo a value object
	 */
	public void deleteApp(ApplicationInfo record)
	{
	    try
	    {
			m_db.delete(TABLE_NAME, "packagename=\"" + record.package_name + "\"", null);
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
	public boolean exists(ApplicationInfo record)
	{
		return exists(record.package_name);
	}

	/**
	 * 
	 * @return true if Application exists
	 */
	public boolean exists(String packageName)
	{
	    boolean ret = false;
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_NAME, COLS, "packagename=" + "\"" + packageName + "\"", null, null, null, null);
	        int numRows = c.getCount();
	        c.moveToFirst();
	        if (numRows == 1)
	        {
	        	Log.i(TAG, "Package " + packageName + " was found in whitelist");
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

	public HashMap<String, Integer> getWitelist()
	{
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
	    try
	    {
	        Cursor c;
	        c = m_db.query(TABLE_NAME, COLS, null, null, null, null, null);

	        c.moveToFirst();
	        while (c.isAfterLast() == false) 
	        {
	            ret.put(c.getString(1), c.getInt(0));
	            Log.i(TAG, "Added " + c.getString(1) + " to app wihitelist");
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

}
	
