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

/******************************************************************************
 * A simple TxFileBuffer implementation that uses a tag bit to indicate if
 * the file is in a clean state. As long as all commit operations finish, the
 * file will be tagged clean. Rollback operations are not supported.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
public class TaggedTxFileBuffer extends NonTxFileBuffer
    implements TxFileBuffer
{
    private final Bitlist mBitlist;
    private final long mBitlistPos;
    private int mTranCount;
    private boolean mClosing;

    public TaggedTxFileBuffer(FileBuffer dataFile, Bitlist bitlist,
                              long bitlistPosition) {
        super(dataFile);
        mBitlist = bitlist;
        mBitlistPos = bitlistPosition;
    }

    /**
     * Truncating the file to zero restores it to a clean state.
     */
    public void truncate(long size) throws IOException {
        super.truncate(size);
        if (size == 0) {
            synchronized (this) {
                mTranCount = 0;
                // Clear the bit to indicate clean.
                mBitlist.clear(mBitlistPos);
            }
        }
    }

    public synchronized boolean force() throws IOException {
        // Order the force calls differently in case an IOException is thrown
        // on the second call.
        if (mTranCount > 0) {
            return mBitlist.force() & mFile.force();
        }
        else {
            return mFile.force() & mBitlist.force();
        }
    }

    public synchronized void close() throws IOException {
        mFile.close();
    }

    public synchronized void close(long timeout) throws IOException {
        if (!mFile.isOpen()) {
            return;
        }
        mClosing = true;
        if (timeout != 0 && mTranCount > 0) {
            try {
                if (timeout < 0) {
                    wait();
                }
                else {
                    wait(timeout);
                }
            }
            catch (InterruptedException e) {
            }
        }
        mFile.close();
        mClosing = false;
    }

    public synchronized boolean isClean() throws IOException {
        return mTranCount == 0 && !mBitlist.get(mBitlistPos);
    }

    public synchronized void begin() throws IOException {
        if (!mFile.isOpen()) {
            throw new IOException("FileBuffer closed");
        }
        if (mTranCount <= 0) {
            if (mClosing) {
                throw new IOException("FileBuffer closing");
            }
            mTranCount = 1;
            // Set the bit to indicate dirty.
            mBitlist.set(mBitlistPos);
        }
        else {
            mTranCount++;
        }
    }

    public synchronized boolean commit() throws IOException {
        if (mTranCount > 0) {
            if (--mTranCount <= 0) {
                // Clear the bit to indicate clean.
                mBitlist.clear(mBitlistPos);
                notifyAll();
            }
            return true;
        }
        return false;
    }

    public synchronized boolean rollback() {
        notifyAll();
        throw new UnsupportedOperationException();
    }
}
