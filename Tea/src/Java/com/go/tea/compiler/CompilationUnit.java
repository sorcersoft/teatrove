/* ====================================================================
 * Tea - Copyright (c) 1997-2000 Walt Disney Internet Group
 * ====================================================================
 * The Tea Software License, Version 1.1
 *
 * Copyright (c) 2000 Walt Disney Internet Group. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Walt Disney Internet Group (http://opensource.go.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "BeanDoc" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact opensource@dig.com.
 *
 * 5. Products derived from this software may not be called "Tea",
 *    "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
 *    "Kettle", "Trove" or "BeanDoc" appear in their name, without prior
 *    written permission of the Walt Disney Internet Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE WALT DISNEY INTERNET GROUP OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information about Tea, please see http://opensource.go.com/.
 */

package com.go.tea.compiler;

import com.go.tea.parsetree.Template;
import java.io.Reader;
import java.io.OutputStream;
import java.io.IOException;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/02/06 <!-- $-->
 */
public abstract class CompilationUnit implements ErrorListener {
    private String mName;
    private Compiler mCompiler;
    private Template mTree;

    private int mErrorCount;

    public CompilationUnit(String name, Compiler compiler) {
        mName = name;
        mCompiler = compiler;
    }

    public String getName() {
        return mName;
    }

    public String getShortName() {
        String name = getName();
        int index = name.lastIndexOf('.');
        if (index >= 0) {
            return name.substring(index + 1);
        }

        return name;
    }

    public Compiler getCompiler() {
        return mCompiler;
    }

    /**
     * Called when there is an error when compiling this CompilationUnit.
     */
    public void compileError(ErrorEvent e) {
        mErrorCount++;
    }

    /**
     * Returns the number of errors generated while compiling this
     * CompilationUnit.
     */
    public int getErrorCount() {
        return mErrorCount;
    }

    public Template getParseTree() {
        if (mTree == null && mCompiler != null) {
            return mCompiler.getParseTree(this);
        }
        return mTree;
    }

    public void setParseTree(Template tree) {
        mTree = tree;
    }

    /**
     * Current implementation returns only the same packages as the compiler.
     *
     * @see Compiler#getImportedPackages()
     */
    public final String[] getImportedPackages() {
        return mCompiler.getImportedPackages();
    }
   
    /**
     * Return the package name that this CompilationUnit should be compiled
     * into. Default implementation returns null, or no package.
     */
    public String getTargetPackage() {
        return null;
    }

    public abstract String getSourceFileName();

    /**
     * @return A new source file reader.
     */
    public abstract Reader getReader() throws IOException;

    /**
     * @return true if the CompilationUnit should be compiled. Default is true.
     */
    public boolean shouldCompile() throws IOException {
        return true;
    }

    /**
     * @return An OutputStream to write compiled code to. Returning null is
     * disables code generation for this CompilationUnit.
     */
    public abstract OutputStream getOutputStream() throws IOException;
}
