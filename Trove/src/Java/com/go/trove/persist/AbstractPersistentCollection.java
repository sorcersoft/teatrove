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
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * Just like {@link java.util.AbstractCollection} except methods may throw
 * IOExceptions. Also, the toString method isn't defined, as persistent
 * collections may be very large.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
public abstract class AbstractPersistentCollection
    implements PersistentCollection
{
    protected final ReadWriteLock mLock;

    protected AbstractPersistentCollection(ReadWriteLock lock) {
        mLock = lock;
    }

    public boolean isEmpty() throws IOException {
        try {
            mLock.acquireReadLock();
            return size() == 0;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public boolean contains(Object o) throws IOException {
        try {
            mLock.acquireReadLock();
            PersistentIterator e = iterator();
            if (o==null) {
                while (e.hasNext()) {
                    if (e.next() == null) {
                        return true;
                    }
                }
            }
            else {
                while (e.hasNext()) {
                    if (o.equals(e.next())) {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public Object[] toArray() throws IOException {
        try {
            mLock.acquireReadLock();
            Object[] result = new Object[size()];
            PersistentIterator e = iterator();
            for (int i=0; e.hasNext(); i++) {
                result[i] = e.next();
            }
            return result;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public Object[] toArray(Object[] a) throws IOException {
        try {
            mLock.acquireReadLock();
            int size = size();
            if (a.length < size) {
                a = (Object[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);
            }
            
            PersistentIterator it=iterator();
            for (int i=0; i<size; i++) {
                a[i] = it.next();
            }
            
            if (a.length > size) {
                a[size] = null;
            }

            return a;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public boolean add(Object o) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) throws IOException {
        try {
            mLock.acquireWriteLock();
            PersistentIterator e = iterator();
            if (o==null) {
                while (e.hasNext()) {
                    if (e.next()==null) {
                        e.remove();
                        return true;
                    }
                }
            }
            else {
                while (e.hasNext()) {
                    if (o.equals(e.next())) {
                        e.remove();
                        return true;
                    }
                }
            }
            return false;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    /**
     * Checks if this collection contains all the same elements as the one
     * given without acquiring long-held locks on either collection.
     */
    public boolean containsAll(PersistentCollection c) throws IOException {
        PersistentIterator e = c.iterator();
        while (e.hasNext()) {
            if (!contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds all of the elements of the given collection without acquiring
     * long-held locks on either collection.
     */
    public boolean addAll(PersistentCollection c) throws IOException {
        boolean modified = false;
        PersistentIterator e = c.iterator();
        while (e.hasNext()) {
            modified |= add(e.next());
        }
        return modified;
    }

    /**
     * Removes all of the elements of the given collection without acquiring
     * long-held locks on either collection.
     */
    public boolean removeAll(PersistentCollection c) throws IOException {
        boolean modified = false;
        PersistentIterator e = iterator();
        while (e.hasNext()) {
            if (c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Retains all of the elements of the given collection without acquiring
     * long-held locks on either collection.
     */
    public boolean retainAll(PersistentCollection c) throws IOException {
        boolean modified = false;
        PersistentIterator e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Clears this collection without acquiring a long-held write lock.
     */
    public void clear() throws IOException {
        PersistentIterator e = iterator();
        while (e.hasNext()) {
            e.next();
            e.remove();
        }
    }

    public ReadWriteLock lock() {
        return mLock;
    }
}

