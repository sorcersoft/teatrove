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

package com.go.teaservlet;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.go.teaservlet.util.FilteredServletContext;
import com.go.trove.log.*;
import com.go.trove.util.Utils;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.Config;
import com.go.trove.util.ConfigSupport;
import com.go.trove.util.PropertyMap;
import com.go.trove.util.PropertyParser;
import com.go.trove.util.plugin.Plugin;
import com.go.trove.util.plugin.PluginContext;
import com.go.trove.util.plugin.PluginFactory;
import com.go.trove.util.plugin.PluginFactoryConfig;
import com.go.trove.util.plugin.PluginFactoryConfigSupport;
import com.go.trove.util.plugin.PluginFactoryException;
import com.go.trove.io.ByteBuffer;
import com.go.tea.runtime.TemplateLoader;

import com.go.tea.engine.*;

/******************************************************************************
 * The ApplicationDepot stores the Applications that were specified in the
 * props file. The ApplicationDepot is also responsible for creating logs for
 * each Application.
 * <p>
 * depot -- a place for storing goods
 *
 * @author Reece Wilton, Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  3/05/02 <!-- $-->
 */
class ApplicationDepot {

    private TeaServletEngineImpl mEngine;

    private ContextSource mContextSource;
    private ContextSource mGenericContextSource;

    // a map to retrieve applications by name.
    private Map mAppMap;
    private Map mAppContextMap;

    // an array containing the applications in the order of initialization. 
    private Application[] mApplications;
    // a array of the application names in the same order
    private String[] mApplicationNames;
    // same as above, but cleaned to contain only valid java identifiers
    private String[] mContextPrefixNames;


    /**
     * Creates the ApplicationDepot.
     * @param engine the teaservlet engine in which this depot lives.
     */
    ApplicationDepot(TeaServletEngineImpl engine) throws ServletException {

        mEngine = engine;

        // Initialize Applications
        loadApplications();
    }

    public ContextSource getContextSource() {
        if (mContextSource == null) {
            return reloadContextSource();
        }
        return mContextSource;
    }

    public ContextSource getGenericContextSource() {
        if (mGenericContextSource == null) {
            return reloadGenericContextSource();
        }
        return mGenericContextSource;
    }

    
    public ContextSource reloadContextSource() {
        try {
            mContextSource = createContextSource(true);
        }
        catch (Exception e) {
            mEngine.getLog().error(e);
        }
        return mContextSource;
    }


    public ContextSource reloadGenericContextSource() {
        try {
            mGenericContextSource = createContextSource(false);
        }
        catch (Exception e) {
            mEngine.getLog().error(e);
        }
        return mGenericContextSource;
    }


    public final Class getContextType() throws Exception {
            return getContextSource().getContextType();
    }

    public Application[] getApplications() {
        return mApplications;
    }

    public String[] getApplicationNames() {
        return mApplicationNames;
    }

    public String[] getContextPrefixNames() {
        return mContextPrefixNames;
    }

    /**
     * This method destroys the ApplicationDepot.
     */
    public void destroy() {
        for (int j = 0; j < mApplications.length; j++) {
            if (mApplications[j] != null) {
                mApplications[j].destroy();
            }
        }
    }

    /**
     * creates a single context source from the applications in the depot.
     */
    private TeaServletContextSource createContextSource(boolean http) 
        throws Exception {

        return new TeaServletContextSource(getClass().getClassLoader(), 
                                           this, mEngine.getServletContext(),
                                           mEngine.getLog(), http);
    }

    private void loadApplications() throws ServletException {
        
        PropertyMap props = mEngine.getProperties().subMap("applications");
        Set appSet = props.subMapKeySet();

        mAppMap = new TreeMap();
        int numApps = appSet.size();
        mApplications = new Application[numApps];
        mApplicationNames = new String[numApps];
        mContextPrefixNames = new String[numApps];

        Iterator appIt = appSet.iterator();
        int appCounter = 0;
        Log log = mEngine.getLog();

        log.info("Loading Applications");
        while (appIt.hasNext()) {
            String appName = (String)appIt.next();

            log.info("Loading: " + appName);

            mContextPrefixNames[appCounter] = (cleanName(appName) + '$');

            PropertyMap appProps = 
                props.subMap(appName); 

            ApplicationConfig ac = 
                new InternalApplicationConfig(mEngine, appProps,
                                              mEngine.getPlugins(), 
                                              appName);

            String appClassName = appProps.getString("class");

            if (appClassName != null) {
                try {
                    Application app = (Application)getClass()
                        .getClassLoader().loadClass(appClassName)
                        .newInstance();
                    
                    app.init(ac);
                    mApplications[appCounter] = app;
                    mApplicationNames[appCounter] = appName;
                    mAppMap.put(appName, app);
                    
                }
                catch (ClassNotFoundException cnfe) {
                    log.error("Could not find class: " + appClassName);
                }
                catch (InstantiationException ie) {
                    log.error("Could not create an instance of: " 
                             + appClassName);              
                }
                catch (IllegalAccessException iae) {
                    log.error("Could not create an instance of: " 
                             + appClassName);              
                }
                catch (ClassCastException cce) {
                    log.error(appClassName 
                             + " does not implement Application.");
                    log.error(cce);
                }
            }
            appCounter++;
        }
    }

    /**
     * Ensures that name only contains valid Java identifiers by converting
     * non identifier characters to '$' characters. If name begins with a
     * numeral, name is prefixed with an underscore. The returned name is also
     * trimmed at the first hyphen character, if there is one. This allows
     * multiple applications to appear to provide a unified set of functions.
     */
    public static String cleanName(String name) {
        int index = name.indexOf('-');
        if (index > 0) {
            name = name.substring(0, index);
        }

        int length = name.length();
        StringBuffer buf = new StringBuffer(length + 1);

        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            buf.append('_');
        }
        else if (Character.isJavaIdentifierPart(name.charAt(0))) {
            buf.append(name.charAt(0));
        }

        for (int i=1; i<length; i++) {
            char c = name.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                buf.append(c);
            }
            else {
                buf.append('$');
            }
        }

        return buf.toString();
    }
}







