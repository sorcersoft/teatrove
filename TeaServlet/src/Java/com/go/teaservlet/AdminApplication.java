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

import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import com.go.trove.log.Log;
import com.go.trove.util.BeanComparator;
import com.go.trove.util.PropertyMap;
import com.go.trove.io.CharToByteBuffer;
import com.go.trove.io.ByteBuffer;
import com.go.tea.engine.Template;
import com.go.tea.engine.TemplateCompilationResults;
import com.go.tea.engine.TemplateError;
import com.go.tea.util.BeanAnalyzer;
import com.go.teatools.*;
import com.go.teaservlet.util.ObjectIdentifier;
import com.go.teaservlet.util.ServerNote;
//REMOTE stuff
import java.rmi.RemoteException;
import com.go.teaservlet.util.cluster.Restartable;
import com.go.teaservlet.util.cluster.Clustered;
import com.go.teaservlet.util.cluster.ClusterManager;
import com.go.teaservlet.util.cluster.TeaServletClusterHook;

/******************************************************************************
 * The Admin application defines functions for administering the TeaServlet.
 *
 * @author Reece Wilton, Brian S O'Neill, Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/01/02 <!-- $-->
 */
public class AdminApplication implements AdminApp {
    protected ApplicationConfig mConfig;
    protected Log mLog;
    protected TeaServlet mTeaServlet;
    protected String mAdminKey;
    protected String mAdminValue;
    protected AppAdminLinks[] mAdminLinks;
    protected Map mNotes;
    protected int mMaxNotes;
    protected int mNoteAge;

    // REMOTE stuff
    TeaServletAdmin mAdmin;
    String mClusterName;
    int mRmiPort,mMulticastPort;
    InetAddress mMulticastGroup;
    ClusterManager mClusterManager;

    /**
     * Initializes the Application. Accepts the following initialization
     * parameters:
     * <pre>
     * admin.key - the security parameter key
     * admin.value - the security parameter value
     * notes.max - number of notes to store for each group
     * notes.age - how long to hang on to a note, in seconds
     * </pre>
     *
     * @param config the application's configuration object
     */
    public void init(ApplicationConfig config) {
        mConfig = config;
        mLog = config.getLog();
        PropertyMap props = config.getProperties();
        // Get the admin key/value.
        mAdminKey = props.getString("admin.key");
        mAdminValue = props.getString("admin.value");
        mMaxNotes = props.getInt("notes.max", 20);
        mNoteAge = props.getInt("notes.age", 60 * 60 * 24 * 7);

        List serverList = new ArrayList();

        /* OLD CLUSTER stuff
           // Get the server list.
           String clusterServers = config.getInitParameter("cluster.servers");
           if (clusterServers != null) {
           StringTokenizer st = new StringTokenizer
           (clusterServers, ",;");

           while (st.hasMoreTokens()) {
           serverList.add(st.nextToken().trim());
           }

           if (serverList.isEmpty()) {
           mLog.warn("No servers specified for this cluster");
           }
           }

           mClusteredServers = (String[])serverList.toArray(new String[0]);
        */

        // Save the TeaServlet reference. The TeaServlet places an instance of
        // itself in the config in this hidden way because applications don't
        // need direct access to the TeaServlet.
        ServletContext context = config.getServletContext();
        mTeaServlet =
            (TeaServlet)context.getAttribute(TeaServlet.class.getName());
        if (mTeaServlet == null) {
            mLog.warn("TeaServlet attribute not found");
        }
        else {           

            //REMOTE stuff
            try {
                String servers = config
                    .getInitParameter("cluster.servers");
                String clusterName = config
                    .getInitParameter("cluster.name");
                int rmiPort = 1099;
                int multicastPort = 1099;
                InetAddress multicastGroup = null;
                String netInterface = config
                    .getInitParameter("cluster.localNet");

                try {
                    rmiPort = Integer.parseInt(config
                                     .getInitParameter("cluster.rmi.port"));
                    
                    multicastPort = Integer.parseInt(config
                                     .getInitParameter("cluster.multicast.port"));

                    multicastGroup = InetAddress.getByName(config
                                     .getInitParameter("cluster.multicast.group"));
                     
                }
                catch (NumberFormatException nfe) {}
                catch (UnknownHostException uhe) {}
                if (multicastGroup != null) {
                    mClusterManager = new ClusterManager(getAdmin(),
                                                         clusterName,
                                                         null,
                                                         multicastGroup,
                                                         multicastPort,
                                                         rmiPort,
                                                         netInterface,
                                                         servers);
                    mClusterManager.joinCluster();
                    mClusterManager.launchAuto();
                }
                else if (servers != null) {
                    mClusterManager = new ClusterManager(getAdmin(),
                                                         clusterName,
                                                         null,
                                                         rmiPort,
                                                         netInterface,
                                                         servers);
                }
            }
            catch (Exception e) {
                mLog.warn(e);
            }
        }
    }

    public void destroy() {
        if (mClusterManager != null) {
            mClusterManager.killAuto();
        }
    }

    /**
     * Returns an instance of {@link AdminContext}.
     */
    public Object createContext(ApplicationRequest request,
                                ApplicationResponse response) {
        return new ContextImpl(request, response);
    }

    /**
     * Returns {@link AdminContext}.class.
     */
    public Class getContextType() {
        return AdminContext.class;
    }

    void adminCheck(ApplicationRequest request, ApplicationResponse response)
        throws AbortTemplateException
    {
        if (mAdminKey == null) {
            return;
        }

        // Check for admin key.
        String adminParam = request.getParameter(mAdminKey);

        // Look in cookie for admin param.
        if (adminParam == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if (cookie.getName().equals(mAdminKey)) {
                        adminParam = cookie.getValue();
                    }
                }
            }
        }

        if (adminParam != null && adminParam.equals(mAdminValue)) {
            // Set the admin param in the cookie.
            Cookie c = new Cookie(mAdminKey, adminParam);
            // Save cookie for 7 days.
            c.setMaxAge(24 * 60 * 60 * 7);
            c.setPath("/");
            response.addCookie(c);
        }
        else {
            // User is unauthorized.

            mLog.warn("Unauthorized Admin access to " +
                      request.getRequestURI() +
                      " from " + request.getRemoteAddr() +
                      " - " + request.getRemoteHost() +
                      " - " + request.getRemoteUser());

            try {
                response.sendError
                    (response.SC_NOT_FOUND, request.getRequestURI());
            }
            catch (IOException e) {
            }

            throw new AbortTemplateException();
        }
    }

    /**
     * This implementation uses hard coded link information, but other
     * applications can dynamically determine their admin links.
     */
    public AppAdminLinks getAdminLinks() {

        AppAdminLinks links = new AppAdminLinks(mConfig.getName());

        links.addAdminLink("Templates","/system/teaservlet/AdminTemplates");
        links.addAdminLink("Functions","/system/teaservlet/AdminFunctions");
        links.addAdminLink("Applications",
                           "/system/teaservlet/AdminApplications");
        links.addAdminLink("Logs","/system/teaservlet/LogViewer");
        links.addAdminLink("Servlet Engine",
                           "/system/teaservlet/AdminServletEngine");
        return links;
    }

    private TeaServletAdmin getAdmin() {
        if (mAdmin == null) {
            mAdmin = new TeaServletAdmin(mTeaServlet.getEngine());
        }
        return mAdmin;
    }


    public class ContextImpl extends TeaToolsUtils implements AdminContext {
        protected ApplicationRequest mRequest;
        protected ApplicationResponse mResponse;
        private TeaServletAdmin mTSAdmin;
        private TemplateCompilationResults mCompilationResults;

        protected ContextImpl(ApplicationRequest request,
                              ApplicationResponse response) {
            mRequest = request;
            mResponse = response;
        }

        public TemplateCompilationResults getCompilationResults() {
            return mCompilationResults;
        }

        public TeaServletAdmin getTeaServletAdmin() throws ServletException {
            if (mTSAdmin != null) {
                return mTSAdmin;
            }
            adminCheck(mRequest, mResponse);
            mTSAdmin = getAdmin();
            // Cluster related Stuff
            mTSAdmin.clearServerReloadStatus();

            if (mClusterManager != null) {
                mTSAdmin.setClusteredServers(mClusterManager
                                             .resolveServerNames());
            }
            
            // Admin Link Stuff
            if (mTSAdmin.getAdminLinks() == null) {
                
                // add the Teaservlet links first.
                List links = new ArrayList();
                links.add(getAdminLinks());

                // go through the apps looking for other AdminApps
                Iterator it = mRequest.getApplicationContextTypes()
                    .keySet().iterator();

                while (it.hasNext()) {
                    Application app = (Application)it.next();
                    if (app instanceof AdminApp 
                        && !AdminApplication.this.equals(app)) {
                        links.add(((AdminApp)app).getAdminLinks());
                    }
                }
                mTSAdmin
                    .setAdminLinks((AppAdminLinks[])links
                                   .toArray(new AppAdminLinks[links.size()]));
            }
            

            // Does the user want to reload templates?
            String param = mRequest.getParameter("reloadTemplates");
            if (param != null) {


                int command = TeaServletAdmin.RELOAD_TEMPLATE_CHANGES;
                if (contextChanged()) {
                    command = TeaServletAdmin.RELOAD_CONTEXT;
                }
                else if ("all".equals(param)) {
                    command = TeaServletAdmin.RELOAD_ALL_TEMPLATES;
                }
                
                Integer all = new Integer(command);
                
                try {
                    if (mRequest.getParameter("cluster") != null 
                        && mClusterManager != null) {
                        mCompilationResults = clusterReload(all);  
                    }
                    else {
                        mCompilationResults = 
                            (TemplateCompilationResults)mTSAdmin.restart(all);
                    }
                }
                catch (RemoteException re) {
                    mLog.warn(re);
                }
            }

            // Is the user changing the log settings?
            param = mRequest.getParameter("log");
            if (param != null) {
                Log log;
                try {
                    log = (Log)ObjectIdentifier.retrieve(param);
                }
                catch (ClassCastException e) {
                    log = null;
                }

                if (log != null) {
                    String setting = mRequest.getParameter("enabled");
                    if (setting != null) {
                        log.setEnabled(setting.equals("true"));
                    }
                    setting = mRequest.getParameter("debug");
                    if (setting != null) {
                        log.setDebugEnabled(setting.equals("true"));
                    }
                    setting = mRequest.getParameter("info");
                    if (setting != null) {
                        log.setInfoEnabled(setting.equals("true"));
                    }
                    setting = mRequest.getParameter("warn");
                    if (setting != null) {
                        log.setWarnEnabled(setting.equals("true"));
                    }
                    setting = mRequest.getParameter("error");
                    if (setting != null) {
                        log.setErrorEnabled(setting.equals("true"));
                    }
                }

                try {
                    mResponse.sendRedirect(mRequest.getRequestURI());
                    throw new AbortTemplateException();
                }
                catch (IOException e) {
                }
            }

            return mTSAdmin;
        }

        public String getObjectIdentifier(Object obj) {
            return ObjectIdentifier.identify(obj);
        }

        public Class getClassForName(String classname) {
            try {
                ClassLoader cl = mTeaServlet.getEngine().getApplicationDepot()
                    .getContextType().getClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }             
                return cl.loadClass(classname);
            }
            catch (Exception cpe) {
                mLog.warn(cpe);
                return null;
            }
        }

        /**
         * Streams the structural bytes of the named class via the 
         * HttpResponse.
         */
        public void streamClassBytes(String className)
            throws AbortTemplateException
        {
            adminCheck(mRequest, mResponse);

            mLog.debug("streamClassBytes: " + className);

            if (className == null) {
                return;
            }

            String classResource = className.replace('.', '/') + ".class";

            ClassLoader cl;

            try {
                cl = mTeaServlet.getEngine().getApplicationDepot()
                    .getContextSource().getContextType().getClassLoader();
            }
            catch (Exception e) {
                cl = ClassLoader.getSystemClassLoader();
            }
           
            InputStream in = cl.getResourceAsStream(classResource);

            if (in != null) {
                mLog.debug("streamClassBytes: Got InputStream for class: " + 
                           className);

                int len = 4000;
                byte[] b = new byte[len];
                int bytesRead = 0;
                ByteBuffer buffer = mResponse.getResponseBuffer();
                
                try {
                    while ((bytesRead = in.read(b, 0, len)) > 0) {
                        buffer.append(b, 0, bytesRead);
                    }
        
                    in.close();                
                }
                catch (Exception e) {
                    mResponse.setStatus(mResponse.SC_NOT_FOUND);            
                    mLog.debug(e);
                }
            }
            else {
                mResponse.setStatus(mResponse.SC_NOT_FOUND);  
            }

            mResponse.setContentType("application/java");
        }


        /** 
         * allows a template to dynamically call another template
         */
        public void dynamicTemplateCall(String templateName)
            throws Exception {
            dynamicTemplateCall(templateName,new Object[0]);
        }

        /** 
         * allows a template to dynamically call another template
         * this time with parameters.
         */
        public void dynamicTemplateCall(String templateName, Object[] params) 
            throws Exception
        {
            com.go.tea.runtime.Context context = mResponse
                .getHttpContext();

            Template currentTemplate = 
                (Template)mRequest.getTemplate();

            com.go.tea.runtime.TemplateLoader.Template td = 
                mTeaServlet.getEngine().findTemplate(templateName,
                                                     mRequest,
                                                     mResponse,
                                                     (TeaServletTemplateSource)
                                                     currentTemplate
                                                     .getTemplateSource());

            // make sure we have the right number and types of parameters
            String[] paramNames = td.getParameterNames();
            Object[] oldParams = params;
            if (oldParams == null 
                || oldParams.length != paramNames.length) {
                params = new Object[paramNames.length];
                
                /*
                 * if the provided parameters don't match up with the 
                 * required parameters first try to fill in the params 
                 * from the request then fill the rest with nulls.
                 * NOTE: if the parameters explicitly passed in do not 
                 * match the template signature, none of those parameters 
                 * will be used
                 */

                for (int j=0;j<paramNames.length;j++) {
                    params[j] = mRequest.getParameter(paramNames[j]);
                    if (params[j] == null && oldParams.length > j) {
                        params[j] = oldParams[j];
                    }                       
                }                
            }

            td.execute(context, params);
        }
    
        public Object obtainContextByName(String appName) 
            throws ServletException {
            ApplicationInfo[] applications = 
                getTeaServletAdmin().getApplications();
            for (int j=0;j<applications.length;j++) {
                if (appName.equals(applications[j].getName())) {
                    return ((Application)applications[j].getValue())
                        .createContext(mRequest,mResponse);
                }
            }
            return null;
        }

        public Set addNote(String ID, String contents, int lifespan) {

            Set noteSet = null;
            if (mNotes == null) {
                mNotes = Collections.synchronizedSortedMap(new TreeMap());
            }
            if (ID != null) {
                if ((noteSet = (Set)mNotes.get(ID)) == null) {
                    Comparator comp = BeanComparator.forClass(ServerNote.class)
                        .orderBy("timestamp").orderBy("contents");
                    noteSet = Collections
                        .synchronizedSortedSet(new TreeSet(comp));
                    mNotes.put(ID, noteSet);
                }
                else {
                    Date now = new Date();
                    synchronized (noteSet) {
                        Iterator expireIt = noteSet.iterator();
                        while (expireIt.hasNext()) {
                            ServerNote nextNote = (ServerNote)expireIt.next();
                            if (now.after(nextNote.getExpiration())) {
                                expireIt.remove();
                            }
                        }
                    }
                }
                if (contents != null) {
                    if (lifespan == 0) {
                        lifespan = mNoteAge;
                    }
                    ServerNote note = new ServerNote(contents, 
                                                     lifespan);
                    noteSet.add(note);
                }
                return noteSet;
            }
            return mNotes.keySet();
        }

        /* not currently used. TODO: delete these when sure we won't use again
        
        public FeatureDescription[] sort(FeatureDescription[] fds) {
            return sortDescriptions(fds);
        }

        public FeatureDescriptor[] sort(FeatureDescriptor[] fds) {
            return sortDescriptors(fds);
        }

        public Object[] sort(Object[] objArray) {
            Object[] dolly = (Object[])objArray.clone();
            Arrays.sort(dolly);
            return dolly;
        }        

        public PropertyDescriptor[] getBeanProperties(Class beanClass)
            throws IntrospectionException
        {
            if (beanClass == null) {
                return null;
            }

            PropertyDescriptor[] pdarray = new PropertyDescriptor[0];

            Collection props =
                BeanAnalyzer.getAllProperties(beanClass).values();
            pdarray = (PropertyDescriptor[])props.toArray(pdarray);
            Comparator pdcomp = BeanComparator
                .forClass(PropertyDescriptor.class).orderBy("name");
            java.util.Arrays.sort(pdarray, pdcomp);

            return pdarray;
        }
        */

        /**
         * @return true if the context type has changed making a context 
         * reload necessary.
         */
        private boolean contextChanged() {

            TeaServletAdmin admin = getAdmin();
            ApplicationInfo[] apps = admin.getApplications();
            Map expectedTypes = mRequest.getApplicationContextTypes();
            
            for (int j = 0; j < apps.length; j++) {
                Application app = (Application)apps[j].getValue();
                Class currentContextType = app.getContextType();
                Class expectedContextType = (Class)expectedTypes.get(app);
                if (currentContextType != expectedContextType) {
                    return true;
                }
            }
            return false;
        }

        private TemplateCompilationResults clusterReload(Integer all) 
            throws RemoteException {
            /* OLD Cluster Stuff
               StringBuffer uri = new StringBuffer();

               uri.append(mRequest.getContextPath());
               uri.append(mRequest.getServletPath());
               uri.append("/system/teaservlet/ClusterReload?reloadTemplates");

               if (all) {
               uri.append("=all");
               }

               if (mAdminKey != null && mAdminValue != null) {
               uri.append('&');
               uri.append(mAdminKey);
               uri.append('=');
               uri.append(mAdminValue);
               }

               int port = mRequest.getServerPort();
            */
            if (mClusterManager != null) {

                mClusterManager.resolveServerNames();
                TemplateCompilationResults results = 
                    new TemplateCompilationResults
                        (Collections.synchronizedSet(new TreeSet()),
                         new Vector());

                Clustered[] peers = 
                    (Clustered[])mClusterManager.getCluster()
                    .getKnownPeers();

                final ClusterThread[] ct = new ClusterThread[peers.length];

                for (int i=0; i<peers.length; i++) {
                    Clustered peer = peers[i];
                    ct[i] = new ClusterThread(results, peer, all);
                    ct[i].start();
                }
            
                Thread monitor = new Thread("Template reload monitor") {
                        public void run() {
                            for (int i=0; i<ct.length; i++) {
                                if (ct[i] != null) {
                                    try {
                                        ct[i].join();
                                    }
                                    catch (InterruptedException e) {
                                        break;
                                    }
                                }
                            }
                        }
                    };

                monitor.start();
                try {
                    // Wait at most 30 seconds for all servers to respond.
                    monitor.join(30000);
                }
                catch (InterruptedException e) {
                }

                monitor.interrupt();

                for (int i=0; i<ct.length; i++) {
                    if (ct[i] != null) {
                        ct[i].interrupt();
                    }
                }
                return results;
            }
            throw new RemoteException("kinda hard to reload across a cluster without a ClusterManager");
        }
        
        public TeaToolsContext.HandyClassInfo getHandyClassInfo(String fullClassName) {
            if (fullClassName != null) {
                Class clazz = getClassForName(fullClassName);
                if (clazz != null) {
                    return getHandyClassInfo(clazz);
                }
            }
            return null;
        }
        
        public TeaToolsContext.HandyClassInfo getHandyClassInfo(Class clazz) {
            if (clazz != null) {
                return new HandyClassInfoImpl(clazz);
            }
            return null;
        }       
        
        public class HandyClassInfoImpl extends TypeDescription 
            implements TeaToolsContext.HandyClassInfo
        {
            
            HandyClassInfoImpl(Class clazz) {
                super(clazz,ContextImpl.this);
            }
        }
    }
    
    private class ClusterThread extends Thread {
        private TemplateCompilationResults mResults;
        private Clustered mClusterPeer;
        private Integer mAll;

        public ClusterThread(TemplateCompilationResults res,
                             Clustered peer,
                             Integer all) {
            mResults = res;
            mClusterPeer = peer;
            mAll = all;

        }

        public void run() {
            try {

                TemplateCompilationResults res = (TemplateCompilationResults)
                    ((Restartable)mClusterPeer).restart(mAll);

                if (res != null) {
                    mResults.appendNames(res.getReloadedTemplateNames());
                    mResults.appendErrors(res.getTemplateErrors());
                }
                else {
                    mResults.appendError(new TemplateError
                                     (mClusterPeer.getServerName() 
                                      + " encountered an error while reloading templates"));
                }
            }
            catch (RemoteException re) {
                try {
                    mClusterManager.getCluster().removePeer(mClusterPeer);
                }
                catch (RemoteException re2) {
                    mLog.warn(re2);
                }
            }
        }
    }
}
