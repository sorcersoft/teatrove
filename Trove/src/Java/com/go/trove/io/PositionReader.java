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
 * The PositionReader tracks the postion in the stream of the next character 
 * to be read. PositionReaders chain together such that the position is
 * read from the earliest PositionReader in the chain.
 *
 * <p>Position readers automatically close the underlying input stream when
 * the end of file is reached. Ordinary input streams don't do this.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 12/11/00 <!-- $-->
 */
public class PositionReader extends FilterReader {
    /** Is non-null when this PositionReader is chained to another. */
    protected PositionReader mPosReader;

    protected int mPosition = 0;

    private boolean mClosed = false;

    public PositionReader(Reader reader) {
        super(reader);
        if (reader instanceof PositionReader) {
            mPosReader = (PositionReader)reader;
        }
    }

    /**
     * @return the position of the next character to be read.
     */
    public int getNextPosition() {
        return mPosition;
    }

    public int read() throws IOException {
        try {
            int c;
            if ((c = in.read()) != -1) {
                if (mPosReader == null) {
                    mPosition++;
                }
                else {
                    mPosition = mPosReader.getNextPosition();
                }
            }
            else {
                close();
            }
            
            return c;
        }
        catch (IOException e) {
            if (mClosed) {
                return -1;
            }
            else {
                throw e;
            }
        }
    }

    public int read(char[] buf, int off, int length) throws IOException {
        int i = 0;
        while (i < length) {
            int c;
            if ((c = read()) == -1) {
                return (i == 0)? -1 : i;
            }
            buf[i++ + off] = (char)c;
        }

        return i;
    }

    public void close() throws IOException {
        mClosed = true;
        super.close();
    }
}
