/*
 * TeaServletTemplate.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TeaServletTemplate.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import com.go.tea.runtime.Context;
import com.go.tea.runtime.TemplateLoader;
import com.go.tea.engine.TemplateSource;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public class TeaServletTemplate implements TemplateLoader.Template {

    private TemplateLoader.Template mTemplate;
    private TemplateSource mSource;

    public TeaServletTemplate(TemplateLoader.Template template,
                              TemplateSource source) {
        mTemplate = template;
        mSource = source;
    }

    public TemplateSource getTemplateSource() {
        return mSource;
    }

    public TemplateLoader getTemplateLoader() {
        return mTemplate.getTemplateLoader();
            }

    public String getName() {
        return mTemplate.getName();
            }

    public Class getTemplateClass() {
        return mTemplate.getTemplateClass();
            }

    public Class getContextType() {
        return mTemplate.getContextType();
            }

    public String[] getParameterNames() {
        return mTemplate.getParameterNames();
    }
        
    public Class[] getParameterTypes() {
        return mTemplate.getParameterTypes();
    }

    public void execute(Context context, Object[] parameters) 
        throws Exception
    {
        mTemplate.execute(context, parameters);
    }
}


