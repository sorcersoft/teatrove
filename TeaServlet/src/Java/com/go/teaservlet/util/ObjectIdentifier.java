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

package com.go.teaservlet.util;

import java.util.*;
import java.lang.ref.WeakReference;
import com.go.trove.util.IdentityMap;

/******************************************************************************
 * ObjectIdentifier assigns unique string identifiers to objects for retrieval.
 * The string identifier is safe to use in a URL query string for referencing
 * non-persistent objects in between stateless HTTP transactions. For example,
 * a web page could present a list of items that, when an item is clicked,
 * produces another page that shows detail information for the selected item.
 *
 * <p>The object identifiers that are assigned are not persistent and will be
 * different if the process is restarted. This means that a web page in a
 * browser could contain invalid object identifiers if the web server is
 * restarted. Users of this class should attempt to gracefully handle cases
 * where an object cannot be retrieved, or in rare circumstances, the retrieved
 * object is of an unexpected type.
 *
 * <p>ObjectIdentifier is not appropriate for use on objects that already
 * support a unique identifier, such as objects that can be retrieved from a
 * database. In that case, use the database's unique identifier for the object
 * instead, and possibly encrypt it.
 *
 * <p>Internally, ObjectIdentifier makes use of weak references so that
 * identifiers don't prevent objects from being garbage collected.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/13/00 <!-- $-->
 * @see java.lang.ref.WeakReference
 *
 * @deprecated Use of ObjectIdentifier does not work correctly when Servlet is
 * striped against multiple processes.
 */
public class ObjectIdentifier {
    private static ObjectIdentifier cObjectIdentifer = new ObjectIdentifier();

    /**
     * Returns a URL-safe string that uniquely identifies the given object.
     */
    public static String identify(Object obj) {
        return cObjectIdentifer.getIdentifer(obj);
    }

    /**
     * Retrieves an object using an identifer string as produced by the
     * identify method, or null if not found. Under rare circumstances, the
     * retrieved object could be an unexpected type, in which case it should be
     * ignored. Therefore, care must be taken when casting the retrieved
     * object.
     */
    public static Object retrieve(String identifier) {
        return cObjectIdentifer.getObject(identifier);
    }

    // Maps Object identity hashcodes to identifer Strings.
    private Map mIdentifiers;

    // Maps identifer Strings to weakly referenced Objects.
    private Map mObjects;

    private Random mRandom;

    private ObjectIdentifier() {
        mIdentifiers = new IdentityMap();
        mObjects = new WeakHashMap();
        mRandom = new Random();
    }

    private synchronized String getIdentifer(Object obj) {
        String id = (String)mIdentifiers.get(obj);

        if (id == null) {
            do {
                id = Long.toString(mRandom.nextLong() & Long.MAX_VALUE, 36);
            } while (mObjects.containsKey(id));

            mIdentifiers.put(obj, id);
            mObjects.put(id, new WeakReference(obj));
        }

        return id;
    }

    private synchronized Object getObject(String identifier) {
        WeakReference ref = (WeakReference)mObjects.get(identifier);
        return (ref == null) ? null : ref.get();
    }
}
