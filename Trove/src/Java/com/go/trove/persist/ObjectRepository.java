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
import java.io.FileNotFoundException;
import com.go.trove.file.FileRepository;

/******************************************************************************
 * Simple object persistence mechanism that saves objects using
 * ObjectOutputStream. Objects are cached, so calling retrieve repeatedly
 * on the same object doesn't keep hitting the underlying FileRepository.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/03/06 <!-- $-->
 */
public interface ObjectRepository extends FileRepository {
    /**
     * Access the current thread's ObjectRepository during loading and saving
     * of objects. Objects being serialized can access the local repository
     * from their readObject or writeObject method. With direct access to the
     * repository, serialized objects may break themselves up into separately
     * saved objects.
     *
     * @see Link
     */
    public static final LocalRepository local = new LocalRepository();

    /**
     * Returns the Object saved in the given file. To check if it exists, call
     * the fileExists method. If the object has already been retrieved, the
     * existing instance is returned.
     *
     * @param id object file id, as returned from the save or replace method.
     * @throws FileNotFoundException if the file doesn't exist.
     */
    Object retrieveObject(long id)
        throws IOException, FileNotFoundException, ClassNotFoundException;

    /**
     * Returns the Object saved in the given file. To check if it exists, call
     * the fileExists method. If the object has already been retrieved, the
     * existing instance is returned.
     *
     * @param id object file id, as returned from the save or replace method.
     * @param reserved reserved bytes to skip in the head of the file
     * @throws FileNotFoundException if the file doesn't exist.
     */
    Object retrieveObject(long id, int reserved)
        throws IOException, FileNotFoundException, ClassNotFoundException;

    /**
     * Saves the given object and returns a file id for retrieving it. If the
     * given object instance was already saved, it is not replaced.
     *
     * @see #replaceObject
     */
    long saveObject(Object obj) throws IOException;

    /**
     * Saves the given object and returns a file id for retrieving it. If the
     * given object instance was already saved, it is not replaced.
     *
     * @param reserved reserved bytes to skip in the head of the file
     * @see #replaceObject
     */
    long saveObject(Object obj, int reserved) throws IOException;

    /**
     * Saves the given object and returns a file id for retrieving it. If the
     * given object instance was already saved, it is replaced, and the
     * original id is returned.
     *
     * @see #saveObject
     */
    long replaceObject(Object obj) throws IOException;

    /**
     * Saves the given object and returns a file id for retrieving it. If the
     * given object instance was already saved, it is replaced, and the
     * original id is returned.
     *
     * @param reserved reserved bytes to skip in the head of the file
     * @see #saveObject
     */
    long replaceObject(Object obj, int reserved) throws IOException;

    /**
     * Saves the given object and returns a file id for retrieving it. If the
     * given object instance was already saved, it is replaced, and the
     * original id is returned. If the object wasn't saved, it is written into
     * the identified file, only if it exists.
     *
     * @param reserved reserved bytes to skip in the head of the file
     * @see #saveObject
     */
    long replaceObject(Object obj, int reserved, long id) throws IOException;

    /**
     * Removes the referred object from the repository, but deletion may be
     * deferred until all in memory references are cleared or this repository
     * is closed. If the object is saved or replaced before it is deleted, the
     * deferred delete is canceled.
     * <p>
     * To immediately delete the object, call the deleteFile method.
     *
     * @return true if no in memory references and object was deleted
     */
    boolean removeObject(long id) throws IOException;

    public static final class LocalRepository {
        private final ThreadLocal mLocal = new ThreadLocal();

        private LocalRepository() {
        }

        /**
         * Get the current thread's ObjectRepository, or null if none.
         */
        public ObjectRepository get() {
            return (ObjectRepository)mLocal.get();
        }

        /**
         * Set the current thread's ObjectRepository, returning the previously
         * set one. This method should only be called by ObjectRepository
         * implementations.
         *
         * @return previously set ObjectRepository
         */
        public ObjectRepository set(ObjectRepository local) {
            ObjectRepository old = get();
            mLocal.set(local);
            return old;
        }
    }
}
