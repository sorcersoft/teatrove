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
 * Template is the main container node for Tea templates.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class Template extends Node {
    private Name mName;
    private Variable[] mParams;
    private boolean mSubParam;
    private Statement mStatement;
    private Type mType;

    public Template(SourceInfo info,
                    Name name, Variable[] params, boolean subParam,
                    Statement statement) {

        super(info);

        mName = name;
        mParams = params;
        mSubParam = subParam;
        mStatement = statement;
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        Template t = (Template)super.clone();

        int length = mParams.length;
        Variable[] newParams = new Variable[length];
        for (int i=0; i<length; i++) {
            newParams[i] = (Variable)mParams[i].clone();
        }
        t.mParams = newParams;

        t.mStatement = (Statement)mStatement.clone();
        return t;
    }

    public Name getName() {
        return mName;
    }

    public Variable[] getParams() {
        return mParams;
    }

    public boolean hasSubstitutionParam() {
        return mSubParam;
    }

    /**
     * Will likely return a StatementList or Block in order to hold many
     * statements.
     *
     * @see StatementList
     * @see Block
     */
    public Statement getStatement() {
        return mStatement;
    }

    public void setStatement(Statement stmt) {
        mStatement = stmt;
    }

    /**
     * The return type is set by a type checker. Returns null if this template
     * returns void, which is the default value.
     */
    public Type getReturnType() {
        return mType;
    }

    public void setReturnType(Type type) {
        mType = type;
    }
}
