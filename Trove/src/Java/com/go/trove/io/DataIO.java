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

import java.io.IOException;
import java.io.EOFException;
import java.io.UTFDataFormatException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInput;
import java.io.DataOutput;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/03/04 <!-- $-->
 */
public class DataIO {
    public static final void readFully(InputStream in, byte[] b)
        throws IOException
    {
        readFully(in, b, 0, b.length);
    }

    public static final void readFully(InputStream in,
                                       byte[] b, int offset, int length)
        throws IOException
    {
        int n = 0;
        while (n < length) {
            int amt = in.read(b, offset + n, length - n);
            if (amt <= 0) {
                throw new EOFException();
            }
            n += amt;
        }
    }

    public static final boolean readBoolean(InputStream in)
        throws IOException
    {
        int v = in.read();
        if (v < 0) {
            throw new EOFException();
        }
        return v != 0;
    }

    public static final byte readByte(InputStream in) throws IOException {
        int v = in.read();
        if (v < 0) {
            throw new EOFException();
        }
        return (byte)v;
    }

    public static final int readUnsignedByte(InputStream in)
        throws IOException
    {
        int v = in.read();
        if (v < 0) {
            throw new EOFException();
        }
        return v & 0xff;
    }

    public static final short readShort(InputStream in, byte[] temp)
        throws IOException
    {
        if (in.read(temp, 0, 2) < 2) {
            throw new EOFException();
        }
        return (short)((temp[0] << 8) | (temp[1] & 0xff));
    }

    public static final int readUnsignedShort(InputStream in, byte[] temp)
        throws IOException
    {
        if (in.read(temp, 0, 2) < 2) {
            throw new EOFException();
        }
        return ((temp[0] & 0xff) << 8) | (temp[1] & 0xff);
    }

    public static final char readChar(InputStream in, byte[] temp)
        throws IOException
    {
        if (in.read(temp, 0, 2) < 2) {
            throw new EOFException();
        }
        return (char)((temp[0] << 8) | (temp[1] & 0xff));
    }

    public static final int readInt(InputStream in, byte[] temp)
        throws IOException
    {
        if (in.read(temp, 0, 4) < 4) {
            throw new EOFException();
        }
        return (temp[0] << 24) | ((temp[1] & 0xff) << 16) |
            ((temp[2] & 0xff) << 8) | (temp[3] & 0xff);
    }

    public static final long readLong(InputStream in, byte[] temp)
        throws IOException
    {
        if (in.read(temp, 0, 8) < 8) {
            throw new EOFException();
        }
        return
            (((long)(((temp[0]       ) << 24) |
                     ((temp[1] & 0xff) << 16) |
                     ((temp[2] & 0xff) << 8 ) | 
                     ((temp[3] & 0xff)      ))              ) << 32) |
            (((long)(((temp[4]       ) << 24) |
                     ((temp[5] & 0xff) << 16) |
                     ((temp[6] & 0xff) << 8 ) | 
                     ((temp[7] & 0xff)      )) & 0xffffffffL)      );
    }

    public static final float readFloat(InputStream in, byte[] temp)
        throws IOException
    {
        return Float.intBitsToFloat(readInt(in, temp));
    }

    public static final double readDouble(InputStream in, byte[] temp)
        throws IOException
    {
        return Double.longBitsToDouble(readLong(in, temp));
    }

    /**
     * Reads UTF-8 encoded characters from the given stream, but does not read
     * the length.
     *
     * @return number of characters actually read, or -1 if EOF reached.
     */
    public static final int readUTF(InputStream in,
                                    char[] chars, int offset, int length)
        throws IOException
    {
        if (length == 0) {
            return 0;
        }

        int c, c2, c3;

        int charCount = 0;
        while (charCount < length) {
            c = in.read();
            if (c < 0) {
                break;
            }
            c &= 0xff;
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                chars[offset + charCount++] = (char)c;
                break;
            case 12: case 13:
                // 110x xxxx  10xx xxxx
                c2 = in.read();
                if (c2 < 0) {
                    throw new EOFException();
                }
                if ((c2 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] =
                    (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                c2 = in.read();
                if (c2 < 0) {
                    throw new EOFException();
                }
                c3 = in.read();
                if (c3 < 0) {
                    throw new EOFException();
                }
                if ((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] = (char)(((c & 0x0f) << 12) |
                                                     ((c2 & 0x3f) << 6) |
                                                     (c3 & 0x3f));
                break;
            default:
                // 10xx xxxx,  1111 xxxx
                throw new UTFDataFormatException();
            }
        }

        return (charCount == 0) ? -1 : charCount;
    }

    /**
     * Reads UTF-8 encoded characters from the given stream, but does not read
     * the length.
     *
     * @return number of characters actually read, or -1 if EOF reached.
     */
    public static final int readUTF(DataInput in,
                                    char[] chars, int offset, int length)
        throws IOException
    {
        if (length == 0) {
            return 0;
        }

        int c, c2, c3;

        int charCount = 0;
        while (charCount < length) {
            try {
                c = in.readByte() & 0xff;
            }
            catch (EOFException e) {
                break;
            }
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                chars[offset + charCount++] = (char)c;
                break;
            case 12: case 13:
                // 110x xxxx  10xx xxxx
                c2 = in.readByte();
                if ((c2 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] =
                    (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                c2 = in.readByte();
                c3 = in.readByte();
                if ((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] = (char)(((c & 0x0f) << 12) |
                                                     ((c2 & 0x3f) << 6) |
                                                     (c3 & 0x3f));
                break;
            default:
                // 10xx xxxx,  1111 xxxx
                throw new UTFDataFormatException();
            }
        }

        return (charCount == 0) ? -1 : charCount;
    }

    /**
     * Reads UTF-8 encoded characters from the given stream, but does not read
     * the length.
     *
     * @param bytesExpected number of bytes expected to read
     * @return number of characters actually read
     */
    public static final int readUTF(InputStream in,
                                    char[] chars, int offset, int length,
                                    int bytesExpected)
        throws IOException
    {
        int c, c2, c3;

        int byteCount = 0;
        int charCount = 0;
        while (byteCount < bytesExpected && charCount < length) {
            c = in.read();
            if (c < 0) {
                throw new EOFException();
            }
            c &= 0xff;
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                byteCount++;
                chars[offset + charCount++] = (char)c;
                break;
            case 12: case 13:
                // 110x xxxx  10xx xxxx
                if ((byteCount += 2) > bytesExpected) {
                    throw new UTFDataFormatException();
                }
                c2 = in.read();
                if (c2 < 0 || (c2 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] =
                    (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                if ((byteCount += 3) > bytesExpected) {
                    throw new UTFDataFormatException();
                }
                c2 = in.read();
                c3 = in.read();
                if (c2 < 0 || (c2 & 0xc0) != 0x80 ||
                    c3 < 0 || (c3 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] = (char)(((c & 0x0f) << 12) |
                                                     ((c2 & 0x3f) << 6) |
                                                     (c3 & 0x3f));
                break;
            default:
                // 10xx xxxx,  1111 xxxx
                throw new UTFDataFormatException();
            }
        }

        return charCount;
    }

    /**
     * Reads UTF-8 encoded characters from the given stream, but does not read
     * the length.
     *
     * @param bytesExpected number of bytes expected to read
     * @return number of characters actually read
     */
    public static final int readUTF(DataInput in,
                                    char[] chars, int offset, int length,
                                    int bytesExpected)
        throws IOException
    {
        int c, c2, c3;

        int byteCount = 0;
        int charCount = 0;
        while (byteCount < bytesExpected && charCount < length) {
            c = in.readByte() & 0xff;
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                byteCount++;
                chars[offset + charCount++] = (char)c;
                break;
            case 12: case 13:
                // 110x xxxx  10xx xxxx
                if ((byteCount += 2) > bytesExpected) {
                    throw new UTFDataFormatException();
                }
                c2 = in.readByte();
                if ((c2 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] =
                    (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                if ((byteCount += 3) > bytesExpected) {
                    throw new UTFDataFormatException();
                }
                c2 = in.readByte();
                c3 = in.readByte();
                if ((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                chars[offset + charCount++] = (char)(((c & 0x0f) << 12) |
                                                     ((c2 & 0x3f) << 6) |
                                                     (c3 & 0x3f));
                break;
            default:
                // 10xx xxxx,  1111 xxxx
                throw new UTFDataFormatException();
            }
        }

        return charCount;
    }

    public static final int calculateUTFLength(String str) {
        int length = str.length();
        int utflen = 0;
        for (int i = 0; i < length; i++) {
            int c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen;
    }

    public static final int calculateUTFLength(char[] chars,
                                               int offset, int length)
    {
        int utflen = 0;
        for (int i = 0; i < length; i++) {
            int c = chars[i + offset];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen;
    }

    public static final void writeShort(OutputStream out, int v, byte[] temp)
        throws IOException
    {
        temp[0] = (byte)(v >> 8);
        temp[1] = (byte)v;
        out.write(temp, 0, 2);
    }

    public static final void writeChar(OutputStream out, int v, byte[] temp)
        throws IOException
    {
        temp[0] = (byte)(v >> 8);
        temp[1] = (byte)v;
        out.write(temp, 0, 2);
    }

    public static final void writeInt(OutputStream out, int v, byte[] temp)
        throws IOException
    {
        temp[0] = (byte)(v >> 24);
        temp[1] = (byte)(v >> 16);
        temp[2] = (byte)(v >> 8);
        temp[3] = (byte)v;
        out.write(temp, 0, 4);
    }

    public static final void writeLong(OutputStream out, long v, byte[] temp)
        throws IOException
    {
        int w = (int)(v >> 32);
        temp[0] = (byte)(w >> 24);
        temp[1] = (byte)(w >> 16);
        temp[2] = (byte)(w >> 8);
        temp[3] = (byte)w;
        w = (int)v;
        temp[4] = (byte)(w >> 24);
        temp[5] = (byte)(w >> 16);
        temp[6] = (byte)(w >> 8);
        temp[7] = (byte)w;
        out.write(temp, 0, 8);
    }

    public static final void writeFloat(OutputStream out, float v, byte[] temp)
        throws IOException
    {
        writeInt(out, Float.floatToIntBits(v), temp);
    }

    public static final void writeDouble(OutputStream out, double v,
                                         byte[] temp)
        throws IOException
    {
        writeLong(out, Double.doubleToLongBits(v), temp);
    }

    public static final void writeBytes(OutputStream out, String s)
        throws IOException
    {
        int strlen = s.length();
        char[] chars = new char[strlen];
        s.getChars(0, strlen, chars, 0);
        
        byte[] bytes = new byte[strlen];

        for (int i = 0; i < strlen; ) {
            bytes[i++] = (byte)chars[i];
        }

        out.write(bytes);
    }

    public static final void writeChars(OutputStream out, String s)
        throws IOException
    {
        int strlen = s.length();
        char[] chars = new char[strlen];
        s.getChars(0, strlen, chars, 0);
        
        byte[] bytes = new byte[strlen * 2];

        for (int i = 0, j = 0; i < strlen; ) {
            int c = chars[i++];
            bytes[j++] = (byte)(c >> 8);
            bytes[j++] = (byte)c;
        }

        out.write(bytes);
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     *
     * @param workspace temporary buffer to store characters in
     */
    public static final void writeUTF(OutputStream out,
                                      String str, char[] workspace)
        throws IOException
    {
        writeUTF(out, str, 0, str.length(), workspace);
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     *
     * @param workspace temporary buffer to store characters in
     */
    public static final void writeUTF(DataOutput out,
                                      String str, char[] workspace)
        throws IOException
    {
        writeUTF(out, str, 0, str.length(), workspace);
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     *
     * @param workspace temporary buffer to store characters in
     */
    public static final void writeUTF(OutputStream out,
                                      String str, int offset, int strlen,
                                      char[] workspace)
        throws IOException
    {
        int worklen = workspace.length;
        while (true) {
            int amt = strlen <= worklen ? strlen : worklen;
            str.getChars(offset, offset + amt, workspace, 0);
            writeUTF(out, workspace, 0, amt);
            if ((strlen -= amt) <= 0) {
                break;
            }
            offset += amt;
        }
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     *
     * @param workspace temporary buffer to store characters in
     */
    public static final void writeUTF(DataOutput out,
                                      String str, int offset, int strlen,
                                      char[] workspace)
        throws IOException
    {
        int worklen = workspace.length;
        while (true) {
            int amt = strlen <= worklen ? strlen : worklen;
            str.getChars(offset, offset + amt, workspace, 0);
            writeUTF(out, workspace, 0, amt);
            if ((strlen -= amt) <= 0) {
                break;
            }
            offset += amt;
        }
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     */
    public static final void writeUTF(OutputStream out,
                                      char[] chars, int offset, int length)
        throws IOException
    {
        for (int i=0; i<length; i++) {
            int c = chars[i + offset];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.write(c);
            }
            else if (c > 0x07FF) {
                out.write(0xe0 | ((c >> 12) & 0x0f));
                out.write(0x80 | ((c >> 6) & 0x3f));
                out.write(0x80 | (c & 0x3f));
            }
            else {
                out.write(0xc0 | ((c >> 6) & 0x1f));
                out.write(0x80 | (c & 0x3f));
            }
        }
    }

    /**
     * Writes UTF-8 encoded characters to the given stream, but does not write
     * the length.
     */
    public static final void writeUTF(DataOutput out,
                                      char[] chars, int offset, int length)
        throws IOException
    {
        for (int i=0; i<length; i++) {
            int c = chars[i + offset];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.writeByte(c);
            }
            else if (c > 0x07FF) {
                out.writeByte(0xe0 | ((c >> 12) & 0x0f));
                out.writeByte(0x80 | ((c >> 6) & 0x3f));
                out.writeByte(0x80 | (c & 0x3f));
            }
            else {
                out.writeByte(0xc0 | ((c >> 6) & 0x1f));
                out.writeByte(0x80 | (c & 0x3f));
            }
        }
    }
}
