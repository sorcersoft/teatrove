/*
 * PropertyMapFactory.java
 * 
 * Copyright (c) 2000 GO.com. All Rights Reserved.
 * 
 * Original author: Brian S O'Neill
 * 
 * $Workfile:: PropertyMapFactory.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.trove.util;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/07/02 <!-- $-->
 */
public interface PropertyMapFactory {
    public PropertyMap createProperties() throws java.io.IOException;
    public PropertyMap createProperties(PropertyChangeListener listener) 
        throws java.io.IOException;
}
