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
import com.go.tea.compiler.Token;
import com.go.tea.compiler.Type;

/******************************************************************************
 * A BinaryExpression contains a left expression, a right expression and
 * an operator. BinaryExpressions never evaluate to null.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public abstract class BinaryExpression extends Expression {
    private Token mOperator;
    private Expression mLeft;
    private Expression mRight;

    public BinaryExpression(SourceInfo info,
                            Token operator,
                            Expression left,
                            Expression right) {
        super(info);

        mOperator = operator;
        mLeft = left;
        mRight = right;
    }
    
    public Object clone() {
        BinaryExpression be = (BinaryExpression)super.clone();
        be.mLeft = (Expression)mLeft.clone();
        be.mRight = (Expression)mRight.clone();
        return be;
    }

    public boolean isExceptionPossible() {
        if (mLeft != null) {
            if (mLeft.isExceptionPossible()) {
                return true;
            }
            Type type = mLeft.getType();
            if (type != null && type.isNullable()) {
                return true;
            }
        }

        if (mRight != null) {
            if (mRight.isExceptionPossible()) {
                return true;
            }
            Type type = mRight.getType();
            if (type != null && type.isNullable()) {
                return true;
            }
        }

        return false;
    }

    public void setType(Type type) {
        // BinaryExpressions never evaluate to null.
        super.setType(type.toNonNull());
    }

    public Token getOperator() {
        return mOperator;
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    public void setLeftExpression(Expression left) {
        mLeft = left;
    }

    public void setRightExpression(Expression right) {
        mRight = right;
    }
}
