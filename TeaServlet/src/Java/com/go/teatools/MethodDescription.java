/* ====================================================================
 * TeaTools - Copyright (c) 1997-2000 GO.com
 * ====================================================================
 * The Tea Software License, Version 1.0
 *
 * Copyright (c) 2000 GO.com.  All rights reserved.
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
 *       "This product includes software developed by GO.com
 *        (http://opensource.go.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "GO.com" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact opensource@go.com.
 *
 * 5. Products derived from this software may not be called "Tea",
 *    "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
 *    "Kettle" or "Trove" appear in their name, without prior written
 *    permission of GO.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL GO.COM OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information about Tea, please see http://opensource.go.com/.
 */

package com.go.teatools;

import com.go.trove.classfile.AccessFlags;

import java.beans.*;
import java.lang.reflect.*;

/******************************************************************************
 * Wrapper for a MethodDescriptor object.
 *
 * @author Mark Masse
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 11/14/00 <!-- $-->
 */
public class MethodDescription extends FeatureDescription {

    private MethodDescriptor mMethodDescriptor;

    private TypeDescription mReturnType;
    private ParameterDescription[] mParams;

    /**
     * Creates a new MethodDescription
     */
    public MethodDescription(MethodDescriptor md, TeaToolsUtils utils) {
        super(utils);
        mMethodDescriptor = md;
    }

    /**
     * Returns the MethodDescriptor
     */
    public MethodDescriptor getMethodDescriptor() {
        return mMethodDescriptor;
    }

    /**
     * Returns the Method
     */
    public Method getMethod() {
        return getMethodDescriptor().getMethod();
    }

    /**
     * Returns a AccessFlags instance that can be used to check the type's
     * modifiers.
     */
    public AccessFlags getAccessFlags() {
        return getTeaToolsUtils().getAccessFlags(getMethod().getModifiers());
    }
    
    /**
     * Returns the method's return type
     */
    public TypeDescription getReturnType() {
        if (mReturnType == null) {
            mReturnType = 
                getTeaToolsUtils().createTypeDescription(
                                          getMethod().getReturnType());
        }

        return mReturnType;
    }

    /**
     * Returns the method's parameters
     */
    public ParameterDescription[] getParameters() {
        if (mParams == null) {
            
            mParams = getTeaToolsUtils().createParameterDescriptions(
                                                        getMethodDescriptor());
        }

        return mParams;
    }

    /**
     * Returns true if the specified method accepts a 
     * <code>Substitution</code> as its last parameter.
     */
    public boolean getAcceptsSubstitution() {
        return getTeaToolsUtils().acceptsSubstitution(getMethodDescriptor());
    }

    //
    // FeatureDescription methods
    //

    public FeatureDescriptor getFeatureDescriptor() {
        return getMethodDescriptor();
    }

    public String getShortFormat() {

        StringBuffer format = new StringBuffer();
        format.append(getName());
        format.append('(');
        ParameterDescription[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            format.append(params[i].getShortFormat());
            if (i < (params.length - 1)) {
                format.append(", ");
            } 
        }

        format.append(')');

        return format.toString();
    }

    public String getLongFormat() {
        StringBuffer format = new StringBuffer();

        format.append(getReturnType().getLongFormat());
        format.append(getName());
        format.append('(');
        ParameterDescription[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            format.append(params[i].getLongFormat());
            if (i < (params.length - 1)) {
                format.append(", ");
            } 
        }

        format.append(')');

        return format.toString();
    }

    

}


