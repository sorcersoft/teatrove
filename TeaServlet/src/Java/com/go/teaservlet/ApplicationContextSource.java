/*
 * ApplicationContextSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: ApplicationContextSource.java                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet; 

import com.go.tea.engine.DynamicContextSource;

/******************************************************************************
 * Allows an Application into masquerade as a ContextSource.
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/01/02 <!-- $-->
 */
public class ApplicationContextSource implements DynamicContextSource {

    private Application mApp;
    private boolean mContextTypeMayChange;

    public ApplicationContextSource(Application app) {
        mApp = app;
        // storing this saves an instanceof call for every hit.
        mContextTypeMayChange = (app instanceof DynamicContextSource);
    }

    /**
     * @return the Class of the object returned by createContext.
     */
    public Class getContextType() {
        return mApp.getContextType();
    }

    public Object createContext(Object param) throws Exception {
        RequestAndResponse rar;
        if (param != null) {
            rar = (RequestAndResponse) param;
        } else {
            rar = new RequestAndResponse();
        }
        return mApp.createContext(rar.getRequest(), rar.getResponse());
    }

    public Object createContext(Class clazz, Object param) throws Exception {
        if (mContextTypeMayChange) {
            return ((DynamicContextSource)mApp).createContext(clazz, param);
        }
        return createContext(param);
    }
}


