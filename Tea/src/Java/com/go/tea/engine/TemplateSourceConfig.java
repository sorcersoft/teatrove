/*
 * TemplateSourceConfig.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TemplateSourceConfig.java                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import com.go.trove.util.Config;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 11/14/01 <!-- $-->
 */
public interface TemplateSourceConfig extends Config {

    public ContextSource getContextSource();

    public String getPackagePrefix();

    public boolean isExceptionGuardianEnabled();
}
