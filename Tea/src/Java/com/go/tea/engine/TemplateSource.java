/*
 * TemplateSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TemplateSource.java                                            $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import java.util.Date;
import com.go.trove.util.ClassInjector;

/******************************************************************************
 * Implementations are responsible for providing compiled templates to an 
 * ApplicationDepot.  The context to compile against must be provided by the 
 * TemplateSourceConfig and a subset of available template sources may be 
 * provided to reduce the number of compilation issues.  Template classes from
 * either a precompiled library or earlier dynamic compilations may also be 
 * provided.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/18/02 <!-- $-->
 */
public interface TemplateSource {

    public void init(TemplateSourceConfig config) throws Exception;

    public boolean compileTemplates(ClassInjector sharedNeedle,
                                    boolean all) throws Exception;

    public int getKnownTemplateCount();

    public String[] getKnownTemplateNames();

    public String[] getReloadedTemplateNames();

    public Date getTimeOfLastReload();

    public boolean isExceptionGuardianEnabled();

    public boolean isSuccessful();

    public TemplateError[] getTemplateErrors();

    public Template[] getLoadedTemplates();

    public Template getTemplate(String name) 
        throws ClassNotFoundException, NoSuchMethodException;
    
    /**
     * @return the context source used internally for template compilation.
     */
    public ContextSource getContextSource();

}

