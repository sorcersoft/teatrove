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
 * A Variable represents a variable declaration. A VariableRef is used to
 * reference Variables.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/05/07 <!-- $-->
 * @see VariableRef
 */
public class Variable extends Node {
    private String mName;
    private TypeName mTypeName;
    private Type mType;

    private boolean mField;
    private boolean mStatic;
    private boolean mTransient;

    /**
     * Used for variable declarations.
     */
    public Variable(SourceInfo info, String name, TypeName typeName) {
        super(info);

        mName = name;
        mTypeName = typeName;
    }

    /**
     * Used when creating variables whose type has already been checked.
     */
    public Variable(SourceInfo info, String name, Type type) {
        super(info);

        mName = name;
        mTypeName = new TypeName(info, type);
        mType = type;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public TypeName getTypeName() {
        return mTypeName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /**
     * Returns null if type is unknown.
     */
    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mTypeName = new TypeName(getSourceInfo(), type);
        mType = type;
    }

    /**
     * @return true if this variable is a field instead of a local variable.
     */   
    public boolean isField() {
        return mField;
    }

    /**
     * @return true if this variable is a field and is static.
     */
    public boolean isStatic() {
        return mStatic;
    }

    /**
     * @return true if this variable is transient.
     */
    public boolean isTransient() {
        return mTransient;
    }

    public void setField(boolean b) {
        mField = b;
        if (!b) {
            mStatic = false;
        }
    }

    public void setStatic(boolean b) {
        mStatic = b;
        if (b) {
            mField = true;
        }
    }

    public void setTransient(boolean b) {
        mTransient = b;
    }

    public int hashCode() {
        return mName.hashCode() + mTypeName.hashCode();
    }

    /**
     * Variables are tested for equality only by their name and type.
     * Field status is ignored.
     */
    public boolean equals(Object other) {
        if (other instanceof Variable) {
            Variable v = (Variable)other;
            return mName.equals(v.mName) && mTypeName.equals(v.mTypeName);
        }
        else {
            return false;
        }
    }
}
