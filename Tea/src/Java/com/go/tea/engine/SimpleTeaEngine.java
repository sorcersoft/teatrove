/*
 * SimpleTeaEngine.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: SimpleTeaEngine.java                                           $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import com.go.tea.runtime.Context;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/30/01 <!-- $-->
 */
public class SimpleTeaEngine implements TeaExecutionEngine {

    TemplateSource mTemplateSource;

    public void init(TeaEngineConfig config) {
        mTemplateSource = config.getTemplateSource();
    }

    public void executeTemplate(String templateName, 
                                Object contextParameter, 
                                Object[] templateParameters)
        throws Exception {
        
        mTemplateSource.getTemplate(templateName)
            .execute((Context)mTemplateSource.getContextSource()
                     .createContext(contextParameter), 
                     templateParameters);
    }

    public TemplateSource getTemplateSource() {
        return mTemplateSource;
    }
}
