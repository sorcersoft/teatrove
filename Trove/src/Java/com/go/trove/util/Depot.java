/* ====================================================================
 * Trove - Copyright (c) 1997-2000 Walt Disney Internet Group
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

package com.go.trove.util;

import java.util.*;
import com.go.trove.util.tq.*;

/******************************************************************************
 * Depot implements a simple and efficient caching strategy. It is thread-safe,
 * and it allows requests of different objects to occur concurrently. Depot
 * is best suited as a front-end for accessing objects from a remote device,
 * like a database. If the remote device is not responding, the Depot will
 * continue to serve invalidated objects so that the requester may continue
 * as normal.
 * <p>
 * Depot is designed as a cache in front of an object {@link Factory factory}.
 * Objects may be invalidated, but they are not explicitly removed from the
 * cache until a replacement has been provided by the factory. The factory is
 * invoked from another thread, allowing for the requester to timeout and use
 * an invalidated object. When the factory eventually finishes, its object will
 * be cached.
 * <p>
 * By allowing for eventual completion of the factory, Depot enables
 * applications to dynamically adjust to the varying performance and
 * reliability of remote data providers.
 * <p>
 * Depot will never return an object or null that did not originate from the
 * factory. When retrieving an object that wasn't found cached, a call to the
 * factory will block until it is finished.
 * <p>
 * Objects may be invalided from the Depot
 * {@link PerishablesFactory automatically}. This approach is based on a fixed
 * time expiration and is somewhat inflexible. An ideal invalidation strategy
 * requires asynchronous notification from the actual data providers.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/03/04 <!-- $-->
 * @see MultiKey
 */
public class Depot {
    private static final Object NOTHING = new Object();

    private final Factory mDefaultFactory;
    final Map mValidCache;
    final Map mInvalidCache;
    private final Kernel mKernel;
    final TransactionQueue mQueue;
    private final long mTimeout;

    // Maps keys to Retrievers.
    private final Map mRetrievers =
        Collections.synchronizedMap(new SoftHashMap());

    private final Object mExpireLock = new Object();
    private Map mExpirations;

    /**
     * @param factory Default factory from which objects are obtained
     * @param validCache Map to use for caching valid objects
     * @param invalidCache Map to use for caching invalid objects
     * @param tq TransactionQueue for scheduling factory invocations.
     * @param timeout Default timeout (in milliseconds) to apply to "get"
     * method.
     */
    public Depot(Factory factory, Map validCache, Map invalidCache,
                 TransactionQueue tq, long timeout) {
        mDefaultFactory = factory;
        mValidCache = Collections.synchronizedMap(validCache);
        mInvalidCache = Collections.synchronizedMap(invalidCache);
        mKernel = new SimpleKernel();
        mQueue = tq;
        mTimeout = timeout;

    }
    
    /**
     * @param factory Default factory from which objects are obtained
     * @param cacheSize Number of items guaranteed to be in cache, if negative,
     * cache is completely disabled.
     * @param tq TransactionQueue for scheduling factory invocations.
     * @param timeout Default timeout (in milliseconds) to apply to "get"
     * method.
     */
    public Depot(Factory factory, int cacheSize, 
                 TransactionQueue tq, long timeout) {
        Map valid, invalid;

        if (cacheSize < 0) {
            valid = Utils.VOID_MAP;
            invalid = Utils.VOID_MAP;
        }
        else if (cacheSize > 0) {
            valid = new Cache(cacheSize);
            invalid = new Cache((Cache)valid);
        }
        else {
            valid = new SoftHashMap();
            invalid = new SoftHashMap();
        }

        mDefaultFactory = factory;
        mValidCache = Collections.synchronizedMap(valid);
        mInvalidCache = Collections.synchronizedMap(invalid);
        mKernel = new SimpleKernel();
        mQueue = tq;
        mTimeout = timeout;
    }

    /**
     * @param factory Default factory from which objects are obtained
     * @param kernel Kernel for supporting more advanced Depots, such as
     * persistent ones.
     * @param tq TransactionQueue for scheduling factory invocations.
     * @param timeout Default timeout (in milliseconds) to apply to "get"
     * method.
     */
    public Depot(Factory factory, Kernel kernel,
                 TransactionQueue tq, long timeout) {
        mDefaultFactory = factory;
        mValidCache = kernel.validCache();
        mInvalidCache = kernel.invalidCache();
        mKernel = kernel;
        mQueue = tq;
        mTimeout = timeout;
    }

    public String toString() {
        return "Depot " + mQueue.getName();
    }

    /**
     * Returns the total number objects in the Depot.
     */
    public int size() {
        return mKernel.size();
    }

    public boolean isEmpty() {
        return mKernel.isEmpty();
    }

    /**
     * Returns the number of valid objects in the Depot.
     */
    public int validSize() {
        return mValidCache.size();
    }

    /**
     * Returns the number of invalid objects in the Depot.
     */
    public int invalidSize() {
        return mInvalidCache.size();
    }

    /**
     * Returns an unmodifiable view of the valid cache.
     */
    /* Commented out for now. If iterating entries of a SoftHashMap or Cache,
       a lock must be held on the cache or else concurrent modification
       exceptions are thrown.
    public Map validCache() {
        return Collections.unmodifiableMap(mValidCache);
    }
    */

    /**
     * Returns an unmodifiable view of the invalid cache.
     */
    /* Commented out for now. If iterating entries of a SoftHashMap or Cache,
       a lock must be held on the cache or else concurrent modification
       exceptions are thrown.
    public Map invalidCache() {
        return Collections.unmodifiableMap(mInvalidCache);
    }
    */
    
    /**
     * Retrieve an object from the Depot. If the requested object is in the
     * cache of valid objects, it is returned immediately. If the object is
     * found in the cache of invalid objects, then it will be returned only if
     * the factory cannot create a replacement in a timely manner. If the
     * requested object is not in any cache at all, the factory is called to
     * create the object, and the calling thread will block until the factory
     * has finished.
     *
     * @param key key of object to retrieve
     */
    public Object get(Object key) {
        return get(mDefaultFactory, key, mTimeout);
    }

    /**
     * Retrieve an object from the Depot. If the requested object is in the
     * cache of valid objects, it is returned immediately. If the object is
     * found in the cache of invalid objects, then it will be returned only if
     * the factory cannot create a replacement in a timely manner. If the
     * requested object is not in any cache at all, the factory is called to
     * create the object, and the calling thread will block until the factory
     * has finished.
     *
     * @param key key of object to retrieve
     * @param timeout max time (in milliseconds) to wait for an invalid value
     * to be replaced from the factory, if negative, wait forever. Ignored if
     * no cached value exists at all.
     */
    public Object get(Object key, long timeout) {
        return get(mDefaultFactory, key, timeout);
    }

    /**
     * Retrieve an object from the Depot. If the requested object is in the
     * cache of valid objects, it is returned immediately. If the object is
     * found in the cache of invalid objects, then it will be returned only if
     * the factory cannot create a replacement in a timely manner. If the
     * requested object is not in any cache at all, the factory is called to
     * create the object, and the calling thread will block until the factory
     * has finished.
     *
     * @param factory factory to use to retrieve object if not cached
     * @param key key of object to retrieve
     */
    public Object get(Factory factory, Object key) {
        return get(factory, key, mTimeout);
    }

    /**
     * Retrieve an object from the Depot. If the requested object is in the
     * cache of valid objects, it is returned immediately. If the object is
     * found in the cache of invalid objects, then it will be returned only if
     * the factory cannot create a replacement in a timely manner. If the
     * requested object is not in any cache at all, the factory is called to
     * create the object, and the calling thread will block until the factory
     * has finished.
     *
     * @param factory factory to use to retrieve object if not cached
     * @param key key of object to retrieve
     * @param timeout max time (in milliseconds) to wait for an invalid value
     * to be replaced from the factory, if negative, wait forever. Ignored if
     * no cached value exists at all.
     */
    public Object get(Factory factory, Object key, long timeout) {
        Retriever r = getRetriever(key);
        synchronized (r) {
            boolean priority;
            Object value = mValidCache.get(key);
            if (value != null || mValidCache.containsKey(key)) {
                validTest: {
                    if (value instanceof Perishable) {
                        if (!((Perishable)value).isValid()) {
                            break validTest;
                        }
                    }
                    
                    synchronized (mExpireLock) {
                        if (mExpirations == null) {
                            return value;
                        }
                        Long expire = (Long)mExpirations.get(key);
                        if (expire == null ||
                            System.currentTimeMillis() <= expire.longValue()) {
                            // Value is still valid.
                            return value;
                        }
                        
                        // Value has expired.
                        mExpirations.remove(key);
                    }
                }
                
                mValidCache.remove(key);
                mInvalidCache.put(key, value);
                
                priority = false;
            }
            else {
                value = mInvalidCache.get(key);
                if (value == null && !mInvalidCache.containsKey(key)) {
                    // Wait forever since not even an invalid value exists.
                    timeout = -1;
                    priority = true;
                }
                else {
                    priority = false;
                }
            }
            
            Object newValue = r.retrieve(factory, timeout, false);
            return (newValue == NOTHING) ? value : newValue;
        }
    }

    /**
     * Invalidate the object referenced by the given key, if it is already
     * cached in this Depot. Invalidated objects are not removed from the
     * Depot until a replacement object has been successfully created from the
     * factory.
     *
     * @param key key of object to invalidate
     */
    public void invalidate(Object key) {
        Retriever r = getRetriever(key);
        synchronized (r) {
            if (mValidCache.containsKey(key)) {
                Object value = mValidCache.remove(key);
                mInvalidCache.put(key, value);
            }
        }
    }

    /**
     * Invalidates objects in the Depot, using a filter. Each key that the
     * filter accepts is invalidated.
     */
    public void invalidateAll(Filter filter) {
        mKernel.invalidateAll(filter);
    }

    /**
     * Invalidates all the objects in the Depot.
     */
    public void invalidateAll() {
        mKernel.invalidateAll();
    }

    /**
     * Put a value into the Depot, bypassing the factory. Invalidating an
     * object and relying on the factory to produce a new value is generally
     * preferred. This method will notify any threads waiting on a factory to
     * produce a value, but it will not disrupt the behavior of the factory.
     *
     * @param key key with which to associate the value.
     * @param value value to be associated with key.
     */
    public void put(Object key, Object value) {
        Retriever r = getRetriever(key);
        synchronized (r) {
            mInvalidCache.remove(key);
            mValidCache.put(key, value);
            // Bypass the factory produced value so that any waiting
            // threads are notified.
            r.bypassValue(value);
        }
    }

    /**
     * Completely removes an item from the Depot's caches. Invalidating an
     * object is preferred, and remove should be called only if the object
     * should absolutely never be used again.
     */
    public Object remove(Object key) {
        Retriever r = getRetriever(key);
        synchronized (r) {
            Object old;
            if (mValidCache.containsKey(key)) {
                old = mValidCache.remove(key);
                mInvalidCache.remove(key);
            }
            else {
                mValidCache.remove(key);
                old = mInvalidCache.remove(key);
            }
            r.setValue(NOTHING);
            return old;
        }
    }

    /**
     * Completely removes all the items from the Depot that the given filter
     * accepts.
     */
    public void removeAll(Filter filter) {
        mKernel.removeAll(filter);
    }

    /**
     * Completely removes all items from the Depot's caches. Invalidating all
     * the objects is preferred, and clear should be called only if all the
     * cached objects should absolutely never be used again.
     */
    public void clear() {
        synchronized (mRetrievers) {
            mKernel.clear();
            mRetrievers.clear();
        }
    }

    void setExpiration(Object key, long duration) {
        Long expire = new Long(System.currentTimeMillis() + duration);
        synchronized (mExpireLock) {
            if (mExpirations == null) {
                mExpirations = new HashMap();
            }
            mExpirations.put(key, expire);
        }
    }

    private Retriever getRetriever(Object key) {
        synchronized (mRetrievers) {
            Retriever r = (Retriever)mRetrievers.get(key);
            if (r == null) {
                r = new Retriever(key);
                mRetrievers.put(key, r);
            }
            return r;
        }
    }

    /**
     * Implement this interface in order for Depot to retrieve objects when
     * needed, often in a thread other than the requester's.
     *
     * @see PerishablesFactory
     */
    public interface Factory {
        /**
         * Create an object that is mapped by the given key. This method must
         * be thread-safe, but simply making it synchronized may severely
         * impact the Depot's support of concurrent activity.
         * <p>
         * Create may abort its operation by throwing an InterruptedException.
         * This forces an invalid object to be used or null if none. If an
         * InterruptedException is thrown, nether the invalid object or null
         * will be cached. Null is cached only if the factory returns it
         * directly.
         *
         * @throws InterruptedException explicitly throwing this exception
         * allows the factory to abort creating an object.
         */
        public Object create(Object key) throws InterruptedException;
    }

    /**
     * A special kind of Factory that creates objects that are considered
     * invalid after a specific amount of time has elapsed.
     */
    public interface PerishablesFactory extends Factory {
        /**
         * Returns the maximum amout of time (in milliseconds) that objects
         * from this factory should remain valid. Returning a value less than
         * or equal to zero causes objects to be immediately invalidated.
         */
        public long getValidDuration();
    }

    /**
     * Values returned from the Factories may implement this interface if they
     * manually handle expiration.
     */
    public interface Perishable {
        /**
         * If this Perishable is still valid, but it came from a
         * PerishablesFactory, it may be considered invalid if the valid
         * duration has elapsed.
         */
        public boolean isValid();
    }

    public interface Filter {
        /**
         * Returns true if the given key should be included in an operation,
         * such as invalidation.
         */
        public boolean accept(Object key);
    }

    /**
     * Interface provides basic data structures and additional services in
     * order for the Depot to function. The Kernel must perform operations in a
     * thread-safe manner.
     */
    public interface Kernel {
        /**
         * Returns the map to cache valid Depot entries.
         */
        public Map validCache();
        
        /**
         * Returns the map to cache invalid Depot entries.
         */
        public Map invalidCache();
        
        /**
         * Returns the combined size of the valid and invalid cache.
         */
        public int size();
        
        /**
         * Returns true if both the valid and invalid caches are empty.
         */
        public boolean isEmpty();
        
        /**
         * Moves all entries in the valid cache to the invalid cache, if the
         * filter accepts it.
         */
        public void invalidateAll(Filter filter);
        
        /**
         * Moves all entries in the valid cache to the invalid cache.
         */
        public void invalidateAll();
        
        /**
         * Removes all entries from the valid cache and invalid cache, if the
         * filter accepts it.
         */
        public void removeAll(Filter filter);
        
        /**
         * Removes all entries from the valid cache and invalid cache.
         */
        public void clear();
    }

    /**
     * Used in place of a user supplied kernel.
     */
    private class SimpleKernel implements Kernel {
        public Map validCache() {
            return null;
        }
        
        public Map invalidCache() {
            return null;
        }
        
        public int size() {
            synchronized (mValidCache) {
                return mValidCache.size() + mInvalidCache.size();
            }
        }
        
        public boolean isEmpty() {
            synchronized (mValidCache) {
                return mValidCache.isEmpty() && mInvalidCache.isEmpty();
            }
        }
        
        public void invalidateAll(Filter filter) {
            Map valid = mValidCache;
            Map invalid = mInvalidCache;
            synchronized (valid) {
                Iterator it = valid.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    Object key = entry.getKey();
                    if (filter.accept(key)) {
                        it.remove();
                        invalid.put(key, entry.getValue());
                    }
                }
            }
        }
        
        public void invalidateAll() {
            synchronized (mValidCache) {
                synchronized (mInvalidCache) {
                    mInvalidCache.putAll(mValidCache);
                    mValidCache.clear();
                }
            }
        }
        
        public void removeAll(Filter filter) {
            Map valid = mValidCache;
            Map invalid = mInvalidCache;
            synchronized (valid) {
                synchronized (invalid) {
                    Iterator it = valid.keySet().iterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        if (filter.accept(key)) {
                            it.remove();
                        }
                    }
                    it = invalid.keySet().iterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        if (filter.accept(key)) {
                            it.remove();
                        }
                    }
                }
            }
        }

        public void clear() {
            synchronized (mValidCache) {
                synchronized (mInvalidCache) {
                    mValidCache.clear();
                    mInvalidCache.clear();
                }
            }
        }
    }

    private class Retriever implements Transaction {
        private final Object mKey;
        private Factory mFactory;
        private Object mValue;

        Retriever(Object key) {
            mKey = key;
            mValue = NOTHING;
        }

        /**
         * Returns sentinal value NOTHING if object couldn't be retrieved.
         */
        public synchronized Object retrieve(Factory factory,
                                            long timeout,
                                            boolean priority)
        {
            if (mFactory != null) {
                // Work in progress to retrieve new value.
                return waitForValue(timeout);
            }

            if ((mFactory = factory) == null) {
                throw new NullPointerException("Factory is null");
            }

            if (mQueue.enqueue(this)) {
                return waitForValue(timeout);
            }

            // No threads available in TQ to retrieve new value.
            mFactory = null;

            if (priority) {
                // Skip the queue, service it in this thread.
                service();
                return mValue;
            }

            return NOTHING;
        }

        public synchronized void bypassValue(Object value) {
            if (mFactory != null) {
                mValue = value;
                notifyAll();
            }
        }

        public synchronized void setValue(Object value) {
            mValue = value;
        }

        public void service() {
            Factory factory = mFactory;
            if (factory == null) {
                return;
            }
            
            fetch: try {
                Thread t = Thread.currentThread();
                String originalName = t.getName();
                t.setName(originalName + ' ' + mKey);
                Object value;
                try {
                    value = factory.create(mKey);
                }
                catch (InterruptedException e) {
                    break fetch;
                }
                finally {
                    t.setName(originalName);
                }
                synchronized (this) {
                    mValue = value;
                    if (factory instanceof PerishablesFactory) {
                        long duration =
                            ((PerishablesFactory)factory).getValidDuration();
                        if (duration <= 0) {
                            mInvalidCache.put(mKey, value);
                            mValidCache.remove(mKey);
                        }
                        else {
                            mInvalidCache.remove(mKey);
                            mValidCache.put(mKey, value);
                            setExpiration(mKey, duration);
                        }
                    }
                    else {
                        mInvalidCache.remove(mKey);
                        mValidCache.put(mKey, value);
                    }
                    mFactory = null;
                    notifyAll();
                }
            }
            finally {
                done();
            }
        }

        public void cancel() {
            done();
        }
        
        private synchronized void done() {
            if (mFactory != null) {
                mFactory = null;
                notifyAll();
            }
        }

        private Object waitForValue(long timeout) {
            if (timeout == 0) {
                return mValue;
            }
            if (timeout < 0) {
                timeout = 0;
            }
            synchronized (this) {
                try {
                    wait(timeout);
                }
                catch (InterruptedException e) {
                }
            }
            return mValue;
        }
    }
}
