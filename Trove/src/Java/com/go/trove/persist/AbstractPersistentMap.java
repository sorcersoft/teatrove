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
import java.lang.reflect.UndeclaredThrowableException;
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * Just like {@link java.util.AbstractMap} except methods may throw
 * IOExceptions. Also, the toString method isn't defined, as persistent maps
 * may be very large.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/01/03 <!-- $-->
 */
public abstract class AbstractPersistentMap implements PersistentMap {
    protected final ReadWriteLock mLock;

    protected AbstractPersistentMap(ReadWriteLock lock) {
        mLock = lock;
    }

    public int size() throws IOException {
        return entrySet().size();
    }

    public boolean isEmpty() throws IOException {
        return size() == 0;
    }

    public boolean containsValue(Object value) throws IOException {
        try {
            mLock.acquireReadLock();
            PersistentIterator i = values().iterator();
            if (value == null) {
                while (i.hasNext()) {
                    if (i.next() == null) {
                        return true;
                    }
                }
            }
            else {
                while (i.hasNext()) {
                    if (value.equals(i.next())) {
                        return true;
                    }
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

    public boolean containsKey(Object key) throws IOException {
        try {
            mLock.acquireReadLock();
            PersistentIterator i = entrySet().iterator();
            if (key == null) {
                while (i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (e.getKey() == null) {
                        return true;
                    }
                }
            }
            else {
                while (i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (key.equals(e.getKey())) {
                        return true;
                    }
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
            PersistentIterator i = entrySet().iterator();
            if (key == null) {
                while (i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (e.getKey() == null) {
                        return e.getValue();
                    }
                }
            }
            else {
                while (i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (key.equals(e.getKey())) {
                        return e.getValue();
                    }
                }
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
        return null;
    }

    public Object put(Object key, Object value) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) throws IOException {
        try {
            mLock.acquireWriteLock();
            PersistentIterator i = entrySet().iterator();
            PersistentMap.Entry correctEntry = null;
            if (key == null) {
                while (correctEntry == null && i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (e.getKey()==null) {
                        correctEntry = e;
                    }
                }
            }
            else {
                while (correctEntry == null && i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    if (key.equals(e.getKey())) {
                        correctEntry = e;
                    }
                }
            }
            
            Object oldValue = null;
            if (correctEntry != null) {
                oldValue = correctEntry.getValue();
                i.remove();
            }
            return oldValue;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Puts all the elements of the given map into this one without acquiring
     * a long-held locks on either map.
     */
    public void putAll(PersistentMap map) throws IOException {
        PersistentIterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            PersistentMap.Entry entry = (PersistentMap.Entry)it.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() throws IOException {
        try {
            mLock.acquireWriteLock();
            entrySet().clear();
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public ReadWriteLock lock() {
        return mLock;
    }

    private transient PersistentSet keySet = null;

    public PersistentSet keySet() throws IOException {
        if (keySet == null) {
            keySet = new KeySet(mLock);
        }
        return keySet;
    }
    
    private transient PersistentCollection values = null;

    public PersistentCollection values() throws IOException {
        if (values == null) {
            values = new Values(mLock);
        }
        return values;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        
        if (!(o instanceof PersistentMap)) {
            return false;
        }
        
        PersistentMap t = (PersistentMap)o;

        try {
            mLock.acquireReadLock();
            try {
                t.lock().acquireReadLock();
                if (t.size() != size()) {
                    return false;
                }
                
                PersistentIterator i = entrySet().iterator();
                while (i.hasNext()) {
                    PersistentMap.Entry e = (PersistentMap.Entry)i.next();
                    Object key = e.getKey();
                    Object value = e.getValue();
                    if (value == null) {
                        if (!(t.get(key) == null && t.containsKey(key))) {
                            return false;
                        }
                    }
                    else {
                        if (!value.equals(t.get(key))) {
                            return false;
                        }
                    }
                }
            }
            finally {
                t.lock().releaseLock();
            }
        }
        catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
        finally {
            mLock.releaseLock();
        }

        return true;
    }
    
    public int hashCode() {
        int h = 0;
        try {
            mLock.acquireReadLock();
            PersistentIterator i = entrySet().iterator();
            while (i.hasNext()) {
                h += i.next().hashCode();
            }
        }
        catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
        finally {
            mLock.releaseLock();
        }
        return h;
    }

    public String toString() {
        return getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(this));
    }

    private class KeySet extends AbstractPersistentSet {
        KeySet(ReadWriteLock lock) {
            super(lock);
        }

        public PersistentIterator iterator() throws IOException {
            final PersistentIterator i = entrySet().iterator();
            
            return new PersistentIterator() {
                    public boolean hasNext() throws IOException {
                        return i.hasNext();
                    }
                    
                    public Object next() throws IOException {
                        return ((PersistentMap.Entry)i.next()).getKey();
                    }
                    
                    public void remove() throws IOException {
                        i.remove();
                    }
                };
        }
        
        public int size() throws IOException {
            return AbstractPersistentMap.this.size();
        }
        
        public boolean contains(Object k) throws IOException {
            return AbstractPersistentMap.this.containsKey(k);
        }
    }

    private class Values extends AbstractPersistentCollection {
        Values(ReadWriteLock lock) {
            super(lock);
        }

        public PersistentIterator iterator() throws IOException {
            final PersistentIterator i = entrySet().iterator();
            
            return new PersistentIterator() {
                    public boolean hasNext() throws IOException {
                        return i.hasNext();
                    }
                    
                    public Object next() throws IOException {
                        return ((PersistentMap.Entry)i.next()).getValue();
                    }
                    
                    public void remove() throws IOException {
                        i.remove();
                    }
                };
        }
        
        public int size() throws IOException {
            return AbstractPersistentMap.this.size();
        }
        
        public boolean contains(Object v) throws IOException {
            return AbstractPersistentMap.this.containsValue(v);
        }
    }
}
