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
package com.asksven.betterwifionoff.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author sven
 *
 */
public class EventWatcherServiceBinder
{
	static EventWatcherServiceBinder m_instance = null;
	Context m_context = null;
	static boolean m_isBound = false;
	

	private EventWatcherServiceBinder()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static EventWatcherServiceBinder getInstance(Context ctx)
	{
		if (m_instance == null)
		{
			m_instance = new EventWatcherServiceBinder();
			m_instance.m_context = ctx;
			m_instance.doBindService(); 
		}
		return m_instance;
		
	}
	
	public EventWatcherService getService()
	{
		if (m_isBound)
		{
			return m_boundService;
		}
		else
		{
			return null;
		}
	}
	private EventWatcherService m_boundService;

	private ServiceConnection mConnection = new ServiceConnection()
	{
	    public void onServiceConnected(ComponentName className, IBinder service)
	    {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        m_boundService = ((EventWatcherService.LocalBinder)service).getService();
	    }
	    
	    public void onServiceDisconnected(ComponentName className)
	    {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        m_boundService = null;
	        

	    }
	};
	
	void doBindService()
	{
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    m_context.bindService(new Intent(m_context, 
	            EventWatcherService.class), mConnection, Context.BIND_AUTO_CREATE);
	    m_isBound = true;
	}

	void doUnbindService()
	{
	    if (m_isBound)
	    {
	        // Detach our existing connection.
	        m_context.unbindService(mConnection);
	        m_isBound = false;
	    }
	}
}
