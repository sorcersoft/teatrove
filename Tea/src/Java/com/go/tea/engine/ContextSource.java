/*
 * ContextSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: ContextSource.java                                             $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

/******************************************************************************
 * Implementations of this class are responsible for providing context 
 * instances as well as the context type.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/01/04 <!-- $-->
 * @see com.go.tea.runtime.Context
 */
public interface ContextSource {

    /**
     * @return the Class of the object returned by createContext.
     */
    public Class getContextType() throws Exception;

    /**
     * A generic method to create context instances.
     *
     * @param param {@link TeaExecutionEngine engine} specific parameter that
     * may be used to create the context instance
     */
    public Object createContext(Object param) throws Exception;
}
