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
 * Object wrapper for TeaToolsUtils functions that operate on Class objects.
 * This class offers an O-O interface alternative to the TeaToolsUtils 
 * procedural design.
 *
 * @author Mark Masse
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 11/13/00 <!-- $-->
 */
public class TypeDescription extends FeatureDescription {
    
    /** The type to wrap and describe */
    private Class mType;
    
    /** The BeanInfo for the type */
    private BeanInfo mBeanInfo;

    /**
     * Create a new TypeDescription to wrap the specified type. 
     * The TeaToolsUtils object provides the implementation of 
     * this class's methods.
     */
    public TypeDescription(Class type, TeaToolsUtils utils) {
        super(utils);
        mType = type;
    }

    /**
     * Returns the type.
     */
    public Class getType() {
        return mType;
    }


    /**
     * Returns a AccessFlags instance that can be used to check the type's
     * modifiers.
     */
    public AccessFlags getAccessFlags() {
        return getTeaToolsUtils().getAccessFlags(mType.getModifiers());
    }

    /**
     * Returns the full name of the type.  This method 
     * provides special formatting for array and inner classes.
     */
    public String getFullName() {
        return getTeaToolsUtils().getFullClassName(mType);
    }

    /**
     * Returns the class name of the type.  The class name returned
     * does not include the package. This method provides special formatting 
     * for array and inner classes.
     */
    public String getName() {
        return getTeaToolsUtils().getClassName(mType);
    }

    /**
     * Returns the package name of the class.  Returns "" if the
     * class has no package.
     */
    public String getPackage() {
        return getTeaToolsUtils().getClassPackage(mType);
    }

    /**
     * Returns the name of the type of the Class described by this 
     * TypeDescription.
     * <p>
     * <UL>
     * <LI>A Class returns "class"
     * <LI>An Interface returns "interface"
     * <LI>An array returns null
     * <LI>A primitive returns null
     * </UL>
     */
    public String getTypeName() {        
        return getTeaToolsUtils().getClassTypeName(mType);
    }

    /**
     * Create a version information string based on what the build process
     * provided.  The string is of the form "M.m.r" or 
     * "M.m.r.bbbb" (i.e. 1.1.0.0004) if the build number can be retrieved.
     * Returns <code>null</code> if the version string cannot be retrieved.
     */
    public String getVersion() { 
        return getTeaToolsUtils().getPackageVersion(getPackage());             
    }
    
    /**
     * Returns the array type.  Returns this if it is not an 
     * array type.  
     */
    public TypeDescription getArrayType() {

        Class c = getTeaToolsUtils().getArrayType(mType);      
        if (mType == c) {
            return this;
        }

        return getTeaToolsUtils().createTypeDescription(c);
    }


    /**
     * Returns the array dimensions.  
     * Returns 0 if the type is not an array.  
     */
    public int getArrayDimensions() {
        return getTeaToolsUtils().getArrayDimensions(mType);
    }

    /**
     * Returns the array dimensions String (i.e. "[][][]").  
     * Returns "" (empty string) if the type is not an array.  
     */
    public String getArrayDimensionsString() {
        return getTeaToolsUtils().getArrayDimensionsString(mType);
    }

    /**
     * Introspects a Java bean to learn about all its properties, 
     * exposed methods, and events.  Returns null if the BeanInfo 
     * could not be created.
     */
    public BeanInfo getBeanInfo() {
        if (mBeanInfo == null) {
            try { 
                mBeanInfo = getTeaToolsUtils().getBeanInfo(mType);
            }
            catch (Exception e) {
                return null;
            }
        }

        return mBeanInfo;
    }

    /**
     * Introspects a Java bean to learn all about its properties, exposed 
     * methods, below a given "stop" point.
     *
     * @param stopClass the base class at which to stop the analysis. 
     * Any methods/properties/events in the stopClass or in its baseclasses 
     * will be ignored in the analysis
     */
    public BeanInfo getBeanInfo(Class stopClass) 
    throws IntrospectionException {
        return getTeaToolsUtils().getBeanInfo(mType, stopClass);
    }


    /**
     * Returns the type's PropertyDescriptors.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        BeanInfo info = getBeanInfo();
        if (info == null) {
            return null;
        }

        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        getTeaToolsUtils().sortPropertyDescriptors(pds);
        return pds;
    }

    /**
     * Returns the type's PropertyDescriptions.
     */
    public PropertyDescription[] getPropertyDescriptions() {
        return getTeaToolsUtils().createPropertyDescriptions(
                                          getPropertyDescriptors());

    }

    /**
     * Returns an array of all the available properties on the class.
     */
    public PropertyDescriptor[] getTeaBeanPropertyDescriptors() {
        return getTeaToolsUtils().getTeaBeanPropertyDescriptors(mType);
    }

    /**
     * Returns an array of all the available properties on the class.
     */
    public PropertyDescription[] getTeaBeanPropertyDescriptions() {
        return getTeaToolsUtils().createPropertyDescriptions(
                                          getTeaBeanPropertyDescriptors());
    }

    /**
     * Returns the type's MethodDescriptors.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        BeanInfo info = getBeanInfo();
        if (info == null) {
            return null;
        }

        MethodDescriptor[] mds = info.getMethodDescriptors();      
        getTeaToolsUtils().sortMethodDescriptors(mds);
        return mds;
    }

    /**
     * Returns the type's MethodDescriptions.
     */
    public MethodDescription[] getMethodDescriptions() {
        return getTeaToolsUtils().createMethodDescriptions(
                                                     getMethodDescriptors());
    }


    /**
     * Gets the MethodDescriptors of the context class including
     * all of the MethodDescriptors for methods declared in the class's 
     * superclass and interfaces
     */
    public MethodDescriptor[] getTeaContextMethodDescriptors() {
        return getTeaToolsUtils().getTeaContextMethodDescriptors(mType);
    }


    /**
     * Gets the MethodDescriptions of the context class including
     * all of the MethodDescriptions for methods declared in the class's 
     * superclass and interfaces
     */
    public MethodDescription[] getTeaContextMethodDescriptions() {
        return getTeaToolsUtils().createMethodDescriptions(
                                          getTeaContextMethodDescriptors());
    }


    /**
     * Gets the MethodDescriptors of the context class
     *
     * @param thisClassOnly true indicates that this function should 
     * only return MethodDescriptors declared by the wrapped Class.  
     */
    public MethodDescriptor[] getTeaContextMethodDescriptors(
                                                 boolean thisClassOnly) {

        return getTeaToolsUtils().getTeaContextMethodDescriptors(
                                                              mType, 
                                                              thisClassOnly);
    }
    
    /**
     * Returns the full class name of the class.  This method 
     * provides special formatting for array and inner classes.  If the 
     * specified class is implicitly imported by Tea, then its package is
     * omitted in the returned name.
     */
    public String getTeaFullName() {
        return getTeaToolsUtils().getTeaFullClassName(mType);
    }

    /**
     * Returns true if the class is 
     * implicitly imported by Tea.
     * <p>
     * Returns true if the specified class represents a primitive type or
     * a class or interface defined in one of the IMPLICIT_TEA_IMPORTS 
     * packages.  This method also works for array types.
     */
    public boolean isImplicitTeaImport() {
        return getTeaToolsUtils().isImplicitTeaImport(mType);
    }

    /**
     * Returns true if the class is compatible with Tea's 
     * <code>foreach</code> statement.  Compatibility implies that the
     * class can be iterated on by the <code>foreach</code>.
     */
    public boolean isForeachCompatible() {
        return getTeaToolsUtils().isForeachCompatible(mType);
    }

    /**
     * Returns true if the class is compatible with Tea's <code>if
     * </code> statement.  Only Boolean.class and boolean.class qualify.
     */    
    public boolean isIfCompatible() {    
        return getTeaToolsUtils().isIfCompatible(mType);
    }

    /**
     * Returns true if it is likely that the class serves as 
     * a Tea runtime context class.
     */
    public boolean isLikelyContextClass() {     
        return getTeaToolsUtils().isLikelyContextClass(mType);
    }


    //
    // FeatureDescription methods
    //

    public FeatureDescriptor getFeatureDescriptor() {
        BeanInfo info = getBeanInfo();
        if (info == null) {
            return null;
        }

        return info.getBeanDescriptor();
    }


    public String getShortFormat() {
        return getName();
    }

    public String getLongFormat() {
        return getFullName();
    }



}




