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

import java.util.*;
import javax.servlet.*;
import com.go.trove.log.Log;
import com.go.trove.util.PropertyMap;
import com.go.trove.util.plugin.Plugin;

/******************************************************************************
 * An ApplicationConfig implementation that is used for deriving configuration
 * for internal Applications.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
class InternalApplicationConfig implements ApplicationConfig {  
    private ApplicationConfig mBaseConfig;
    private PropertyMap mProperties;
    private Log mLog;
    private Map mPluginMap;

    /**
     * Derive a new ApplicationConfig using the configuration passed in. Any
     * properties in the "log" sub-map are applied to the new Log, and the
     * "init" sub-map defines the new properties. Properties not contained
     * in "applications" from the base configuration are added as defaults to
     * to the new properties.
     *
     * @param config base configuration to derive from
     * @param properties properties to use
     * @param name name of internal application; is applied to Log name and
     * is used to extract the correct properties.
     */
    public InternalApplicationConfig(ApplicationConfig config,
                                     PropertyMap properties,
                                     Map plugins,
                                     String name) {
        mBaseConfig = config;
        mLog = new Log(name, config.getLog());
        mLog.applyProperties(properties.subMap("log"));
        mProperties = new PropertyMap(properties.subMap("init"));

        PropertyMap baseProps = config.getProperties();
        String filtered = "applications" + baseProps.getSeparator();
        Iterator it = baseProps.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            if (!key.startsWith(filtered) && !mProperties.containsKey(key)) {
                mProperties.put(key, baseProps.get(key));
            }
        }

        mPluginMap = plugins;
    }

    /**
     * Returns initialization parameters in an easier to use map format.
     */
    public PropertyMap getProperties() {
        return mProperties;
    }

    /**
     * Returns the name of this application, which is the same as the log's
     * name.
     */
    public String getName() {
        return mLog.getName();
    }

    /**
     * Returns a log object that this application should use for reporting
     * information pertaining to the operation of the application.
     */
    public Log getLog() {
        return mLog;
    }
    
    public Plugin getPlugin(String name) {
        return (Plugin)mPluginMap.get(name);
    }
    
    public Map getPlugins() {       
        return mPluginMap;
    }

    public ServletContext getServletContext() {
        return mBaseConfig.getServletContext();
    }
    
    public String getInitParameter(String name) {
        return mProperties.getString(name);
    }
    
    public Enumeration getInitParameterNames() {
        return Collections.enumeration(mProperties.keySet());
    }

    public String getServletName() {
        return mBaseConfig.getServletName();
    }
}
