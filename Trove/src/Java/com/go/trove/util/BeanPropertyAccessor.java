/* ====================================================================
 * Trove - Copyright (c) 2000 Walt Disney Internet Group
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

package com.go.trove.util;

import java.lang.reflect.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.IdentityMap;
import com.go.trove.util.CompleteIntrospector;
import com.go.trove.classfile.*;

/******************************************************************************
 * Provides a simple and efficient means of reading and writing bean
 * properties. BeanPropertyAccessor auto-generates code, eliminating the
 * need to invoke methods via reflection. Bean access methods are bound-to
 * directly, using a special hash/switch design pattern.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public abstract class BeanPropertyAccessor {
    // Maps classes to BeanPropertyAccessors.
    private static Map cAccessors = new IdentityMap();

    /**
     * Returns a new or cached BeanPropertyAccessor for the given class.
     */
    public static BeanPropertyAccessor forClass(Class clazz) {
        synchronized (cAccessors) {
            BeanPropertyAccessor bpa =
                (BeanPropertyAccessor)cAccessors.get(clazz);
            if (bpa != null) {
                return bpa;
            }
            bpa = generate(clazz);
            cAccessors.put(clazz, bpa);
            return bpa;
        }
    }

    private static BeanPropertyAccessor generate(Class beanType) {
        ClassInjector injector = new ClassInjector
            (beanType.getClassLoader(), (File)null, null);
        
        int id = beanType.hashCode();
            
        String baseName = BeanPropertyAccessor.class.getName() + '$';
        String className = baseName;
        try {
            while (true) {
                className = baseName + (id & 0xffffffffL);
                try {
                    injector.loadClass(className);
                }
                catch (LinkageError e) {
                }
                id++;
            }
        }
        catch (ClassNotFoundException e) {
        }
        
        ClassFile cf = generateClassFile(className, beanType);

        /*
        try {
            String name = cf.getClassName();
            name = name.substring(name.lastIndexOf('.') + 1) + ".class";
            System.out.println(name);
            java.io.FileOutputStream out =
                new java.io.FileOutputStream(name);
            cf.writeTo(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        */
        
        try {
            OutputStream stream = injector.getStream(cf.getClassName());
            cf.writeTo(stream);
            stream.close();
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
        
        try {
            Class clazz = injector.loadClass(cf.getClassName());
            return (BeanPropertyAccessor)clazz.newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new InternalError(e.toString());
        }
        catch (InstantiationException e) {
            throw new InternalError(e.toString());
        }
        catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        }
    }

    private static ClassFile generateClassFile(String className,
                                               Class beanType)
    {
        PropertyDescriptor[][] props = getBeanProperties(beanType);

        ClassFile cf = new ClassFile(className, BeanPropertyAccessor.class);
        cf.markSynthetic();

        Modifiers publicAccess = new Modifiers();
        publicAccess.setPublic(true);

        MethodInfo ctor = cf.addConstructor(publicAccess, null);
        ctor.markSynthetic();
        CodeBuilder builder = new CodeBuilder(ctor);

        builder.loadThis();
        builder.invokeSuperConstructor(null);
        builder.returnVoid();

        generateMethod(cf, beanType, props[0], true);
        generateMethod(cf, beanType, props[1], false);

        return cf;
    }

    private static void generateMethod(ClassFile cf,
                                       Class beanType,
                                       PropertyDescriptor[] properties,
                                       boolean forRead)
    {
        Modifiers publicAccess = new Modifiers();
        publicAccess.setPublic(true);

        TypeDesc objectType = TypeDesc.OBJECT;
        TypeDesc stringType = TypeDesc.STRING;
        TypeDesc intType = TypeDesc.INT;
        TypeDesc booleanType = TypeDesc.BOOLEAN;
        TypeDesc exceptionType =
            TypeDesc.forClass(NoSuchPropertyException.class);

        MethodInfo mi;
        if (forRead) {
            TypeDesc[] params = {objectType, stringType};
            mi = cf.addMethod
                (publicAccess, "getPropertyValue", objectType, params);
        }
        else {
            TypeDesc[] params = new TypeDesc[] {
                objectType, stringType, objectType
            };
            mi = cf.addMethod
                (publicAccess, "setPropertyValue", null, params);
        }

        mi.markSynthetic();
        CodeBuilder builder = new CodeBuilder(mi);

        LocalVariable beanVar = builder.getParameters()[0];
        LocalVariable propertyVar = builder.getParameters()[1];
        LocalVariable valueVar;
        if (forRead) {
            valueVar = null;
        }
        else {
            valueVar = builder.getParameters()[2];
        }

        builder.loadLocal(beanVar);
        builder.checkCast(TypeDesc.forClass(beanType));
        builder.storeLocal(beanVar);

        if (properties.length > 0) {
            int[] cases = new int[hashCapacity(properties.length)];
            int caseCount = cases.length;
            for (int i=0; i<caseCount; i++) {
                cases[i] = i;
            }

            Label[] switchLabels = new Label[caseCount];
            Label noMatch = builder.createLabel();
            List[] caseMethods = caseMethods(caseCount, properties);
            
            for (int i=0; i<caseCount; i++) {
                List matches = caseMethods[i];
                if (matches == null || matches.size() == 0) {
                    switchLabels[i] = noMatch;
                }
                else {
                    switchLabels[i] = builder.createLabel();
                }
            }

            if (properties.length > 1) {
                builder.loadLocal(propertyVar);
                builder.invokeVirtual(String.class.getName(),
                                      "hashCode", intType, null);
                builder.loadConstant(0x7fffffff);
                builder.math(Opcode.IAND);
                builder.loadConstant(caseCount);
                builder.math(Opcode.IREM);
            
                builder.switchBranch(cases, switchLabels, noMatch);
            }
            
            // Params to invoke String.equals.
            TypeDesc[] params = {objectType};
            
            for (int i=0; i<caseCount; i++) {
                List matches = caseMethods[i];
                if (matches == null || matches.size() == 0) {
                    continue;
                }
                
                switchLabels[i].setLocation();
                
                int matchCount = matches.size();
                for (int j=0; j<matchCount; j++) {
                    PropertyDescriptor pd = (PropertyDescriptor)matches.get(j);
                    
                    // Test against name to find exact match.
                    
                    builder.loadConstant(pd.getName());
                    builder.loadLocal(propertyVar);
                    builder.invokeVirtual(String.class.getName(),
                                          "equals", booleanType, params);
                    
                    Label notEqual;
                    
                    if (j == matchCount - 1) {
                        notEqual = null;
                        builder.ifZeroComparisonBranch(noMatch, "==");
                    }
                    else {
                        notEqual = builder.createLabel();
                        builder.ifZeroComparisonBranch(notEqual, "==");
                    }
                    
                    if (forRead) {
                        loadPropertyAsObject(builder, beanVar,
                                             pd.getReadMethod());
                        builder.returnValue(TypeDesc.OBJECT);
                    }
                    else {
                        savePropertyFromObject(builder, beanVar, valueVar,
                                               pd.getWriteMethod());
                        builder.returnVoid();
                    }
                    
                    if (notEqual != null) {
                        notEqual.setLocation();
                    }
                }
            }
            
            noMatch.setLocation();
        }

        builder.newObject(exceptionType);
        builder.dup();
        builder.loadLocal(propertyVar);
        builder.loadConstant(forRead);

        // Params to invoke NoSuchPropertyException.<init>.
        TypeDesc[] params = {stringType, booleanType};

        builder.invokeConstructor
            (NoSuchPropertyException.class.getName(), params);
        builder.throwObject();
    }

    private static void loadPropertyAsObject(CodeBuilder builder,
                                             LocalVariable beanVar,
                                             Method m) {
        Class returnType = m.getReturnType();

        if (!returnType.isPrimitive()) {
            builder.loadLocal(beanVar);
            builder.invoke(m);
            return;
        }

        if (returnType == boolean.class) {
            TypeDesc td = TypeDesc.BOOLEAN;
            Label falseLabel = builder.createLabel();
            Label endLabel = builder.createLabel();
            builder.loadLocal(beanVar);
            builder.invoke(m);
            builder.ifZeroComparisonBranch(falseLabel, "==");
            builder.loadStaticField("java.lang.Boolean", "TRUE", td);
            builder.branch(endLabel);
            falseLabel.setLocation();
            builder.loadStaticField("java.lang.Boolean", "FALSE", td);
            endLabel.setLocation();
            return;
        }

        TypeDesc objectType = TypeDesc.forClass(returnType).toObjectType();
        TypeDesc[] params = {objectType.toPrimitiveType()};

        builder.newObject(objectType);
        builder.dup();
        builder.loadLocal(beanVar);
        builder.invoke(m);
        builder.invokeConstructor(objectType.getRootName(), params);
    }

    private static void savePropertyFromObject(CodeBuilder builder,
                                               LocalVariable beanVar,
                                               LocalVariable valueVar,
                                               Method m) {
        Class valueType = m.getParameterTypes()[0];

        if (!valueType.isPrimitive()) {
            builder.loadLocal(beanVar);
            builder.loadLocal(valueVar);
            builder.checkCast(TypeDesc.forClass(valueType));
            builder.invoke(m);
            return;
        }

        String methodName;
        String objectName;

        String name = valueType.getName();
        methodName = name + "Value";

        if (valueType == int.class) {
            objectName = Integer.class.getName();
        }
        else if (valueType == char.class) {
            objectName = Character.class.getName();
        }
        else {
            objectName = "java.lang." + Character.toUpperCase(name.charAt(0)) +
                name.substring(1);
        }

        builder.loadLocal(beanVar);
        builder.loadLocal(valueVar);
        builder.checkCast(TypeDesc.forClass(objectName));
        builder.invokeVirtual(objectName, methodName,
                              TypeDesc.forClass(valueType), null);
        builder.invoke(m);
    }

    /**
     * Returns a prime number, at least twice as large as needed. This should
     * minimize hash collisions. Since all the hash keys are known up front,
     * the capacity could be tweaked until there are no collisions, but this
     * technique is easier and deterministic.
     */
    private static int hashCapacity(int min) {
        BigInteger capacity = BigInteger.valueOf(min * 2 + 1);
        while (!capacity.isProbablePrime(100)) {
            capacity = capacity.add(BigInteger.valueOf(2));
        }
        return capacity.intValue();
    }

    /**
     * Returns an array of Lists of PropertyDescriptors. The first index
     * matches a switch case, the second index provides a list of all the
     * PropertyDescriptors whose name hash matched on the case.
     */
    private static List[] caseMethods(int caseCount,
                                      PropertyDescriptor[] props) {
        List[] cases = new List[caseCount];

        for (int i=0; i<props.length; i++) {
            PropertyDescriptor prop = props[i];
            int hashCode = prop.getName().hashCode();
            int caseValue = (hashCode & 0x7fffffff) % caseCount;
            List matches = cases[caseValue];
            if (matches == null) {
                matches = cases[caseValue] = new ArrayList();
            }
            matches.add(prop);
        }

        return cases;
    }

    /**
     * Returns two arrays of PropertyDescriptors. Array 0 has contains read
     * PropertyDescriptors, array 1 contains the write PropertyDescriptors.
     */
    private static PropertyDescriptor[][] getBeanProperties(Class beanType) {
        List readProperties = new ArrayList();
        List writeProperties = new ArrayList();

        try {
            Map map = CompleteIntrospector.getAllProperties(beanType);

            Iterator it = map.values().iterator();
            while (it.hasNext()) {
                PropertyDescriptor pd = (PropertyDescriptor)it.next();
                if (pd.getReadMethod() != null) {
                    readProperties.add(pd);
                }
                if (pd.getWriteMethod() != null) {
                    writeProperties.add(pd);
                }
            }
        }
        catch (IntrospectionException e) {
            throw new RuntimeException(e.toString());
        }

        PropertyDescriptor[][] props = new PropertyDescriptor[2][];
        
        props[0] = new PropertyDescriptor[readProperties.size()];
        readProperties.toArray(props[0]);
        props[1] = new PropertyDescriptor[writeProperties.size()];
        writeProperties.toArray(props[1]);

        return props;
    }

    protected BeanPropertyAccessor() {
    }

    // The actual public methods that will need to be defined.

    public abstract Object getPropertyValue(Object bean, String property)
        throws NoSuchPropertyException;

    public abstract void setPropertyValue(Object bean, String property,
                                          Object value)
        throws NoSuchPropertyException;

    // Auto-generated code sample:
    /*
    public Object getPropertyValue(Object bean, String property) {
        Bean bean = (Bean)bean;
        
        switch ((property.hashCode() & 0x7fffffff) % 11) {
        case 0:
            if ("name".equals(property)) {
                return bean.getName();
            }
            break;
        case 1:
            // No case
            break;
        case 2:
            // Hash collision
            if ("value".equals(property)) {
                return bean.getValue();
            }
            else if ("age".equals(property)) {
                return new Integer(bean.getAge());
            }
            break;
        case 3:
            if ("start".equals(property)) {
                return bean.getStart();
            }
            break;
        case 4:
        case 5:
        case 6:
            // No case
            break;
        case 7:
            if ("end".equals(property)) {
                return bean.isEnd() ? Boolean.TRUE : Boolean.FALSE;
            }
            break;
        case 8:
        case 9:
        case 10:
            // No case
            break;
        }
        
        throw new NoSuchPropertyException(property, true);
    }

    public void setPropertyValue(Object bean, String property, Object value) {
        Bean bean = (Bean)bean;
        
        switch ((property.hashCode() & 0x7fffffff) % 11) {
        case 0:
            if ("name".equals(property)) {
                bean.setName(value);
            }
            break;
        case 1:
            // No case
            break;
        case 2:
            // Hash collision
            if ("value".equals(property)) {
                bean.setValue(value);
            }
            else if ("age".equals(property)) {
                bean.setAge(((Integer)value).intValue());
            }
            break;
        case 3:
            if ("start".equals(property)) {
                bean.setStart(value);
            }
            break;
        case 4:
        case 5:
        case 6:
            // No case
            break;
        case 7:
            if ("end".equals(property)) {
                bean.setEnd(((Boolean)value).booleanValue());
            }
            break;
        case 8:
        case 9:
        case 10:
            // No case
            break;
        }
        
        throw new NoSuchPropertyException(property, false);
    }
    */
}
