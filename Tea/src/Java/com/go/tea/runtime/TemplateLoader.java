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

package com.go.tea.runtime;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.go.tea.compiler.JavaClassGenerator;

/******************************************************************************
 * TemplateLoader manages the loading and execution of Tea templates. To
 * reload templates, create a new TemplateLoader with a new ClassLoader.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 * @see com.go.tea.util.ClassInjector
 */
public class TemplateLoader {
    private ClassLoader mBaseLoader;
    private String mPackagePrefix;

    // Maps full template names to Templates.
    private Map mTemplates;

    /**
     * Creates a TemplateLoader that uses the current ClassLoader as a base.
     * It is recommended that templates be compiled to a base package and that
     * a TemplateLoader be constructed with the package prefix. This way,
     * the TemplateLoader can easily distinguish between template classes and
     * normal classes, only loading the templates.
     */
    public TemplateLoader() {
        this(null);
    }

    /**
     * Creates a TemplateLoader that uses the current ClassLoader as a base.
     *
     * @param packagePrefix Package that templates should be loaded from
     */
    public TemplateLoader(String packagePrefix) {
        init(getClass().getClassLoader(), packagePrefix);
    }

    /**
     * Creates a TemplateLoader that uses the given ClassLoader as a base. A
     * base ClassLoader is used to load both template and non-template classes.
     * The base ClassLoader can use the package prefix for determining
     * whether or not it is loading a template.
     *
     * @param baseLoader Base ClassLoader
     * @param packagePrefix Package that templates should be loaded from
     */
    public TemplateLoader(ClassLoader baseLoader, String packagePrefix) {
        init(baseLoader, packagePrefix);
    }

    private void init(ClassLoader baseLoader, String packagePrefix) {
        mBaseLoader = baseLoader;

        if (packagePrefix == null) {
            packagePrefix = "";
        }
        else if (!packagePrefix.endsWith(".")) {
            packagePrefix += '.';
        }
        mPackagePrefix = packagePrefix.trim();

        mTemplates = new HashMap();
    }
    
    /**
     * Get or load a template by its full name. The full name of a template
     * has '.' characters to separate name parts, and it does not include a
     * Java package prefix.
     *
     * @throws ClassNotFoundException when template not found
     * @throws NoSuchMethodException when the template is invalid
     */
    public final synchronized Template getTemplate(String name)
        throws ClassNotFoundException, NoSuchMethodException, LinkageError
    {
        Template template = (Template)mTemplates.get(name);
        if (template == null) {
            template = loadTemplate(name);
            mTemplates.put(name, template);
        }
        return template;
    }

    /**
     * Returns all the templates that have been loaded thus far.
     */
    public final synchronized Template[] getLoadedTemplates() {
        return (Template[])mTemplates.values().toArray
            (new Template[mTemplates.size()]);
    }

    protected Template loadTemplate(String name)
        throws ClassNotFoundException, NoSuchMethodException, LinkageError
    {
        return new TemplateImpl
            (name, mBaseLoader.loadClass(mPackagePrefix + name));
    }

    /**************************************************************************
     * A ready-to-use Tea template.
     *
     * @author Brian S O'Neill
     * @version
     * <!--$$Revision$--> 4 <!-- $$JustDate:-->  9/07/00 <!-- $-->
     */
    public static interface Template {
        public TemplateLoader getTemplateLoader();

        /**
         * Returns the full name of this template.
         */
        public String getName();

        /**
         * Returns the class that defines this template.
         */
        public Class getTemplateClass();

        /**
         * Returns the type of runtime context that this template accepts.
         *
         * @see com.go.tea.runtime.Context
         */
        public Class getContextType();

        /**
         * Returns the parameter names that this template accepts. The length
         * of the returned array is the same as returned by getParameterTypes.
         * If any template parameter names is unknown, the array entry is null.
         */
        public String[] getParameterNames();
        
        /**
         * Returns the parameter types that this template accepts. The length
         * of the returned array is the same as returned by getParameterNames.
         */
        public Class[] getParameterTypes();

        /**
         * Executes this template using the given runtime context instance and
         * parameters.
         *
         * @param context Must be assignable to the type returned by
         * {@link #getContextType()}.
         * @param parameters Must have same length and types as returned by
         * {@link #getParameterTypes()}.
         */
        public void execute(Context context, Object[] parameters) 
            throws Exception;

        /**
         * Returns the template signature.
         */
        public String toString();
    }

    private class TemplateImpl implements Template {
        private String mName;
        private Class mClass;

        private transient Method mExecuteMethod;
        private transient Class mReturnType;
        private transient String[] mParameterNames;
        private transient Class[] mParameterTypes;

        private TemplateImpl(String name, Class clazz)
            throws NoSuchMethodException
        {
            mName = name;
            mClass = clazz;
            doReflection();
        }

        public TemplateLoader getTemplateLoader() {
            return TemplateLoader.this;
        }

        public String getName() {
            return mName;
        }

        public Class getTemplateClass() {
            return mClass;
        }

        public Class getContextType() {
            return mExecuteMethod.getParameterTypes()[0];
        }

        public String[] getParameterNames() {
            return (String[])mParameterNames.clone();
        }
        
        public Class[] getParameterTypes() {
            return (Class[])mParameterTypes.clone();
        }

        public void execute(Context context, Object[] parameters) 
            throws Exception
        {
            int length = parameters.length;
            Object[] args = new Object[1 + length];
            args[0] = context;
            for (int i=0; i<length; i++) {
                args[i + 1] = parameters[i];
            }

            try {
                Object ret = mExecuteMethod.invoke(null, args);
                if (mReturnType != void.class) {
                    context.print(ret);
                }
            }
            catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof Exception) {
                    throw (Exception)t;
                }
                else if (t instanceof Error) {
                    throw (Error)t;
                }
                else {
                    throw e;
                }
            }
        }

        public String toString() {
            StringBuffer buf = new StringBuffer(80);

            buf.append("template ");
            buf.append(getName());
            buf.append('(');
            
            buf.append(getContextType().getName());

            String[] paramNames = getParameterNames();
            Class[] paramTypes = getParameterTypes();
            int length = paramTypes.length;
            for (int i=0; i<length; i++) {
                buf.append(", ");
                buf.append(paramTypes[i].getName());
                if (paramNames[i] != null) {
                    buf.append(' ');
                    buf.append(paramNames[i]);
                }
            }

            buf.append(')');

            return buf.toString();
        }

        private void doReflection() throws NoSuchMethodException {
            // Bind to first execute method found; there should be one.
            Method[] methods = getTemplateClass().getMethods();
            
            int foundCount = 0;
            for (int i=0; i<methods.length; i++) {
                Method m = methods[i];
                if (m.getName().equals
                    (JavaClassGenerator.EXECUTE_METHOD_NAME) &&
                    Modifier.isStatic(m.getModifiers())) {

                    mExecuteMethod = m;
                    break;
                }
            }

            if (mExecuteMethod == null) {
                throw new NoSuchMethodException
                    ("No execute method found in class " + 
                     "for template \"" + getName() + "\"");
            }

            mReturnType = mExecuteMethod.getReturnType();

            Class[] methodParams = mExecuteMethod.getParameterTypes();
            if (methodParams.length == 0 ||
                !Context.class.isAssignableFrom(methodParams[0])) {

                throw new NoSuchMethodException
                    ("Execute method does not accept a context " +
                     "for template \"" + getName() + "\"");
            }

            int length = methodParams.length - 1;
            mParameterNames = new String[length];
            mParameterTypes = new Class[length];

            for (int i=0; i<length; i++) {
                mParameterTypes[i] = methodParams[i + 1];
            }

            try {
                Method namesMethod = getTemplateClass().getMethod
                    (JavaClassGenerator.PARAMETER_METHOD_NAME, null);

                String[] names = (String[])namesMethod.invoke(null, null);
                if (names != null) {
                    // Copy, just in case the length differs.
                    for (int i=0; i<length; i++) {
                        mParameterNames[i] = names[i];
                    }
                }
            }
            catch (Exception e) {
                // No big deal, we just don't set paramater names.
            }
        }
    }
}
