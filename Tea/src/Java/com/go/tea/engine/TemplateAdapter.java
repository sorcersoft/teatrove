/* ====================================================================
 * TeaServlet - Copyright (c) 1999-2000 Walt Disney Internet Group
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

package com.go.tea.engine;

import java.lang.reflect.*;
import com.go.trove.util.MergedClass;
import com.go.trove.util.ClassInjector;
import com.go.tea.runtime.Context;
import com.go.tea.runtime.TemplateLoader;

/******************************************************************************
 * Loads templates but can also create context adapters for pre-compiled
 * templates.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/20/02 <!-- $-->
 */
class TemplateAdapter extends TemplateLoader {
    private final Class mContextClass;
    private final ClassInjector mInjector;

    public TemplateAdapter(Class contextClass, 
                           ClassInjector injector, String packagePrefix) {
        super(injector, packagePrefix);
        mContextClass = contextClass;
        mInjector = injector;
    }

    protected TemplateLoader.Template loadTemplate(String name)
        throws ClassNotFoundException, NoSuchMethodException, LinkageError
    {
        TemplateLoader.Template t = super.loadTemplate(name);
        if (t != null) {
            Class templateContext = t.getContextType();
            if (!templateContext.isAssignableFrom(mContextClass)) {
                if (!templateContext.isInterface()) {
                    throw new NoSuchMethodException
                        ("Cannot adapt to context " + templateContext +
                         " because it is not an interface.");
                }

                // Create an adapter context.
                Constructor ctor = MergedClass.getConstructor
                    (mInjector, new Class[] {mContextClass, templateContext});

                return new AdaptedTemplate(t, ctor);
            }
        }
        return t;
    }

    private class AdaptedTemplate implements TemplateLoader.Template {
        private final Template mTemplate;
        private final Constructor mContextConstructor;

        public AdaptedTemplate(Template t, Constructor ctor) {
            mTemplate = t;
            mContextConstructor = ctor;
        }

        public TemplateLoader getTemplateLoader() {
            return TemplateAdapter.this;
        }

        public String getName() {
            return mTemplate.getName();
        }

        public Class getTemplateClass() {
            return mTemplate.getTemplateClass();
        }

        public Class getContextType() {
            return mContextConstructor.getDeclaringClass();
        }

        public String[] getParameterNames() {
            return mTemplate.getParameterNames();
        }
        
        public Class[] getParameterTypes() {
            return mTemplate.getParameterTypes();
        }

        public void execute(Context context, Object[] parameters) 
            throws Exception
        {
            // Instantiate adapter.
            context = (Context)mContextConstructor.newInstance
                (new Object[] {context, null});
            mTemplate.execute(context, parameters);
        }

        public String toString() {
            return mTemplate.toString();
        }
    }
}
