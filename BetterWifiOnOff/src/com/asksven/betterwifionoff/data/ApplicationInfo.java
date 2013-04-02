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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Represents a launchable application. An application is made of a name (or
 * title), an intent and an icon.
 */
public class ApplicationInfo implements Comparable<ApplicationInfo>
{
	/**
	 * The application name.
	 */
	public CharSequence title;

	/**
	 * The intent used to start the application.
	 */
	public Intent intent;

	/**
	 * The application icon.
	 */
	public Drawable icon;

	/**
	 * The package name.
	 */
	public String package_name;

	/**
	 * When set to true, indicates that the icon has been resized.
	 */
	public boolean filtered;

	public final void setTitle(CharSequence strTitle)
	{
		title = strTitle;
	}

	/**
	 * Creates the application intent based on a component name and various
	 * launch flags.
	 * 
	 * @param className
	 *            the class name of the component representing the intent
	 * @param launchFlags
	 *            the launch flags
	 */
	public final void setActivity(ComponentName className, int launchFlags)
	{
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(className);
		intent.setFlags(launchFlags);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ApplicationInfo))
		{
			return false;
		}

		ApplicationInfo that = (ApplicationInfo) o;
		return title.equals(that.title)
				&& intent.getComponent().getClassName().equals(that.intent.getComponent().getClassName());
	}

	@Override
	public int hashCode()
	{
		int result;
		result = (title != null ? title.hashCode() : 0);
		final String name = intent.getComponent().getClassName();
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	public String fullName()
	{
		return intent.getComponent().getClassName();
	}

	public boolean isWhitelisted(Context ctx)
	{
		boolean ret = false;
		AppWhitelistDBHelper myDB = new AppWhitelistDBHelper(ctx);
		ret = myDB.exists(this);
		myDB.close();
		return ret;
	}

	public int compareTo(ApplicationInfo o)
	{
		// we want to sort in descending order
		return (o.fullName().compareTo(this.fullName()));
	}

}
