/*
 * LineTooLongException.java
 * 
 * Copyright (c) 2000 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Brian S O'Neill
 * 
 * $Workfile:: LineTooLongException.java                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.trove.net;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 00/12/06 <!-- $-->
 */
public class LineTooLongException extends java.io.IOException {
    public LineTooLongException(int limit) {
        super("> " + limit);
    }
}
