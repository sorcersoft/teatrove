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

package com.go.tea.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.OutputStream;
import java.util.Hashtable;
import com.go.trove.util.ClassInjector;
import com.go.tea.compiler.Compiler;
import com.go.tea.compiler.CompilationUnit;

/******************************************************************************
 * Simple compiler implementation that compiles a Tea template whose source
 * is in a String. Call {@link #setTemplateSource setTemplateSource} to
 * supply source code for templates before calling compile.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/22/01 <!-- $-->
 */
public class StringCompiler extends Compiler {

    private ClassInjector mInjector;
    private String mPackagePrefix;
    private Hashtable mTemplateSources;
    
    /**
     * @param injector ClassInjector to feed generated classes into
     */
    public StringCompiler(ClassInjector injector) {
        this(injector, null);
    }

    /**
     * @param injector ClassInjector to feed generated classes into
     * @param packagePrefix The target package for the compiled templates
     */
    public StringCompiler(ClassInjector injector, String packagePrefix) {
        super();
        mInjector = injector;
        mPackagePrefix = packagePrefix;
        mTemplateSources = new Hashtable();
    }

    public boolean sourceExists(String name) {
        return mTemplateSources.containsKey(name);
    }

    protected CompilationUnit createCompilationUnit(String name) {
        return new Unit(name, this);
    }

    /**
     * @param name The name of the template
     * @param source The source code for the template
     */
    public void setTemplateSource(String name, String source) {
        mTemplateSources.put(name, source);
    }

    private class Unit extends CompilationUnit {
        private String mSourceFileName;

        public Unit(String name, Compiler compiler) {
            super(name, compiler);

            mSourceFileName = 
                name.substring(name.lastIndexOf('.') + 1) + ".tea";
        }

        public String getSourceFileName() {
            return mSourceFileName;
        }
        
        public String getTargetPackage() {
            return mPackagePrefix;
        }
        
        public Reader getReader() throws IOException {
            String source = (String)mTemplateSources.get(getName());
            return new StringReader(source);
        }
        
        public OutputStream getOutputStream() throws IOException {
            String className = getName();
            String pack = getTargetPackage();
            if (pack != null && pack.length() > 0) {
                className = pack + '.' + className;
            }

            return mInjector.getStream(className);
        }
    }
}
