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

package com.go.tea.compiler;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class ErrorEvent extends java.util.EventObject {

    private String mErrorMsg;
    private Token mCulprit;
    private SourceInfo mInfo;
    private CompilationUnit mUnit;

    ErrorEvent(Object source, String errorMsg, Token culprit) {
        this(source, errorMsg, culprit, null);
    }

    ErrorEvent(Object source, String errorMsg, SourceInfo info) {
        this(source, errorMsg, info, null);
    }

    ErrorEvent(Object source, String errorMsg, Token culprit, 
               CompilationUnit unit) {
        super(source);
        mErrorMsg = errorMsg;
        mCulprit = culprit;
        if (culprit != null) {
            mInfo = culprit.getSourceInfo();
        }
        mUnit = unit;
    }

    ErrorEvent(Object source, String errorMsg, SourceInfo info, 
               CompilationUnit unit) {
        super(source);
        mErrorMsg = errorMsg;
        mInfo = info;
        mUnit = unit;
    }

    public String getErrorMessage() {
        return mErrorMsg;
    }

    /**
     * Returns the error message prepended with source file information.
     */
    public String getDetailedErrorMessage() {
        String prepend = getSourceInfoMessage();
        if (prepend == null || prepend.length() == 0) {
            return mErrorMsg;
        }
        else {
            return prepend + ": " + mErrorMsg;
        }
    }

    public String getSourceInfoMessage() {
        String msg;
        if (mUnit == null) {
            if (mInfo == null) {
                msg = "";
            }
            else {
                msg = String.valueOf(mInfo.getLine());
            }
        }
        else {
            if (mInfo == null) {
                msg = mUnit.getSourceFileName();
            }
            else {
                msg = 
                    mUnit.getSourceFileName() + ':' + mInfo.getLine();
            }
        }

        return msg;
    }

    /**
     * This method reports on where in the source code an error was found.
     *
     * @return Source information on this error or null if not known.
     */
    public SourceInfo getSourceInfo() {
        return mInfo;
    }

    /**
     * @return Null if there was no offending token
     */
    public Token getCulpritToken() {
        return mCulprit;
    }

    /**
     * @return Null if there was no CompilationUnit
     */
    public CompilationUnit getCompilationUnit() {
        return mUnit;
    }
}
