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
package com.asksven.betterwifionoff.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

/**
 * Helper class that returns info about the configuration (free of full)
 * @author sven
 *
 */
public class Configuration
{
	final static String TAG = "Configuration";
	
	public static boolean isFullVersion(Context ctx)
	{
		return (getVersion(ctx) > 0);
	}
	
	private static int getVersion(Context ctx)
	{
		int iRet = 0;
		// try accessing the License class by reflection
		try
		{
	          // ClassLoader cl = ctx.getClassLoader();
	          Context foreignContext = ctx.createPackageContext(
	        		  "com.asksven.betterwifionoff_donate",
	        		  Context.CONTEXT_INCLUDE_CODE |
	        		  Context.CONTEXT_IGNORE_SECURITY);
	          Class<?> c =
	        		  foreignContext.getClassLoader().loadClass("com.asksven.betterwifionoff.License");
	         
	          Method methodGetVersion = c.getMethod("getVersion");
	          	     
	          iRet = (Integer) methodGetVersion.invoke(c); 

	    }
		catch( IllegalArgumentException e )
		{
	        iRet = -1;
	    }
		catch (ClassNotFoundException e)
		{
	    	iRet = -2;
	    }
		catch( Exception e )
		{
	    	iRet = -3;
	    }    
		if (iRet > 0)
		{
			Log.i(TAG, "license was detected");
		}
		else
		{
			Log.i(TAG, "no license was detected: " + iRet);
		}
		
		return iRet;
	}
}
