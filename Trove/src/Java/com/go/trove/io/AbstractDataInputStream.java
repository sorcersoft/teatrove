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

package com.go.trove.io;

import java.io.*;

/******************************************************************************
 * Unlike {@link java.io.DataInputStream}, no InputStream is required by the
 * constructor. InputStream implementations that also implement DataInput may
 * simply extend this class to simplify implementation.
 * <p>
 * AbstractDataInputStream is not thread-safe, but then its uncommon for
 * multiple threads to read from the same InputStream.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/03/04 <!-- $-->
 */
public abstract class AbstractDataInputStream extends InputStream
    implements DataInput
{
    private byte[] mTemp;

    public void readFully(byte[] b) throws IOException {
        DataIO.readFully(this, b, 0, b.length);
    }

    public void readFully(byte[] b, int offset, int length)
        throws IOException
    {
        DataIO.readFully(this, b, offset, length);
    }

    public int skipBytes(int n) throws IOException {
        return (int)skip(n);
    }

    public boolean readBoolean() throws IOException {
        return DataIO.readBoolean(this);
    }

    public byte readByte() throws IOException {
        return DataIO.readByte(this);
    }

    public int readUnsignedByte() throws IOException {
        return DataIO.readUnsignedByte(this);
    }

    public short readShort() throws IOException {
        return DataIO.readShort(this, tempArray());
    }

    public int readUnsignedShort() throws IOException {
        return DataIO.readUnsignedShort(this, tempArray());
    }

    public char readChar() throws IOException {
        return DataIO.readChar(this, tempArray());
    }

    public int readInt() throws IOException {
        return DataIO.readInt(this, tempArray());
    }

    public long readLong() throws IOException {
        return DataIO.readLong(this, tempArray());
    }

    public float readFloat() throws IOException {
        return DataIO.readFloat(this, tempArray());
    }

    public double readDouble() throws IOException {
        return DataIO.readDouble(this, tempArray());
    }

    /**
     * Always throws an IOException.
     */
    public String readLine() throws IOException {
        throw new IOException("readLine not supported");
    }

    public String readUTF() throws IOException {
        int bytesExpected = readUnsignedShort();
        char[] chars = new char[bytesExpected];
        int charCount = DataIO.readUTF
            ((InputStream)this, chars, 0, bytesExpected, bytesExpected);
        return new String(chars, 0, charCount);
    }

    private byte[] tempArray() {
        if (mTemp == null) {
            mTemp = new byte[8];
        }
        return mTemp;
    }
}
