/*
 * DynamicContextSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: DynamicContextSource.java                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

/******************************************************************************
 * Instances of this kind of ContextSource can support reloading their context
 * class. {@link TeaExecutionEngine Engines} that support context reload will
 * discover that the context class has changed by calling getContextType. This
 * call will likely be initiated by a template reload request, since all
 * templates may need to be recompiled if the context class has changed.
 * <p>
 * Because Tea engines may execute many templates concurrently, the two
 * argument createContext method is called to inform the context source
 * what specific context class is tied to the application request. The returned
 * context instance must be of the requested type. DynamicContextSources must
 * support instantiating the old context class(es) as well as the new context
 * class.
 * <p>
 * The context source may release old context classes to free resources only
 * when it is safe to do so. One way of achieving this is by using weak
 * references.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/01/04 <!-- $-->
 */
public interface DynamicContextSource extends ContextSource {

    /**
     * Creates a context instance that must be of the requested type.
     *
     * @param contextClass Expected type of returned context instance
     * @param param {@link TeaExecutionEngine engine} specific parameter that
     * may be used to create the context instance
     */
    public Object createContext(Class contextClass, Object param)
        throws Exception;
}

