/* ====================================================================
 * Tea - Copyright (c) 1997-2000 Walt Disney Internet Group
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

package com.go.tea.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import com.go.tea.compiler.SourceInfo;
import com.go.tea.compiler.CompilationUnit;
import com.go.tea.compiler.ErrorEvent;
import com.go.tea.compiler.ErrorListener;
import com.go.trove.io.LinePositionReader;

/******************************************************************************
 * ConsoleErrorReporter takes ErrorEvents and prints
 * detailed messages to a PrintStream. When no longer needed, close the
 * ConsoleErrorReporter to ensure all open resources (except the PrintStream)
 * are closed. 
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 00/12/13 <!-- $-->
 */
public class ConsoleErrorReporter implements ErrorListener {
    private PrintStream mOut;
    private LinePositionReader mPositionReader;
    private CompilationUnit mPositionReaderUnit;

    public ConsoleErrorReporter(PrintStream out) {
        mOut = out;
    }

    /**
     * Closes all open resources.
     */
    public void close() throws IOException {
        if (mPositionReader != null) {
            mPositionReader.close();
        }

        mPositionReader = null;
        mPositionReaderUnit = null;
    }

    public void compileError(ErrorEvent e) {
        mOut.println(e.getDetailedErrorMessage());

        SourceInfo info = e.getSourceInfo();
        CompilationUnit unit = e.getCompilationUnit();

        try {
            if (unit != null && info != null) {
                int line = info.getLine();
                int start = info.getStartPosition();
                int end = info.getEndPosition();

                if (mPositionReader == null ||
                    mPositionReaderUnit != unit ||
                    mPositionReader.getLineNumber() >= line) {
                    
                    mPositionReaderUnit = unit;
                    mPositionReader = new LinePositionReader
                        (new BufferedReader(unit.getReader()));
                } 

                mPositionReader.skipForwardToLine(line);
                int position = mPositionReader.getNextPosition();

                String lineStr = mPositionReader.readLine();
                lineStr = mPositionReader.cleanWhitespace(lineStr);
                mOut.println(lineStr);
                
                int indentSize = start - position;
                String indent = 
                    mPositionReader.createSequence(' ', indentSize);

                int markerSize = end - start + 1;
                String marker = 
                    mPositionReader.createSequence('^', markerSize);
                
                mOut.print(indent);
                mOut.println(marker);
                mOut.println();
            }
        }
        catch (IOException ex) {
            Thread t = Thread.currentThread();
            t.getThreadGroup().uncaughtException(t, ex);
        }
    }

    protected void finalize() throws Throwable {
        close();
    }
}
