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

import com.asksven.betterwifionoff.MainActivity;

import android.content.Context;
import android.content.Intent;

/**
 * @author sven
 *
 */
public class EventBroadcaster
{ 

	public static void sendStatusEvent(Context ctx, String message)
	{
		sendEvent(ctx, Event.STATUS_CHANGE, message);
    }

	public static void sendErrorEvent(Context ctx, String message)
	{
		sendEvent(ctx, Event.ERROR_CONDITION, message);
    }

	public static void sendUserEvent(Context ctx, String message)
	{
		sendEvent(ctx, Event.USER_INTERACTION, message);
    }
	
	private static void sendEvent(Context ctx, int type, String message)
	{
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
		intent.putExtra("type", type);
    	intent.putExtra("event", message);
    	ctx.sendBroadcast(intent);
    }
	

}
