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

import java.io.Serializable;
import java.util.*;
import java.net.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.ref.*;
import com.go.trove.io.*;
import com.go.trove.log.Log;
import com.go.trove.util.*;
import com.go.trove.util.tq.*;
import com.go.tea.runtime.TemplateLoader;
import com.go.tea.runtime.Substitution;
import com.go.teaservlet.util.cluster.*;
import javax.servlet.ServletException;


/******************************************************************************
 * Application that defines a cache function for templates. The cache is
 * applied over a region within a template and is called like:
 *
 * <pre>
 * cache() {
 *     // Cached template code and text goes here
 * }
 * </pre>
 *
 * The contents within the cache are used up to a configurable time-to-live
 * value, specified in milliseconds. A specific TTL value can be passed as an
 * argument to the cache function, in order to override the configured default.
 * <p>
 * Regions are keyed on enclosing template, region number, and HTTP query
 * parameters. An optional secondary key may be passed in which helps to
 * further identify the region. To pass in multiple values for the secondary
 * key, pass in an array.
 * <p>
 * The cache function can be invoked multiple times within a template and the
 * cache calls can be nested within each other. When templates are reloaded,
 * the cached regions are dropped.
 * <p>
 * The RegionCachingApplication can also compress cached regions and send the
 * GZIP compressed response to the client. The intention is not to reduce
 * memory usage on the server, but to conserve bandwidth. If the client
 * supports GZIP encoding, then it is eligible to receive a fully or partially
 * GZIP compressed response.
 * <p>
 * If provided with cluster configuration information to pass along to the 
 * {@link com.go.teaservlet.util.cluster.ClusterManager}, status information 
 * may be shared between machines to compare cache sizes.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public class RegionCachingApplication implements AdminApp {
    private Log mLog;
    private int mCacheSize;
    private long mDefaultTTL;
    private long mTimeout;
    private String[] mHeaders;
    private int mCompressLevel;

    private ClusterManager mClusterManager;
    private ClusterCacheInfo mInfo;

    // Maps Templates to TemplateKeys
    private Map mTemplateKeys = new IdentityMap();

    // List of DepotLinks for linking TemplateLoaders and Depots. A Map isn't
    // used here because the list will be small, usually just one element.
    // After a template reload, the old Depots should be discarded as soon as
    // possible in order to free up memory. Iterating over the list can pick
    // up cleared references.
    private List mDepots = new ArrayList(10);

    // Shared by all Depots.
    private TransactionQueue mTQ;

    /**
     * Accepts the following optional parameters:
     *
     * <pre>
     * cache.size   Amount of most recently used cache items to keep, which is
     *              500 by default.
     * default.ttl  Default time-to-live of cached regions, in milliseconds.
     *              If left unspecified, default.ttl is 5000 milliseconds.
     * timeout      Maximum milliseconds to wait on cache before serving an
     *              expired region. Default value is 500 milliseconds.
     * headers      List of headers to use for all keys. i.e. User-Agent or
     *              Host.
     * gzip         Accepts a value from 0 to 9 to set compression level. When
     *              non-zero, GZIP compression is enabled for cached regions.
     *              A value of 1 offers fast compression, and a value of 9
     *              offers best compression. A value of 6 is the typical
     *              default used by GZIP.
     *
     * transactionQueue  Properties for TransactionQueue that executes regions.
     *    max.threads    Maximum thread count. Default is 100.
     *    max.size       Maximum size of TransactionQueue. Default is 100.
     * </pre>
     */
    public void init(ApplicationConfig config) throws ServletException {
        mLog = config.getLog();

        PropertyMap props = config.getProperties();
        mCacheSize = props.getInt("cache.size", 500);
        mDefaultTTL = props.getNumber
            ("default.ttl", new Long(5000)).longValue();
        mTimeout = props.getNumber("timeout", new Long(500)).longValue();

        PropertyMap tqProps = props.subMap("transactionQueue");

        int maxPool = tqProps.getInt("max.threads", 100);
        ThreadPool tp = new ThreadPool(config.getName(), maxPool);
        tp.setTimeout(5000);
        tp.setIdleTimeout(60000);

        mTQ = new TransactionQueue(tp, config.getName() + " TQ", 100, 100);
        mTQ.applyProperties(tqProps);

        String headers = props.getString("headers");

        if (headers == null) {
            mHeaders = null;
        }
        else {
            StringTokenizer st = new StringTokenizer(headers, " ;,");
            int count = st.countTokens();
            if (count == 0) {
                mHeaders = null;
            }
            else {
                mHeaders = new String[count];
                for (int i=0; i<count; i++) {
                    mHeaders[i] = st.nextToken();
                }
            }
        }

        mCompressLevel = props.getInt("gzip", 0);

        if (mCompressLevel < 0) {
            mLog.warn("GZIP compression lowest level is 0. Level " +
                      mCompressLevel + " interpretted as 0");
            mCompressLevel = 0;
        }
        else if (mCompressLevel > 9) {
            mLog.warn("GZIP compression highest level is 9. Level " +
                      mCompressLevel + " interpretted as 9");
            mCompressLevel = 9;
        }

        initCluster(config);
    }
    
    public void destroy() {
        if (mClusterManager != null) {
            mClusterManager.killAuto();
        }
    }

    /**
     * Returns an instance of {@link RegionCachingContext}.
     */
    public Object createContext(ApplicationRequest request,
                                ApplicationResponse response) {
        return new RegionCachingContextImpl(this, request, response);
    }

    /**
     * Returns {@link RegionCachingContext}.class.
     */
    public Class getContextType() {
        return RegionCachingContext.class;
    }

    public AppAdminLinks getAdminLinks() {

        AppAdminLinks links = new AppAdminLinks(mLog.getName());
        links.addAdminLink("Depot","system.teaservlet.Depot");
        return links;
    }


    void cache(ApplicationRequest request, 
               ApplicationResponse response,
               Substitution s)
        throws Exception
    {
        cache(request, response, mDefaultTTL, null, s);
    }

    void cache(ApplicationRequest request, 
               ApplicationResponse response,
               long ttlMillis,
               Substitution s)
        throws Exception
    {
        cache(request, response, ttlMillis, null, s);
    }

    void cache(ApplicationRequest request, 
               ApplicationResponse response,
               long ttlMillis,
               Object key,
               Substitution s)
        throws Exception
    {
        TemplateKey templateKey = getTemplateKey(request.getTemplate());

        Object[] keyElements = {
            templateKey,
            s.getIdentifier(),
            getHeaderValues(request),
            request.getQueryString(),
            key,
        };
        
        key = new MultiKey(keyElements);
            
        ApplicationResponse.Command c =
            new CacheCommand(s, templateKey, ttlMillis, key);
        if (!response.insertCommand(c)) {
            c.execute(request, response);
        }
    }

    void nocache(ApplicationRequest request, 
                 ApplicationResponse response,
                 Substitution s)
        throws Exception
    {
        ApplicationResponse.Command c = new NoCacheCommand(s);
        if (!response.insertCommand(c)) {
            c.execute(request, response);
        }
    }

    private TemplateKey getTemplateKey(TemplateLoader.Template template) {
        synchronized (mTemplateKeys) {
            TemplateKey key = (TemplateKey)mTemplateKeys.get(template);
            if (key == null) {
                key = new TemplateKey(template);
                mTemplateKeys.put(template, key);
            }
            return key;
        }
    }

    private String[] getHeaderValues(ApplicationRequest request) {
        if (mHeaders == null) {
            return null;
        }

        String[] headers = (String[])mHeaders.clone();
        for (int i = headers.length; --i >= 0; ) {
            headers[i] = request.getHeader(headers[i]);
        }

        return headers;
    }

    Depot getDepot(TemplateLoader.Template template) {
        return getDepot(getTemplateKey(template));
    }

    Depot getDepot(TemplateKey key) {
        TemplateLoader depotKey = key.getTemplateLoader();
        if (depotKey == null) {
            return null;
        }

        DepotLink link;
        Object linkKey;
        Depot depot;

        synchronized (mDepots) {
            int size = mDepots.size();
            if (size == 1) {
                link = (DepotLink)mDepots.get(0);
                linkKey = link.get();
                if (linkKey == null) {
                    // Remove cleared reference.
                    mDepots.clear();
                }
                else if (linkKey == depotKey) {
                    return link.mDepot;
                }
            }
            else if (size > 1) {
                depot = null;
                Iterator it = mDepots.iterator();
                while (it.hasNext()) {
                    link = (DepotLink)it.next();
                    linkKey = link.get();
                    if (linkKey == null) {
                        // Remove cleared reference.
                        it.remove();
                    }
                    else if (linkKey == depotKey) {
                        depot = link.mDepot;
                        // Don't break loop: keep searching for cleared refs.
                    }
                }
                if (depot != null) {
                    return depot;
                }
            }

            // If here, no Depot found. Make one.
            depot = new Depot(null, mCacheSize, mTQ, mTimeout);
            mDepots.add(new DepotLink(depotKey, depot));
        }

        return depot;
    }


    private void initCluster(ApplicationConfig config) {
        
        try {
            String clusterName = config.getInitParameter("cluster.name");
            if (clusterName != null) {
                mInfo = new ClusterCacheInfoImpl(clusterName, null);
                mClusterManager = ClusterManager
                    .createClusterManager(config.getProperties(), mInfo);
            }
        }
        catch(Exception e) {
            mLog.warn("Failed to create ClusterManager.");
            mLog.warn(e);
        }
    }

    // Allows old templates to be garbage collected when reloaded.
    private static class TemplateKey extends WeakReference {
        TemplateKey(TemplateLoader.Template template) {
            super(template);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TemplateKey) {
                TemplateKey other = (TemplateKey)obj;
                Object t = this.get();
                if (t != null) {
                    return t == other.get();
                }
            }
            return false;
        }

        public int hashCode() {
            Object t = get();
            return (t == null) ? 0 : t.hashCode();
        }

        public String toString() {
            TemplateLoader.Template t = (TemplateLoader.Template)get();
            if (t != null) {
                return t.getName();
            }
            else {
                return super.toString();
            }
        }

        TemplateLoader getTemplateLoader() {
            TemplateLoader.Template t = (TemplateLoader.Template)get();
            return (t != null) ? t.getTemplateLoader() : null;
        }
    }

    private static class DepotLink extends WeakReference {
        final Depot mDepot;

        DepotLink(TemplateLoader loader, Depot depot) {
            super(loader);
            mDepot = depot;
        }
    }

    private class CacheCommand implements ApplicationResponse.Command {
        final Substitution mSub;
        final TemplateKey mTemplateKey;
        final long mTTLMillis;
        final Object mKey;

        CacheCommand(Substitution s, TemplateKey templateKey,
                     long ttlMillis, Object key) {
            mSub = s.detach();
            mTemplateKey = templateKey;
            mTTLMillis = ttlMillis;
            mKey = key;
        }

        public void execute(ApplicationRequest request,
                            ApplicationResponse response)
            throws Exception
        {
            DetachedDataFactory factory =
                new DetachedDataFactory(this, response);

            ApplicationResponse.DetachedData data;
            Depot depot = getDepot(mTemplateKey);

            if (depot != null) {
                data = (ApplicationResponse.DetachedData)
                    depot.get(factory, mKey);
                if (mCompressLevel > 0 && !factory.mCalled) {
                    // If the factory wasn't called, then this was a cache hit.
                    // Its likely it will be seen again, so take the time to
                    // compress.
                    if (data != null && request.isCompressionAccepted()) {
                        try {
                            data.compress(mCompressLevel);
                        }
                        catch (UnsatisfiedLinkError e) {
                            // Native library to support compression not found.
                            mCompressLevel = 0;
                            mLog.error(e);
                        }
                    }
                }
            }
            else {
                // This should never happen, but just in case, generate data
                // without caching.
                data = (ApplicationResponse.DetachedData)factory.create(mKey);
            }
            
            if (data != null) {
                data.playback(request, response);
            }
        }

        Log getLog() {
            return mLog;
        }
    }

    private static class DetachedDataFactory
        implements Depot.PerishablesFactory
    {
        boolean mCalled;

        private CacheCommand mCommand;
        private ApplicationResponse mResponse;

        DetachedDataFactory(CacheCommand c, ApplicationResponse response) {
            mCommand = c;
            mResponse = response;
        }

        public Object create(Object xxx) throws InterruptedException {
            mCalled = true;
            try {
                return mResponse.execDetached(mCommand.mSub);
            }
            catch (InterruptedException e) {
                mCommand.getLog().error(e);
                throw e;
            }
            catch (Exception e) {
                mCommand.getLog().error(e);
                throw new InterruptedException(e.getMessage());
            }
        }
        
        public long getValidDuration() {
            return mCommand.mTTLMillis;
        }
    };

    private class NoCacheCommand implements ApplicationResponse.Command {
        private Substitution mSub;

        NoCacheCommand(Substitution s) {
            mSub = s.detach();
        }

        public void execute(ApplicationRequest request,
                            ApplicationResponse response) {
            try {
                mSub.detach().substitute(response.getHttpContext());
            }
            catch (Exception e) {
                mLog.error(e);
            }
        }
    }

    private class RegionCachingContextImpl 
        implements RegionCachingContext {

        private final RegionCachingApplication mApp;
        private final ApplicationRequest mRequest;
        private final ApplicationResponse mResponse;

        public RegionCachingContextImpl(RegionCachingApplication app,
                                        ApplicationRequest request,
                                        ApplicationResponse response) {
            mApp = app;
            mRequest = request;
            mResponse = response;
        }

        /**
         * Caches and reuses a region of a page. The cached region expies after
         * a default time-to-live period has elapsed.
         *
         * @param s substitution block whose contents will be cached
         */
        public void cache(Substitution s) throws Exception {
            mApp.cache(mRequest, mResponse, s);
        }

        /**
         * Caches and reuses a region of a page. The cached region expies after
         * a the specified time-to-live period has elapsed.
         *
         * @param ttlMillis maximum time to live of cached region, in milliseconds
         * @param s substitution block whose contents will be cached
         */
        public void cache(long ttlMillis, Substitution s) throws Exception {
            mApp.cache(mRequest, mResponse, ttlMillis, s);
        }

        /**
         * Caches and reuses a region of a page. The cached region expies after
         * a the specified time-to-live period has elapsed. An additional parameter
         * is specified which helps to identify the uniqueness of the region.
         *
         * @param ttlMillis maximum time to live of cached region, in milliseconds
         * @param key key to further identify cache region uniqueness
         * @param s substitution block whose contents will be cached
         */
        public void cache(long ttlMillis, Object key, Substitution s)
            throws Exception
        {
            mApp.cache(mRequest, mResponse, ttlMillis, key, s);
        }

        public void nocache(Substitution s) throws Exception {
            mApp.nocache(mRequest, mResponse, s);
        }

        public RegionCacheInfo getRegionCacheInfo() {
            return new RegionCacheInfo(mApp.getDepot(mRequest.getTemplate()));
        }

        public RegionCachingApplication.ClusterCacheInfo getClusterCacheInfo() {
            try {
                if (mClusterManager != null) {
                    mClusterManager.resolveServerNames();
                }
                return mInfo;
            }
            catch (Exception e) {
                return null;
            }
        }
    }

    public interface ClusterCacheInfo extends Clustered {

        public RegionCacheInfo getRegionCacheInfo() 
            throws RemoteException;
    }

    public class ClusterCacheInfoImpl extends ClusterHook 
        implements ClusterCacheInfo {
        
        ClusterCacheInfoImpl(String cluster, String server) 
            throws RemoteException {
            super(cluster, server);
        }

        public RegionCacheInfo getRegionCacheInfo() 
            throws RemoteException {
            List depotList = RegionCachingApplication.this.mDepots;
            if (depotList != null && depotList.size() > 0) {
                DepotLink link = (DepotLink)depotList.get(0);
                if (link != null) {
                    return new RegionCacheInfo(link.mDepot);
                }
            }
            return null;
        }
    }
}
