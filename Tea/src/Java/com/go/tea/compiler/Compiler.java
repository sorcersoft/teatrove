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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.go.trove.io.SourceReader;
import com.go.tea.parsetree.*;

/******************************************************************************
 * The Tea compiler. This class is abstract, and a few concrete
 * implementations can be found in the com.go.tea.util package.
 *
 * <p>A Compiler instance should be used for only one "build" because
 * some information is cached internally like parse trees and error count.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/08/16 <!-- $-->
 * @see com.go.tea.util.FileCompiler
 * @see com.go.tea.util.ResourceCompiler
 */
public abstract class Compiler {
    // Maps qualified names to ParseTrees.
    final Map mParseTreeMap;

    // Maps qualified names to CompilationUnits.
    private final Map mCompilationUnitMap = new HashMap();

    // Set of names for CompilationUnits that have already been compiled.
    private final Set mCompiled = new HashSet();

    private Set mPreserveTree;

    private Class mContextClass = com.go.tea.runtime.UtilityContext.class;
    private Method[] mRuntimeMethods;
    private Method[] mStringConverters;

    private ErrorListener mErrorListener;

    private Vector mErrorListeners = new Vector(4);
    private int mErrorCount = 0;

    private Vector mStatusListeners = new Vector();

    private boolean mGenerateCode = true;
    private boolean mExceptionGuardian = false;

    private ClassLoader mClassLoader;

    private MessageFormatter mFormatter;

    public Compiler() {
        this(Collections.synchronizedMap(new HashMap()));
    }

    /**
     * This constructor allows template signatures to be shared among compiler
     * instances. This is useful in interactive environments, where compilation
     * is occurring on a regular basis, but most called templates are not
     * being modified. The Compiler will map qualified template names to
     * ParseTree objects that have their code removed. Removing a template
     * entry from the map will force the compiler to re-parse the template if
     * it is called. Any template passed into the compile method will always be
     * re-parsed, even if its parse tree is already present in the map.
     *
     * @param parseTreeMap map should be thread-safe
     */
    public Compiler(Map parseTreeMap) {
        mParseTreeMap = parseTreeMap;
        mErrorListener = new ErrorListener() {
            public void compileError(ErrorEvent e) {
                dispatchCompileError(e);
            }
        };
        mFormatter = MessageFormatter.lookup(this);
    }

    /**
     * Add an ErrorListener in order receive events of compile-time errors.
     * @see com.go.tea.util.ConsoleErrorReporter
     */
    public void addErrorListener(ErrorListener listener) {
        mErrorListeners.addElement(listener);
    }

    public void removeErrorListener(ErrorListener listener) {
        mErrorListeners.removeElement(listener);
    }

    private void dispatchCompileError(ErrorEvent e) {
        mErrorCount++;

        synchronized (mErrorListeners) {
            for (int i = 0; i < mErrorListeners.size(); i++) {
                ((ErrorListener)mErrorListeners.elementAt(i)).compileError(e);
            }
        }
    }


    /**
     * Add a StatusListener in order to receive events of compilation progress.
     */
    public void addStatusListener(StatusListener listener) {
        mStatusListeners.addElement(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        mStatusListeners.removeElement(listener);
    }

    private void dispatchCompileStatus(StatusEvent e) {
        synchronized (mStatusListeners) {
            for(int i = 0; i < mStatusListeners.size(); i++) {
                ((StatusListener)mStatusListeners.elementAt(i)).
                    statusUpdate(e);
            }
        }
    }

    private void uncaughtException(Exception e) {
        Thread t = Thread.currentThread();
        t.getThreadGroup().uncaughtException(t, e);
    }

    /**
     * By default, code generation is enabled. Passing false disables the
     * code generation phase of the compiler.
     */
    public void setCodeGenerationEnabled(boolean flag) {
        mGenerateCode = flag;
    }

    /**
     * Returns true if code generation is enabled. The default setting is true.
     */
    public boolean isCodeGenerationEnabled() {
        return mGenerateCode;
    }

    public void setExceptionGuardianEnabled(boolean flag) {
        mExceptionGuardian = flag;
    }

    /**
     * Returns true if the exception guardian is enabled. The default setting
     * is false.
     */
    public boolean isExceptionGuardianEnabled() {
        return mExceptionGuardian;
    }

    /**
     * Sets the ClassLoader to use to load classes with. If set to null,
     * then classes are loaded using Class.forName.
     */
    public void setClassLoader(ClassLoader loader) {
        mClassLoader = loader;
    }

    /**
     * Returns the ClassLoader used by the Compiler, or null if none set.
     */
    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    /**
     * Loads and returns a class by the fully qualified name given. If a
     * ClassLoader is specified, it is used to load the class. Otherwise,
     * the class is loaded via Class.forName.
     *
     * @see #setClassLoader(ClassLoader)
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        while (true) {
            try {
                if (mClassLoader == null) {
                    return Class.forName(name);
                }
                else {
                    return mClassLoader.loadClass(name);
                }
            }
            catch (ClassNotFoundException e) {
                int index = name.lastIndexOf('.');
                if (index < 0) {
                    throw e;
                }

                // Search for inner class.
                name = name.substring(0, index) + '$' +
                    name.substring(index + 1);
            }
        }
    }

    /**
     * After a template is compiled, all but the root node of its parse tree
     * is clipped, in order to save memory. Applications that wish to traverse
     * CompilationUnit parse trees should call this method to preserve them.
     * This method must be called prior to compilation and prior to requesting
     * a parse tree from a CompilationUnit.
     *
     * @param name fully qualified name of template whose parse tree is to be
     * preserved.
     */
    public void preserveParseTree(String name) {
        if (mPreserveTree == null) {
            mPreserveTree = new HashSet();
        }
        mPreserveTree.add(name);
    }

    /**
     * Compile a single compilation unit. This method can be called multiple
     * times, but it will not compile compilation units that have already been
     * compiled.
     *
     * @param name the fully qualified template name
     *
     * @return The names of all the sources compiled by this compiler
     * @exception IOException
     */
    public String[] compile(String name) throws IOException {
        return compile(new String[] {name});
    }

    /**
     * Compile a list of compilation units. This method can be called multiple
     * times, but it will not compile compilation units that have already been
     * compiled.
     *
     * @param names an array of fully qualified template names
     *
     * @return The names of all the sources compiled by this compiler
     * @exception IOException
     */
    public String[] compile(String[] names) throws IOException {
        synchronized (mParseTreeMap) {
            for (int i=0; i<names.length; i++) {
                if(Thread.interrupted()) {
                    break;
                }
                dispatchCompileStatus(new StatusEvent(this, i, names.length, 
                                                      names[i]));
                CompilationUnit unit = getCompilationUnit(names[i], null);
                if (unit == null) {
                    String msg = mFormatter.format("not.found", names[i]);
                    dispatchCompileError
                        (new ErrorEvent(this, msg, (SourceInfo)null, null));
                }
                else if (!mCompiled.contains(names[i]) &&
                         unit.shouldCompile()) {
                    mParseTreeMap.remove(names[i]);
                    getParseTree(unit);
                }
            }       
        }
    
        names = new String[mCompiled.size()];
        Iterator it = mCompiled.iterator();
        int i = 0;
        while (it.hasNext()) {
            names[i++] = (String)it.next();
        }
        
        return names;
    }

    public int getErrorCount() {
        return mErrorCount;
    }

    /**
     * Returns a compilation unit associated with the given name, or null if
     * not found.
     *
     * @param name the requested name
     * @param from optional CompilationUnit is passed because requested name
     * should be found relative to it.
     */
    public CompilationUnit getCompilationUnit(String name, 
                                              CompilationUnit from) {
        name = determineQualifiedName(name, from);

        if (name == null) {
            return null;
        }

        CompilationUnit unit = (CompilationUnit)mCompilationUnitMap.get(name);
        if (unit == null) {
            unit = createCompilationUnit(name);
            if (unit != null) {
                mCompilationUnitMap.put(name, unit);
            }
        }

        return unit;
    }

    /**
     * Returns the list of imported packages that all templates have. This
     * always returns "java.lang" and "java.util". Template parameters can
     * abbreviate the names of all classes in java.lang and java.util.
     */
    public static final String[] getImportedPackages() {
        return new String[] {"java.lang", "java.util"};
    }

    /**
     * Return a class that defines a template's runtime context. The runtime
     * context contains methods that are callable by templates. A template
     * is compiled such that the first parameter of its execute method must
     * be an instance of the runtime context.
     *
     * <p>Default implementation returns
     * com.go.tea.runtime.UtilityContext.
     *
     * @see com.go.tea.runtime.UtilityContext
     */
    public Class getRuntimeContext() {
        return mContextClass;
    }

    /**
     * Call to override the default runtime context class that a template is
     * compiled to use.
     *
     * @see com.go.tea.runtime.Context
     */
    public void setRuntimeContext(Class contextClass) {
        mContextClass = contextClass;
        mRuntimeMethods = null;
        mStringConverters = null;
    }

    /**
     * Returns all the methods available in the runtime context.
     */
    public final Method[] getRuntimeContextMethods() {
        if (mRuntimeMethods == null) {
            mRuntimeMethods = getRuntimeContext().getMethods();
        }

        return (Method[])mRuntimeMethods.clone();
    }

    /**
     * Return the name of a method in the runtime context to bind to for 
     * receiving objects emitted by templates. The compiler will bind to the 
     * closest matching public method based on the type of its single 
     * parameter.
     *
     * <p>Default implementation returns "print".
     */
    public String getRuntimeReceiver() {
        return "print";
    }

    /**
     * Return the name of a method in the runtime context to bind to for 
     * converting objects and primitives to strings. The compiler will bind to 
     * the closest matching public method based on the type of its single 
     * parameter.
     *
     * <p>Default implementation returns "toString". Returning null indicates 
     * that a static String.valueOf method should be invoked.
     */
    public String getRuntimeStringConverter() {
        return "toString";
    }

    /**
     * Returns the set of methods that are used to perform conversion to
     * strings. The compiler will bind to the closest matching method based
     * on its parameter type.
     */
    public final Method[] getStringConverterMethods() {
        if (mStringConverters == null) {
            String name = getRuntimeStringConverter();

            Vector methods = new Vector();

            if (name != null) {
                Method[] contextMethods = getRuntimeContextMethods();
                for (int i=0; i<contextMethods.length; i++) {
                    Method m = contextMethods[i];
                    if (m.getName().equals(name) &&
                        m.getReturnType() == String.class &&
                        m.getParameterTypes().length == 1) {

                        methods.addElement(m);
                    }
                }
            }

            int customSize = methods.size();

            Method[] stringMethods = String.class.getMethods();
            for (int i=0; i<stringMethods.length; i++) {
                Method m = stringMethods[i];
                if (m.getName().equals("valueOf") &&
                    m.getReturnType() == String.class &&
                    m.getParameterTypes().length == 1 &&
                    Modifier.isStatic(m.getModifiers())) {

                    // Don't add to list if a custom converter already handles
                    // this method's parameter type.
                    Class type = m.getParameterTypes()[0];
                    int j;
                    for (j=0; j<customSize; j++) {
                        Method cm = (Method)methods.elementAt(j);
                        if (cm.getParameterTypes()[0] == type) {
                            break;
                        }
                    }

                    if (j == customSize) {
                        methods.addElement(m);
                    }
                }
            }

            mStringConverters = new Method[methods.size()];
            methods.copyInto(mStringConverters);
        }
        
        return (Method[])mStringConverters.clone();
    }

    /**
     * Given a name, as requested by the given CompilationUnit, return a
     * fully qualified name or null if the name could not be found.
     *
     * @param name requested name
     * @param from optional CompilationUnit
     */
    private String determineQualifiedName(String name, CompilationUnit from) {
        if (from != null) {
            // Determine qualified name as being relative to "from"

            String fromName = from.getName();
            int index = fromName.lastIndexOf('.');
            if (index >= 0) {
                String qual = fromName.substring(0, index + 1) + name;
                if (sourceExists(qual)) {
                    return qual;
                }
            }
        }

        if (sourceExists(name)) {
            return name;
        }

        return null;
    }

    /**
     * @return true if source exists for the given qualified name
     */
    public abstract boolean sourceExists(String name);

    protected abstract CompilationUnit createCompilationUnit(String name);

    /**
     * Default implementation returns a SourceReader that uses "<%" and "%>" 
     * as code delimiters.
     */
    protected SourceReader createSourceReader(CompilationUnit unit)
        throws IOException {

        Reader r = new BufferedReader(unit.getReader());
        return new SourceReader(r, "<%", "%>");
    }

    protected Scanner createScanner(SourceReader reader, CompilationUnit unit) 
        throws IOException {

        return new Scanner(reader, unit);
    }

    protected Parser createParser(Scanner scanner, CompilationUnit unit) 
        throws IOException {

        return new Parser(scanner, unit);
    }

    protected TypeChecker createTypeChecker(CompilationUnit unit) {
        TypeChecker tc = new TypeChecker(unit);
        tc.setClassLoader(getClassLoader());
        tc.setExceptionGuardianEnabled(isExceptionGuardianEnabled());
        return tc;
    }

    /**
     * Default implementation returns a new JavaClassGenerator.
     *
     * @see JavaClassGenerator
     */
    protected CodeGenerator createCodeGenerator(CompilationUnit unit) 
        throws IOException {
        
        return new JavaClassGenerator(unit);
    }

    /**
     * Called by the Compiler or by a CompilationUnit when its parse tree is 
     * requested. Requesting a parse tree may cause template code to be
     * generated.
     */
    Template getParseTree(CompilationUnit unit) {
        synchronized (mParseTreeMap) {
            return getParseTree0(unit);
        }
    }

    private Template getParseTree0(CompilationUnit unit) {
        String name = unit.getName();
        Template tree = (Template)mParseTreeMap.get(name);
        if (tree != null) {
            return tree;
        }
            
        try {
            // Parse and type check the parse tree.
            
            // Direct all compile errors into the CompilationUnit.
            // Remove the unit as an ErrorListener in the finally block
            // at the end of this method.
            addErrorListener(unit);
            
            try {
                Scanner s = createScanner(createSourceReader(unit), unit);
                s.addErrorListener(mErrorListener);
                Parser p = createParser(s, unit);
                p.addErrorListener(mErrorListener);
                tree = p.parse();
                mParseTreeMap.put(name, tree);
                s.close();
            }
            catch (IOException e) {
                uncaughtException(e);
                String msg = mFormatter.format("read.error", e.toString());
                dispatchCompileError
                    (new ErrorEvent(this, msg, (SourceInfo)null, unit));
                return tree;
            }
            
            TypeChecker tc = createTypeChecker(unit);
            tc.setClassLoader(getClassLoader());
            tc.addErrorListener(mErrorListener);
            tc.typeCheck();
            
            if (mCompiled.contains(name) || !unit.shouldCompile()) {
                return tree;
            }
            else {
                mCompiled.add(name);
            }

            // Code generate the CompilationUnit only if no errors and
            // the code generate option is enabled.

            if (unit.getErrorCount() == 0 && mGenerateCode) {
                try {
                    OutputStream out = unit.getOutputStream();

                    if (out != null) {
                        tree = (Template)new BasicOptimizer(tree).optimize();
                        mParseTreeMap.put(name, tree);
                        
                        CodeGenerator codegen = createCodeGenerator(unit);
                        codegen.writeTo(out);
                        out.flush();
                        out.close();
                    }
                }
                catch (IOException e) {
                    uncaughtException(e);
                    String msg = mFormatter.format
                        ("write.error", e.toString());
                    dispatchCompileError
                        (new ErrorEvent(this, msg, (SourceInfo)null, unit));
                    return tree;
                }
            }
        }
        catch (Exception e) {
            uncaughtException(e);
            String msg = mFormatter.format("internal.error", e.toString());
            dispatchCompileError
                (new ErrorEvent(this, msg, (SourceInfo)null, unit));
        }
        finally {
            removeErrorListener(unit);
            // Conserve memory by removing the bulk of the parse tree after
            // compilation. This preserves the signature for templates that
            // may need to call this one.
            if (tree != null &&
                (mPreserveTree == null || !mPreserveTree.contains(name))) {
                tree.setStatement(null);
            }
        }

        return tree;
    }
}
