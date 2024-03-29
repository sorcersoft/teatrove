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

import java.io.*;

/******************************************************************************
 * This class corresponds to the CONSTANT_Methodref_info structure as defined
 * in section 4.4.2 of <i>The Java Virtual Machine Specification</i>.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public class ConstantMethodInfo extends ConstantInfo {
    private ConstantClassInfo mParentClass;
    private ConstantNameAndTypeInfo mNameAndType;
    
    /** 
     * Will return either a new ConstantMethodInfo object or one already in
     * the constant pool. If it is a new ConstantMethodInfo, it will be 
     * inserted into the pool.
     */
    static ConstantMethodInfo make(ConstantPool cp,
                                   ConstantClassInfo parentClass,
                                   ConstantNameAndTypeInfo nameAndType) {

        ConstantInfo ci = new ConstantMethodInfo(parentClass, nameAndType);
        return (ConstantMethodInfo)cp.addConstant(ci);
    }

    ConstantMethodInfo(ConstantClassInfo parentClass,
                       ConstantNameAndTypeInfo nameAndType) {
        super(TAG_METHOD);
        
        mParentClass = parentClass;
        mNameAndType = nameAndType;
    }

    public ConstantClassInfo getParentClass() {
        return mParentClass;
    }
    
    public ConstantNameAndTypeInfo getNameAndType() {
        return mNameAndType;
    }

    public int hashCode() {
        return mNameAndType.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ConstantMethodInfo) {
            ConstantMethodInfo other = (ConstantMethodInfo)obj;
            return (mParentClass.equals(other.mParentClass) && 
                    mNameAndType.equals(other.mNameAndType));
        }
        
        return false;
    }
    
    public void writeTo(DataOutput dout) throws IOException {
        super.writeTo(dout);
        dout.writeShort(mParentClass.getIndex());
        dout.writeShort(mNameAndType.getIndex());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("CONSTANT_Methodref_info: ");
        buf.append(getParentClass().getType().getFullName());

        ConstantNameAndTypeInfo cnati = getNameAndType();

        buf.append(' ');
        buf.append(cnati.getName());
        buf.append(' ');
        buf.append(cnati.getType());

        return buf.toString();
    }
}
