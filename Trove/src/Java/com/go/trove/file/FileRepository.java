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

package com.go.trove.file;

import java.io.IOException;
import java.io.FileNotFoundException;
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * Allows files to be created with automatically assigned identifiers. Files
 * should not be opened or deleted unless the identifier was returned by
 * createFile.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/05 <!-- $-->
 */
public interface FileRepository {
    /**
     * Returns the count of files in this repository.
     */
    long fileCount() throws IOException;

    /**
     * Returns an iterator of all the file ids in this repository.
     */
    Iterator fileIds() throws IOException;

    /**
     * Returns true if the file by the given id exists.
     */
    boolean fileExists(long id) throws IOException;

    /**
     * Returns the FileBuffer for the file by the given id.
     *
     * @throws FileNotFoundException if the file doesn't exist
     */
    FileBuffer openFile(long id) throws IOException, FileNotFoundException;

    /**
     * Returns the id of the newly created file, which is never zero.
     */
    long createFile() throws IOException;

    /**
     * Returns false if the file by the given doesn't exist.
     */
    boolean deleteFile(long id) throws IOException;

    /**
     * Lock access to FileRepository methods. Use of this lock does not
     * restrict operations on open FileBuffers.
     * <p>
     * When only a read lock is held, no files may be created or deleted. When
     * an upgradable lock is held, only the owner thread can create or delete
     * files. When a write lock is held, all operations are suspended except
     * for the owner thread.
     */
    ReadWriteLock lock();

    public interface Iterator {
        boolean hasNext() throws IOException;

        long next() throws IOException;
    }
}
