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

package com.go.trove.persist;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.*;
import com.go.trove.util.SoftHashMap;
import com.go.trove.util.UsageMap;
import com.go.trove.util.IdentityMap;
import com.go.trove.util.MapBackedSet;

/******************************************************************************
 * A PersistentMap that implements a write-through or deferred write cache to
 * another map. Two maps are supplied: one for caching and one for main
 * storage. CachedPersistentMap uses the backing map's lock for thread safety.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/20 <!-- $-->
 * @see com.go.trove.util.WrappedCache
 */
public class CachedPersistentMap extends AbstractPersistentMap {
    private final Map mCacheMap;

    // Keys for values that will be written after they are evicted.
    private final Set mDeferred;

    // List of entries that must be saved on next put or remove operation.
    private final List mEntriesToWrite;

    private final PersistentMap mBackingMap;

    /**
     * Construct CachedPersistentMap with write-through behavior.
     *
     * @param cacheMap the cache map should offer fast access, but it should
     * automatically limit its maximum size
     * @param backingMap the backingMap will be read from only if the requested
     * entry isn't in the cache
     */
    public CachedPersistentMap(Map cacheMap, PersistentMap backingMap) {
        super(backingMap.lock());
        mBackingMap = backingMap;
        mCacheMap = cacheMap;
        mDeferred = null;
        mEntriesToWrite = null;
    }

    /**
     * Construct CachedPersistentMap with deferred write behavior. The entries
     * are written to the backing map when they are evicted from the
     * most-recently-used set.
     *
     * @param cacheSize Amount of cache entries guaranteed to be in memory, and
     * not immediately written to the backing map.
     * @param backingMap the backingMap will be read from only if the requested
     * entry isn't in the cache
     */
    public CachedPersistentMap(int cacheSize, PersistentMap backingMap) {
        super(backingMap.lock());
        mBackingMap = backingMap;
        if (cacheSize <= 0) {
            mCacheMap = new SoftHashMap();
        }
        else {
            mCacheMap = new DeferredCacheMap(cacheSize);
        }
        mDeferred = new MapBackedSet(new IdentityMap());
        mEntriesToWrite = new ArrayList();
    }

    /**
     * Returns the size of the backing map.
     */
    public int size() throws IOException {
        return mBackingMap.size();
    }

    /**
     * Returns the empty status of the backing map.
     */
    public boolean isEmpty() throws IOException {
        return mBackingMap.isEmpty();
    }

    /**
     * Returns true if the cache contains the key or else if the backing map
     * contains the key.
     */
    public boolean containsKey(Object key) throws IOException {
        try {
            mLock.acquireReadLock();
            return mCacheMap.containsKey(key) || mBackingMap.containsKey(key);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Returns true if the cache contains the value or else if the backing map
     * contains the value.
     */
    public boolean containsValue(Object value) throws IOException {
        try {
            mLock.acquireReadLock();
            return mCacheMap.containsValue(value) ||
                mBackingMap.containsValue(value);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Returns the value from the cache, or if not found, the backing map.
     * If the backing map is accessed, the value is saved in the cache for
     * future gets.
     */
    public Object get(Object key) throws IOException {
        try {
            mLock.acquireReadLock();
            Object value = mCacheMap.get(key);
            if (value != null || mCacheMap.containsKey(key)) {
                return value;
            }
            value = mBackingMap.get(key);
            if (value != null || mBackingMap.containsKey(key)) {
                mCacheMap.put(key, value);
            }
            return value;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Puts the entry into both the cache and backing map. The old value in
     * the backing map is returned, unless the write is deferred.
     */
    public Object put(Object key, Object value) throws IOException {
        try {
            mLock.acquireWriteLock();
            mCacheMap.put(key, value);
            if (mDeferred != null) {
                mDeferred.add(key);
                saveEntriesToWrite();
                return null;
            }
            else {
                return mBackingMap.put(key, value);
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Removes the key from both the cache and backing map. The old value in
     * the backing map is returned.
     */
    public Object remove(Object key) throws IOException {
        try {
            mLock.acquireWriteLock();
            mCacheMap.remove(key);
            if (mDeferred != null) {
                mDeferred.remove(key);
                saveEntriesToWrite();
            }
            return mBackingMap.remove(key);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Clears both the cache and backing map.
     */
    public void clear() throws IOException {
        try {
            mLock.acquireWriteLock();
            mCacheMap.clear();
            if (mDeferred != null) {
                mDeferred.clear();
                synchronized (mEntriesToWrite) {
                    mEntriesToWrite.clear();
                }
            }
            mBackingMap.clear();
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Returns the key set of the backing map.
     */
    public PersistentSet keySet() throws IOException {
        return mBackingMap.keySet();
    }

    /**
     * Returns the values of the backing map.
     */
    public PersistentCollection values() throws IOException {
        return mBackingMap.values();
    }

    /**
     * Returns the entry set of the backing map.
     */
    public PersistentSet entrySet() throws IOException {
        return mBackingMap.entrySet();
    }

    /**
     * Saves unwritten entries to the backing map.
     */
    public void flush() throws IOException {
        if (mDeferred != null) {
            try {
                mLock.acquireWriteLock();
                saveEntriesToWrite();
                Iterator it = mDeferred.iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    mBackingMap.put(key, super.get(key));
                    it.remove();
                }
            }
            catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
            finally {
                mLock.releaseLock();
            }
        }
    }

    void addEntryToWrite(Object key, Object value) {
        synchronized (mEntriesToWrite) {
            mEntriesToWrite.add(key);
            mEntriesToWrite.add(value);
        }
    }

    /**
     * Write lock must be held.
     */
    private void saveEntriesToWrite() throws IOException {
        List list = mEntriesToWrite;
        synchronized (list) {
            int size = list.size();
            for (int i=0; i<size; ) {
                mBackingMap.put(list.get(i++), list.get(i++));
            }
            list.clear();
        }
    }

    private class DeferredCacheMap extends SoftHashMap {
        private final int mMaxRecent;
        // Contains hard references to entries.
        private final UsageMap mUsageMap;

        DeferredCacheMap(int maxRecent) {
            mMaxRecent = maxRecent;
            mUsageMap = new UsageMap();
        }

        public Object get(Object key) {
            Object value = super.get(key);
            if (value != null || super.containsKey(key)) {
                synchronized (mUsageMap) {
                    mUsageMap.put(key, value);
                }
            }
            return value;
        }
        
        public Object put(Object key, Object value) {
            synchronized (mUsageMap) {
                mUsageMap.put(key, value);
                while (mUsageMap.size() > mMaxRecent) {
                    Object evictedKey = mUsageMap.lastKey();
                    Object evictedValue = mUsageMap.remove(evictedKey);
                    if (mDeferred.contains(evictedKey)) {
                        mDeferred.remove(evictedKey);
                        addEntryToWrite(evictedKey, evictedValue);
                    }
                }
            }

            return super.put(key, value);
        }
        
        public Object remove(Object key) {
            synchronized (mUsageMap) {
                mUsageMap.remove(key);
            }
            return super.remove(key);
        }
        
        public void clear() {
            super.clear();
            synchronized (mUsageMap) {
                mUsageMap.clear();
            }
        }
    }
}
