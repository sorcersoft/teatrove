/*
 * PropertyChangeEvent.java
 * 
 * Copyright (c) 2001 WDIG Corporation.  All Rights Reserved.
 * 
 * Original author: Sean T. Treat
 * 
 * $Workfile:: PropertyChangeEvent.java                                       $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.trove.util;

import com.go.trove.io.SourceInfo;
import java.util.EventObject;

/******************************************************************************
 * 
 * @author Sean T. Treat
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/06/01 <!-- $-->
 */
public class PropertyChangeEvent extends java.util.EventObject {

    private String mKey;
    private String mValue;
    private SourceInfo mInfo;

    public PropertyChangeEvent(Object source) {
        this(source, null, null, null);
    }

    public PropertyChangeEvent(Object source, String key, String value, 
                               SourceInfo info) {
        super(source);
        mKey = key;
        mValue = key;
        mInfo = info;
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mValue;
    }

    public SourceInfo getSourceInfo() {
        return mInfo;
    }
}
