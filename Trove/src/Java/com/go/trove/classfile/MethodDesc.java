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

import java.io.Serializable;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.List;
import java.util.ArrayList;
import com.go.trove.util.FlyweightSet;

/******************************************************************************
 * This class is used to build method descriptor strings as 
 * defined in <i>The Java Virtual Machine Specification</i>, section 4.3.3.
 * MethodDesc instances are canonicalized and therefore "==" comparable.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public class MethodDesc extends Descriptor implements Serializable {
    private static final TypeDesc[] EMPTY_PARAMS = new TypeDesc[0];

    // MethodDesc and TypeDesc can share the same instance cache.
    private final static FlyweightSet mInstances = TypeDesc.cInstances;

    static MethodDesc intern(MethodDesc desc) {
        return (MethodDesc)mInstances.put(desc);
    }

    /**
     * Acquire a MethodDesc from a set of arguments.
     * @param ret return type of method; null implies void
     * @param params parameters to method; null implies none
     */
    public static MethodDesc forArguments(TypeDesc ret, TypeDesc[] params) {
        if (ret == null) {
            ret = TypeDesc.VOID;
        }
        if (params == null || params.length == 0) {
            params = EMPTY_PARAMS;
        }
        return intern(new MethodDesc(ret, params));
    }

    /**
     * Acquire a MethodDesc from a type descriptor. This syntax is described in
     * section 4.3.3, Method Descriptors.
     */
    public static MethodDesc forDescriptor(String desc) 
        throws IllegalArgumentException
    {
        try {
            int cursor = 0;
            char c;

            if ((c = desc.charAt(cursor++)) != '(') {
                throw invalidDescriptor(desc);
            }

            StringBuffer buf = new StringBuffer();
            List list = new ArrayList();

            while ((c = desc.charAt(cursor++)) != ')') {
                switch (c) {
                case 'V':
                case 'I':
                case 'C':
                case 'Z':
                case 'D':
                case 'F':
                case 'J':
                case 'B':
                case 'S':
                    buf.append(c);
                    break;
                case '[':
                    buf.append(c);
                    continue;
                case 'L':
                    while (true) {
                        buf.append(c);
                        if (c == ';') {
                            break;
                        }
                        c = desc.charAt(cursor++);
                    }
                    break;
                default:
                    throw invalidDescriptor(desc);
                }

                list.add(TypeDesc.forDescriptor(buf.toString()));
                buf.setLength(0);
            }

            TypeDesc ret = TypeDesc.forDescriptor(desc.substring(cursor));

            TypeDesc[] tds = new TypeDesc[list.size()];
            tds = (TypeDesc[])list.toArray(tds);

            return intern(new MethodDesc(desc, ret, tds));
        }
        catch (NullPointerException e) {
            throw invalidDescriptor(desc);
        }
        catch (IndexOutOfBoundsException e) {
            throw invalidDescriptor(desc);
        }
    }

    private static IllegalArgumentException invalidDescriptor(String desc) {
        return new IllegalArgumentException("Invalid descriptor: " + desc);
    }

    private transient final String mDescriptor;
    private transient final TypeDesc mRetType;
    private transient final TypeDesc[] mParams;
    
    private MethodDesc(TypeDesc ret, TypeDesc[] params) {
        mDescriptor = generateDescriptor(ret, params);
        mRetType = ret;
        mParams = params;
    }

    private MethodDesc(String desc, TypeDesc ret, TypeDesc[] params) {
        mDescriptor = desc;
        mRetType = ret;
        mParams = params;
    }

    /**
     * Returns the described return type, which is TypeDesc.VOID if void.
     */
    public TypeDesc getReturnType() {
        return mRetType;
    }

    public int getParameterCount() {
        return mParams.length;
    }

    public TypeDesc[] getParameterTypes() {
        TypeDesc[] params = mParams;
        return (params != EMPTY_PARAMS) ? (TypeDesc[])params.clone() : params;
    }

    /**
     * Returns this in Java method signature syntax.
     * @param name method name
     */
    public String toMethodSignature(String name) {
        StringBuffer buf = new StringBuffer();
        buf.append(mRetType.getFullName());
        buf.append(' ');
        buf.append(name);
        buf.append('(');

        TypeDesc[] params = mParams;
        for (int i=0; i<params.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(params[i].getFullName());
        }

        return buf.append(')').toString();
    }

    /**
     * Returns this in method descriptor syntax.
     */
    public String toString() {
        return mDescriptor;
    }

    public int hashCode() {
        return mDescriptor.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof MethodDesc) {
            return ((MethodDesc)other).mDescriptor.equals(mDescriptor);
        }
        return false;
    }

    Object writeReplace() throws ObjectStreamException {
        return new External(mDescriptor);
    }

    private static String generateDescriptor(TypeDesc ret, TypeDesc[] params) {
        int length = ret.toString().length() + 2;
        int paramsLength = params.length;
        for (int i=paramsLength; --i >=0; ) {
            length += params[i].toString().length();
        }
        char[] buf = new char[length];
        buf[0] = '(';
        int index = 1;
        String paramDesc;
        for (int i=0; i<paramsLength; i++) {
            paramDesc = params[i].toString();
            int paramDescLength = paramDesc.length();
            paramDesc.getChars(0, paramDescLength, buf, index);
            index += paramDescLength;
        }
        buf[index++] = ')';
        paramDesc = ret.toString();
        paramDesc.getChars(0, paramDesc.length(), buf, index);
        return new String(buf);
    }

    private static class External implements Externalizable {
        private String mDescriptor;

        public External() {
        }

        public External(String desc) {
            mDescriptor = desc;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mDescriptor);
        }

        public void readExternal(ObjectInput in) throws IOException {
            mDescriptor = in.readUTF();
        }

        public Object readResolve() throws ObjectStreamException {
            return forDescriptor(mDescriptor);
        }
    }
}
