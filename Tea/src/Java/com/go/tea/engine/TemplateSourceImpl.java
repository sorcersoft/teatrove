/*
 * TemplateSourceImpl.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TemplateSourceImpl.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import com.go.tea.compiler.CompilationUnit;
import com.go.tea.compiler.ErrorEvent;
import com.go.tea.compiler.ErrorListener;
import com.go.tea.compiler.SourceInfo;
import com.go.tea.runtime.Context;
import com.go.tea.runtime.TemplateLoader;
import com.go.tea.util.FileCompiler;
import com.go.tea.util.StringCompiler;
import com.go.tea.util.ResourceCompiler;
import com.go.trove.io.LinePositionReader;
import com.go.trove.log.Log;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.PropertyMap;

/******************************************************************************
 * This class should be created using the {@link TemplateSourceFactory} 
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/20/02 <!-- $-->
 */
public class TemplateSourceImpl implements TemplateSource {

    /**
     * converts a path to a File for storing compiled template classes.
     * @return the directory or null if the directory is not there or
     * if it cannot be written to.
     */
    public static File createTemplateClassesDir(String dirPath, Log log) {
        File destDir = null;
        if (dirPath != null) {
            destDir = new File(dirPath);
            if (!destDir.isDirectory()) {
                // try creating it but not the parents.
                if (!destDir.mkdir()) {
                    log.warn("Could not create template classes directory: " 
                              + destDir.getAbsolutePath());
                    destDir = null;
                }
            }
            if (destDir != null && !destDir.canWrite()) {
                log.warn("Unable to write to template classes directory: " 
                          + destDir.getAbsolutePath());
                destDir = null;
            }
        }
        return destDir;
    }

    public static String[] chopString(String target, String delimiters) {
        if (target != null) {
            StringTokenizer st = new StringTokenizer(target, delimiters);
            String[] chopped = new String[st.countTokens()];
            for (int j = 0; st.hasMoreTokens(); j++) {
                chopped[j] = st.nextToken();
            }
            return chopped;                 
        }
        return null;
    }

    // fields that subclasses might need access to
    protected TemplateSourceConfig mConfig;
    protected Log mLog;  
    protected PropertyMap mProperties;
    protected File mCompiledDir;
    protected TemplateErrorListener mErrorListener;    

    // fields specific to this implementation
    private File[] mTemplateRootDirs;
    private String[] mTemplateResources;
    private String[] mTemplateStrings;
    private Boolean mReloading;
    
 
    // result fields
    private TemplateLoader mLoader;
    private boolean mSuccessfulReload;
    private Date mLastReloadTime;
    private Set mReloadedTemplateNames;
    private Set mKnownTemplateNames;
    private Map mWrappedTemplates;
    
    // no arg constructor for dynamic classloading.
    public TemplateSourceImpl() {
    }

    public void init(TemplateSourceConfig config) {
        mConfig = config;
        mLog = config.getLog();
        mProperties = config.getProperties();
        mLog.info("initializing template source");

        mTemplateRootDirs = parseRootDirs(mProperties);
        
        mTemplateResources = chopString(mProperties
                                        .getString("resource"),";,");
        
        mCompiledDir = createTemplateClassesDir
            (mProperties.getString("classes"), mLog);
    }

    
    public synchronized boolean 
        compileTemplates(ClassInjector injector, boolean all) 
        throws Exception {
        
        if (injector == null) {
            injector = createClassInjector();
        }

        mReloadedTemplateNames = new TreeSet();
        mKnownTemplateNames = new TreeSet();
        mWrappedTemplates = new HashMap();
        mLastReloadTime = new Date();
        mErrorListener = createErrorListener();
        Class type = mConfig.getContextSource().getContextType();
        boolean exceptionGuardian = mConfig.isExceptionGuardianEnabled();
        String prefix = mConfig.getPackagePrefix();

        // create a FileCompiler and compile templates.
        if (mTemplateRootDirs != null 
            && mTemplateRootDirs.length > 0) {
                    
            FileCompiler fcomp = 
                new FileCompiler(mTemplateRootDirs,
                                 mConfig.getPackagePrefix(),
                                 mCompiledDir,
                                 injector);
            fcomp.setClassLoader(injector);
            fcomp.setRuntimeContext(type);
            fcomp.setExceptionGuardianEnabled(exceptionGuardian);
            fcomp.addErrorListener(mErrorListener);
            fcomp.setForceCompile(all);
            mReloadedTemplateNames
                .addAll(Arrays.asList(fcomp.compileAll()));
            mKnownTemplateNames
                .addAll(Arrays.asList(fcomp.getAllTemplateNames()));
        }
        if (mTemplateStrings != null && mTemplateStrings.length > 0) {
            Collection names = Arrays
                .asList(compileFromStrings(type,injector, 
                                           prefix,
                                           mTemplateStrings,
                                           exceptionGuardian));
            mReloadedTemplateNames.addAll(names);
            mKnownTemplateNames.addAll(names);
        }

        if (mTemplateResources != null 
            && mTemplateResources.length > 0) {
            Collection names = Arrays
                .asList(compileFromResourcePaths
                        (type, injector,
                         prefix,
                         mTemplateResources,
                         exceptionGuardian));
            mReloadedTemplateNames.addAll(names);
            mKnownTemplateNames.addAll(names);
        }
        
        
        mLoader = new TemplateAdapter(type, injector, 
                                     mConfig.getPackagePrefix());


        mSuccessfulReload = (mErrorListener
                             .getTemplateErrors().length == 0);
        
        // notify any threads waiting on the initial compilation.
        notify();

        return mSuccessfulReload;
        
    }

    public ContextSource getContextSource() {
        return mConfig.getContextSource();
    }

    public int getKnownTemplateCount() {
        if (mKnownTemplateNames == null) {
            return 0;
        }
        return mKnownTemplateNames.size();
    }

    public String[] getKnownTemplateNames() {

        if (mKnownTemplateNames == null) {
            return new String[0];
        }
        return (String[])mKnownTemplateNames
            .toArray(new String[mKnownTemplateNames.size()]);
    }

    public String[] getReloadedTemplateNames() {

        if (mReloadedTemplateNames == null) {
            return new String[0];
        }
        return (String[])mReloadedTemplateNames
            .toArray(new String[mReloadedTemplateNames.size()]);
    }

    public Date getTimeOfLastReload() {
        return mLastReloadTime;
    }

    public boolean isExceptionGuardianEnabled() {
        return mConfig.isExceptionGuardianEnabled();
    }

    public boolean isSuccessful() {
        return mSuccessfulReload;
    }

    public TemplateError[] getTemplateErrors() {
        if (mErrorListener == null) {
            return new TemplateError[0];
        }
        return mErrorListener.getTemplateErrors();
    }

    public com.go.tea.engine.Template[] getLoadedTemplates() {
        if (mWrappedTemplates != null) {        
            return (com.go.tea.engine.Template[])mWrappedTemplates.values()
                .toArray(new com.go.tea.engine.Template
                    [mWrappedTemplates.size()]);
        }
        return new com.go.tea.engine.Template[0];
    }

    public com.go.tea.engine.Template getTemplate(String name) 
        throws ClassNotFoundException, NoSuchMethodException {
        com.go.tea.engine.Template wrapped = null;
        try {
            wrapped = (com.go.tea.engine.Template)
                mWrappedTemplates.get(name);
            if (wrapped == null) {
                wrapped = new TemplateImpl
                    (getTemplateLoader().getTemplate(name), this);
            }
            mWrappedTemplates.put(name, wrapped);
        }
        catch (NullPointerException npe) {
            throw new ClassNotFoundException
                ("TemplateLoader not yet available");
        }
        return wrapped;
    }


    /**
     * provides subclasses with access to modify the KnownTemplateNames
     */
    protected Set getKnownTemplateNameSet() {
        return mKnownTemplateNames;
    }
      
    /**
     * provides subclasses with access to modify the ReloadedTemplateNames
     */
    protected Set getReloadedTemplateNameSet() {
        return mReloadedTemplateNames;
    }

    /**
     * allows a subclass to set the source strings directly rather than using 
     * init to parse the config.
     */
    protected void setTemplateStrings(String[]  sourceData) {
        mTemplateStrings = sourceData;
    }


    /**
     * allows a subclass to set the resource paths directly rather than using 
     * init to parse the config.
     */
    protected void setTemplateResources(String[]  resourcePaths) {
        mTemplateResources = resourcePaths;
    }

    /**
     * allows a subclass to directly specify the directories to be searched 
     * for template sources.
     */
    protected void setTemplateRootDirs(File[] rootDirs) {
        mTemplateRootDirs = rootDirs;
    }

    /**
     * allows a subclass to set directory to write the compiled templates
     * this directory may be overridden if the ClassInjector passed into the 
     * compileTemplates() method points to a different location.
     */
    protected void setDestinationDirectory(File  compiledDir) {
        mCompiledDir = compiledDir;
    }


    /** 
     * provides a default class injector using the contextType's ClassLoader
     * as a parent.
     */
    protected ClassInjector createClassInjector() throws Exception {

        return new ResolvingInjector(mConfig.getContextSource()
                                     .getContextType().getClassLoader(), 
                                     new File[] {mCompiledDir}, 
                                     mConfig.getPackagePrefix(), 
                                     false);
    }

    protected TemplateErrorListener createErrorListener() {
        return new ErrorRetriever();
    }

    private File[] parseRootDirs(PropertyMap properties) {
        String[] roots = chopString(properties.getString("path"), ";,");
        File[] rootDirs = null;
        if (roots != null) {
            rootDirs = new File[roots.length];
            for (int j = 0; j < roots.length; j++) {
                rootDirs[j] = new File(roots[j]);
            }
        }
        return rootDirs;
    }

    private String[] compileFromResourcePaths(Class contextType,
                                              ClassInjector injector,
                                              String packagePrefix,
                                              String[] resourcePaths,
                                              boolean guardian)
        throws Exception {

        ResourceCompiler rcomp = new ResourceCompiler(injector, 
                                                      packagePrefix);
        rcomp.setClassLoader(injector);
        rcomp.setRuntimeContext(contextType);
        rcomp.setExceptionGuardianEnabled(guardian);
        rcomp.addErrorListener(mErrorListener);
        String[] result = rcomp.compile(mTemplateResources);

        // show error count and messages.
        mLog.info(rcomp.getErrorCount() 
                           + " Resource compilation errors.");

        mLog.info(Integer.toString(result.length));
        for (int j = 0;j < result.length; j++) {
            mLog.info(result[j]);
        }

        return result;

    }

    private String[] compileFromStrings(Class contextType,
                                        ClassInjector injector, 
                                        String packagePrefix,
                                        String[] templateSourceStrings,
                                        boolean guardian) 
        throws Exception {

        StringCompiler scomp = new StringCompiler(injector, packagePrefix);
        scomp.setClassLoader(injector);
        scomp.setRuntimeContext(contextType);
        scomp.setExceptionGuardianEnabled(guardian);
        scomp.addErrorListener(mErrorListener);
        
        String[] templateNames = new String[mTemplateStrings.length];

        // extract the template name from the template source string.
        for (int j = 0; j < templateNames.length; j++) {
            int index = mTemplateStrings[j].indexOf("template ") 
                + "template ".length();
            templateNames[j] = mTemplateStrings[j]
                .substring(index,
                           mTemplateStrings[j].indexOf('(',index));

            //prepend the template's name with the package prefix
            /*
              if (mPackagePrefix != null) {
              templateNames[j] = mPackagePrefix + '.' 
              + templateNames[j];
              }
            */
            scomp.setTemplateSource(templateNames[j],
                                    mTemplateStrings[j]);
        }

        String[] result = scomp.compile(templateNames);

        /*
         * now strip the package prefix since the TemplateLoader will 
         * prepending it.
         
         if (mPackagePrefix != null) {
         for (int k = 0; k < result.length; k++) {
         result[k] = result[k]
         .substring(mPackagePrefix.length() + 1);
         }
         }
        */

        // show error count and messages.
        mLog.info(scomp.getErrorCount() 
                           + " String compilation errors.");

        mLog.info(Integer.toString(result.length));
        for (int j = 0;j < result.length; j++) {
            mLog.info(result[j]);
        }

        return result;
    }

    private TemplateLoader getTemplateLoader() {
        // only synchronize if needed.
        if (mLoader == null) {
            synchronized(this) {
                try {
                    while (mLoader == null) {
                        // the initial recompilation should be in progress, 
                        // wait a second to see if it's done
                        wait(1000);
                    }
                }
                catch (InterruptedException ie) {
                    return null;
                }
            }
        }
        return mLoader;
    }

    private class ResolvingInjector extends ClassInjector {


        public ResolvingInjector(ClassLoader cl, File[] classDirs, 
                                 String pkg, boolean keepByteCode) {

            super(cl, classDirs, pkg, keepByteCode);
        }

        public Class loadClass(String className) 
            throws ClassNotFoundException {
            return loadClass(className, true);
        }
    }

    private class ErrorRetriever implements TemplateErrorListener {
        private Collection mTemplateErrors = new ArrayList();

        /** Reads error line from template files */
        private LinePositionReader mOpenReader;

        private CompilationUnit mOpenUnit;

        public TemplateError[] getTemplateErrors() {
            return (TemplateError[])mTemplateErrors.toArray
                (new TemplateError[mTemplateErrors.size()]);
        }

        /**
         * This method is called for each error that occurs while compiling
         * templates. The error is reported in the log and in the error list.
         */
        public void compileError(ErrorEvent event) {
            mConfig.getLog().warn("Error in " 
                                  + event.getDetailedErrorMessage());
                       
            SourceInfo info = event.getSourceInfo();
            if (info == null) {
                mTemplateErrors.add(new TemplateError(event, "", 0, 0, 0));
                return;
            }

            CompilationUnit unit = event.getCompilationUnit();
            if (unit == null) {
                mTemplateErrors.add(new TemplateError(event, "", 0, 0, 0));
                return;
            }

            int line = info.getLine();
            int errorStartPos = info.getStartPosition();
            int errorEndPos = info.getEndPosition();
            int detailPos = info.getDetailPosition();

            try {
                if (mOpenReader == null ||
                    mOpenUnit != unit ||
                    mOpenReader.getLineNumber() >= line) {

                    if (mOpenReader != null) {
                        mOpenReader.close();
                    }
                    mOpenUnit = unit;
                    mOpenReader = new LinePositionReader
                        (new BufferedReader(unit.getReader()));
                }

                mOpenReader.skipForwardToLine(line);
                int linePos = mOpenReader.getNextPosition();

                String lineStr = mOpenReader.readLine();
                lineStr = mOpenReader.cleanWhitespace(lineStr);

                TemplateError te = new TemplateError
                    (event, lineStr,
                     errorStartPos - linePos,
                     detailPos - linePos,
                     errorEndPos - linePos);

                mTemplateErrors.add(te);
            }
            catch (IOException ex) {
                mTemplateErrors.add(new TemplateError(event, "", 0, 0, 0));
                mLog.error(ex);
            }
        }

        public void finalize() {
            // Close the template error reader.
            if (mOpenReader != null) {
                try {
                    mOpenReader.close();
                }
                catch (IOException e) {
                    mConfig.getLog().error(e);
                }
            }
            mOpenReader = null;
            mOpenUnit = null;
        }    
    }

    private class TemplateImpl implements com.go.tea.engine.Template {
        private TemplateLoader.Template mTemplate;
        private TemplateSource mSource;

        protected TemplateImpl(TemplateLoader.Template template,
                               TemplateSource source) {
            mTemplate = template;
            mSource = source;
        }

        public TemplateSource getTemplateSource() {
            return mSource;
        }

        public TemplateLoader getTemplateLoader() {
            return mTemplate.getTemplateLoader();
        }

        public String getName() {
            return mTemplate.getName();
        }
    
        public Class getTemplateClass() {
            return mTemplate.getTemplateClass();
        }

        public Class getContextType() {
            return mTemplate.getContextType();
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
            if (context == null) {
                throw new Exception("cannot execute against a  null context");
            }
            mTemplate.execute(context, parameters);
        }
    }
}
