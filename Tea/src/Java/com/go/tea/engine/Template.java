/*
 * Template.java
 * 
 * Copyright (c) 2002 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: Template.java                                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import com.go.tea.runtime.TemplateLoader;

/******************************************************************************
 * While Templates already have access to the loader that loaded them, this
 * interface enables them to gain access to their TemplateSource.
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface Template extends TemplateLoader.Template {
    
    /**
     * provides a reference to this Template's TemplateSource.
     */
    public TemplateSource getTemplateSource();
}
