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

import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;

/******************************************************************************
 * This class corresponds to the method_info data structure as defined in
 * section 4.6 of <i>The Java Virtual Machine Specification</i>. 
 * To make it easier to create bytecode for a method's CodeAttr, the 
 * CodeBuilder class is provided.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 * @see ClassFile
 * @see CodeBuilder
 */
public class MethodInfo {
    private ClassFile mParent;
    private ConstantPool mCp;

    private String mName;
    private MethodDesc mDesc;
    
    private int mModifier;

    private ConstantUTFInfo mNameConstant;
    private ConstantUTFInfo mDescriptorConstant;
    
    private List mAttributes = new ArrayList(2);

    private CodeAttr mCode;
    private ExceptionsAttr mExceptions;

    MethodInfo(ClassFile parent,
               Modifiers modifiers,
               String name,
               MethodDesc desc) {

        mParent = parent;
        mCp = parent.getConstantPool();
        mName = name;
        mDesc = desc;
        
        mModifier = modifiers.getModifier();
        mNameConstant = ConstantUTFInfo.make(mCp, name);
        mDescriptorConstant = ConstantUTFInfo.make(mCp, desc.toString());

        if (!modifiers.isAbstract() && !modifiers.isNative()) {
            addAttribute(new CodeAttr(mCp));
        }
    }

    private MethodInfo(ClassFile parent,
                       int modifiers,
                       ConstantUTFInfo nameConstant,
                       ConstantUTFInfo descConstant) {

        mParent = parent;
        mCp = parent.getConstantPool();
        mName = nameConstant.getValue();
        mDesc = MethodDesc.forDescriptor(descConstant.getValue());

        mModifier = modifiers;
        mNameConstant = nameConstant;
        mDescriptorConstant = descConstant;
    }
    
    /**
     * Returns the parent ClassFile for this MethodInfo.
     */
    public ClassFile getClassFile() {
        return mParent;
    }

    /**
     * Returns the name of this method.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns a MethodDesc which describes return and parameter types
     * of this method.
     */
    public MethodDesc getMethodDescriptor() {
        return mDesc;
    }

    /**
     * Returns a copy of this method's modifiers.
     */
    public Modifiers getModifiers() {
        return new Modifiers(mModifier);
    }
    
    /**
     * @deprecated call getModifiers
     */
    public AccessFlags getAccessFlags() {
        return new AccessFlags(mModifier);
    }

    /**
     * Returns a constant from the constant pool with this method's name.
     */
    public ConstantUTFInfo getNameConstant() {
        return mNameConstant;
    }
    
    /**
     * Returns a constant from the constant pool with this method's type 
     * descriptor string.
     * @see MethodDesc
     */
    public ConstantUTFInfo getDescriptorConstant() {
        return mDescriptorConstant;
    }

    /**
     * Returns the exceptions that this method is declared to throw.
     */
    public String[] getExceptions() {
        if (mExceptions == null) {
            return new String[0];
        }
        else {
            return mExceptions.getExceptions();
        }
    }

    /**
     * Returns a CodeAttr object used to manipulate the method code body, or
     * null if this method is abstract or native.
     */
    public CodeAttr getCodeAttr() {
        return mCode;
    }

    public boolean isSynthetic() {
        for (int i = mAttributes.size(); --i >= 0; ) {
            Object obj = mAttributes.get(i);
            if (obj instanceof SyntheticAttr) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeprecated() {
        for (int i = mAttributes.size(); --i >= 0; ) {
            Object obj = mAttributes.get(i);
            if (obj instanceof DeprecatedAttr) {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Add a declared exception that this method may throw.
     */
    public void addException(String className) {
        if (mExceptions == null) {
            addAttribute(new ExceptionsAttr(mCp));
        }

        ConstantClassInfo cci = ConstantClassInfo.make(mCp, className);
        mExceptions.addException(cci);
    }

    /**
     * Mark this method as being synthetic by adding a special attribute.
     */
    public void markSynthetic() {
        addAttribute(new SyntheticAttr(mCp));
    }

    /**
     * Mark this method as being deprecated by adding a special attribute.
     */
    public void markDeprecated() {
        addAttribute(new DeprecatedAttr(mCp));
    }

    public void addAttribute(Attribute attr) {
        if (attr instanceof CodeAttr) {
            if (mCode != null) {
                mAttributes.remove(mCode);
            }
            mCode = (CodeAttr)attr;
        }
        else if (attr instanceof ExceptionsAttr) {
            if (mExceptions != null) {
                mAttributes.remove(mExceptions);
            }
            mExceptions = (ExceptionsAttr)attr;
        }

        mAttributes.add(attr);
    }

    public Attribute[] getAttributes() {
        Attribute[] attrs = new Attribute[mAttributes.size()];
        return (Attribute[])mAttributes.toArray(attrs);
    }

    /**
     * Returns the length (in bytes) of this object in the class file.
     */
    public int getLength() {
        int length = 8;
        
        int size = mAttributes.size();
        for (int i=0; i<size; i++) {
            length += ((Attribute)mAttributes.get(i)).getLength();
        }
        
        return length;
    }
    
    public void writeTo(DataOutput dout) throws IOException {
        dout.writeShort(mModifier);
        dout.writeShort(mNameConstant.getIndex());
        dout.writeShort(mDescriptorConstant.getIndex());
        
        int size = mAttributes.size();
        dout.writeShort(size);
        for (int i=0; i<size; i++) {
            Attribute attr = (Attribute)mAttributes.get(i);
            attr.writeTo(dout);
        }
    }

    public String toString() {
        String modStr = Modifier.toString(mModifier);
        if (modStr.length() == 0) {
            return mDesc.toMethodSignature(getName());
        }
        else {
            return modStr + ' ' + mDesc.toMethodSignature(getName());
        }
    }

    static MethodInfo readFrom(ClassFile parent, 
                               DataInput din,
                               AttributeFactory attrFactory)
        throws IOException
    {
        ConstantPool cp = parent.getConstantPool();

        int modifier = din.readUnsignedShort();
        int index = din.readUnsignedShort();
        ConstantUTFInfo nameConstant = (ConstantUTFInfo)cp.getConstant(index);
        index = din.readUnsignedShort();
        ConstantUTFInfo descConstant = (ConstantUTFInfo)cp.getConstant(index);

        MethodInfo info = new MethodInfo(parent, modifier,
                                         nameConstant, descConstant);

        // Read attributes.
        int size = din.readUnsignedShort();
        for (int i=0; i<size; i++) {
            info.addAttribute(Attribute.readFrom(cp, din, attrFactory));
        }

        return info;
    }
}
