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
import java.lang.reflect.Method;

/******************************************************************************
 * A Lookup can access properties on objects. A Bean Introspector is used to
 * get the available properties from an object. Arrays, Lists and Strings also
 * have a built-in property named "length". For arrays, the length field is
 * retrieved, for Lists, the size() method is called, and for Strings, the
 * length() method is called.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 * @see java.beans.Introspector
 */
public class Lookup extends Expression {
    private Expression mExpr;
    private Token mDot;
    private Name mLookupName;
    private Method mMethod;

    public Lookup(SourceInfo info, Expression expr, Token dot, 
                  Name lookupName) {
        super(info);

        mExpr = expr;
        mDot = dot;
        mLookupName = lookupName;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        Lookup lookup = (Lookup)super.clone();
        lookup.mExpr = (Expression)mExpr.clone();
        return lookup;
    }

    public boolean isExceptionPossible() {
        if (super.isExceptionPossible()) {
            return true;
        }
        
        if (mExpr != null) {
            if (mExpr.isExceptionPossible()) {
                return true;
            }
            Type type = mExpr.getType();
            if (type != null && type.isNullable()) {
                return true;
            }
        }

        return mMethod != null;
    }

    public Expression getExpression() {
        return mExpr;
    }

    public Token getDot() {
        return mDot;
    }

    public Name getLookupName() {
        return mLookupName;
    }

    /**
     * Returns the method to invoke in order to perform the lookup. This is
     * filled in by the type checker. If the lookup name is "length" and
     * the expression type is an array, the read method is null. A code
     * generator must still be able to get the length of the array.
     */
    public Method getReadMethod() {
        return mMethod;
    }

    public void setExpression(Expression expr) {
        mExpr = expr;
    }

    public void setReadMethod(Method m) {
        mMethod = m;
    }
}
