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
import com.go.tea.compiler.Type;

/******************************************************************************
 * A ForeachStatement iterates over the values of an array or a Collection, 
 * storing each value in a variable, allowing a statement or statements to 
 * operate on each. Reverse looping is supported for arrays and Lists.
 *
 * <p>Because Collections don't know the type of elements they 
 * contain (they only know that they are Objects), the only operations allowed
 * on the loop variable are those that are defined for Object. 
 *
 * <p>Collection class can be subclassed to contain a special
 * field that defines the element type. The field must have the following
 * signature: <tt>public static final Class ELEMENT_TYPE</tt>
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class ForeachStatement extends Statement {
    private VariableRef mLoopVar;
    private Expression mRange;
    private Expression mEndRange;
    private boolean mReverse;
    private Statement mInitializer;
    private Block mBody;

    public ForeachStatement(SourceInfo info,
                            VariableRef loopVar,
                            Expression range,
                            Expression endRange,
                            boolean reverse,
                            Block body) {
        super(info);
        
        mLoopVar = loopVar;
        mRange = range;
        mEndRange = endRange;
        mReverse = reverse;
        mBody = body;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        ForeachStatement fs = (ForeachStatement)super.clone();
        fs.mLoopVar = (VariableRef)mLoopVar.clone();
        fs.mRange = (Expression)mRange.clone();
        fs.mEndRange = (Expression)mEndRange.clone();
        if (mInitializer != null) {
            fs.mInitializer = (Statement)mInitializer.clone();
        }
        fs.mBody = (Block)mBody.clone();
        return fs;
    }

    public VariableRef getLoopVariable() {
        return mLoopVar;
    }

    public Expression getRange() {
        return mRange;
    }

    /**
     * Returns null if this foreach statement iterates over an array/collection
     * instead of an integer range of values.
     */
    public Expression getEndRange() {
        return mEndRange;
    }

    public boolean isReverse() {
        return mReverse;
    }

    /**
     * Initializer is a section of code that executes before the loop is
     * entered. By default, it is null. A type checker may define an
     * initializer.
     */
    public Statement getInitializer() {
        return mInitializer;
    }

    public Block getBody() {
        return mBody;
    }

    public void setRange(Expression range) {
        mRange = range;
    } 

    public void setEndRange(Expression endRange) {
        mEndRange = endRange;
    } 

    public void setInitializer(Statement stmt) {
        mInitializer = stmt;
    }

    public void setBody(Block body) {
        mBody = body;
    }
}
