/*
 * TeaServletTemplateSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TeaServletTemplateSource.java                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.go.tea.runtime.TemplateLoader;
import com.go.tea.engine.ContextSource;
import com.go.tea.engine.ReloadLock;
import com.go.tea.engine.Template;
import com.go.tea.engine.TemplateCompilationResults;
import com.go.tea.engine.TemplateError;
import com.go.tea.engine.TemplateErrorListener;
import com.go.tea.engine.TemplateSource;
import com.go.tea.engine.TemplateSourceConfig;
import com.go.tea.engine.TemplateSourceImpl;
import com.go.teaservlet.util.RemoteCompiler;
import com.go.trove.log.Log;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.PropertyMap;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  4/03/02 <!-- $-->
 */
public class TeaServletTemplateSource extends TemplateSourceImpl {

    /** Package for templates */
    public final static String TEMPLATE_PACKAGE = "com.go.teaservlet.template";
    
    /** Short package for system templates */
    public final static String SYSTEM_PACKAGE = "system";
    
    /** Full package for system templates */
    public final static String SYSTEM_TEMPLATE_PACKAGE = TEMPLATE_PACKAGE 
        + '.' + SYSTEM_PACKAGE;
    

    private boolean mPreloadTemplates;
    private boolean mRemoteSuccess;
    private boolean mDelegatedSuccess;
    private String[] mTemplateURLs;
    private String mDefaultTemplateName;
    private String mEncoding;
    private ClassInjector mInjector;
    private TemplateSource[] mCustomTemplateSources;
    private ReloadLock mReloadLock;


    public static TeaServletTemplateSource 
        createTemplateSource(TeaServletContextSource contextSrc,
                             PropertyMap properties,
                             Log log) {
        
        TemplateSourceConfig tsConfig = new TSConfig(contextSrc,
                                                     properties, 
                                                     log);

        File destDir = TemplateSourceImpl.createTemplateClassesDir
            (properties.getString("classes"), log);
        

        TemplateSource[] customTemplateSources = 
            createCustomTemplateSources(tsConfig);

        String sourcePathString = properties.getString("path");
        File[] localDirs = null;
        String[] remoteDirs = null;

        if (sourcePathString != null) {
            StringTokenizer sourcePathTokenizer =
                new StringTokenizer(sourcePathString, ",;");

            Vector remoteVec = new Vector();
            Vector localVec = new Vector();
        
            // Sort out the local directories from those using http.
            while (sourcePathTokenizer.hasMoreTokens()) {
                String nextPath = sourcePathTokenizer.nextToken();
                if (nextPath.startsWith("http://")) {
                    remoteVec.add(nextPath);
                }
                else {
                    localVec.add(new File(nextPath));
                }
            }
        
            localDirs = (File[])localVec
                .toArray(new File[localVec.size()]);
            remoteDirs = (String[])remoteVec.toArray
                (new String[remoteVec.size()]);
        }

        return new TeaServletTemplateSource(tsConfig, localDirs,
                                            remoteDirs, destDir,
                                            customTemplateSources);
    }

    private static TemplateSource[] 
        createCustomTemplateSources(final TemplateSourceConfig config) {
        
        final PropertyMap props = config.getProperties().subMap("sources");
        List results = new Vector();
        Iterator nameIt = props.subMapKeySet().iterator();
        while(nameIt.hasNext()) {
            try {
                final String name = (String)nameIt.next();
                String className = props.getString(name + ".class");
                Class tsClass = config.getContextSource().getContextType()
                    .getClassLoader().loadClass(className);
                TemplateSource tsObj = (TemplateSource)tsClass.newInstance();
                tsObj.init(new TemplateSourceConfig() {
                        
                        private Log mLog = new Log(name, config.getLog());
                        private PropertyMap mProps = props
                            .subMap(name + ".init");
                        
                        public PropertyMap getProperties() {
                            return mProps;
                        }

                        public Log getLog() {
                            return mLog;
                        }

                        public ContextSource getContextSource() {
                            return config.getContextSource();
                        }

                        public String getPackagePrefix() {
                            return config.getPackagePrefix();
                        }

                        public boolean isExceptionGuardianEnabled() {
                            return config.isExceptionGuardianEnabled();
                        }
                        
                    });
                results.add(tsObj);
            }
            catch (Exception e) {
                config.getLog().warn(e);
            }
        }
        if (results == null) {
            config.getLog().debug("null results vector");
        }

        TemplateSource[] tSrc = (TemplateSource[])results
            .toArray(new TemplateSource[results.size()]);
        if (tSrc == null) {
            config.getLog().debug("null results array");
            tSrc = new TemplateSource[0];
        }
        return tSrc;
    }

    private TeaServletTemplateSource(TemplateSourceConfig config, 
                                     File[] localTemplateDirs, 
                                     String[] remoteTemplateURLs, 
                                     File compiledTemplateDir,
                                     TemplateSource[] customSources) {

        super();
        
        //since I'm not calling init to parse the config, set things up.
        mConfig = config;
        mLog = config.getLog();
        mProperties = config.getProperties();
        mLog.info("initializing the TeaServletTemplateSource.");

        mReloadLock = new ReloadLock();
        setTemplateRootDirs(localTemplateDirs);
        setDestinationDirectory(compiledTemplateDir);
        
        if (customSources == null) {
            mLog.debug("No custom TemplateSources configured.");
        }
        else {
            mLog.info(customSources.length 
                       + " custom TemplateSources configured.");
        }
    
        mCustomTemplateSources = customSources;
        mTemplateURLs = remoteTemplateURLs;
        mDefaultTemplateName = config.getProperties().getString("default");
        mEncoding = config.getProperties()
            .getString("file.encoding", "ISO-8859-1");
        mPreloadTemplates = config.getProperties().getBoolean("preload", true);
        
        if (mCompiledDir == null && !mPreloadTemplates) {
            mLog.warn("Now preloading templates.");
            mPreloadTemplates = true;
        }
    }


    public int getKnownTemplateCount() {
        int total = super.getKnownTemplateCount();
        for (int j = 0; j < mCustomTemplateSources.length; j++) {
            total += mCustomTemplateSources[j].getKnownTemplateCount();
        }
        return total;
    }

    public String[] getKnownTemplateNames() {
        String[] allNames = new String[getKnownTemplateCount()];
        String[] names = super.getKnownTemplateNames();
        System.arraycopy(names, 0, allNames, 0, names.length);
        int pos = names.length;
        for (int j = 0; j < mCustomTemplateSources.length; j++) {
            names = mCustomTemplateSources[j].getKnownTemplateNames();
            System.arraycopy(names, 0, allNames, pos, names.length);
            pos += names.length;
        }

        if (pos < allNames.length) {
            String[] tmp = new String[pos];
            if (pos > 0) {
                System.arraycopy(allNames, 0, tmp, 0, pos);
            }
            allNames = tmp;
        }
        return allNames;
    }
    
    public TemplateCompilationResults
        compileTemplates(ClassInjector commonInjector, 
                         boolean all) throws Exception {

        return compileTemplates(commonInjector, all, true);
    }

    private TemplateCompilationResults
        compileTemplates(ClassInjector commonInjector, 
                         boolean all, boolean enforceReloadLock) 
        throws Exception {

        synchronized(mReloadLock) {
            if (mReloadLock.isReloading() && enforceReloadLock) {
                return new TemplateCompilationResults();
            }
            else {
                mReloadLock.setReloading(true);
            }
            
        }
     
        try {
            if (commonInjector == null) {
                commonInjector = createClassInjector();
            }
            else {
                mLog.debug("at this point, the injector should still be null since the template source delegation starts here");
            }
     
            Results results = 
                actuallyCompileTemplates(commonInjector, all);

            for (int j = 0; j < mCustomTemplateSources.length; j++) {
                TemplateCompilationResults delegateResults = 
                    mCustomTemplateSources[j].compileTemplates(commonInjector,all);
                if (delegateResults.isAlreadyReloading()) {
                    return delegateResults;
                }
                else {
                    TemplateCompilationResults transients = 
                        results.getTransientResults();
                    transients.appendNames(delegateResults
                                        .getReloadedTemplateNames());
                    transients
                        .appendErrors(delegateResults.getTemplateErrors());
                }
            }
            
            compileRemoteTemplates(all,
                                   commonInjector,
                                   results);
        
            if (results.getTransientResults().isSuccessful() 
                || mResults == null) {

                mResults = results;

                if (mPreloadTemplates) {
                    try {       
                        preloadTemplates(this);
                        for (int j = 0; j < mCustomTemplateSources.length; j++) {
                            preloadTemplates(mCustomTemplateSources[j]);
                        }
                    } 
                    catch (Throwable t) {
                        if (all == false) {
                            return compileTemplates(null, true, false);
                        }
                        mLog.error(t);
                    }
                }
            }
            return results.getTransientResults();
        }
        finally {       
            synchronized(mReloadLock) {
                mReloadLock.setReloading(false);
            }
        }
    }

    public String getDefaultTemplateName() {
        return mDefaultTemplateName;
    }

    private void preloadTemplates(TemplateSource ts) 
        throws Throwable {
        
        String[] knownTemplateNames = ts.getKnownTemplateNames();
        for (int j = 0; j < knownTemplateNames.length; j++) {
            getTemplate(knownTemplateNames[j]);
        }
    }


    private void compileRemoteTemplates(boolean force, 
                                        ClassInjector injector,
                                        Results results) 
        throws Exception {

        if (mTemplateURLs != null && mTemplateURLs.length > 0) {
            TemplateErrorListener errorListener = createErrorListener();
                RemoteCompiler rmcomp = 
                    new RemoteCompiler(mTemplateURLs,
                                       TEMPLATE_PACKAGE,
                                       mCompiledDir,
                                       injector,
                                       mEncoding);
                rmcomp.setClassLoader(injector);
                rmcomp.setRuntimeContext(getContextSource().getContextType());
                rmcomp.setExceptionGuardianEnabled
                    (mConfig.isExceptionGuardianEnabled());
                rmcomp.addErrorListener(errorListener);
                rmcomp.setForceCompile(force);
                results.getKnownTemplateNames()
                    .addAll(Arrays.asList(rmcomp.getAllTemplateNames()));
                TemplateCompilationResults transients = 
                    results.getTransientResults();
                transients.appendNames(Arrays.asList(rmcomp.compileAll()));
                transients.appendErrors(errorListener.getTemplateErrors());
        }
    }

    private static class TSConfig implements TemplateSourceConfig {

        private ContextSource mContextSource;
        private PropertyMap mProperties;
        private Log mLog;
        
        TSConfig(ContextSource contextSource,
                 PropertyMap properties, Log log) {
            mContextSource = contextSource;
            mProperties = properties;
            mLog = log;
        }

        public ContextSource getContextSource() {
            return mContextSource;
        }
        
        public String getPackagePrefix() {
            return TEMPLATE_PACKAGE;
        }
        
        public boolean isExceptionGuardianEnabled() {
            return mProperties.getBoolean("exception.guardian", false);
        }
        
        public PropertyMap getProperties() {
            return mProperties;
        }

        public Log getLog() {
            return mLog;
        }
    }
}


