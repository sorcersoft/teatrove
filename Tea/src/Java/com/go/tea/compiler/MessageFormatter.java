/* ====================================================================
 * Tea - Copyright (c) 1997-2000 Walt Disney Internet Group
 * ====================================================================
 * The Tea Software License, Version 1.1
 *
 * Copyright (c) 2000 Walt Disney Internet Group. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Walt Disney Internet Group (http://opensource.go.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "BeanDoc" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact opensource@dig.com.
 *
 * 5. Products derived from this software may not be called "Tea",
 *    "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
 *    "Kettle", "Trove" or "BeanDoc" appear in their name, without prior
 *    written permission of the Walt Disney Internet Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE WALT DISNEY INTERNET GROUP OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information about Tea, please see http://opensource.go.com/.
 */

package com.go.tea.compiler;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.text.MessageFormat;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
class MessageFormatter {
    // Maps Classes to MessageFormatters.
    private static Map cMessageFormatters;

    static {
        try {
            cMessageFormatters = new WeakHashMap(7);
        }
        catch (LinkageError e) {
            cMessageFormatters = new HashMap(7);
        }
        catch (Exception e) {
            // Microsoft VM sometimes throws an undeclared
            // ClassNotFoundException instead of doing the right thing and
            // throwing some form of a LinkageError if the class couldn't
            // be found.
            cMessageFormatters = new HashMap(7);
        }

    }

    public static MessageFormatter lookup(Object user)
        throws MissingResourceException
    {
        return lookup(user.getClass());
    }

    private static MessageFormatter lookup(Class clazz) {
        MessageFormatter formatter =
            (MessageFormatter)cMessageFormatters.get(clazz);
        if (formatter == null) {
            String className = clazz.getName();
            String resourcesName;
            int index = className.lastIndexOf('.');
            if (index >= 0) {
                resourcesName = className.substring(0, index + 1) +
                    "resources." + className.substring(index + 1);
            }
            else {
                resourcesName = "resources." + className;
            }
            try {
                formatter = new MessageFormatter
                    (ResourceBundle.getBundle(resourcesName));
            }
            catch (MissingResourceException e) {
                if (clazz.getSuperclass() == null) {
                    throw e;
                }
                try {
                    formatter = lookup(clazz.getSuperclass());
                }
                catch (MissingResourceException e2) {
                    throw e;
                }
            }
            cMessageFormatters.put(clazz, formatter);
        }
        return formatter;
    }

    private ResourceBundle mResources;

    private MessageFormatter(ResourceBundle resources) {
        mResources = resources;
    }

    public String format(String key) {
        String message = null;
        try {
            message = mResources.getString(key);
        }
        catch (MissingResourceException e) {
        }

        if (message != null) {
            return message;
        }
        else {
            return key;
        }
    }

    public String format(String key, String arg) {
        String message = null;
        try {
            message = mResources.getString(key);
        }
        catch (MissingResourceException e) {
        }

        if (message != null) {
            return MessageFormat.format(message, new String[] {arg});
        }
        else {
            return key + ": " + arg;
        }
    }

    public String format(String key, String arg1, String arg2) {
        String message = null;
        try {
            message = mResources.getString(key);
        }
        catch (MissingResourceException e) {
        }

        if (message != null) {
            return MessageFormat.format(message, new String[] {arg1, arg2});
        }
        else {
            return key + ": " + arg1 + ", " + arg2;
        }
    }

    public String format(String key, String arg1, String arg2, String arg3) {
        String message = null;
        try {
            message = mResources.getString(key);
        }
        catch (MissingResourceException e) {
        }

        if (message != null) {
            return MessageFormat.format(message,
                                        new String[] {arg1, arg2, arg3});
        }
        else {
            return key + ": " + arg1 + ", " + arg2 + ", " + arg3;
        }
    }
}
