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
 * 
 * This file was contributed by two forty four a.m. LLC <http://www.twofortyfouram.com>
 * unter the terms of the Apache License, Version 2.0
 */

package com.asksven.betterwifionoff.localeplugin;

import android.content.Context;

/**
 * Class of constants used by this Locale plug-in.
 */
public final class Constants
{
    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private Constants()
    {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }

    /**
     * Log tag for logcat messages
     */
    // TODO: Change this to your application's own log tag
    public static final String LOG_TAG = "ALTitudeLocalePlugin:Constants"; //$NON-NLS-1$

    /**
     * Flag to enable logcat messages.
     */
    public static final boolean IS_LOGGABLE = true;

    /**
     * Flag to enable runtime checking of method parameters
     */
    public static final boolean IS_PARAMETER_CHECKING_ENABLED = false;

    /**
     * Flag to enable runtime checking of whether a method is called on the correct thread
     */
    public static final boolean IS_CORRECT_THREAD_CHECKING_ENABLED = false;

    /**
     * Determines the "versionCode" in the {@code AndroidManifest}.
     *
     * @param context to read the versionCode. Cannot be null.
     * @return versionCode of the app.
     * @throws IllegalArgumentException if {@code context} is null.
     */
    public static int getVersionCode(final Context context)
    {
        if (Constants.IS_PARAMETER_CHECKING_ENABLED)
        {
            if (null == context)
            {
                throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
            }
        }

        try
        {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        }
        catch (final UnsupportedOperationException e)
        {
            /*
             * This exception is thrown by test contexts
             */
            return 1;
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}