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
import com.go.trove.io.AbstractDataInputStream;

/******************************************************************************
 * An InputStream interface to a FileBuffer which supports marking and
 * repositioning. FileBufferInputStream is not thread-safe, but then its
 * uncommon for multiple threads to read from the same InputStream.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/26 <!-- $-->
 */
public class FileBufferInputStream extends AbstractDataInputStream
    implements DataInput
{
    private FileBuffer mFileBuffer;
    private long mPosition;
    private long mMark;
    private final boolean mCloseBuffer;

    /**
     * Creates a FileBufferInputStream initially positioned at the beginning of
     * the file. When this InputStream is closed, the underlying FileBuffer is
     * also closed.
     *
     * @param fb FileBuffer to read from
     */
    public FileBufferInputStream(FileBuffer fb) {
        this(fb, 0, true);
    }

    /**
     * Creates a FileBufferInputStream with any start position.
     *
     * @param fb FileBuffer to read from
     * @param position Initial read position
     * @param closeBuffer When true, FileBuffer is closed when this
     * InputStream is closed.
     */
    public FileBufferInputStream(FileBuffer fb,
                                 long position,
                                 boolean closeBuffer) {
        mFileBuffer = fb;
        mPosition = position;
        mCloseBuffer = closeBuffer;
    }

    public int read() throws IOException {
        checkClosed();
        int value = mFileBuffer.read(mPosition);
        if (value >= 0) {
            mPosition++;
        }
        return value;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        checkClosed();

        if (length == 0) {
            return 0;
        }

        int originalOffset = offset;
        int amt;

        do {
            amt = mFileBuffer.read(mPosition, b, offset, length);
            if (amt <= 0) {
                break;
            }
            mPosition += amt;
            offset += amt;
            length -= amt;
        } while (length > 0);

        amt = offset - originalOffset;;
        return amt == 0 ? -1 : amt;
    }

    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long newPos = mPosition + n;
        long size = mFileBuffer.size();
        if (newPos > size) {
            newPos = size;
            n = newPos - mPosition;
        }
        mPosition = newPos;
        return n;
    }

    public int available() throws IOException {
        long avail = mFileBuffer.size() - mPosition;
        if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        else if (avail <= 0) {
            return 0;
        }
        else {
            return (int)avail;
        }
    }

    public void mark(int readlimit) {
        mMark = mPosition;
    }

    public void reset() {
        mPosition = mMark;
    }

    public boolean markSupported() {
        return true;
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

    public String readLine() throws IOException {
        StringBuffer buf = null;

    loop:
        while (true) {
            int c = read();

            if (c < 0) {
                break;
            }
            else if (buf == null) {
                buf = new StringBuffer(128);
            }

            switch (c) {
            case '\n':
                break loop;
                
            case '\r':
                long oldPos = mPosition;
                int c2 = read();
                if (c2 != '\n' && c2 != -1) {
                    mPosition = oldPos;
                }
                break loop;
                
            default:
                buf.append((char)c);
                break;
            }
        }

        return buf == null ? null : buf.toString();
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
