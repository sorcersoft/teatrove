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
import java.util.Iterator;
import java.util.ListIterator;
import com.go.tea.compiler.SourceInfo;
import com.go.tea.compiler.Type;

/******************************************************************************
 * An Expression is a piece of code that, when executed, produces a value.
 * All expressions have a type which represents the type of value is produces.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/01/25 <!-- $-->
 */
public class Expression extends Node {
    private LinkedList mConversions;
    private boolean mPrimitive;
    private boolean mExceptionPossible;
    
    public Expression(SourceInfo info) {
        super(info);
        mConversions = new LinkedList();
    }

    public Object accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Object clone() {
        Expression e = (Expression)super.clone();
        e.mConversions = (LinkedList)mConversions.clone();
        return e;
    }

    /**
     * Returns true if an exception can be thrown while executing this
     * Expression. By default, returns true only if a type conversion could
     * cause an exception.
     */
    public boolean isExceptionPossible() {
        return mExceptionPossible;
    }

    /**
     * The type of an expression is not necessarily set by a parser. It is
     * typically set by a type checker. An expression's type may represent
     * its natural type or its coerced type. A code generator is responsible
     * for ensuring that the type it generates correctly matches the type
     * of the expression.
     *
     * @return null if type is unknown
     */
    public Type getType() {
        if (mConversions.isEmpty()) {
            return null;
        }
        else {
            return ((Conversion)mConversions.getLast()).getToType();
        }
    }

    /**
     * Returns the type of this expression before any conversions were applied,
     * or null if the type isn't set.
     */
    public Type getInitialType() {
        if (mConversions.isEmpty()) {
            return null;
        }
        else {
            return ((Conversion)mConversions.getFirst()).getToType();
        }
    }

    /**
     * Applies a type conversion to this expression which is chained to all
     * previous conversions.
     *
     * @param toType the type to convert to.
     */
    public final void convertTo(Type toType) {
        convertTo(toType, true);
    }

    /**
     * Applies a type conversion to this expression which is chained to all
     * previous conversions.
     *
     * @param toType the type to convert to.
     * @param preferCast a hint that the conversion should be performed by a
     * type cast operation, by default is true.
     * @throws IllegalArgumentException when the conversion is illegal.
     */
    public void convertTo(Type toType, boolean preferCast) {
        Type fromType = getType();

        if (toType.equals(fromType)) {
            return;
        }

        boolean legal = false;

        if (!preferCast && fromType == Type.NULL_TYPE) {
            preferCast = true;
        }

        if (fromType == null) {
            legal = true;
        }
        else if (fromType.isPrimitive()) {
            if (toType.isPrimitive()) {
                if (toType.getNaturalClass() != void.class) {
                    legal = true;
                }
            }
            else {
                Class fromObj = fromType.getObjectClass();
                Class toObj = toType.getObjectClass();

                if (toObj.isAssignableFrom(fromObj)) {
                    legal = true;
                    if (fromObj != toObj) {
                        toType = fromType.toNonPrimitive();
                    }
                }
                else if (Number.class.isAssignableFrom(fromObj) &&
                         toType.hasPrimitivePeer()) {

                    if (Number.class.isAssignableFrom(toObj)) {
                        legal = true;
                        convertTo(toType.toPrimitive());
                    }
                    else if (Character.class.isAssignableFrom(toObj)) {
                        legal = true;
                        convertTo(new Type(char.class));
                    }
                }
            }
        }
        else {
            // From non-primitive...
            if (toType.isPrimitive()) {
                if (fromType.hasPrimitivePeer()) {
                    legal = true;
                    if (fromType.isNullable()) {
                        // NullPointerException is possible.
                        mExceptionPossible = true;
                    }

                    Type fromPrim = fromType.toPrimitive();

                    if (fromPrim.getNaturalClass() !=
                        toType.getNaturalClass()) {

                        convertTo(fromPrim);
                    }
                }
                else {
                    Class fromObj = fromType.getObjectClass();
                    Class toObj = toType.getObjectClass();
                    
                    if (Number.class.isAssignableFrom(fromObj) &&
                        Number.class.isAssignableFrom(toObj)) {
                        legal = true;
                        if (fromType.isNullable()) {
                            // NullPointerException is possible.
                            mExceptionPossible = true;
                        }
                    }
                    else if (preferCast) {
                        legal = true;
                        convertTo(toType.toNonPrimitive(), true);
                    }
                }
            }
            else {
                Class fromObj = fromType.getObjectClass();
                Class toObj = toType.getObjectClass();

                if (fromObj.isAssignableFrom(toObj)) {
                    // Downcast.
                    if (preferCast) {
                        legal = true;
                    }
                }
                else if (toObj.isAssignableFrom(fromObj)) {
                    // Upcast.
                    legal = true;
                    if (fromType.isNonNull() || !toType.isNonNull()) {
                        // No useful conversion applied, bail out.
                        return;
                    }
                }
                else if (Number.class.isAssignableFrom(fromObj) &&
                         Number.class.isAssignableFrom(toObj) &&
                         toType.hasPrimitivePeer()) {
                    // Conversion like Integer -> Double.
                    legal = true;
                    if (fromType.isNonNull()) {
                        convertTo(toType.toPrimitive(), true);
                    }
                }
                // This test only captures array conversions.
                else if (fromObj.getComponentType() != null &&
                         toObj.getComponentType() != null &&
                         toType.convertableFrom(fromType) >= 0) {
                    legal = true;
                    if (fromType.isNullable()) {
                        // NullPointerException is possible.
                        mExceptionPossible = true;
                    }
                }
            }
        }

        if (!legal) {
            // Try String conversion.
            if (toType.getNaturalClass().isAssignableFrom(String.class)) {
                legal = true;
                if (toType.isNonNull()) {
                    addConversion(Type.NON_NULL_STRING_TYPE, false);
                }
                else {
                    addConversion(Type.STRING_TYPE, false);
                }
            }
        }

        if (!legal && !preferCast &&
            !fromType.isPrimitive() && !toType.isPrimitive()) {

            // Even though a cast isn't preferred, its the last available
            // option.
            
            Class fromObj = fromType.getObjectClass();
            Class toObj = toType.getObjectClass();

            if (fromObj.isAssignableFrom(toObj)) {
                // Downcast.
                legal = true;
            }
            else if (toObj.isAssignableFrom(fromObj)) {
                // Upcast.
                legal = true;
            }
        }

        if (legal) {
            addConversion(toType, preferCast);
        }
        else {
            throw new IllegalArgumentException("Can't convert " + fromType +
                                               " to " + toType);
        }
    }

    private void addConversion(Type toType, boolean preferCast) {
        Type fromType = getType();

        if (!toType.equals(fromType)) {
            mConversions.add(new Conversion(fromType, toType, preferCast));
        }
    }

    /**
     * Returns a list of Conversion objects representing the all the
     * conversions that have been applied to this Expression. Unless the type
     * isn't set, the chain contains at least one element. The conversion
     * chain may be reduced or expanded, so its length doesn't necessarily
     * represent the exact sequence of calls to {@link #convertTo}.
     */
    public LinkedList getConversionChain() {
        return reduce(mConversions);
    }

    /**
     * Sets the type of this expression, clearing the conversion chain.
     */
    public void setType(Type type) {
        mConversions.clear();
        mExceptionPossible = false;
        if (type != null) {
            // Prefer cast for initial type for correct operation of
            // setInitialType if a conversion needs to be inserted at the
            // beginning.
            mConversions.add(new Conversion(null, type, true));
        }
    }

    /**
     * Sets the intial type in the conversion chain, but does not clear the
     * conversions.
     */
    public void setInitialType(Type type) {
        Type initial = getInitialType();
        if (type != null && !type.equals(initial)) {
            if (initial == null) {
                setType(type);
            }
            else {
                Iterator it = mConversions.iterator();
                mConversions = new LinkedList();
                // Prefer cast for initial type for correct operation of
                // setInitialType if a conversion needs to be inserted at the
                // beginning.
                mConversions.add(new Conversion(null, type, true));
                while (it.hasNext()) {
                    Conversion conv = (Conversion)it.next();
                    convertTo(conv.getToType(), conv.isCastPreferred());
                }
            }
        }
    }

    /**
     * Returns true if the value generated by this expression is known at
     * compile-time. For most expressions, false is returned. Literals
     * always return true.
     * @see Literal
     */
    public boolean isValueKnown() {
        return false;
    }

    /**
     * Most expressions can't generate a value at compile-time, so this
     * method simply returns null. Call isValueKnown to check if the
     * expression's value is known at compile-time.
     */
    public Object getValue() {
        return null;
    }

    private LinkedList reduce(LinkedList conversions) {
    outer:
        while (true) {
            // Eliminate conversions that cancel each other out.

            ListIterator fromIterator = conversions.listIterator();
            while (fromIterator.hasNext()) {
                int fromIndex = fromIterator.nextIndex();
                Type from = ((Conversion)fromIterator.next()).getToType();
                
                ListIterator toIterator =
                    conversions.listIterator(fromIndex + 1);

                while (toIterator.hasNext()) {
                    int toIndex = toIterator.nextIndex();
                    Type to = ((Conversion)toIterator.next()).getToType();
                    if (from.equals(to)) {
                        conversions.subList(fromIndex + 1,toIndex + 1).clear();
                        continue outer;
                    }
                }
            }

            // Reduce sequence where a primitive is converted to its
            // non-primitive peer and then to a string. Eliminate the middle
            // step and convert directly to a string.

            ListIterator it = conversions.listIterator();
            while (it.hasNext()) {
                Type type = ((Conversion)it.next()).getToType();
                while (type.isPrimitive() && it.hasNext()) {
                    Type nextType = ((Conversion)it.next()).getToType();
                    if (type.toNonPrimitive().equals(nextType)
                        && it.hasNext()) {

                        Type thirdType = ((Conversion)it.next()).getToType();
                        if (thirdType.getNaturalClass() == String.class) {
                            it.previous();
                            it.remove();
                            it.previous();
                            it.remove();
                            it.add(new Conversion(type, thirdType, false));
                        }
                        else {
                            type = thirdType;
                        }
                    }
                    else {
                        type = nextType;
                    }
                }
            }

            break;
        }

        return conversions;
    }

    public static class Conversion {
        private Type mFromType;
        private Type mToType;
        private boolean mPreferCast;

        Conversion(Type fromType, Type toType, boolean preferCast) {
            mFromType = fromType;
            if ((mToType = toType) == null) {
                throw new NullPointerException("Cannot convert to null");
            }
            mPreferCast = preferCast;
        }

        /**
         * Is null if this is the first conversion in the chain.
         */
        public Type getFromType() {
            return mFromType;
        }

        public Type getToType() {
            return mToType;
        }

        public boolean isCastPreferred() {
            return mPreferCast;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Conversion)) {
                return false;
            }
            
            Conversion conv = (Conversion)other;

            if (mFromType == null) {
                if (conv.mFromType != null) {
                    return false;
                }
            }
            else {
                if (!mFromType.equals(conv.mFromType)) {
                    return false;
                }
            }

            return mToType.equals(conv.mToType) &&
                mPreferCast == conv.mPreferCast;
        }

        public String toString() {
            if (mFromType == null) {
                return "Convert to " + mToType.getFullName();
            }
            else {
                return "Convert from " + mFromType.getFullName() + 
                    " to " + mToType.getFullName();
            }
        }
    }
}
