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

package com.go.tea.parsetree;

import com.go.tea.compiler.SourceInfo;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public abstract class CallExpression extends Expression {
    private Name mTarget;
    private ExpressionList mParams;
    private Statement mInitializer;
    private Block mSubParam;
    private boolean mVoidPermitted = false;

    public CallExpression(SourceInfo info, 
                          Name target, 
                          ExpressionList params,
                          Block subParam) {
        super(info);

        mTarget = target;
        mParams = params;
        mSubParam = subParam;
    }

    public Object clone() {
        CallExpression ce = (CallExpression)super.clone();
        ce.mParams = (ExpressionList)mParams.clone();
        if (mInitializer != null) {
            ce.mInitializer = (Statement)mInitializer.clone();
        }
        if (mSubParam != null) {
            ce.mSubParam = (Block)mSubParam.clone();
        }
        return ce;
    }

    public boolean isExceptionPossible() {
        return true;
    }

    public Name getTarget() {
        return mTarget;
    }

    public ExpressionList getParams() {
        return mParams;
    }

    /**
     * Initializer is a section of code that executes before the substitution
     * param. By default, it is null. If a CallExpression has a substitution
     * param, a type checker may define an initializer.
     */
    public Statement getInitializer() {
        return mInitializer;
    }

    public Block getSubstitutionParam() {
        return mSubParam;
    }

    /**
     * A CallExpression is permitted to return void only in certain cases.
     * By default this method returns false.
     */
    public boolean isVoidPermitted() {
        return mVoidPermitted;
    }

    public void setParams(ExpressionList params) {
        mParams = params;
    }

    public void setInitializer(Statement stmt) {
        mInitializer = stmt;
    }

    public void setSubstitutionParam(Block subParam) {
        mSubParam = subParam;
    }

    public void setVoidPermitted(boolean b) {
        mVoidPermitted = b;
    }
}
