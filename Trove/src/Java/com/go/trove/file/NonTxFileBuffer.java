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
import java.io.InterruptedIOException;
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * A TxFileBuffer implementation that satisfies the API requirements, but
 * doesn't actually do anything useful for transactions. All calls are
 * delegated to a wrapped FileBuffer, and calls to write and truncate call
 * begin and commit. At a minimum, subclasses implementing transaction support 
 * need to override only begin and commit.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
public class NonTxFileBuffer implements TxFileBuffer {
    protected final FileBuffer mFile;

    public NonTxFileBuffer(FileBuffer file) {
        mFile = file;
    }

    public int read(long position, byte[] dst, int offset, int length)
        throws IOException
    {
        return mFile.read(position, dst, offset, length);
    }

    public int write(long position, byte[] src, int offset, int length)
        throws IOException
    {
        begin();
        int amt = mFile.write(position, src, offset, length);
        commit();
        return amt;
    }

    public int read(long position) throws IOException {
        return mFile.read(position);
    }

    public void write(long position, int value) throws IOException {
        begin();
        mFile.write(position, value);
        commit();
    }
    
    public long size() throws IOException {
        return mFile.size();
    }

    public void truncate(long size) throws IOException {
        begin();
        mFile.truncate(size);
        commit();
    }

    public ReadWriteLock lock() {
        return mFile.lock();
    }

    public boolean force() throws IOException {
        return mFile.force();
    }

    public boolean isReadOnly() throws IOException {
        return mFile.isReadOnly();
    }

    public boolean isOpen() {
        return mFile.isOpen();
    }

    public void close() throws IOException {
        mFile.close();
    }

    public void close(long timeout) throws IOException {
        mFile.close();
    }

    /**
     * Always returns false.
     */
    public boolean isRollbackSupported() {
        return false;
    }

    /**
     * Always returns true.
     */
    public boolean isClean() throws IOException {
        return true;
    }

    /**
     * Does nothing.
     */
    public void begin() throws IOException {
    }

    /**
     * Always returns false.
     */
    public boolean commit() throws IOException {
        return false;
    }

    /**
     * Always throws UnsupportedOperationException.
     */
    public boolean rollback() {
        throw new UnsupportedOperationException();
    }
}
