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
 * An IfStatement consists of a condition, a "then" part and an "else" part.
 * Both the then and else parts are optional, but a parser should never
 * create an IfStatement without a then part. An optimizer may detect that
 * the then part never executes, and so eliminates it.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 */
public class IfStatement extends Statement {
    private Expression mCondition;
    private Block mThenPart;
    private Block mElsePart;
    private Variable[] mMergedVariables;

    public IfStatement(SourceInfo info,
                       Expression condition,
                       Block thenPart) {
        this(info, condition, thenPart, null);
    }

    public IfStatement(SourceInfo info,
                       Expression condition,
                       Block thenPart,
                       Block elsePart) {
        super(info);
        
        mCondition = condition;
        mThenPart = thenPart;
        mElsePart = elsePart;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        IfStatement is = (IfStatement)super.clone();
        is.mCondition = (Expression)mCondition.clone();
        if (mThenPart != null) {
            is.mThenPart = (Block)mThenPart.clone();
        }
        if (mElsePart != null) {
            is.mElsePart = (Block)mElsePart.clone();
        }
        return is;
    }

    public boolean isReturn() {
        return
            mThenPart != null && mThenPart.isReturn() &&
            mElsePart != null && mElsePart.isReturn();
    }

    public boolean isBreak() {
        return
            mThenPart != null && mThenPart.isBreak() &&
            mElsePart != null && mElsePart.isBreak();
    }

    public Expression getCondition() {
        return mCondition;
    }

    /** 
     * @return Null if no then part.
     */
    public Block getThenPart() {
        return mThenPart;
    }

    /**
     * @return Null if no else part.
     */
    public Block getElsePart() {
        return mElsePart;
    }

    public void setCondition(Expression condition) {
        mCondition = condition;
    }

    public void setThenPart(Block block) {
        mThenPart = block;
    }

    public void setElsePart(Block block) {
        mElsePart = block;
    }

    /**
     * Returns the variables that were commonly assigned in both the "then"
     * and "else" parts of the if statement, were merged together and moved
     * into the parent scope. Returns null if not set.
     */
    public Variable[] getMergedVariables() {
        return mMergedVariables;
    }

    public void setMergedVariables(Variable[] vars) {
        mMergedVariables = vars;
    }
}
