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
 * Base class for all Literals that have a numeric type. 
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class NumberLiteral extends Literal {
    private Number mValue;

    public NumberLiteral(SourceInfo info, Number value) {
        super(info);
        if (value == null) {
            throw new IllegalArgumentException
                ("NumberLiterals cannot be null");
        }
        mValue = value;
        super.setType(new Type(value.getClass()).toPrimitive());
    }

    public NumberLiteral(SourceInfo info, int value) {
        this(info, new Integer(value));
    }

    public NumberLiteral(SourceInfo info, float value) {
        this(info, new Float(value));
    }

    public NumberLiteral(SourceInfo info, long value) {
        this(info, new Long(value));
    }

    public NumberLiteral(SourceInfo info, double value) {
        this(info, new Double(value));
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public void convertTo(Type type, boolean preferCast) {
        if (Number.class.isAssignableFrom(type.getObjectClass())) {
            if (type.isPrimitive()) {
                super.setType(type);
            }
            else {
                super.setType(type.toPrimitive());
                super.convertTo(type.toNonNull(), preferCast);
            }
        }
        else {
            super.convertTo(type, preferCast);
        }
    }

    public void setType(Type type) {
        super.setType(type);

        // NumberLiterals never evaluate to null when the value is known.
        if (isValueKnown()) {
            super.setType(getType().toNonNull());
        }
    }

    public Object getValue() {
        return mValue;
    }

    /**
     * Value is known only if type is a number or can be assigned a number.
     */
    public boolean isValueKnown() {
        Type type = getType();
        if (type != null) {
            Class clazz = type.getObjectClass();
            return Number.class.isAssignableFrom(clazz) ||
                clazz.isAssignableFrom(Number.class);
        }
        else {
            return false;
        }
    }
}
