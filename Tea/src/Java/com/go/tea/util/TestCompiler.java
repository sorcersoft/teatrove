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

import java.lang.reflect.*;
import java.io.*;
import com.go.tea.compiler.*;
import com.go.tea.io.*;
import com.go.tea.runtime.*;

/******************************************************************************
 * A compiler implementation suitable for testing from a command console.
 * The runtime context is a PrintStream so that template output can go to
 * standard out.
 *
 * <p>Templates are read from files that must have the extension ".tea". The
 * code generated are Java class files which are written in the same directory
 * as the source files. Compilation error messages are sent to standard out.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class TestCompiler extends FileCompiler {
    public static void main(String[] args) throws Exception {
        File dir = new File(".");
        TestCompiler tc = new TestCompiler(dir, null, dir);
        tc.addErrorListener(new ConsoleErrorReporter(System.out));
        tc.setForceCompile(true);
        String[] names = tc.compile(args[0]);

        System.out.println("Compiled " + names.length + " sources");
        for (int i=0; i<names.length; i++){
            System.out.println(names[i]);
        }

        int errorCount = tc.getErrorCount();
        if (errorCount > 0) {
            String msg = String.valueOf(errorCount) + " error";
            if (errorCount != 1) {
                msg += 's';
            }
            System.out.println(msg);
            return;
        }

        TemplateLoader loader = new TemplateLoader();
        TemplateLoader.Template template = loader.getTemplate(args[0]);

        int length = args.length - 1;
        Object[] params = new Object[length];
        for (int i=0; i<length; i++) {
            params[i] = args[i + 1];
        }

        System.out.println("Executing " + template);

        template.execute(new Context(System.out), params);
    }

    public TestCompiler(File rootSourceDir, 
                        String rootPackage,
                        File rootDestDir) {

        super(rootSourceDir, rootPackage, rootDestDir, null);
    }

    public static class Context extends DefaultContext {
        private PrintStream mOut;

        public Context(PrintStream out) {
            super();
            mOut = out;
        }

        public void print(Object obj) {
            mOut.print(toString(obj));
        }
    }
}
