/*
 * ContextCreationException.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: ContextCreationException.java                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import java.lang.reflect.UndeclaredThrowableException;
import java.io.PrintStream;
import java.io.PrintWriter;

/******************************************************************************
 * Intended to wrap any exceptions thrown by createContext when used in the 
 * MergingContextSource so exceptions can pass through the getInstance() call
 * of MergedClass.InstanceFactory.  Calls are passed to the wrapped Exception
 * leaving this class as a transparent wrapper.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 11/27/01 <!-- $-->
 */
public class ContextCreationException extends UndeclaredThrowableException {

    public ContextCreationException(Exception e) {
        super(e);
    }

    public ContextCreationException(Exception e, String str) {
        super(e, str);
    }

    // overridden Throwable methods
    public String getMessage() {
        Throwable rock = getUndeclaredThrowable();
        if (rock != null) {
            return rock.getMessage();
        }
        else {
            return super.getMessage();
        }
    }

    // overridden UndeclaredThrowableException methods

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            Throwable rock = getUndeclaredThrowable();
            if (rock != null) {
                rock.printStackTrace(ps);
            } 
            else {
                super.printStackTrace(ps);
            }
        }
    }

    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            Throwable rock = getUndeclaredThrowable();
            if (rock != null) {
                rock.printStackTrace(pw);
            } 
            else {
                super.printStackTrace(pw);
            }
        }
    }
}
