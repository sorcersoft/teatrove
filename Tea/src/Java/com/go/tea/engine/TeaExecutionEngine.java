/*
 * TeaExecutionEngine.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TeaExecutionEngine.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

/******************************************************************************
 * Implementations of this interface will provide a simplified means of using 
 * tea in other programs.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/30/01 <!-- $-->
 */
public interface TeaExecutionEngine {

    public void init(TeaEngineConfig config);

    public TemplateSource getTemplateSource();   

    public void executeTemplate(String templateName, 
                                Object contextParameter,
                                Object[] templateParameters) 
        throws Exception;
}
