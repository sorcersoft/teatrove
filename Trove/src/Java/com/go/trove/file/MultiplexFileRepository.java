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
import java.io.FileNotFoundException;
import com.go.trove.util.ReadWriteLock;
import com.go.trove.util.SecureReadWriteLock;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/05 <!-- $-->
 */
public class MultiplexFileRepository implements FileRepository {
    private final MultiplexFile mMF;

    private final FileBuffer mFreeIds;
    // Set bits indicate that the file exists.
    private final Bitlist mFreeIdBitlist;
    private final FileBufferInputStream mFreeIdsIn;
    private final FileBufferOutputStream mFreeIdsOut;
    private final ReadWriteLock mLock;

    /**
     * @param mf MultiplexFile to store files
     */
    public MultiplexFileRepository(MultiplexFile mf) throws IOException {
        this(mf, 1);
    }

    /**
     * @param mf MultiplexFile to store files
     * @param firstId First id in MultiplexFile to use
     */
    public MultiplexFileRepository(MultiplexFile mf, int firstId)
        throws IOException
    {
        mMF = mf;
        if (firstId <= 0) {
            // Never use file zero.
            firstId = 1;
        }
        mFreeIds = mf.openFile(firstId);
        mFreeIdBitlist = new Bitlist(mf.openFile(firstId + 1));
        mFreeIdsIn = new FileBufferInputStream(mFreeIds);
        mFreeIdsOut = new FileBufferOutputStream(mFreeIds);
        mLock = mFreeIds.lock();
    }

    public long fileCount() throws IOException {
        try {
            mLock.acquireReadLock();
            return mFreeIdBitlist.size() - mFreeIds.size() / 8;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public Iterator fileIds() throws IOException {
        return new Iter();
    }

    public boolean fileExists(long id) throws IOException {
        try {
            mLock.acquireReadLock();
            return mFreeIdBitlist.get(id);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public FileBuffer openFile(long id)
        throws IOException, FileNotFoundException
    {
        try {
            mLock.acquireReadLock();
            if (!mFreeIdBitlist.get(id)) {
                throw new FileNotFoundException(String.valueOf(id));
            }
            return mMF.openFile(id);
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public long createFile() throws IOException {
        try {
            mLock.acquireWriteLock();
            long id;
            if (mFreeIds.size() < 8) {
                id = mMF.getFileCount();
                // This creates the file.
                mMF.openFile(id).close();
            }
            else {
                long pos = mFreeIds.size() - 8;
                mFreeIdsIn.position(pos);
                id = mFreeIdsIn.readLong();
                mFreeIds.truncate(pos);
            }
            mFreeIdBitlist.set(id);
            return id;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            mLock.releaseLock();
        }
    }

    public boolean deleteFile(long id) throws IOException {
        try {
            mLock.acquireUpgradableLock();
            if (mFreeIdBitlist.get(id)) {
                mLock.acquireWriteLock();
                try {
                    mMF.deleteFile(id);
                    mFreeIdBitlist.clear(id);
                    long pos = mFreeIds.size();
                    mFreeIdsOut.position(pos);
                    mFreeIdsOut.writeLong(id);
                }
                finally {
                    mLock.releaseLock();
                }
                return true;
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

    public ReadWriteLock lock() {
        return mLock;
    }

    public int getBlockSize() {
        return mMF.getBlockSize();
    }

    public int getBlockIdScale() {
        return mMF.getBlockIdScale();
    }

    public int getLengthScale() {
        return mMF.getLengthScale();
    }

    public long getMaximumFileLength() {
        return mMF.getMaximumFileLength();
    }

    private class Iter implements Iterator {
        private byte[] mTemp;
        private long mIndex;

        Iter() throws IOException {
            mTemp = new byte[32];
            mIndex = mFreeIdBitlist.findFirstSet(0, mTemp);
        }

        public boolean hasNext() throws IOException {
            return mIndex >= 0;
        }

        public long next() throws IOException {
            if (mIndex < 0) {
                throw new java.util.NoSuchElementException();
            }
            long index = mIndex;
            mIndex = mFreeIdBitlist.findFirstSet(index + 1, mTemp);
            return index;
        }
    }
}
