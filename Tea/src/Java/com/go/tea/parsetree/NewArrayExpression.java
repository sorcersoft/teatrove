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

import java.beans.IntrospectionException;
import com.go.tea.compiler.SourceInfo;
import com.go.tea.compiler.Type;

/******************************************************************************
 * An expression that evaluates to a new array or Map of values.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class NewArrayExpression extends Expression {
    private ExpressionList mList;
    private boolean mAssociative;

    public NewArrayExpression(SourceInfo info,
                              ExpressionList list,
                              boolean associative) {
        super(info);

        mList = list;
        mAssociative = associative;
    }
    
    public Object clone() {
        NewArrayExpression nae = (NewArrayExpression)super.clone();
        nae.mList = (ExpressionList)mList.clone();
        return nae;
    }

    public boolean isExceptionPossible() {
        if (mList != null) {
            Expression[] exprs = mList.getExpressions();
            for (int i=0; i<exprs.length; i++) {
                if (exprs[i].isExceptionPossible()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public void convertTo(Type toType, boolean preferCast) {
        super.convertTo(toType, preferCast);

        // If converting to a different array element type, convert all the
        // expressions in the list.

        if (String.class.isAssignableFrom(toType.getNaturalClass())) {
            // Special case to prevent String conversion from setting all
            // internal elements to chars.
            return;
        }

        Type elementType;
        try {
            elementType = toType.getArrayElementType();
        }
        catch (IntrospectionException e) {
            throw new RuntimeException(e.toString());
        }

        if (elementType != null) {
            super.setType(toType);

            Expression[] exprs = getExpressionList().getExpressions();

            int index, increment;
            if (isAssociative()) {
                index = 1;
                increment = 2;
            }
            else {
                index = 0;
                increment = 1;
            }

            for (; index < exprs.length; index += increment) {
                if (exprs[index].getType() != Type.NULL_TYPE) {
                    exprs[index].convertTo(elementType, preferCast);
                }
            }
        }
    }

    public void setType(Type type) {
        super.setType(null);
        if (type != null) {
            // NewArrayExpressions never evaluate to null.
            // Call the overridden convertTo method in order for elements to
            // be converted to the correct type.
            this.convertTo(type.toNonNull(), false);
        }
    }

    public ExpressionList getExpressionList() {
        return mList;
    }

    public boolean isAssociative() {
        return mAssociative;
    }

    public void setExpressionList(ExpressionList list) {
        mList = list;
    }

    /**
     * @return true if this array is composed entirely of constants.
     */
    public boolean isAllConstant() {
        Expression[] exprs = mList.getExpressions();

        int i;
        for (i=0; i<exprs.length; i++) {
            Expression expr = exprs[i];

            if (expr instanceof NewArrayExpression) {
                NewArrayExpression nae = (NewArrayExpression)expr;
                if (!nae.isAllConstant()) {
                    return false;
                }
            }
            else if (!expr.isValueKnown()) {
                return false;
            }
        }
        
        return true;
    }
}
