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
import java.util.Map;
import java.lang.reflect.Array;
import com.go.trove.util.FlyweightSet;
import com.go.trove.util.IdentityMap;

/******************************************************************************
 * This class is used to build field and return type descriptor strings as 
 * defined in <i>The Java Virtual Machine Specification</i>, section 4.3.2.
 * TypeDesc instances are canonicalized and therefore "==" comparable.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public abstract class TypeDesc extends Descriptor implements Serializable {
    /**
     * Type code returned from getTypeCode, which can be used with the
     * newarray instruction.
     */
    public final static int
        OBJECT_CODE = 0,
        VOID_CODE = 1,
        BOOLEAN_CODE = 4,
        CHAR_CODE = 5,
        FLOAT_CODE = 6,
        DOUBLE_CODE = 7,
        BYTE_CODE = 8,
        SHORT_CODE = 9,
        INT_CODE = 10,
        LONG_CODE = 11;

    /** primitive type void */
    public final static TypeDesc VOID;
    /** primitive type boolean */
    public final static TypeDesc BOOLEAN;
    /** primitive type char */
    public final static TypeDesc CHAR;
    /** primitive type byte */
    public final static TypeDesc BYTE;
    /** primitive type short */
    public final static TypeDesc SHORT;
    /** primitive type int */
    public final static TypeDesc INT;
    /** primitive type long */
    public final static TypeDesc LONG;
    /** primitive type float */
    public final static TypeDesc FLOAT;
    /** primitive type double */
    public final static TypeDesc DOUBLE;

    /** object type java.lang.Object, provided for convenience */
    public final static TypeDesc OBJECT;
    /** object type java.lang.String, provided for convenience */
    public final static TypeDesc STRING;

    // Pool of all shared instances. Ensures identity comparison works.
    final static FlyweightSet cInstances;

    // Cache that maps Classes to TypeDescs.
    final static Map cClassesToInstances;

    static {
        cInstances = new FlyweightSet();
        cClassesToInstances = new IdentityMap();

        VOID = intern(new PrimitiveType("V", VOID_CODE));
        BOOLEAN = intern(new PrimitiveType("Z", BOOLEAN_CODE));
        CHAR = intern(new PrimitiveType("C", CHAR_CODE));
        BYTE = intern(new PrimitiveType("B", BYTE_CODE));
        SHORT = intern(new PrimitiveType("S", SHORT_CODE));
        INT = intern(new PrimitiveType("I", INT_CODE));
        LONG = intern(new PrimitiveType("J", LONG_CODE));
        FLOAT = intern(new PrimitiveType("F", FLOAT_CODE));
        DOUBLE = intern(new PrimitiveType("D", DOUBLE_CODE));

        OBJECT = forClass("java.lang.Object");
        STRING = forClass("java.lang.String");
    }

    static TypeDesc intern(TypeDesc type) {
        return (TypeDesc)cInstances.put(type);
    }

    /**
     * Acquire a TypeDesc from any class, including primitives and arrays.
     */
    public synchronized static TypeDesc forClass(Class clazz) {
        if (clazz == null) {
            return null;
        }

        TypeDesc type = (TypeDesc)cClassesToInstances.get(clazz);
        if (type != null) {
            return type;
        }

        if (clazz.isArray()) {
            type = forClass(clazz.getComponentType()).toArrayType();
        }
        else if (clazz.isPrimitive()) {
            if (clazz == int.class) {
                type = INT;
            }
            if (clazz == boolean.class) {
                type = BOOLEAN;
            }
            if (clazz == char.class) {
                type = CHAR;
            }
            if (clazz == byte.class) {
                type = BYTE;
            }
            if (clazz == long.class) {
                type = LONG;
            }
            if (clazz == float.class) {
                type = FLOAT;
            }
            if (clazz == double.class) {
                type = DOUBLE;
            }
            if (clazz == short.class) {
                type = SHORT;
            }
            if (clazz == void.class) {
                type = VOID;
            }
        }
        else {
            String name = clazz.getName();
            type = intern(new ObjectType(generateDescriptor(name), name));
        }

        cClassesToInstances.put(clazz, type);
        return type;
    }

    /**
     * Acquire a TypeDesc from any class name, including primitives and arrays.
     * Primitive and array syntax matches Java declarations.
     */
    public static TypeDesc forClass(String name)
        throws IllegalArgumentException
    {
        // TODO: Figure out how to cache these. Using a plain IdentityMap poses
        // a problem. The back reference to the key causes a memory leak.

        if (name.length() < 1) {
            throw invalidName(name);
        }

        int index1 = name.lastIndexOf('[');
        int index2 = name.lastIndexOf(']');
        if (index2 >= 0) {
            if (index2 + 1 != name.length() || index1 + 1 != index2) {
                throw invalidName(name);
            }
            try {
                return forClass(name.substring(0, index1)).toArrayType();
            }
            catch (IllegalArgumentException e) {
                throw invalidName(name);
            }
        }
        else if (index1 >= 0) {
            throw invalidName(name);
        }

        switch (name.charAt(0)) {
        case 'v':
            if (name.equals("void")) {
                return VOID;
            }
            break;
        case 'b':
            if (name.equals("boolean")) {
                return BOOLEAN;
            }
            else if (name.equals("byte")) {
                return BYTE;
            }
            break;
        case 'c':
            if (name.equals("char")) {
                return CHAR;
            }
            break;
        case 's':
            if (name.equals("short")) {
                return SHORT;
            }
            break;
        case 'i':
            if (name.equals("int")) {
                return INT;
            }
            break;
        case 'l':
            if (name.equals("long")) {
                return LONG;
            }
            break;
        case 'f':
            if (name.equals("float")) {
                return FLOAT;
            }
            break;
        case 'd':
            if (name.equals("double")) {
                return DOUBLE;
            }
            break;
        }

        String desc = generateDescriptor(name);
        if (name.indexOf('/') >= 0) {
            name = name.replace('/', '.');
        }
        return intern(new ObjectType(desc, name));
    }

    private static IllegalArgumentException invalidName(String name) {
        return new IllegalArgumentException("Invalid name: " + name);
    }

    /**
     * Acquire a TypeDesc from a type descriptor. This syntax is described in
     * section 4.3.2, Field Descriptors.
     */
    public static TypeDesc forDescriptor(String desc)
        throws IllegalArgumentException
    {
        // TODO: Figure out how to cache these. Using a plain IdentityMap poses
        // a problem. The back reference to the key causes a memory leak.

        TypeDesc td;
        int cursor = 0;
        int dim = 0;
        try {
            char c;
            while ((c = desc.charAt(cursor++)) == '[') {
                dim++;
            }

            switch (c) {
            case 'V':
                td = VOID;
                break;
            case 'Z':
                td = BOOLEAN;
                break;
            case 'C':
                td = CHAR;
                break;
            case 'B':
                td = BYTE;
                break;
            case 'S':
                td = SHORT;
                break;
            case 'I':
                td = INT;
                break;
            case 'J':
                td = LONG;
                break;
            case 'F':
                td = FLOAT;
                break;
            case 'D':
                td = DOUBLE;
                break;
            case 'L':
                if (dim > 0) {
                    desc = desc.substring(dim);
                    cursor = 1;
                }
                StringBuffer name = new StringBuffer(desc.length() - 2);
                while ((c = desc.charAt(cursor++)) != ';') {
                    if (c == '/') {
                        c = '.';
                    }
                    name.append(c);
                }
                td = intern(new ObjectType(desc, name.toString()));
                break;
            default:
                throw invalidDescriptor(desc);
            }
        }
        catch (NullPointerException e) {
            throw invalidDescriptor(desc);
        }
        catch (IndexOutOfBoundsException e) {
            throw invalidDescriptor(desc);
        }

        if (cursor != desc.length()) {
            throw invalidDescriptor(desc);
        }

        while (--dim >= 0) {
            td = td.toArrayType();
        }

        return td;
    }

    private static IllegalArgumentException invalidDescriptor(String desc) {
        return new IllegalArgumentException("Invalid descriptor: " + desc);
    }

    private static String generateDescriptor(String classname) {
        int length = classname.length();
        char[] buf = new char[length + 2];
        buf[0] = 'L';
        classname.getChars(0, length, buf, 1);
        int i;
        for (i=1; i<=length; i++) {
            char c = buf[i];
            if (c == '.') {
                buf[i] = '/';
            }
        }
        buf[i] = ';';
        return new String(buf);
    }

    transient final String mDescriptor;

    TypeDesc(String desc) {
        mDescriptor = desc;
    }

    /**
     * Returns the class name for this descriptor. If the type is primitive,
     * then the Java primitive type name is returned. If the type is an array,
     * only the root component type name is returned.
     */
    public abstract String getRootName();

    /**
     * Returns the class name for this descriptor. If the type is primitive,
     * then the Java primitive type name is returned. If the type is an array,
     * "[]" is append at the end of the name for each dimension.
     */
    public abstract String getFullName();

    /**
     * Returns a type code for operating on primitive types in switches. If
     * not primitive, OBJECT_CODE is returned.
     */
    public abstract int getTypeCode();

    /**
     * Returns true if this is a primitive type.
     */
    public abstract boolean isPrimitive();

    /**
     * Returns true if this is a primitive long or double type.
     */
    public abstract boolean isDoubleWord();

    /**
     * Returns true if this is an array type.
     */
    public abstract boolean isArray();

    /**
     * Returns the number of dimensions this array type has. If not an array,
     * zero is returned.
     */
    public abstract int getDimensions();

    /**
     * Returns the component type of this array type. If not an array, null is
     * returned.
     */
    public abstract TypeDesc getComponentType();

    /**
     * Returns the root component type of this array type. If not an array,
     * null is returned.
     */
    public abstract TypeDesc getRootComponentType();

    /**
     * Convertes this type to an array type. If already an array, another
     * dimension is added.
     */
    public abstract TypeDesc toArrayType();

    /**
     * Returns the object peer of this primitive type. For int, the object peer
     * is java.lang.Integer. If this type is an object type, it is simply 
     * returned.
     */
    public abstract TypeDesc toObjectType();

    /**
     * Returns the primitive peer of this object type, if one exists. For
     * java.lang.Integer, the primitive peer is int. If this type is a
     * primitive type, it is simply returned. Arrays have no primitive peer.
     */
    public abstract TypeDesc toPrimitiveType();

    /**
     * Returns this type as a class. If the class isn't found, null is
     * returned.
     */
    public abstract Class toClass();

    /**
     * Returns this type as a class. If the class isn't found, null is
     * returned.
     * @param loader optional ClassLoader to load class from
     */
    public abstract Class toClass(ClassLoader loader);

    /**
     * Returns this in type descriptor syntax.
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
        if (other instanceof TypeDesc) {
            return ((TypeDesc)other).mDescriptor.equals(mDescriptor);
        }
        return false;
    }

    Object writeReplace() throws ObjectStreamException {
        return new External(mDescriptor);
    }

    private static class PrimitiveType extends TypeDesc {
        private transient final int mCode;
        private transient TypeDesc mArrayType;
        private transient TypeDesc mObjectType;
        
        PrimitiveType(String desc, int code) {
            super(desc);
            mCode = code;
        }

        public String getRootName() {
            switch (mCode) {
            default:
            case VOID_CODE:
                return "void";
            case BOOLEAN_CODE:
                return "boolean";
            case CHAR_CODE:
                return "char";
            case BYTE_CODE:
                return "byte";
            case SHORT_CODE:
                return "short";
            case INT_CODE:
                return "int";
            case LONG_CODE:
                return "long";
            case FLOAT_CODE:
                return "float";
            case DOUBLE_CODE:
                return "double";
            }
        }
        
        public String getFullName() {
            return getRootName();
        }

        public int getTypeCode() {
            return mCode;
        }
        
        public boolean isPrimitive() {
            return true;
        }
        
        public boolean isDoubleWord() {
            return mCode == DOUBLE_CODE || mCode == LONG_CODE;
        }
        
        public boolean isArray() {
            return false;
        }
        
        public int getDimensions() {
            return 0;
        }
        
        public TypeDesc getComponentType() {
            return null;
        }
        
        public TypeDesc getRootComponentType() {
            return null;
        }
        
        public TypeDesc toArrayType() {
            if (mArrayType == null) {
                char[] buf = new char[2];
                buf[0] = '[';
                buf[1] = mDescriptor.charAt(0);
                mArrayType = intern(new ArrayType(new String(buf), this));
            }
            return mArrayType;
        }
        
        public TypeDesc toObjectType() {
            if (mObjectType == null) {
                switch (mCode) {
                default:
                case VOID_CODE:
                    mObjectType = forClass("java.lang.Void");
                    break;
                case BOOLEAN_CODE:
                    mObjectType = forClass("java.lang.Boolean");
                    break;
                case CHAR_CODE:
                    mObjectType = forClass("java.lang.Character");
                    break;
                case BYTE_CODE:
                    mObjectType = forClass("java.lang.Byte");
                    break;
                case SHORT_CODE:
                    mObjectType = forClass("java.lang.Short");
                    break;
                case INT_CODE:
                    mObjectType = forClass("java.lang.Integer");
                    break;
                case LONG_CODE:
                    mObjectType = forClass("java.lang.Long");
                    break;
                case FLOAT_CODE:
                    mObjectType = forClass("java.lang.Float");
                    break;
                case DOUBLE_CODE:
                    mObjectType = forClass("java.lang.Double");
                    break;
                }
            }
            return mObjectType;
        }
        
        public TypeDesc toPrimitiveType() {
            return this;
        }

        public Class toClass() {
            switch (mCode) {
            default:
            case VOID_CODE:
                return void.class;
            case BOOLEAN_CODE:
                return boolean.class;
            case CHAR_CODE:
                return char.class;
            case BYTE_CODE:
                return byte.class;
            case SHORT_CODE:
                return short.class;
            case INT_CODE:
                return int.class;
            case LONG_CODE:
                return long.class;
            case FLOAT_CODE:
                return float.class;
            case DOUBLE_CODE:
                return double.class;
            }
        }

        public Class toClass(ClassLoader loader) {
            return toClass();
        }
    }

    private static class ObjectType extends TypeDesc {
        private transient final String mName;
        private transient TypeDesc mArrayType;
        private transient TypeDesc mPrimitiveType;
        private transient Class mClass;

        ObjectType(String desc, String name) {
            super(desc);
            mName = name;
        }

        public String getRootName() {
            return mName;
        }

        public String getFullName() {
            return mName;
        }

        public int getTypeCode() {
            return OBJECT_CODE;
        }

        public boolean isPrimitive() {
            return false;
        }
        
        public boolean isDoubleWord() {
            return false;
        }
        
        public boolean isArray() {
            return false;
        }
        
        public int getDimensions() {
            return 0;
        }
        
        public TypeDesc getComponentType() {
            return null;
        }
        
        public TypeDesc getRootComponentType() {
            return null;
        }
        
        public TypeDesc toArrayType() {
            if (mArrayType == null) {
                int length = mDescriptor.length();
                char[] buf = new char[length + 1];
                buf[0] = '[';
                mDescriptor.getChars(0, length, buf, 1);
                mArrayType = intern(new ArrayType(new String(buf), this));
            }
            return mArrayType;
        }
        
        public TypeDesc toObjectType() {
            return this;
        }
        
        public TypeDesc toPrimitiveType() {
            if (mPrimitiveType == null) {
                String name = mName;
                if (name.startsWith("java.lang.") && name.length() > 10) {
                    switch (name.charAt(10)) {
                    case 'V':
                        if (name.equals("java.lang.Void")) {
                            mPrimitiveType = VOID;
                        }
                        break;
                    case 'B':
                        if (name.equals("java.lang.Boolean")) {
                            mPrimitiveType = BOOLEAN;
                        }
                        else if (name.equals("java.lang.Byte")) {
                            mPrimitiveType = BYTE;
                        }
                        break;
                    case 'C':
                        if (name.equals("java.lang.Character")) {
                            mPrimitiveType = CHAR;
                        }
                        break;
                    case 'S':
                        if (name.equals("java.lang.Short")) {
                            mPrimitiveType = SHORT;
                        }
                        break;
                    case 'I':
                        if (name.equals("java.lang.Integer")) {
                            mPrimitiveType = INT;
                        }
                        break;
                    case 'L':
                        if (name.equals("java.lang.Long")) {
                            mPrimitiveType = LONG;
                        }
                        break;
                    case 'F':
                        if (name.equals("java.lang.Float")) {
                            mPrimitiveType = FLOAT;
                        }
                        break;
                    case 'D':
                        if (name.equals("java.lang.Double")) {
                            mPrimitiveType = DOUBLE;
                        }
                        break;
                    }
                }
            }

            return mPrimitiveType;
        }

        public final Class toClass() {
            if (mClass == null) {
                mClass = toClass(null);
            }
            return mClass;
        }

        public Class toClass(ClassLoader loader) {
            TypeDesc type = toPrimitiveType();
            if (type != null) {
                switch (type.getTypeCode()) {
                default:
                case VOID_CODE:
                    return Void.class;
                case BOOLEAN_CODE:
                    return Boolean.class;
                case CHAR_CODE:
                    return Character.class;
                case FLOAT_CODE:
                    return Float.class;
                case DOUBLE_CODE:
                    return Double.class;
                case BYTE_CODE:
                    return Byte.class;
                case SHORT_CODE:
                    return Short.class;
                case INT_CODE:
                    return Integer.class;
                case LONG_CODE:
                    return Long.class;
                }
            }

            try {
                if (loader == null) {
                    return Class.forName(mName);
                }
                else {
                    return loader.loadClass(mName);
                }
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private static class ArrayType extends ObjectType {
        private transient final TypeDesc mComponent;
        private transient final String mFullName;

        ArrayType(String desc, TypeDesc component) {
            super(desc, component.getRootName());
            mComponent = component;
            mFullName = component.getFullName().concat("[]");
        }

        public String getFullName() {
            return mFullName;
        }

        public boolean isArray() {
            return true;
        }
        
        public int getDimensions() {
            return mComponent.getDimensions() + 1;
        }
        
        public TypeDesc getComponentType() {
            return mComponent;
        }
        
        public TypeDesc getRootComponentType() {
            TypeDesc type = mComponent;
            while (type.isArray()) {
                type = type.getComponentType();
            }
            return type;
        }
        
        public TypeDesc toPrimitiveType() {
            return null;
        }

        public Class toClass(ClassLoader loader) {
            if (loader == null) {
                return arrayClass(getRootComponentType().toClass());
            }
            else {
                return arrayClass(getRootComponentType().toClass(loader));
            }
        }

        private Class arrayClass(Class clazz) {
            if (clazz == null) {
                return null;
            }
            int dim = getDimensions();
            try {
                if (dim == 1) {
                    return Array.newInstance(clazz, 0).getClass();
                }
                else {
                    return Array.newInstance(clazz, new int[dim]).getClass();
                }
            }
            catch (IllegalArgumentException e) {
                return null;
            }
        }
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
