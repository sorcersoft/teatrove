/* ====================================================================
 * Trove - Copyright (c) 1997-2000 Walt Disney Internet Group
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

package com.go.trove.classfile;

import java.lang.reflect.Modifier;

/******************************************************************************
 * The Modifiers class is a wrapper around a Modifier bit mask. The
 * methods provided to manipulate the Modifier ensure that it is always
 * legal. i.e. setting it public automatically clears it from being
 * private or protected.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public class Modifiers extends Modifier implements Cloneable {
    /**
     * When set public, the modifier is cleared from being private or
     * protected.
     */
    public static int setPublic(int modifier, boolean b) {
        if (b) {
            return (modifier | PUBLIC) & (~PROTECTED & ~PRIVATE);
        }
        else {
            return modifier & ~PUBLIC;
        }
    }
    
    /**
     * When set private, the modifier is cleared from being public or
     * protected.
     */
    public static int setPrivate(int modifier, boolean b) {
        if (b) {
            return (modifier | PRIVATE) & (~PUBLIC & ~PROTECTED);
        }
        else {
            return modifier & ~PRIVATE;
        }
    }

    /**
     * When set protected, the modifier is cleared from being public or
     * private.
     */
    public static int setProtected(int modifier, boolean b) {
        if (b) {
            return (modifier | PROTECTED) & (~PUBLIC & ~PRIVATE);
        }
        else {
            return modifier & ~PROTECTED;
        }
    }
    
    public static int setStatic(int modifier, boolean b) {
        if (b) {
            return modifier | STATIC;
        }
        else {
            return modifier & ~STATIC;
        }
    }

    /**
     * When set final, the modifier is cleared from being an interface or
     * abstract.
     */
    public static int setFinal(int modifier, boolean b) {
        if (b) {
            return (modifier | FINAL) & (~INTERFACE & ~ABSTRACT);
        }
        else {
            return modifier & ~FINAL;
        }
    }
    
    /**
     * When set synchronized, non-method settings are cleared.
     */
    public static int setSynchronized(int modifier, boolean b) {
        if (b) {
            return (modifier | SYNCHRONIZED) &
                (~VOLATILE & ~TRANSIENT & ~INTERFACE);
        }
        else {
            return modifier & ~SYNCHRONIZED;
        }
    }
    
    /**
     * When set volatile, non-field settings are cleared.
     */
    public static int setVolatile(int modifier, boolean b) {
        if (b) {
            return (modifier | VOLATILE) &
                (~SYNCHRONIZED & ~NATIVE & ~INTERFACE & ~ABSTRACT & ~STRICT);
        }
        else {
            return modifier & ~VOLATILE;
        }
    }
    
    /**
     * When set transient, non-field settings are cleared.
     */
    public static int setTransient(int modifier, boolean b) {
        if (b) {
            return (modifier | TRANSIENT) &
                (~SYNCHRONIZED & ~NATIVE & ~INTERFACE & ~ABSTRACT & ~STRICT);
        }
        else {
            return modifier & ~TRANSIENT;
        }
    }
    
    /**
     * When set native, non-native-method settings are cleared.
     */
    public static int setNative(int modifier, boolean b) {
        if (b) {
            return (modifier | NATIVE) & 
                (~VOLATILE & ~TRANSIENT & ~INTERFACE & ~ABSTRACT & ~STRICT);
        }
        else {
            return modifier & ~NATIVE;
        }
    }
    
    /**
     * When set as an interface, non-interface settings are cleared and the
     * modifier is set abstract.
     */
    public static int setInterface(int modifier, boolean b) {
        if (b) {
            return (modifier | (INTERFACE | ABSTRACT)) & 
                (~FINAL & ~SYNCHRONIZED & ~VOLATILE & ~TRANSIENT & ~NATIVE);
        }
        else {
            return modifier & ~INTERFACE;
        }
    }

    /**
     * When set abstract, the modifier is cleared from being final, volatile,
     * transient, native, synchronized, and strictfp. When cleared from being
     * abstract, the modifier is also cleared from being an interface.
     */
    public static int setAbstract(int modifier, boolean b) {
        if (b) {
            return (modifier | ABSTRACT) & 
                (~FINAL & ~VOLATILE & ~TRANSIENT & ~NATIVE &
                 ~SYNCHRONIZED & ~STRICT);
        }
        else {
            return modifier & ~ABSTRACT & ~INTERFACE;
        }
    }

    public static int setStrict(int modifier, boolean b) {
        if (b) {
            return modifier | STRICT;
        }
        else {
            return modifier & ~STRICT;
        }
    }

    int mModifier;
    
    /** Construct with a modifier of 0. */
    public Modifiers() {
        mModifier = 0;
    }

    public Modifiers(int modifier) {
        mModifier = modifier;
    }
    
    public final int getModifier() {
        return mModifier;
    }
    
    public boolean isPublic() {
        return isPublic(mModifier);
    }

    public boolean isPrivate() {
        return isPrivate(mModifier);
    }

    public boolean isProtected() {
        return isProtected(mModifier);
    }
    
    public boolean isStatic() {
        return isStatic(mModifier);
    }

    public boolean isFinal() {
        return isFinal(mModifier);
    }

    public boolean isSynchronized() {
        return isSynchronized(mModifier);
    }

    public boolean isVolatile() {
        return isVolatile(mModifier);
    }

    public boolean isTransient() {
        return isTransient(mModifier);
    }
    
    public boolean isNative() {
        return isNative(mModifier);
    }
    
    public boolean isInterface() {
        return isInterface(mModifier);
    }
    
    public boolean isAbstract() {
        return isAbstract(mModifier);
    }

    public boolean isStrict() {
        return isStrict(mModifier);
    }

    public void setPublic(boolean b) {
        mModifier = setPublic(mModifier, b);
    }
    
    public void setPrivate(boolean b) {
        mModifier = setPrivate(mModifier, b);
    }

    public void setProtected(boolean b) {
        mModifier = setProtected(mModifier, b);
    }

    public void setStatic(boolean b) {
        mModifier = setStatic(mModifier, b);
    }

    public void setFinal(boolean b) {
        mModifier = setFinal(mModifier, b);
    }

    public void setSynchronized(boolean b) {
        mModifier = setSynchronized(mModifier, b);
    }

    public void setVolatile(boolean b) {
        mModifier = setVolatile(mModifier, b);
    }

    public void setTransient(boolean b) {
        mModifier = setTransient(mModifier, b);
    }

    public void setNative(boolean b) {
        mModifier = setNative(mModifier, b);
    }

    public void setInterface(boolean b) {
        mModifier = setInterface(mModifier, b);
    }

    public void setAbstract(boolean b) {
        mModifier = setAbstract(mModifier, b);
    }

    public void setStrict(boolean b) {
        mModifier = setStrict(mModifier, b);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Returns the string value generated by the Modifier class.
     * @see java.lang.reflect.Modifier#toString()
     */
    public String toString() {
        return toString(mModifier);
    }
}
