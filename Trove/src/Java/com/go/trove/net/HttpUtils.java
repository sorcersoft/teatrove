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

package com.go.trove.net;

import java.io.*;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/10/09 <!-- $-->
 */
public class HttpUtils {
    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     */
    public static String readLine(InputStream in, byte[] buffer)
        throws IllegalArgumentException, IOException
    {
        return readLine(in, buffer, -1);
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     * @throws LineTooLongException when line is longer than the limit
     */
    public static String readLine(InputStream in, byte[] buffer, int limit)
        throws IllegalArgumentException, IOException, LineTooLongException
    {
        if (!in.markSupported()) {
            throw new IllegalArgumentException
                ("InputStream doesn't support marking: " + in.getClass());
        }

        String line = null;

        int cursor = 0;
        int len = buffer.length;

        int count = 0;
        int c;
    loop:
        while ((c = in.read()) >= 0) {
            if (limit >= 0 && ++count > limit) {
                throw new LineTooLongException(limit);
            }

            switch (c) {
            case '\r':
                in.mark(1);
                if (in.read() != '\n') {
                    in.reset();
                }
                // fall through
            case '\n':
                if (line == null && cursor == 0) {
                    return "";
                }
                break loop;
            default:
                if (cursor >= len) {
                    if (line == null) {
                        line = new String(buffer, "8859_1");
                    }
                    else {
                        line = line.concat(new String(buffer, "8859_1"));
                    }
                    cursor = 0;
                }
                buffer[cursor++] = (byte)c;
            }
        }

        if (cursor > 0) {
            if (line == null) {
                line = new String(buffer, 0, cursor, "8859_1");
            }
            else {
                line = line.concat(new String(buffer, 0, cursor, "8859_1"));
            }
        }

        return line;
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     */
    public static String readLine(InputStream in, char[] buffer)
        throws IllegalArgumentException, IOException
    {
        return readLine(in, buffer, -1);
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     * @throws LineTooLongException when line is longer than the limit
     */
    public static String readLine(InputStream in, char[] buffer, int limit)
        throws IllegalArgumentException, IOException, LineTooLongException
    {
        if (!in.markSupported()) {
            throw new IllegalArgumentException
                ("InputStream doesn't support marking: " + in.getClass());
        }

        String line = null;

        int cursor = 0;
        int len = buffer.length;

        int count = 0;
        int c;
    loop:
        while ((c = in.read()) >= 0) {
            if (limit >= 0 && ++count > limit) {
                throw new LineTooLongException(limit);
            }

            switch (c) {
            case '\r':
                in.mark(1);
                if (in.read() != '\n') {
                    in.reset();
                }
                // fall through
            case '\n':
                if (line == null && cursor == 0) {
                    return "";
                }
                break loop;
            default:
                if (cursor >= len) {
                    if (line == null) {
                        line = new String(buffer);
                    }
                    else {
                        line = line.concat(new String(buffer));
                    }
                    cursor = 0;
                }
                buffer[cursor++] = (char)c;
            }
        }

        if (cursor > 0) {
            if (line == null) {
                line = new String(buffer, 0, cursor);
            }
            else {
                line = line.concat(new String(buffer, 0, cursor));
            }
        }

        return line;
    }
}
