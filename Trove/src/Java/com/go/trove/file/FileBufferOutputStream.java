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
import com.go.trove.io.AbstractDataOutputStream;

/******************************************************************************
 * An OutputStream interface to a FileBuffer which supports repositioning.
 * FileBufferOutputStream is not thread-safe, but then its uncommon for
 * multiple threads to write to the same OutputStream.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/26 <!-- $-->
 */
public class FileBufferOutputStream extends AbstractDataOutputStream
    implements DataOutput
{
    private FileBuffer mFileBuffer;
    private long mPosition;
    private final boolean mCloseBuffer;

    /**
     * Creates a FileBufferOutputStream initially positioned at the beginning
     * of the file. When this OutputStream is closed, the underlying
     * FileBuffer is also closed.
     *
     * @param fb FileBuffer to write to
     */
    public FileBufferOutputStream(FileBuffer fb) {
        this(fb, 0, true);
    }

    /**
     * Creates a FileBufferOutputStream with any start position.
     *
     * @param fb FileBuffer to write to
     * @param position Initial write position
     * @param closeBuffer When true, FileBuffer is closed when this
     * OutputStream is closed.
     */
    public FileBufferOutputStream(FileBuffer fb,
                                  long position,
                                  boolean closeBuffer) {
        mFileBuffer = fb;
        mPosition = position;
        mCloseBuffer = closeBuffer;
    }

    public void write(int b) throws IOException {
        checkClosed();
        mFileBuffer.write(mPosition, b);
        mPosition++;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int offset, int length) throws IOException {
        checkClosed();

        if (length == 0) {
            return;
        }

        int amt;

        do {
            amt = mFileBuffer.write(mPosition, b, offset, length);
            if (amt <= 0) {
                break;
            }
            mPosition += amt;
            offset += amt;
            length -= amt;
        } while (length > 0);
    }

    public void close() throws IOException {
        if (mFileBuffer != null) {
            if (mCloseBuffer) {
                mFileBuffer.close();
            }
            mFileBuffer = null;
        }
    }

    public boolean isOpen() {
        return mFileBuffer != null;
    }

    public long position() throws IOException {
        checkClosed();
        return mPosition;
    }

    public void position(long position) throws IOException {
        checkClosed();
        if (position < 0) {
            throw new IllegalArgumentException("Position < 0: " + position);
        }
        mPosition = position;
    }

    protected void finalize() throws IOException {
        close();
    }

    private void checkClosed() throws IOException {
        if (mFileBuffer == null) {
            throw new IOException("Stream closed");
        }
    }
}
