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

package com.go.teaservlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.StreamTokenizer;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Vector;
import com.go.trove.net.HttpClient;
import com.go.trove.net.SocketFactory;
import com.go.trove.net.PlainSocketFactory;
import com.go.trove.net.PooledSocketFactory;
import com.go.trove.util.ClassInjector;
import com.go.trove.io.DualOutput;
import com.go.tea.compiler.Compiler;
import com.go.tea.compiler.CompilationUnit;
import com.go.tea.compiler.ErrorListener;
import com.go.tea.util.AbstractFileCompiler;

/******************************************************************************
 * RemoteCompiler compiles tea source files by reading them from a remote 
 * location specified by a URL. The compiled code can be written as class files
 * to a given destination directory, they can be passed to a ClassInjector, or
 * they can be sent to both.
 *
 * <p>When given a URL, RemoteCompiler compiles all files with the
 * extension ".tea". If a destination directory is used, tea files that have a
 * matching class file that is more up-to-date will not be compiled, unless
 * they are forced to be re-compiled.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$--> 21 <!-- $$JustDate:-->  2/25/02 <!-- $-->
 * @see ClassInjector
 */
public class RemoteCompiler extends AbstractFileCompiler {
    
    private static final String TEMPLATE_LOAD_PROTOCOL = "http://";
    private String[] mRemoteSourceDirs;
    private String mRootPackage;
    private File mRootDestDir;
    private ClassInjector mInjector;
    private String mEncoding;
    private boolean mForce = false;
    private Map mTemplateMap;
    private Map mSocketFactories;
    
    
    public RemoteCompiler(String[] rootSourceDirs,
                          String rootPackage,
                          File rootDestDir,
                          ClassInjector injector,
                          String encoding) {
        super();
        mRemoteSourceDirs = rootSourceDirs;
        mRootPackage = rootPackage;
        mRootDestDir = rootDestDir;
        mInjector = injector;
        mEncoding = encoding;

        if (mRootDestDir != null && 
            !mRootDestDir.isDirectory()) {
            throw new IllegalArgumentException
                ("Destination is not a directory: " + rootDestDir);
        }
        mSocketFactories = new HashMap();
        mTemplateMap = retrieveTemplateMap();
    }

    /**
     * @param force When true, compile all source, even if up-to-date
     */
    public void setForceCompile(boolean force) {
        mForce = force;
    }       
    
    /**
     * Checks that the source code for a specified template exists.
     */
    public boolean sourceExists(String name) {
        return mTemplateMap.containsKey(name);
    }
    
    public String[] getAllTemplateNames() {
        return (String[])mTemplateMap.keySet().toArray(new String[0]);
    }

    /**
     * Overrides the method from Compiler to allow timestamp synchronization
     * after the actual compilation has taken place.
     */
    public String[] compile(String[] names) throws IOException {
        String[] compiled = super.compile(names);
        for (int j=0;j<names.length;j++) {
            syncSources(names[j]);
        }
        return compiled;
    }
    
    protected CompilationUnit createCompilationUnit(String name) {
        return new Unit(name,this);
    }

    /**
     * sets the template classes to have the same timestamp as the sources
     * to account for differences between the machine clocks.
     */
    private void syncSources(String name) {
        TemplateSourceInfo info = (TemplateSourceInfo)mTemplateMap.get(name);
        Unit compUnit = (Unit)this.getCompilationUnit(name,null);
        File destFile = compUnit.getDestinationFile();
        if (destFile != null) {
            destFile.setLastModified(info.timestamp);
        }
    }
      
    /**
     * returns a socket connected to a host running the TemplateServerServlet
     */
    private HttpClient getTemplateServerClient(String remoteSource) throws IOException {

        SocketFactory factory = (SocketFactory)mSocketFactories.get(remoteSource);

        if (factory == null) {

            int port = 80;
            String host = remoteSource.substring(TEMPLATE_LOAD_PROTOCOL.length());

            int portIndex = host.indexOf("/");
        
            if (portIndex >= 0) {
                host = host.substring(0,portIndex);                         
            }
            String hostPort = host;        
            portIndex = host.indexOf(":");
            if (portIndex >= 0) {
                try {
                    port = Integer.parseInt(host.substring(portIndex+1));
                }
                catch (NumberFormatException nfe) {
                    System.out.println("Invalid port number specified");
                }
                host = host.substring(0,portIndex);
            }
            factory = new PooledSocketFactory(new PlainSocketFactory(InetAddress.getByName(host), 
                                                                         port, 5000));
            
            if (factory != null) {
                mSocketFactories.put(hostPort, factory);
            }
        }
        return new HttpClient(factory);
    }
    
    /**
     * turns a template name and a servlet path into a  
     */
    private String createTemplateServerRequest(String servletPath,String templateName) {
        String pathInfo = servletPath.substring(servletPath.indexOf("/",TEMPLATE_LOAD_PROTOCOL.length()));
        if (templateName != null) {            
            pathInfo = pathInfo + templateName;
        }
        return pathInfo;
    }
    
    /**
     * creates a map relating the templates found on the template server
     * to the timestamp on the sourcecode
     */
    private Map retrieveTemplateMap() {
        Map templateMap = new TreeMap();
        for (int j=0;j<mRemoteSourceDirs.length;j++) {
            String remoteSource = mRemoteSourceDirs[j];
            if (!remoteSource.endsWith("/")) {
                remoteSource = remoteSource + "/";
            }
            
            try {
                HttpClient tsClient = getTemplateServerClient(remoteSource);

                HttpClient.Response response = tsClient.setURI(createTemplateServerRequest(remoteSource,null))
                    .setPersistent(true).getResponse(); 

                if (response != null && response.getStatusCode() == 200) {

                    Reader rin = new InputStreamReader
                        (new BufferedInputStream(response.getInputStream()));
                
                    StreamTokenizer st = new StreamTokenizer(rin);
                    st.resetSyntax();
                    st.wordChars('!','{');
                    st.wordChars('}','}');
                    st.whitespaceChars(0,' ');
                    st.parseNumbers();
                    st.quoteChar('|');
                    st.eolIsSignificant(true);
                    String templateName = null; 
                    int tokenID = 0;
                    // ditching the headers by looking for "\r\n\r\n"
                    /* 
                     * no longer needed now that HttpClient is being used but leave
                     * in for the moment.
                     *
                     * while (!((tokenID = st.nextToken()) == StreamTokenizer.TT_EOL 
                     *       && st.nextToken() == StreamTokenizer.TT_EOL) 
                     *       && tokenID != StreamTokenizer.TT_EOF) {
                     * }
                     */
                    while ((tokenID = st.nextToken()) != StreamTokenizer.TT_EOF) {
                        if (tokenID == '|' || tokenID == StreamTokenizer.TT_WORD) {
                        
                            templateName = st.sval;
                        }
                        else if (tokenID == StreamTokenizer.TT_NUMBER 
                                 && templateName != null) {
                            templateName = templateName.substring(1);
                            //System.out.println(templateName);
                            templateMap.put(templateName.replace('/','.'),
                                            new TemplateSourceInfo(
                                                                   templateName,
                                                                   remoteSource,
                                                                   (long)st.nval));
                            templateName = null;
                        }
                    }
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        //System.out.println("retrieving templateMap");
        return templateMap;
    }
    
    /**
     * inner class to store a templates' name, server location and timestamp
     */
    private class TemplateSourceInfo {
        public String name;
        public String server;
        public long timestamp;
        
        TemplateSourceInfo(String name,String server,long timestamp) {
            this.name = name;
            this.server = server;
            this.timestamp = timestamp;
        }
    }
    
    public class Unit extends CompilationUnit {
        
        private String mSourceFilePath;
        private String mDotPath;
        private File mDestDir;
        private File mDestFile;
        
        Unit(String name, Compiler compiler) {
            super(name,compiler);
            mDotPath = name;
            String slashPath = name.replace('.','/');
            if (slashPath.endsWith("/")) {
                slashPath = slashPath.substring(0, slashPath.length() - 1);
            }
            if (mRootDestDir != null) {
                if (slashPath.lastIndexOf('/') >= 0) {
                    mDestDir = new File
                        (mRootDestDir,
                         slashPath.substring(0,slashPath.lastIndexOf('/')));
                }
                else {
                    mDestDir = mRootDestDir;
                }
                mDestDir.mkdirs();          
                mDestFile = new File
                    (mDestDir,
                     slashPath.substring(slashPath.lastIndexOf('/') + 1) 
                     + ".class");
            /*
            try {
                if (mDestFile.createNewFile()) {
                    System.out.println(mDestFile.getPath() + " created");
                }
                else {
                    System.out.println(mDestFile.getPath() + " NOT created");
                }
            }
            catch (IOException ioe) {ioe.printStackTrace();}
            */
            }
            mSourceFilePath = slashPath;
        }

        public String getTargetPackage() {
            return mRootPackage;
        }

        public String getSourceFileName() {
            return mSourceFilePath + ".tea";
        }


        public Reader getReader() throws IOException {
            Reader reader = null;
            InputStream in = getTemplateSource(mDotPath);
            if (mEncoding == null) {
                reader = new InputStreamReader(in);
            }
            else {
                reader = new InputStreamReader(in, mEncoding);
            }
            return reader;
        }

        public boolean shouldCompile() {
            if (!mForce &&
                mDestFile != null &&
                mDestFile.exists() &&
                mDestFile.lastModified() >= 
                    ((TemplateSourceInfo)mTemplateMap
                    .get(mDotPath)).timestamp) {

                return false;
            }
            return true;
        }
        

        /**
         * @return the file that gets written by the compiler.
         */
        public File getDestinationFile() {
            return mDestFile;
        }

        public OutputStream getOutputStream() throws IOException {
            OutputStream out1 = null;
            OutputStream out2 = null;

            if (mDestDir != null) {
                if (!mDestDir.exists()) {
                    mDestDir.mkdirs();
                }

                out1 = new FileOutputStream(mDestFile);
            }

            if (mInjector != null) {
                String className = getName();
                String pack = getTargetPackage();
                if (pack != null && pack.length() > 0) {
                    className = pack + '.' + className;
                }
                out2 = mInjector.getStream(className);
            }

            OutputStream out;

            if (out1 != null) {
                if (out2 != null) {
                    out = new DualOutput(out1, out2);
                }
                else {
                    out = out1;
                }
            }
            else if (out2 != null) {
                out = out2;
            }
            else {
                out = new OutputStream() {
                    public void write(int b) {}
                    public void write(byte[] b, int off, int len) {}
                };
            }

            return new BufferedOutputStream(out);
        }
        
        /**
         * get a input stream containing the template source data.
         */
        private InputStream getTemplateSource(String templateSourceName) 
                                                        throws IOException {
            TemplateSourceInfo tsInfo = (TemplateSourceInfo)mTemplateMap
                                                .get(templateSourceName);

            HttpClient client = getTemplateServerClient(tsInfo.server);
            HttpClient.Response response = client
                .setURI(createTemplateServerRequest(tsInfo.server,tsInfo.name + ".tea"))
                .setPersistent(true).getResponse();            
            InputStream in = response.getInputStream();
            return in;
        }
    }
}
