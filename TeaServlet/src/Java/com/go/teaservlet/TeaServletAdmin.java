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

import java.lang.reflect.Method;
import java.beans.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.servlet.ServletContext;
import com.go.teaservlet.util.NameValuePair;
import com.go.tea.runtime.TemplateLoader;
import com.go.tea.engine.ReloadLock;
import com.go.tea.engine.TemplateCompilationResults;
import com.go.tea.engine.TemplateError;
import com.go.tea.engine.TemplateSource;
import com.go.trove.log.Log;
import com.go.trove.log.LogEvent;
import com.go.trove.util.BeanComparator;
import com.go.teaservlet.util.cluster.Restartable;
import com.go.teaservlet.util.cluster.ClusterHook;

/******************************************************************************
 * The Admin object which contains all administrative information. This object
 * is meant to be used by the Admin page of the TeaServlet.
 *
 * @author Reece Wilton, Brian S O'Neill, Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  4/03/02 <!-- $-->
 */
public class TeaServletAdmin implements Restartable {

    // command codes.
    public static final int RELOAD_CONTEXT = -99;
    public static final int RELOAD_TEMPLATE_CHANGES = 4019;
    public static final int RELOAD_ALL_TEMPLATES = -4019;

    private TeaServletEngine mTeaServletEngine;
    private String[] mClusteredServers;
    //private TemplateDepot.TemplateLoadResult mTemplateLoadResult;
    private List mServerStatus;
    private String[] mTemplateNamesFromLastSuccessfulReload;
    private Date mTimeOfLastSuccessfulReload;
    private AppAdminLinks[] mAdminLinks;
    private Comparator mTemplateOrdering;
    private ReloadLock mLock;

    /**
     * Initializes the Admin object for the specific TeaServletEngine instance.
     * @param teaServlet the TeaServletEngine to administer
     */
    public TeaServletAdmin(TeaServletEngine engine) {
        mTeaServletEngine = engine;
        mLock = new ReloadLock();
        mServerStatus = new Vector();
    }

    public Object restart(Object paramObj) 
        throws RemoteException {

        synchronized(mLock) {
            if (mLock.isReloading()) {
                return new TemplateCompilationResults();
            }
            else {
                mLock.setReloading(true);
            }
        }
        try {
            Integer commandCode = (Integer)paramObj;
            if (commandCode == null) {
                return mTeaServletEngine.getTemplateSource()
                    .compileTemplates(null, false);
            }
            else {
                switch (commandCode.intValue()) {

                case RELOAD_CONTEXT:
                    mTeaServletEngine.getApplicationDepot()
                        .reloadContextSource();
                    return mTeaServletEngine.reloadTemplateSource()
                        .compileTemplates(null, true);
                    
                case RELOAD_ALL_TEMPLATES:
                    return mTeaServletEngine.getTemplateSource()
                        .compileTemplates(null, true);

                case RELOAD_TEMPLATE_CHANGES:
                default:
                    return mTeaServletEngine.getTemplateSource()
                        .compileTemplates(null, false);
                }
            }
        }
        catch (Exception e) {
            throw new RemoteException("Restart Error", e);
        }
        finally {
            synchronized(mLock) {
                mLock.setReloading(false);
            }
        }
    }

    public ServletContext getServletContext() {
        return mTeaServletEngine.getServletContext();
    }
   
    public NameValuePair[] getInitParameters() {
        Enumeration e = mTeaServletEngine.getInitParameterNames();
        List list = new ArrayList();
        while (e.hasMoreElements()) {
            String initName = (String)e.nextElement();
            list.add(new NameValuePair
                     (initName, mTeaServletEngine
                      .getInitParameter(initName)));
        }
        return (NameValuePair[])list.toArray(new NameValuePair[list.size()]);
    }

    public NameValuePair[] getAttributes() {
        ServletContext context = getServletContext();
        Enumeration e = context.getAttributeNames();
        List list = new ArrayList();
        while (e.hasMoreElements()) {
            String initName = (String)e.nextElement();
            list.add(new NameValuePair
                     (initName, context.getAttribute(initName)));
        }
        return (NameValuePair[])list.toArray(new NameValuePair[list.size()]);
    }

    public Log getLog() {
        return mTeaServletEngine.getLog();
    }

    public LogEvent[] getLogEvents() {
        return mTeaServletEngine.getLogEvents();
    }

    public ApplicationInfo[] getApplications() {
    
        ApplicationDepot depot = mTeaServletEngine.getApplicationDepot();
        TeaServletContextSource tscs = (TeaServletContextSource)
            mTeaServletEngine.getTemplateSource().getContextSource();
        Map appContextMap = tscs.getApplicationContextTypes();
        Application[] apps = depot.getApplications();
        String[] names = depot.getApplicationNames();
        String[] prefixes = depot.getContextPrefixNames();

        ApplicationInfo[] infos = new ApplicationInfo[apps.length];

        for (int i=0; i < apps.length; i++) {
            infos[i] = new ApplicationInfo(names[i],
                                           apps[i],
                                           (Class)appContextMap.get(apps[i]),
                                           prefixes[i]);
        }

        return infos;
    }

    /**
     *  Returns information about all functions available to the templates.
     */
    public FunctionInfo[] getFunctions() {
        // TODO: make this a little more useful by showing more function
        // details.

        ApplicationInfo[] AppInf = getApplications();
        
        FunctionInfo[] funcArray = null;
        
        try {
            MethodDescriptor[] methods = Introspector
                .getBeanInfo(HttpContext.class)
                .getMethodDescriptors();        
            List funcList = new Vector(50);
            
            for (int j = -1; j < AppInf.length;j++) {
                if (j >= 0) {
                    methods = AppInf[j].getContextFunctions();
                }
                for (int i=0; i<methods.length; i++) {
                    MethodDescriptor m = methods[i];
                    if (m.getMethod().getDeclaringClass() != Object.class &&
                        !m.getMethod().getName().equals("print") &&
                        !m.getMethod().getName().equals("toString")) {
                        
                        if (j >= 0) {
                            funcList.add(new FunctionInfo(m, AppInf[j]));
                        }
                        else {
                            funcList.add(new FunctionInfo(m, null));
                        }
                    }
                }
            }
            
            funcArray = (FunctionInfo[])funcList.toArray
                (new FunctionInfo[funcList.size()]);
            Arrays.sort(funcArray);
        }
        catch (Exception ie) {
            ie.printStackTrace();
        }
        
        return funcArray;
    }

    public TemplateLoader.Template[] getTemplates() {
        TemplateLoader.Template[] templates =
            mTeaServletEngine.getTemplateSource()
            .getLoadedTemplates();
        Comparator c = BeanComparator.forClass(TemplateLoader.Template.class)
            .orderBy("name");
        Arrays.sort(templates, c);
        return templates;
    }

    /**
     * Provides an ordered array of available templates using a 
     * handy wrapper class.
     */
    public TemplateWrapper[] getKnownTemplates() {
        if (mTemplateOrdering == null) {
            mTemplateOrdering = BeanComparator
                .forClass(TemplateWrapper.class).orderBy("name");
        }
        Set known = new TreeSet(mTemplateOrdering);

        TemplateLoader.Template[] loaded = mTeaServletEngine
            .getTemplateSource().getLoadedTemplates();
    
        if (loaded != null) {
            for (int j = 0; j < loaded.length; j++) {
                TeaServletAdmin.TemplateWrapper wrapper = 
                    new TemplateWrapper(loaded[j]);
         
                try {
                    known.add(wrapper);
                }
                catch (ClassCastException cce) {}
            }
        }

        String[] allNames = mTeaServletEngine.getTemplateSource()
            .getKnownTemplateNames();
        if (allNames != null) {
            for (int j = 0; j < allNames.length; j++) {
                TeaServletAdmin.TemplateWrapper wrapper = 
                    new TemplateWrapper(allNames[j]);
                try {
                    known.add(wrapper);
                }
                catch (ClassCastException cce) {}
            }
        }
        
        return (TeaServletAdmin.TemplateWrapper[])known
            .toArray(new TemplateWrapper[known.size()]);
    }

    public Date getTimeOfLastReload() {
        return mTeaServletEngine.getTemplateSource()
            .getTimeOfLastReload();
    }
     
    public Class getTeaServletClass() {
        Object obj = mTeaServletEngine.getServletContext()
            .getAttribute(TeaServlet.class.getName());
        if (obj != null) {
            return obj.getClass();
        }
        return mTeaServletEngine.getClass();
    }

    public String getTeaServletVersion() {
        return com.go.teaservlet.PackageInfo.getImplementationVersion();
    }

    public String getTeaVersion() {
        return com.go.tea.PackageInfo.getImplementationVersion();
    }

    public String[] getClusteredServers() {
        return mClusteredServers;
    }

    protected void setClusteredServers(String[] serverNames) {
        mClusteredServers = serverNames;
    }

    public ServerStatus[] getReloadStatusOfServers() {
        ServerStatus[] statusArray =
            (ServerStatus[])mServerStatus.toArray(new ServerStatus[0]);

        Comparator c = BeanComparator.forClass(ServerStatus.class)
            .orderBy("statusCode").reverse()
            .orderBy("serverName");
        Arrays.sort(statusArray, c);
        return statusArray;
    }

    protected void clearServerReloadStatus() {
        mServerStatus.clear();
    }

    protected void setServerReloadStatus(String name,
                                      int statusCode, String message) {
        mServerStatus.add(new ServerStatus(name, statusCode, message));
    }

    public AppAdminLinks[] getAdminLinks() {
        return mAdminLinks;
    }

    protected void setAdminLinks(AppAdminLinks[] links) {

        mAdminLinks = links;
    }

    public class ServerStatus {
        private String mServerName;
        private String mMessage;
        private int mStatusCode;

        public ServerStatus(String name, int statusCode, String message) {
            mServerName = name;
            mMessage = message;
            mStatusCode = statusCode;
        }

        public String getServerName() {
            return mServerName;
        }

        public String getMessage() {
            return mMessage;
        }

        public int getStatusCode() {
            return mStatusCode;
        }
    }

    public class TemplateWrapper {
        
        private TemplateLoader.Template mTemplate;
        private String mName;

        public TemplateWrapper(TemplateLoader.Template template) {
            mTemplate = template;
        }

        public TemplateWrapper(String name) {
            mName = name;
        }

        public boolean equals(Object obj) {
            if (obj != null 
                && obj instanceof TemplateWrapper) {
                return getName().equals(((TemplateWrapper)obj).getName());
            }
            return false;
        }

        public TemplateLoader.Template getLoadedTemplate() {
            return mTemplate;
        }

        public String getName() {
            if (mTemplate != null) {
                return mTemplate.getName();
            }
            else {
                return mName;
            }
        }

        public boolean isLoaded() {
            return mTemplate != null;
        }
    }
}
