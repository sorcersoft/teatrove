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
import java.lang.reflect.Method;

/******************************************************************************
 * An ArrayLookup can access indexed properties on objects. A Bean 
 * Introspector can be used to get the available indexed properties from
 * an object, however, the only ones supported by Tea are unnamed.
 * For this reason, the Introspector is not used. Any class with methods named
 * "get" that return something and have a single parameter (the type of which 
 * is not limited to ints) will support an array lookup.
 *
 * <p>Arrays, Collections and Strings are treated specially, and they all 
 * support array lookup on an int typed index.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 * @see com.go.tea.util.BeanAnalyzer
 */
public class ArrayLookup extends Expression {
    private Expression mExpr;
    private Token mToken;
    private Expression mLookupIndex;
    private Method mMethod;

    public ArrayLookup(SourceInfo info, Expression expr, Token lookupToken,
                       Expression lookupIndex) {
        super(info);

        mExpr = expr;
        mToken = lookupToken;
        mLookupIndex = lookupIndex;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        ArrayLookup al = (ArrayLookup)super.clone();
        al.mExpr = (Expression)mExpr.clone();
        al.mLookupIndex = (Expression)mLookupIndex.clone();
        return al;
    }

    public boolean isExceptionPossible() {
        // ArrayIndexOutOfBoundsException is always a possibility.
        return true;
    }

    public Expression getExpression() {
        return mExpr;
    }

    public Token getLookupToken() {
        return mToken;
    }

    public Expression getLookupIndex() {
        return mLookupIndex;
    }

    /**
     * Returns the method to invoke in order to perform the lookup. This is
     * filled in by the type checker. If the expression type is an array, the 
     * read method is null. A code generator must still be able to get 
     * elements from an array.
     */
    public Method getReadMethod() {
        return mMethod;
    }

    public void setExpression(Expression expr) {
        mExpr = expr;
    }

    public void setLookupIndex(Expression lookupIndex) {
        mLookupIndex = lookupIndex;
    }

    public void setReadMethod(Method m) {
        mMethod = m;
    }
}
