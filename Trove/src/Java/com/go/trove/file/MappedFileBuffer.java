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

import java.io.*;
import com.go.trove.util.ReadWriteLock;
import com.go.trove.util.SecureReadWriteLock;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
class MappedFileBuffer implements FileBuffer {
    private long mAddr;
    private int mSize;
    private final boolean mReadOnly;
    private final SecureReadWriteLock mLock;

    public MappedFileBuffer(long handle, int mode, long position, int size,
                            SecureReadWriteLock lock)
        throws IOException
    {
        mAddr = open(handle, mode, position, size);
        mSize = size;
        mReadOnly = mode == SystemFileBuffer.MAP_RO;
        mLock = lock;
    }

    public int read(long position, byte[] dst, int offset, int length)
        throws IOException
    {
        checkArgs(position, dst, offset, length);
        if (length == 0) {
            return 0;
        }
        try {
            mLock.acquireReadLock();
            checkClosed();
            if (position >= mSize) {
                return -1;
            }
            if (position + length > mSize) {
                length = (int)(mSize - position);
            }
            return read(dst, offset, length, mAddr + position);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public int write(long position, byte[] src, int offset, int length)
        throws IOException
    {
        checkArgs(position, src, offset, length);
        if (length == 0) {
            return 0;
        }
        try {
            mLock.acquireWriteLock();
            checkClosed();
            if (position >= mSize) {
                throw new EOFException("position > max file length: " +
                                       position + " > " + mSize);
            }
            if (position + length > mSize) {
                length = (int)(mSize - position);
            }
            return write(src, offset, length, mAddr + position);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public int read(long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException
                ("position < 0: " + position);
        }
        try {
            mLock.acquireReadLock();
            checkClosed();
            if (position >= mSize) {
                return -1;
            }
            return read(mAddr + position);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public void write(long position, int value) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException
                ("position < 0: " + position);
        }
        try {
            mLock.acquireWriteLock();
            checkClosed();
            if (position >= mSize) {
                throw new EOFException("position > max file length: " +
                                       position + " > " + mSize);
            }
            write(mAddr + position, value);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public long size() throws IOException {
        try {
            mLock.acquireReadLock();
            checkClosed();
            return mSize;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public void truncate(long size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0: " + size);
        }
        try {
            mLock.acquireWriteLock();
            checkClosed();
            if (size < mSize) {
                mSize = (int)size;
            }
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

    public boolean force() throws IOException {
        try {
            mLock.acquireWriteLock();
            checkClosed();
            force(mAddr, mSize);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
        return true;
    }

    public boolean isReadOnly() throws IOException {
        checkClosed();
        return mReadOnly;
    }

    public boolean isOpen() {
        return mAddr != 0;
    }

    public void close() throws IOException {
        try {
            mLock.acquireWriteLock();
            if (mAddr != 0) {
                mAddr = 0;
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    private void checkClosed() throws IOException {
        if (mAddr == 0) {
            throw new IOException("FileBuffer closed");
        }
    }

    private void checkArgs(long position,
                           byte[] array, int offset, int length) {
        if (position < 0) {
            throw new IllegalArgumentException("position < 0: " + position);
        }
        
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0: " + offset);
        }

        if (length < 0) {
            throw new IndexOutOfBoundsException("length < 0: " + length);
        }

        if (offset + length > array.length) {
            throw new ArrayIndexOutOfBoundsException
                ("offset + length > array length: " +
                 (offset + length) + " > " + array.length);
        }
    }

    private static native long open(long handle, int mode,
                                    long position, int size)
        throws IOException;

    private native int read(byte[] dst, int offset, int length, long position)
        throws IOException;

    private native int write(byte[] src, int offset, int length, long position)
        throws IOException;

    private native int read0(long position) throws IOException;

    private native void write0(long position, int value) throws IOException;

    private native void force(long addr, int size)
        throws IOException;
}
