/*
 * TemplateErrorListener.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TemplateErrorListener.java                                     $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import com.go.tea.compiler.ErrorListener;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 11/14/01 <!-- $-->
 */
public interface TemplateErrorListener extends ErrorListener {

    public TemplateError[] getTemplateErrors();
}
