/*
 * TeaEngineConfig.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TeaEngineConfig.java                                           $
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
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/30/01 <!-- $-->
 */
public interface TeaEngineConfig extends Config {
    
    public TemplateSource getTemplateSource();   

}
