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
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.go.trove.util.ClassInjector;
import com.go.trove.io.DualOutput;
import com.go.tea.compiler.Compiler;
import com.go.tea.compiler.CompilationUnit;

/******************************************************************************
 * FileCompiler compiles tea source files by reading them from a file or a
 * directory. The compiled code can be written as class files to a given
 * destination directory, they can be passed to a ClassInjector, or they
 * can be sent to both.
 *
 * <p>When given a directory, FileCompiler compiles all files with the
 * extension ".tea". If a destination directory is used, tea files that have a
 * matching class file that is more up-to-date will not be compiled, unless
 * they are forced to be re-compiled.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$--> 21 <!-- $$JustDate:--> 01/02/06 <!-- $-->
 * @see ClassInjector
 */
public class FileCompiler extends AbstractFileCompiler {
    /**
     * Entry point for a command-line tool suitable for compiling Tea
     * templates to be bundled with a product. Templates are read from files
     * that must have the extension ".tea", and any compilation error messages
     * are sent to standard out.
     *
     * <pre>
     * Usage: java com.go.tea.util.FileCompiler {options} 
     * &lt;template root directory&gt; {templates}
     *
     * where {options} includes:
     * -context &lt;class&gt;     Specify a runtime context class to compile against.
     * -dest &lt;directory&gt;    Specify where to place generated class files.
     * -force               Compile all templates, even if up-to-date.
     * -package &lt;package&gt;   Root package to compile templates into.
     * -encoding &lt;encoding&gt; Specify character encoding used by source files.
     * -guardian            Enable the exception guardian.
     * </pre>
     */
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            usage();
            return;
        }

        Class context = null;
        File destDir = null;
        boolean force = false;
        String rootPackage = null;
        String encoding = null;
        boolean guardian = false;
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
                    else if (arg.equals("-dest") && destDir == null) {
                        destDir = new File(args[i++]);
                        continue;
                    }
                    else if (arg.equals("-force") && !force) {
                        force = true;
                        continue;
                    }
                    else if (arg.equals("-package") && rootPackage == null) {
                        rootPackage = args[i++];
                        continue;
                    }
                    else if (arg.equals("-encoding") && encoding == null) {
                        encoding = args[i++];
                        continue;
                    }
                    else if (arg.equals("-guardian") && !guardian) {
                        guardian = true;
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

        if (destDir == null) {
            destDir = rootDir;
        }

        FileCompiler compiler =
            new FileCompiler(rootDir, rootPackage, destDir, null, encoding);

        compiler.setRuntimeContext(context);
        compiler.setForceCompile(force);
        compiler.addErrorListener(new ConsoleErrorReporter(System.out));
        compiler.setExceptionGuardianEnabled(guardian);

        String[] names;
        if (templates.size() == 0) {
            names = compiler.compileAll(true);
        }
        else {
            names = (String[])templates.toArray(new String[templates.size()]);
            names = compiler.compile(names);
        }

        int errorCount = compiler.getErrorCount();

        if (errorCount > 0) {
            String msg = String.valueOf(errorCount) + " error";
            if (errorCount != 1) {
                msg += 's';
            }
            System.out.println(msg);
            System.exit(1);
        }
    }

    private static void usage() {
        String usageDetail =
            " -context <class>     Specify a runtime context class to compile against.\n" +
            " -dest <directory>    Specify where to place generated class files.\n" +
            " -force               Compile all templates, even if up-to-date.\n" +
            " -package <package>   Root package to compile templates into.\n" +
            " -encoding <encoding> Specify character encoding used by source files.\n" +
            " -guardian            Enable the exception guardian.";

        System.out.print("\nUsage: ");
        System.out.print("java ");
        System.out.print(FileCompiler.class.getName());
        System.out.println(" {options} <template root directory> {templates}");
        System.out.println();
        System.out.println("where {options} includes:");
        System.out.println(usageDetail);
    }

    private File[] mRootSourceDirs;
    private String mRootPackage;
    private File mRootDestDir;
    private ClassInjector mInjector;
    private String mEncoding;
    private boolean mForce = false;
    
    /**
     * @param rootSourceDir Required root source directory
     * @param rootPackage Optional root package to compile source to
     * @param rootDestDir Optional directory to place generated class files
     * @param injector Optional ClassInjector to feed generated classes into
     */
    public FileCompiler(File rootSourceDir,
                        String rootPackage,
                        File rootDestDir,
                        ClassInjector injector) {
        this(new File[]{rootSourceDir}, rootPackage, rootDestDir, injector, null);
    }

    /**
     * @param rootSourceDir Required root source directory
     * @param rootPackage Optional root package to compile source to
     * @param rootDestDir Optional directory to place generated class files
     * @param injector Optional ClassInjector to feed generated classes into
     * @param encoding Optional character encoding used by source files
     */
    public FileCompiler(File rootSourceDir,
                        String rootPackage,
                        File rootDestDir,
                        ClassInjector injector,
                        String encoding) {
        this(new File[]{rootSourceDir}, rootPackage, rootDestDir, injector, encoding);
    }

    /**
     * @param rootSourceDirs Required root source directories
     * @param rootPackage Optional root package to compile source to
     * @param rootDestDir Optional directory to place generated class files
     * @param injector Optional ClassInjector to feed generated classes into
     */
    public FileCompiler(File[] rootSourceDirs,
                        String rootPackage,
                        File rootDestDir,
                        ClassInjector injector) {
        this(rootSourceDirs, rootPackage, rootDestDir, injector, null);
    }

    /**
     * @param rootSourceDirs Required root source directories
     * @param rootPackage Optional root package to compile source to
     * @param rootDestDir Optional directory to place generated class files
     * @param injector Optional ClassInjector to feed generated classes into
     * @param encoding Optional character encoding used by source files
     */
    public FileCompiler(File[] rootSourceDirs,
                        String rootPackage,
                        File rootDestDir,
                        ClassInjector injector,
                        String encoding) {
        super();
        init(rootSourceDirs, rootPackage, rootDestDir, injector, encoding);
    }

    /**
     * @param rootSourceDirs Required root source directories
     * @param rootPackage Optional root package to compile source to
     * @param rootDestDir Optional directory to place generated class files
     * @param injector Optional ClassInjector to feed generated classes into
     * @param encoding Optional character encoding used by source files
     * @param parseTreeMap Optional map should be thread-safe. See
     * {@link Compiler} for details.
     */
    public FileCompiler(File[] rootSourceDirs,
                        String rootPackage,
                        File rootDestDir,
                        ClassInjector injector,
                        String encoding,
                        Map parseTreeMap) {
        super((parseTreeMap == null) ?
              Collections.synchronizedMap(new HashMap()) : parseTreeMap);
        init(rootSourceDirs, rootPackage, rootDestDir, injector, encoding);
    }

    private void init(File[] rootSourceDirs,
                      String rootPackage,
                      File rootDestDir,
                      ClassInjector injector,
                      String encoding) {
        mRootSourceDirs = (File[])rootSourceDirs.clone();
        mRootPackage = rootPackage;
        mRootDestDir = rootDestDir;
        mInjector = injector;
        mEncoding = encoding;

        for (int i=0; i<rootSourceDirs.length; i++) {
            if (!rootSourceDirs[i].isDirectory()) {
                throw new IllegalArgumentException
                    ("Source location is not a directory: " + 
                     rootSourceDirs[i]);
            }
        }

        if (rootDestDir != null && 
            !rootDestDir.isDirectory()) {
            throw new IllegalArgumentException
                ("Destination is not a directory: " + rootDestDir);
        }
    }

    /**
     * @param force When true, compile all source, even if up-to-date
     */
    public void setForceCompile(boolean force) {
        mForce = force;
    }

    /**
     * Compiles all files in the source directory.
     *
     * @param recurse When true, recursively compiles all files and directories
     *
     * @return The names of all the compiled sources
     */
    public String[] compileAll(boolean recurse) throws IOException {
        return compile(getAllTemplateNames(recurse));
    }

    public String[] getAllTemplateNames() throws IOException {
        return getAllTemplateNames(true);
    }

    private String[] getAllTemplateNames(boolean recurse) throws IOException {
        // Using a Set to prevent duplicate template names.
        Collection sources = new TreeSet();

        for (int i=0; i<mRootSourceDirs.length; i++) {
            gatherSources(sources, mRootSourceDirs[i], recurse);
        }

        return (String[])sources.toArray(new String[sources.size()]);
    }
    
    public boolean sourceExists(String name) {
        return findRootSourceDir(name) != null;
    }

    /**
     * Gathers all sources (template names) in the source directory.
     *
     * @param templateNames Collection of Strings. The gatherSources method 
     * will add the template names to this Collection.  
     * @param sourceDir the root source directory
     * @param recurse When true, recursively gathers all sources in 
     * sub-directories.
     */
    private void gatherSources(Collection templateNames,
                               File sourceDir, 
                               boolean recurse)
        throws IOException
    {
        gatherSources(templateNames, sourceDir, null, recurse);
    }


    private void gatherSources(Collection toCompile,
                               File sourceDir, 
                               String parentName, 
                               boolean recurse)
        throws IOException
    {
        String[] list = sourceDir.list();
        if (list != null) {
            for (int i=0; i<list.length; i++) {
                File file = new File(sourceDir, list[i]);
                if (file.isDirectory()) {
                    if (recurse) {
                        String name = file.getName();

                        if (parentName != null) {
                            name = parentName + '.' + name;
                        }

                        gatherSources(toCompile, file, name, recurse);
                    }
                }
                else if (file.getName().endsWith(".tea")) {
                    String name = file.getName();
                    int index = name.lastIndexOf('.');
                    name = name.substring(0, index);
                    
                    if (parentName != null) {
                        name = parentName + '.' + name;
                    }

                    toCompile.add(name);
                }
            }
        }

        return;
    }

    /**
     * Always returns an instance of FileCompiler.Unit. Any errors reported
     * by the compiler that have a reference to a CompilationUnit will have
     * been created by this factory method. Casting this to FileCompiler.Unit
     * allows error reporters to access the source file via the getSourceFile
     * method.
     *
     * @see FileCompiler.Unit#getSourceFile
     */
    protected CompilationUnit createCompilationUnit(String name) {
        return new Unit(name, this);
    }

    private File findRootSourceDir(String name) {
        String fileName = name.replace('.', File.separatorChar) + ".tea";

        for (int i=0; i<mRootSourceDirs.length; i++) {
            File file = new File(mRootSourceDirs[i], fileName);
            if (file.exists()) {
                return mRootSourceDirs[i];
            }
        }

        return null;
    }

    public class Unit extends CompilationUnit {
        private final String mSourceFileName;
        private final File mSourceFile;
        private final File mDestFile;

        Unit(String name, Compiler compiler) {
            super(name, compiler);

            File rootSourceDir = findRootSourceDir(name);
            if (rootSourceDir == null) {
                // File isn't found, but set to a valid directory so that error
                // is produced later when attempting to get a Reader.
                rootSourceDir = mRootSourceDirs[0];
            }

            String fname = name.replace('.', '/');

            mSourceFileName = fname + ".tea";
            mSourceFile = new File(rootSourceDir, mSourceFileName);
            
            if (mRootDestDir == null) {
                mDestFile = null;
            }
            else {
                mDestFile = new File(mRootDestDir, fname + ".class");
            }
        }

        public String getTargetPackage() {
            return mRootPackage;
        }
        
        public String getSourceFileName() {
            return mSourceFileName;
        }
        
        public File getSourceFile() {
            return mSourceFile;
        }

        public Reader getReader() throws IOException {
            InputStream in = new FileInputStream(mSourceFile);
            if (mEncoding == null) {
                return new InputStreamReader(in);
            }
            else {
                return new InputStreamReader(in, mEncoding);
            }
        }
        
        public boolean shouldCompile() {
            if (!mForce &&
                mDestFile != null &&
                mDestFile.exists() &&
                mDestFile.lastModified() >= mSourceFile.lastModified()) {

                return false;
            }

            return true;
        }

        /**
         * @return the file that gets written by the compiler.
         */
        public File getDestinationFile() {
            return mDestFile;
        }

        public OutputStream getOutputStream() throws IOException {
            OutputStream out1 = null;
            OutputStream out2 = null;

            if (mDestFile != null) {
                File dir = mDestFile.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                out1 = new FileOutputStream(mDestFile);
            }

            if (mInjector != null) {
                String className = getName();
                String pack = getTargetPackage();
                if (pack != null && pack.length() > 0) {
                    className = pack + '.' + className;
                }
                out2 = mInjector.getStream(className);
            }

            OutputStream out;

            if (out1 != null) {
                if (out2 != null) {
                    out = new DualOutput(out1, out2);
                }
                else {
                    out = out1;
                }
            }
            else if (out2 != null) {
                out = out2;
            }
            else {
                out = new OutputStream() {
                    public void write(int b) {}
                    public void write(byte[] b, int off, int len) {}
                };
            }

            return new BufferedOutputStream(out);
        }
    }
}
