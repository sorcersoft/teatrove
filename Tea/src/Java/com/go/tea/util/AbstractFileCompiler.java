/*
 * AbstractFileCompiler.java
 * 
 * Copyright (c) 2000 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Brian S O'Neill
 * 
 * $Workfile:: AbstractFileCompiler.java                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.util;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Map;
import com.go.tea.compiler.Compiler;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/02/06 <!-- $-->
 */
public abstract class AbstractFileCompiler extends Compiler {
    protected AbstractFileCompiler() {
        super();
    }

    protected AbstractFileCompiler(Map parseTreeMap) {
        super(parseTreeMap);
    }

    /**
     * Recursively compiles all files in the source directory.
     *
     * @return The names of all the compiled sources
     */
    public String[] compileAll() throws IOException {
        return compile(getAllTemplateNames());
    }

    /**
     * Returns all sources (template names) available from the source
     * directory and in all sub-directories.
     */
    public abstract String[] getAllTemplateNames() throws IOException;
}
