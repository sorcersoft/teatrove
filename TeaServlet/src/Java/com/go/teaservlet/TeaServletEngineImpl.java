/*
 * TeaServletEngineImpl.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TeaServletEngineImpl.java                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;


import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.go.trove.log.*;
import com.go.trove.util.Utils;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.Config;
import com.go.trove.util.ConfigSupport;
import com.go.trove.util.PropertyMap;
import com.go.trove.util.PropertyParser;
import com.go.trove.util.plugin.Plugin;
import com.go.trove.util.plugin.PluginContext;
import com.go.trove.io.ByteBuffer;
import com.go.tea.runtime.Context;
import com.go.tea.runtime.TemplateLoader;

import com.go.tea.engine.*;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  3/05/02 <!-- $-->
 */
public class TeaServletEngineImpl implements TeaServletEngine {
    
    private static final boolean DEBUG = false;

    // fields needed for implementing the TeaServletEngine interface
    private Log mLog;
    private PropertyMap mProperties;
    private ServletContext mServletContext;
    private String mServletName;

    private ApplicationDepot mApplicationDepot;
    private TeaServletTemplateSource mTemplateSource;
    private List mLogEvents;   

    private PluginContext mPluginContext;

    protected void startEngine(PropertyMap properties,
                               ServletContext servletContext,
                               String servletName,
                               Log log,
                               List memLog, 
                               PluginContext plug) 
        throws ServletException {
        
        try {
            setProperties(properties);
            setServletContext(servletContext);
            setServletName(servletName);
            setLog(log);
            setLogEvents(memLog);
            setPluginContext(plug);
            mApplicationDepot = new ApplicationDepot(this);

            mLog.debug("loading templates");
            getTemplateSource().compileTemplates(null, false);
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // Methods required to implement TeaServletEngine.

    public String getInitParameter(String name) {
        return mProperties.getString(name);
    }

    public Enumeration getInitParameterNames() {
        return Collections.enumeration(mProperties.keySet());
    }

    public String getServletName() {
        return mServletName;
    }

    private void setServletName(String name) {
        mServletName = name;
    }

    public ServletContext getServletContext() {
        return mServletContext;
    }

    private void setServletContext(ServletContext context) {
        mServletContext = context;
    }

    public String getName() {
        return mLog.getName();
    }

    public Log getLog() {
        return mLog;
    }

    private void setLog(Log log) {
        mLog = log;
    }

    public Plugin getPlugin(String name) {
        if (mPluginContext == null) {
            return null;
        }
        return mPluginContext.getPlugin(name);
    }

    public Map getPlugins() {
        if (mPluginContext == null) {
            return Utils.VOID_MAP;
        }
        return mPluginContext.getPlugins();
    }

    private void setPluginContext(PluginContext pContext) {
        mPluginContext = pContext;
    }

    public PropertyMap getProperties() {
        return mProperties;
    }

    private void setProperties(PropertyMap properties) {
        mProperties = properties;
    }

    public TeaServletTransaction createTransaction
        (HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        return createTransaction(request, response, false);
    }

    public TeaServletTransaction createTransaction
        (HttpServletRequest request, HttpServletResponse response, 
         boolean lookupTemplate)
        throws IOException {


        try {

            TeaServletTemplateSource templateSrc = getTemplateSource();

            TeaServletContextSource contextSrc = (TeaServletContextSource)
                templateSrc.getContextSource();

            Template template = null;

            if (lookupTemplate) {
                // get template path
                String path;
                if ((path = request.getPathInfo()) == null) {
                    if ((path = request.getServletPath()) == null) {
                        path = "/";
                    }
                    else {
                        // Strip off any extension.
                        int index = path.lastIndexOf('.');
                        if (index >= 0) {
                            path = path.substring(0, index);
                        }
                    }
                }
        
                if (DEBUG) {
                    // add some comic relief!
                    mLog.debug("aagggghhh... i've been hit! (" + path + ")");
                    mLog.debug("Finding template for " + path);
                }
            
                // Find the matching template.
                template = findTemplate(path, request, response, templateSrc);
            }

            // Wrap the user's http response.
            ApplicationResponse appResponse = 
                new ApplicationResponseImpl(response, this);


            ApplicationRequest appRequest = 
                (lookupTemplate 
                 ? (new ApplicationRequestImpl
                     (request, 
                      contextSrc
                      .getApplicationContextTypes(), 
                      template)) 
                 : (new ApplicationRequestImpl
                     (request, 
                      contextSrc.getApplicationContextTypes(), 
                      templateSrc.getTemplateLoader())));
                            
            try {
                /*
                 * TODO: I dislike this circular logic of having the context 
                 * contain the response and the response containing the 
                 * context, changing this will require a redesign of the 
                 * response.
                 */
                ((ApplicationResponseImpl)appResponse)
                    .setRequestAndHttpContext(createHttpContext(appRequest,
                                                                appResponse, 
                                                                contextSrc),
                                              appRequest);
            }
            catch (Exception e) {
                throw new ServletException(e);
            }        

            return new RequestAndResponse(appRequest, appResponse);
        
        }
        catch (ServletException se) {
            return null;
        }
    }

    /**
     * Returns the ApplicationDepot, which is used by the admin functions.
     */
    public ApplicationDepot getApplicationDepot() {
        return mApplicationDepot;
    }

    public TeaServletTemplateSource getTemplateSource() {
        if (mTemplateSource == null) {
            return reloadTemplateSource();
        }
        return mTemplateSource;
    }    

    /**
     * Destroys the TeaServlet and the user's application.
     */
    public void destroy() {
        if (mApplicationDepot != null) {
            mLog.info("Destroying ApplicationDepot");
            mApplicationDepot.destroy();
        }
    }


    /**
     * Returns the lines that have been written to the log file. This is used
     * by the admin functions.
     */
    public LogEvent[] getLogEvents() {
        if (mLogEvents == null) {
            return new LogEvent[0];
        }
        else {
            LogEvent[] events = new LogEvent[mLogEvents.size()];
            return (LogEvent[])mLogEvents.toArray(events);
        }
    }

    private void setLogEvents(List memLog) {
        mLogEvents = memLog;
    }

    //template stuff
    public TeaServletTemplateSource reloadTemplateSource() {
        mTemplateSource = createTemplateSource();
        return mTemplateSource;
    }
    
    public String[] getTemplatePaths() {
        try {
            String[] paths = getTemplateSource().getKnownTemplateNames();
            for (int i = 0; i < paths.length; i++) {
                paths[i] = paths[i].replace('.', '/');
            }
            return paths;
        }
        catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Finds a template based on the given URI. If path ends in a slash, revert
     * to loading default template. If default not found or not specified,
     * return null.
     *
     * @param uri  the URI of the template to find
     * @return the template that maps to the URI or null if no template maps to
     *         the URI
     */
    public Template findTemplate(String uri,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws ServletException, IOException {

        return findTemplate(uri, request, response, getTemplateSource());
    }

    public Template findTemplate(String uri,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 TemplateSource templateSrc)
        throws ServletException, IOException {

        Template template = null;
        try {
            // If path ends in a slash, revert to loading default template. If
            // default not found or not specified, return null.
            boolean useDefault = uri.endsWith("/");

            // Trim slashes and replace with dots.
            while (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            while (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            String name = uri.replace('/', '.');
            
            // Look up template if not trying to use default.
            if (!useDefault) {
                // Find template that matches the uri
                try {
                    template = templateSrc.getTemplate(name);
                }
                catch (ClassNotFoundException e) {
                    mLog.debug("Can't find template \"" + name + "\": " + e);
                    template = null;
                }
            }

            // Use default if no template found so far.
            if ((template == null) 
                && (templateSrc instanceof TeaServletTemplateSource)) {
                TeaServletTemplateSource tsTsrc = (TeaServletTemplateSource)
                    templateSrc;
                if (tsTsrc.getDefaultTemplateName() != null) {
                    if  (name.length() == 0) {
                        name = tsTsrc.getDefaultTemplateName();
                    }
                    else {
                        name = name + '.' 
                            + tsTsrc.getDefaultTemplateName();
                    }
                }
                try {
                    template = tsTsrc.getTemplate(name);

                    // Redirect if no slash on end of URI.
                    if (template != null && !useDefault) {
                        StringBuffer location = 
                            new StringBuffer(request.getRequestURI());
                        int length = location.length();
                        if (length == 0 
                            || location.charAt(length - 1) != '/') {
                            location.append('/');
                        }
                        String query = request.getQueryString();
                        if (query != null) {
                            location.append('?').append(query);
                        }
                        response.setStatus(response.SC_MOVED_PERMANENTLY);
                        response.sendRedirect(location.toString());
                    }
                }
                catch (ClassNotFoundException e) {
                    mLog.debug("Can't find default template \"" +
                               name + "\": " + e);
                }
            }
        }
        catch (NoClassDefFoundError e) {
            // The file system let the class load, but because some file
            // systems support multiple ways in which a file can be found, the
            // class's exact name may not match. Just report the template
            // as not being found.
            return null;
        }
        catch (NoSuchMethodException e) {
            throw new ServletException("Template at \"" + uri 
                                       + "\" is invalid", e);
        }
        catch (LinkageError e) {
            throw new ServletException("Template at \"" + uri 
                                       + "\" is invalid", e);
        }
        return template;
    }

    /**
     * Lets external classes use the HttpContext for their own, possibly
     * malicious purposes.
     */
    public HttpContext createHttpContext(ApplicationRequest req, 
                                         ApplicationResponse resp) 
        throws Exception {
        
        return createHttpContext(req, resp,
                                 mApplicationDepot.getContextSource());
    }

    private HttpContext createHttpContext(ApplicationRequest req, 
                                          ApplicationResponse resp,
                                          ContextSource cs) 
        throws Exception {
        
        return (HttpContext)cs
            .createContext(new RequestAndResponse(req, resp));
    }

    /**
     * @return a newly created template source using a composite context
     * for all the applications in the ApplicationDepot
     */
    private TeaServletTemplateSource createTemplateSource() {
        return TeaServletTemplateSource
            .createTemplateSource((TeaServletContextSource)
                                  getApplicationDepot().getContextSource(),
                                  getProperties()
                                  .subMap("template"),
                                  getLog());
    }

}






