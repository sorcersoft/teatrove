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
 * Unlike {@link java.io.DataOutputStream}, no OutputStream is required by the
 * constructor. OutputStream implementations that also implement DataOutput may
 * simply extend this class to simplify implementation.
 * <p>
 * AbstractDataOutputStream is not thread-safe, but then its uncommon for
 * multiple threads to write to the same OutputStream.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/03/04 <!-- $-->
 */
public abstract class AbstractDataOutputStream extends OutputStream
    implements DataOutput
{
    private byte[] mTemp;

    public void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    public void writeByte(int v) throws IOException {
        write(v);
    }

    public void writeShort(int v) throws IOException {
        DataIO.writeShort(this, v, tempArray());
    }

    public void writeChar(int v) throws IOException {
        DataIO.writeChar(this, v, tempArray());
    }

    public void writeInt(int v) throws IOException {
        DataIO.writeInt(this, v, tempArray());
    }

    public void writeLong(long v) throws IOException {
        DataIO.writeLong(this, v, tempArray());
    }

    public void writeFloat(float v) throws IOException {
        DataIO.writeFloat(this, v, tempArray());
    }

    public void writeDouble(double v) throws IOException {
        DataIO.writeDouble(this, v, tempArray());
    }

    public void writeBytes(String s) throws IOException {
        DataIO.writeBytes(this, s);
    }

    public void writeChars(String s) throws IOException {
        DataIO.writeChars(this, s);
    }

    public void writeUTF(String s) throws IOException {
        int length = s.length();
        char[] chars = new char[length];
        s.getChars(0, length, chars, 0);
        int utflen = DataIO.calculateUTFLength(chars, 0, length);
        if (utflen > 65535) {
            throw new UTFDataFormatException();
        }
        writeShort(utflen);
        DataIO.writeUTF((OutputStream)this, chars, 0, length);
    }

    private byte[] tempArray() {
        if (mTemp == null) {
            mTemp = new byte[8];
        }
        return mTemp;
    }
}
