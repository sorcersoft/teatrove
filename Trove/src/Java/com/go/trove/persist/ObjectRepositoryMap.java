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
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.ObjectStreamException;
import java.util.Comparator;

/******************************************************************************
 * General purpose persistent map implementation consisting of an index and
 * an {@link ObjectRepository object repository}. The object repository may be
 * shared among multiple ObjectRepositoryMaps. For an index, consider using
 * {@link BTree}.
 * <p>
 * ObjectRepositoryMaps uses the index's lock for thread safety.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/19 <!-- $-->
 */
public class ObjectRepositoryMap extends AbstractPersistentMap
    implements PersistentSortedMap
{
    private final PersistentSortedMap mIndex;
    private final ObjectRepository mRepository;

    private transient PersistentSet mEntrySet;

    /**
     * @param index expected value type is Long.class
     */
    public ObjectRepositoryMap(PersistentSortedMap index,
                               ObjectRepository repository)
    {
        super(index.lock());
        mIndex = index;
        mRepository = repository;
    }

    public int size() throws IOException {
        return mIndex.size();
    }

    public boolean isEmpty() throws IOException {
        return mIndex.isEmpty();
    }

    public boolean containsKey(Object key) throws IOException {
        try {
            mLock.acquireReadLock();
            Long Id = (Long)mIndex.get(key);
            if (Id == null) {
                return false;
            }
            if (mRepository.fileExists(Id.longValue())) {
                return true;
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }

        // This part is reached only if there is an inconsistency in the map.
        // To repair, remove the key.
        removeKeyIfError(key);
        return false;
    }

    public boolean containsValue(Object value) throws IOException {
        try {
            mLock.acquireReadLock();
            PersistentIterator it = mIndex.values().iterator();
            while (it.hasNext()) {
                long id = ((Long)it.next()).longValue();
                try {
                    Object obj = mRepository.retrieveObject(id);
                    if (obj == null) {
                        if (value == null) {
                            return true;
                        }
                    }
                    if (obj.equals(value)) {
                        return true;
                    }
                }
                catch (ClassNotFoundException e) {
                }
                catch (FileNotFoundException e) {
                    // Can't repair with just a read lock.
                    //it.remove();
                }
                catch (EOFException e) {
                    // Can't repair with just a read lock.
                    //it.remove();
                }
                catch (ObjectStreamException e) {
                    // Can't repair with just a read lock.
                    //it.remove();
                }
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
        return false;
    }
    
    public Object get(Object key) throws IOException {
        try {
            mLock.acquireReadLock();
            Long Id = (Long)mIndex.get(key);
            if (Id == null) {
                return null;
            }
            try {
                return mRepository.retrieveObject(Id.longValue());
            }
            catch (ClassNotFoundException e) {
                throw new IOException(e.toString());
            }
            catch (FileNotFoundException e) {
            }
            catch (EOFException e) {
                // Immediately delete corrupt object.
                mRepository.deleteFile(Id.longValue());
            }
            catch (ObjectStreamException e) {
                // Immediately delete corrupt object.
                mRepository.deleteFile(Id.longValue());
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }

        // This part is reached only if there is an inconsistency in the map.
        // To repair, remove the key.
        removeKeyIfError(key);
        return null;
    }

    /**
     * The value is saved in the repository only on the first time it is put
     * into the map. The old value is never returned from this method, as that
     * may involve loading it unnecessarily.
     *
     * @return null
     */
    public Object put(Object key, Object value) throws IOException {
        try {
            mLock.acquireWriteLock();
            long id = mRepository.saveObject(value);
            Object oldId;
            try {
                oldId = mIndex.put(key, new Long(id));
            }
            catch (IOException e) {
                try {
                    mRepository.removeObject(id);
                }
                catch (Exception e2) {
                }
                throw e;
            }
            if (oldId != null) {
                mRepository.removeObject(((Long)oldId).longValue());
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
        // Don't waste time fetching old object.
        return null;
    }

    /**
     * Removes an entry mapped by the given key. The old value is never
     * returned from this method, as that may involve loading it unnecessarily.
     *
     * @return null
     */
    public Object remove(Object key) throws IOException {
        try {
            mLock.acquireWriteLock();
            Object oldId = mIndex.remove(key);
            if (oldId != null) {
                mRepository.removeObject(((Long)oldId).longValue());
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
        // Don't waste time fetching old object.
        return null;
    }

    public void clear() throws IOException {
        try {
            mLock.acquireWriteLock();
            PersistentIterator it = mIndex.values().iterator();
            while (it.hasNext()) {
                mRepository.removeObject(((Long)it.next()).longValue());
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

    public PersistentSet keySet() throws IOException {
        return mIndex.keySet();
    }

    public PersistentSet entrySet() throws IOException {
        if (mEntrySet == null) {
            mEntrySet = new EntrySet();
        }
        return mEntrySet;
    }

    public Comparator comparator() {
        return mIndex.comparator();
    }

    public PersistentSortedMap subMap(Object fromKey, Object toKey)
        throws IOException
    {
        return new ObjectRepositoryMap
            (mIndex.subMap(fromKey, toKey), mRepository);
    }

    public PersistentSortedMap headMap(Object toKey) throws IOException {
        return new ObjectRepositoryMap(mIndex.headMap(toKey), mRepository);
    }

    public PersistentSortedMap tailMap(Object fromKey) throws IOException {
        return new ObjectRepositoryMap(mIndex.tailMap(fromKey), mRepository);
    }

    public Object firstKey() throws IOException {
        return mIndex.firstKey();
    }

    public Object lastKey() throws IOException {
        return mIndex.firstKey();
    }

    // Removes a key iff the object it refers to is missing.
    private void removeKeyIfError(Object key) throws IOException {
        try {
            mLock.acquireWriteLock();
            Long Id = (Long)mIndex.get(key);
            if (Id == null || mRepository.fileExists(Id.longValue())) {
                return;
            }
            // Remove bad entry from map.
            mIndex.remove(key);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    private class EntrySet extends AbstractPersistentSet {
        EntrySet() {
            super(ObjectRepositoryMap.this.mLock);
        }

        public int size() throws IOException {
            return ObjectRepositoryMap.this.size();
        }

        public boolean isEmpty() throws IOException {
            return ObjectRepositoryMap.this.isEmpty();
        }

        public PersistentIterator iterator() throws IOException {
            return new EntryIterator();
        }

        public boolean contains(Object o) throws IOException {
            if (!(o instanceof PersistentMap.Entry)) {
                return false;
            }
            PersistentMap.Entry e = (PersistentMap.Entry)o;
            Object found = get(e.getKey());
            if (found != null) {
                return found.equals(e.getValue());
            }
            if (e.getValue() != null) {
                return false;
            }
            return containsKey(e.getKey());
        }
        
        public boolean remove(Object o) throws IOException {
            if (!(o instanceof PersistentMap.Entry)) {
                return false;
            }
            PersistentMap.Entry e = (PersistentMap.Entry)o;
            try {
                mLock.acquireUpgradableLock();
                Object found = get(e.getKey());
                if ((found != null && found.equals(e.getValue())) ||
                    (e.getValue() == null && containsKey(e.getKey()))) {
                    
                    ObjectRepositoryMap.this.remove(e.getKey());
                    return true;
                }
            }
            catch (InterruptedException ex) {
                throw new InterruptedIOException();
            }
            finally {
                mLock.releaseLock();
            }
            return false;
        }

        public void clear() throws IOException {
            ObjectRepositoryMap.this.clear();
        }
    }

    private class EntryIterator implements PersistentIterator {
        private PersistentIterator mKeyIterator;
        private Object mLastKey;

        EntryIterator() throws IOException {
            mKeyIterator = keySet().iterator();
        }

        public boolean hasNext() throws IOException {
            return mKeyIterator.hasNext();
        }
        
        public Object next() throws IOException {
            return new PseudoEntry(mLastKey = mKeyIterator.next());
        }
        
        public void remove() throws IOException {
            try {
                mLock.acquireWriteLock();
                Object id = mIndex.get(mLastKey);
                // If in an illegal state, then calling remove here will
                // throw an exception.
                mKeyIterator.remove();
                if (id != null) {
                    mRepository.removeObject(((Long)id).longValue());
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

    private class PseudoEntry extends AbstractPersistentMapEntry {
        private final Object mKey;

        PseudoEntry(Object key) {
            mKey = key;
        }

        public Object getKey() throws IOException {
            return mKey;
        }

        public Object getValue() throws IOException {
            return get(mKey);
        }

        public Object setValue(Object value) throws IOException {
            return put(mKey, value);
        }
    }
}
