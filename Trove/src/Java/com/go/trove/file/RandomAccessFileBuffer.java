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
import java.io.File;
import java.io.RandomAccessFile;
import com.go.trove.util.ReadWriteLock;
import com.go.trove.util.SecureReadWriteLock;

/******************************************************************************
 * A FileBufferImplementation that calls into the standard Java
 * RandomAccessFile class.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
public class RandomAccessFileBuffer implements FileBuffer {
    private RandomAccessFile mRAF;
    private long mPosition;

    // Bit 0 set: read only
    // Bit 1 set: closed
    private volatile int mFlags;

    private final SecureReadWriteLock mLock = new SecureReadWriteLock();
    
    public RandomAccessFileBuffer(File file, boolean readOnly)
        throws IOException
    {
        this(new RandomAccessFile(file, readOnly ? "r" : "rw"), readOnly);
    }

    /**
     * @param readOnly specify access mode of raf
     */
    public RandomAccessFileBuffer(RandomAccessFile raf, boolean readOnly)
        throws IOException
    {
        mRAF = raf;
        mPosition = raf.getFilePointer();
        mFlags = readOnly ? 1 : 0;
    }

    public int read(long position, byte[] dst, int offset, int length)
        throws IOException
    {
        try {
            // Exclusive lock must be acquired because of the mutable file
            // position. Pretty lame, eh?
            mLock.acquireWriteLock();
            if (position != mPosition) {
                mRAF.seek(position);
                mPosition = position;
            }
            int amt = mRAF.read(dst, offset, length);
            if (amt > 0) {
                mPosition += amt;
            }
            return amt;
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
        try {
            mLock.acquireWriteLock();
            if (position != mPosition) {
                mRAF.seek(position);
                mPosition = position;
            }
            mRAF.write(src, offset, length);
            mPosition += length;
            return length;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public int read(long position) throws IOException {
        try {
            // Exclusive lock must be acquired because of the mutable file
            // position. Pretty lame, eh?
            mLock.acquireWriteLock();
            if (position != mPosition) {
                mRAF.seek(position);
                mPosition = position;
            }
            int value = mRAF.read();
            if (value >= 0) {
                mPosition++;
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

    public void write(long position, int value) throws IOException {
        try {
            mLock.acquireWriteLock();
            if (position != mPosition) {
                mRAF.seek(position);
                mPosition = position;
            }
            mRAF.write(value);
            mPosition++;
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
            return mRAF.length();
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public void truncate(long size) throws IOException {
        try {
            mLock.acquireWriteLock();
            if (size < size()) {
                mRAF.setLength(size);
                mPosition = mRAF.getFilePointer();
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
            mRAF.getFD().sync();
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
        return (mFlags & 1) != 0;
    }

    public boolean isOpen() {
        return (mFlags & 2) == 0;
    }

    public void close() throws IOException {
        try {
            mLock.acquireWriteLock();
            if (isOpen()) {
                mRAF.close();
                mFlags |= 2;
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
