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

import java.io.*;
import java.util.*;
import com.go.tea.compiler.Compiler;
import com.go.tea.compiler.*;
import com.go.tea.runtime.*;

/******************************************************************************
 * Command-line tool that puts the 'call' keyword in front of template calls
 * in templates compatable with pre 3.x.x versions of Tea.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class CallRetrofitter {
    /**
     * Entry point for a command-line tool that puts the 'call' keyword in
     * front of template calls in templates compatable with pre 3.x.x versions
     * of Tea.
     *
     * <pre>
     * Usage: java com.go.tea.util.CallRetrofitter {options} 
     * &lt;template root directory&gt; {templates}
     *
     * where {options} includes:
     * -context &lt;class&gt;     Specify a runtime context class to compile against.
     * -encoding &lt;encoding&gt; Specify character encoding used by source files.
     * </pre>
     */
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            usage();
            return;
        }

        Class context = null;
        String encoding = null;
        File rootDir = null;
        Collection templates = new ArrayList(args.length);

        try {
            boolean parsingOptions = true;
            for (int i=0; i<args.length;) {
                String arg = args[i++];
                if (arg.startsWith("-") && parsingOptions) {
                    if (arg.equals("-context") && context == null) {
                        context = Class.forName(args[i++]);
                        continue;
                    }
                    else if (arg.equals("-encoding") && encoding == null) {
                        encoding = args[i++];
                        continue;
                    }
                }
                else {
                    if (parsingOptions) {
                        parsingOptions = false;
                        rootDir = new File(arg);
                        continue;
                    }

                    arg = arg.replace('/', '.');
                    arg = arg.replace(File.separatorChar, '.');
                    while (arg.startsWith(".")) {
                        arg = arg.substring(1);
                    }
                    while (arg.endsWith(".")) {
                        arg = arg.substring(0, arg.length() - 1);
                    }
                    templates.add(arg);
                    continue;
                }
                
                usage();
                return;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            usage();
            return;
        }

        if (rootDir == null) {
            usage();
            return;
        }

        if (context == null) {
            context = com.go.tea.runtime.UtilityContext.class;
        }

        int errorCount, changeCount;
        int totalChangeCount = 0;
        do {
            FileCompiler compiler =
                new FileCompiler(rootDir, null, null, null, encoding);
            
            compiler.setRuntimeContext(context);
            compiler.setForceCompile(true);
            compiler.setCodeGenerationEnabled(false);
            
            Retrofitter retrofitter = new Retrofitter(compiler, encoding);
            compiler.addErrorListener(retrofitter);
            
            String[] names;
            if (templates.size() == 0) {
                names = compiler.compileAll(true);
            }
            else {
                names =
                    (String[])templates.toArray(new String[templates.size()]);
                names = compiler.compile(names);
            }
            
            retrofitter.applyChanges();
            
            changeCount = retrofitter.getChangeCount();
            errorCount = compiler.getErrorCount() - changeCount;
            
            String msg = String.valueOf(errorCount) + " error";
            if (errorCount != 1) {
                msg += 's';
            }
            System.out.println(msg);
            
            totalChangeCount += changeCount;
            System.out.println("Total changes made: " + totalChangeCount);
        } while (changeCount > 0);
    }

    private static void usage() {
        String usageDetail =
            " -context <class>     Specify a runtime context class to compile against.\n" +
            " -encoding <encoding> Specify character encoding used by source files.\n";

        System.out.print("\nUsage: ");
        System.out.print("java ");
        System.out.print(CallRetrofitter.class.getName());
        System.out.println(" {options} <template root directory> {templates}");
        System.out.println();
        System.out.println("where {options} includes:");
        System.out.println(usageDetail);
    }

    private static class Retrofitter extends DefaultContext
        implements ErrorListener
    {

        private Compiler mCompiler;
        private String mEncoding;

        // Maps source files to lists of ErrorEvents.
        private Map mChanges;

        private int mChangeCount;
        
        private Retrofitter(Compiler c, String encoding) {
            mCompiler = c;
            mEncoding = encoding;
            mChanges = new TreeMap();
        }
        
        public void compileError(ErrorEvent e) {
            if ("Can't find function".equalsIgnoreCase(e.getErrorMessage())) {
                File sourceFile = ((FileCompiler.Unit)e.getCompilationUnit())
                    .getSourceFile();
                List errorEvents = (List)mChanges.get(sourceFile);
                if (errorEvents == null) {
                    errorEvents = new ArrayList();
                    mChanges.put(sourceFile, errorEvents);
                }
                errorEvents.add(e);
            }
        }
        
        public int getChangeCount() {
            return mChangeCount;
        }
        
        public void applyChanges() throws IOException {
            Iterator it = mChanges.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                applyChanges((File)entry.getKey(), (List)entry.getValue());
            }
            mChanges = null;
        }

        private void applyChanges(File sourceFile, List errorEvents)
            throws IOException
        {
            RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");

            Map replacements = new HashMap();

            Iterator it = errorEvents.iterator();
            while (it.hasNext()) {
                ErrorEvent event = (ErrorEvent)it.next();
                SourceInfo info = event.getSourceInfo();
                CompilationUnit sourceUnit = event.getCompilationUnit();

                int startPos = info.getStartPosition();
                int endPos = info.getEndPosition();
                int len = (endPos - startPos) + 1;

                byte[] bytes = new byte[len];
                raf.seek(startPos);
                raf.readFully(bytes);

                String text;
                if (mEncoding == null) {
                    text = new String(bytes);
                }
                else {
                    text = new String(bytes, mEncoding);
                }

                int index = text.indexOf('(');
                if (index > 0) {
                    String templateName = text.substring(0, index).trim();
                    boolean templateExists = mCompiler.getCompilationUnit
                        (templateName, sourceUnit) != null;
                    if (templateExists) {
                        mChangeCount++;
                        replacements.put(text, "call " + text);
                    }
                }
            }

            raf.close();

            if (replacements.size() <= 0) {
                return;
            }

            print("Modifying: " + sourceFile);

            InputStream in = new FileInputStream(sourceFile);

            Reader reader;
            if (mEncoding == null) {
                reader = new InputStreamReader(in);
            }
            else {
                reader = new InputStreamReader(in, mEncoding);
            }

            reader = new BufferedReader(reader);

            StringBuffer sourceContents =
                new StringBuffer((int)sourceFile.length());

            int c;
            while ((c = reader.read()) != -1) {
                sourceContents.append((char)c);
            }
            reader.close();

            String newContents =
                replace(sourceContents.toString(), replacements);

            OutputStream out = new FileOutputStream(sourceFile);

            Writer writer;
            if (mEncoding == null) {
                writer = new OutputStreamWriter(out);
            }
            else {
                writer = new OutputStreamWriter(out, mEncoding);
            }

            writer = new BufferedWriter(writer);
            writer.write(newContents);
            writer.close();
        }

        public void print(Object obj) {
            System.out.println(toString(obj));
        }
    }
}
