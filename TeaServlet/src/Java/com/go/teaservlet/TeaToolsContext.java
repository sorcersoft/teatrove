/* ====================================================================
 * TeaServlet - Copyright (c) 1999-2000 Walt Disney Internet Group
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

package com.go.teaservlet;

import com.go.teatools.*;

import java.beans.*;
import java.lang.reflect.*;

/******************************************************************************
 * A Tea Tool's best friend.  This class has several useful methods for writing
 * tools that work with Tea.  Many of these methods were taken from Kettle 
 * 3.0.x so that they could be reused in future versions and in other 
 * applications.
 * <p>
 * This class was written with the intent that it could be used as a tea
 * context class. It provides a collection of functions to make introspection
 * possible from Tea.  
 *
 * @author Mark Masse
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface TeaToolsContext {

    /**
     * Returns a bean full of handy information about the specified class.
     */
    public HandyClassInfo getHandyClassInfo(Class clazz);

    /**
     * Returns a bean full of handy information about the specified class.
     */
    public HandyClassInfo getHandyClassInfo(String className);

    /**
     * Returns the first sentence of the specified paragraph.  Uses
     * <code>java.text.BreakIterator.getSentenceInstance()</code>
     */
    public String getFirstSentence(String paragraph);

    /**
     * Creates a String with the specified pattern repeated length
     * times.
     */
    public String createPatternString(String pattern, int length);

    /**
     * Creates a String of spaces with the specified length.
     */
    public String createWhitespaceString(int length);

    /**
     * provides a bean to contain an assortment of methods to handle class 
     * names and properties.
     */
    interface HandyClassInfo {    
    
         /**
          * Returns the class name of the specified class.  This method 
          * provides special formatting for array and inner classes.
          */
         public String getFullName();
        
        
        /**
         * Returns the class name of the specified class.  The class name returned
         * does not include the package. This method provides special formatting 
         * for array and inner classes.
         */
        public String getName();

        /**
         * Returns the package name of the specified class.  Returns null if the
         * class has no package.
         */
        public String getPackage();

        /**
         * Returns the type.
         */
        public Class getType();
        
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
        public String getTypeName();
   
        /**
         * Returns the array type.  Returns the specified class if it is 
         * not an array.  
         */
        public TypeDescription getArrayType();

        /**
         * Returns the array dimensions.  
         * Returns 0 if the specified class is not an array.  
         */
        public int getArrayDimensions();

        /**
         * Returns the array dimensions String (i.e. "[][][]").  
         * Returns "" (empty string) if the specified class is not an array.
         */
        public String getArrayDimensionsString();

        /**
         * Returns the shortDescription or "" if the
         * shortDescription is the same as the displayName.
         */
        public String getDescription();
        
        /**
         * Returns the first sentence of the 
         * shortDescription.  Returns "" if the shortDescription is the same as
         * the displayName (the default for reflection-generated 
         * FeatureDescriptors).  
         */
        public String getDescriptionFirstSentence();
        
        /**
         * Create a version information string based on what the build process
         * provided.  The string is of the form "M.m.r" or 
         * "M.m.r.bbbb" (i.e. 1.1.0.0004) if the build number can be retrieved.
         * Returns <code>null</code> if the version string cannot be retrieved.
         */
        public String getVersion();

        /**
         * Introspects a Java bean to learn about all its properties, 
         * exposed methods, and events.
         *
         * @param beanClass the bean class to be analyzed
         */
        public BeanInfo getBeanInfo();
        
        /**
         * Returns the type's MethodDescriptions.
         */
        public MethodDescription[] getMethodDescriptions();
        
        /**
         * Returns the type's PropertyDescriptions.
         */
        public PropertyDescription[] getPropertyDescriptions();
        
        /**
         * A function that returns an array of all the available properties on
         * a given class.
         * <p>
         * <b>NOTE:</b> If possible, the results of this method should be cached
         * by the caller.
         *
         * @param beanClass the bean class to introspect
         *
         * @return an array of all the available properties on the specified class.
         */
        public PropertyDescriptor[] getTeaBeanPropertyDescriptors();
                                                        
        /**
         * Gets the MethodDescriptors of the specified context class including
         * all of the MethodDescriptors for methods declared in the class's 
         * superclass and interfaces
         *
         * @param contextClass the Tea context Class to introspect (any class will
         * work fine)
         */
        public MethodDescriptor[] getTeaContextMethodDescriptors();

    }
}
