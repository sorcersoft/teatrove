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

import java.util.LinkedList;
import com.go.tea.compiler.SourceInfo;
import com.go.tea.compiler.Type;

/******************************************************************************
 * A ParenExpression is a thin wrapper around an Expression that was
 * delimited by parenthesis in the source code. A parse tree does not
 * necessarily need a ParenExpression, but it is useful when reconstructing
 * something that resembles the source code.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/02/01 <!-- $-->
 */
public class ParenExpression extends Expression {
    private Expression mExpr;

    public ParenExpression(SourceInfo info, Expression expr) {
        super(info);
        mExpr = expr;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        ParenExpression pe = (ParenExpression)super.clone();
        pe.mExpr = (Expression)mExpr.clone();
        return pe;
    }

    public boolean isExceptionPossible() {
        return super.isExceptionPossible() || 
            (mExpr != null && mExpr.isExceptionPossible());
    }

    public Type getType() {
        return mExpr.getType();
    }

    public Type getInitialType() {
        return mExpr.getInitialType();
    }

    public void convertTo(Type toType, boolean preferCast) {
        mExpr.convertTo(toType, preferCast);
    }

    public LinkedList getConversionChain() {
        return mExpr.getConversionChain();
    }

    public void setType(Type type) {
        mExpr.setType(type);
    }

    public void setInitialType(Type type) {
        mExpr.setInitialType(type);
    }

    public boolean isValueKnown() {
        return mExpr.isValueKnown();
    }

    public Object getValue() {
        return mExpr.getValue();
    }

    public Expression getExpression() {
        return mExpr;
    }

    public void setExpression(Expression expr) {
        mExpr = expr;
    }
}
