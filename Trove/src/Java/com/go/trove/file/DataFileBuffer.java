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
import java.io.EOFException;

/******************************************************************************
 * Extends the regular FileBuffer interface for providing I/O operations on
 * primitive data types.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/05 <!-- $-->
 */
public interface DataFileBuffer extends FileBuffer {
    /**
     * Read one signed byte at the given position.
     */
    byte readByte(long position) throws IOException, EOFException;

    /**
     * Read one unsigned byte at the given position.
     */
    int readUnsignedByte(long position) throws IOException, EOFException;

    /**
     * Read one signed short at the given position, big endian order.
     */
    short readShort(long position) throws IOException, EOFException;

    /**
     * Read one signed short at the given position, little endian order.
     */
    short readShortLE(long position) throws IOException, EOFException;

    /**
     * Read one unsigned short at the given position, big endian order.
     */
    int readUnsignedShort(long position) throws IOException, EOFException;

    /**
     * Read one unsigned short at the given position, little endian order.
     */
    int readUnsignedShortLE(long position) throws IOException, EOFException;

    /**
     * Read one char at the given position, big endian order.
     */
    char readChar(long position) throws IOException, EOFException;

    /**
     * Read one char at the given position, little endian order.
     */
    char readCharLE(long position) throws IOException, EOFException;

    /**
     * Read one int at the given position, big endian order.
     */
    int readInt(long position) throws IOException, EOFException;

    /**
     * Read one int at the given position, little endian order.
     */
    int readIntLE(long position) throws IOException, EOFException;

    /**
     * Read one long at the given position, big endian order.
     */
    long readLong(long position) throws IOException, EOFException;

    /**
     * Read one long at the given position, little endian order.
     */
    long readLongLE(long position) throws IOException, EOFException;

    /**
     * Write one byte at the given position.
     */
    void writeByte(long position, int value) throws IOException;

    /**
     * Write one short at the given position, big endian order.
     */
    void writeShort(long position, int value) throws IOException;

    /**
     * Write one short at the given position, little endian order.
     */
    void writeShortLE(long position, int value) throws IOException;

    /**
     * Write one char at the given position, big endian order.
     */
    void writeChar(long position, int value) throws IOException;

    /**
     * Write one char at the given position, little endian order.
     */
    void writeCharLE(long position, int value) throws IOException;

    /**
     * Write one int at the given position, big endian order.
     */
    void writeInt(long position, int value) throws IOException;

    /**
     * Write one int at the given position, little endian order.
     */
    void writeIntLE(long position, int value) throws IOException;

    /**
     * Write one long at the given position, big endian order.
     */
    void writeLong(long position, long value) throws IOException;

    /**
     * Write one int at the given position, little endian order.
     */
    void writeLongLE(long position, long value) throws IOException;
}
