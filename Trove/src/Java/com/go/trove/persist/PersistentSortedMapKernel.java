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

import java.util.Comparator;
import java.io.*;
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 * @see PersistentSortedMapView
 */
public interface PersistentSortedMapKernel {
    final Object NO_KEY = new String("no key");

    Comparator comparator();

    int size() throws IOException;

    boolean isEmpty() throws IOException;

    boolean containsKey(Object key) throws IOException;

    boolean containsValue(Object value) throws IOException;

    Object get(Object key) throws IOException;

    PersistentMap.Entry getEntry(Object key) throws IOException;

    Object put(Object key, Object value) throws IOException;

    Object remove(Object key) throws IOException;

    /**
     * Returns NO_KEY if map is empty.
     */
    Object firstKey() throws IOException;

    /**
     * Returns NO_KEY if map is empty.
     */
    Object lastKey() throws IOException;

    /**
     * Returns null if map is empty.
     */
    PersistentMap.Entry firstEntry() throws IOException;

    /**
     * Returns null if map is empty.
     */
    PersistentMap.Entry lastEntry() throws IOException;

    /**
     * Returns a key, contained in this map, that is higher than the one given.
     * If no contained key is higher, NO_KEY is returned.
     */
    Object nextKey(Object key) throws IOException;

    /**
     * Returns a key contained in this map, that is lower than the one given.
     * If no contained key is lower, NO_KEY is returned.
     */
    Object previousKey(Object key) throws IOException;

    /**
     * Returns an entry, contained in this map, whose key is higher than the
     * one given. If no contained entry is higher, null is returned.
     */
    PersistentMap.Entry nextEntry(Object key) throws IOException;

    /**
     * Returns an entry contained in this map, whose key is lower than the one
     * given. If no contained entry is lower, null is returned.
     */
    PersistentMap.Entry previousEntry(Object key) throws IOException;

    void clear() throws IOException;

    /**
     * Clear all the entries within the specified range. The "from key" is
     * inclusive, but the "to key" is exclusive. If any key value is specified
     * as NO_KEY, then the range is open.
     */
    void clear(Object fromKey, Object toKey) throws IOException;

    /**
     * Copy all of the map keys into the given array, starting at the given
     * index. Return the new incremented index value.
     */
    int copyKeysInto(Object[] a, int index) throws IOException;

    /**
     * Copy all of the map entries into the given array, starting at the given
     * index. Return the new incremented index value.
     */
    int copyEntriesInto(Object[] a, int index) throws IOException;

    ReadWriteLock lock();
}
