/*
 * MergedContextSource.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: MergedContextSource.java                                       $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.tea.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;

import com.go.tea.runtime.Context;
import com.go.tea.runtime.DefaultContext;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.DelegateClassLoader;
import com.go.trove.util.MergedClass;

/******************************************************************************
 * A ContextSource implementation that merges several ContextSources into one.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/17/02 <!-- $-->
 */
public class MergedContextSource implements ContextSource {

    private ClassInjector mInjector;
    private ContextSource[] mSources;
    private Class[] mContextsInOrder;
    
    private Constructor mConstr;

    // TODO: Why use init? Constructers may make more sense.

    public void init(ClassLoader loader, ContextSource[] contextSources) 
        throws Exception {
        
        init(loader, contextSources, null);
    }

    /**
     * Creates a unified context source for all those passed in.  An Exception
     * may be thrown if the call to a source's getContextType() method throws 
     * an exception.
     */
    public void init(ClassLoader loader, 
                     ContextSource[] contextSources,
                     String[] prefixes) throws Exception {
        
     
        mSources = contextSources;
        int len = contextSources.length;
        mContextsInOrder = new Class[len];
        ClassLoader[] delegateLoaders = new ClassLoader[len];
        
        for (int j = 0; j < contextSources.length; j++) {
            Class type = contextSources[j].getContextType();
            mContextsInOrder[j] = type;
            delegateLoaders[j] = type.getClassLoader();
        }

        mInjector = new ClassInjector
            (new DelegateClassLoader(loader, delegateLoaders));

        mConstr = MergedClass.getConstructor2(mInjector, 
                                              mContextsInOrder,
                                              prefixes);
    }

    protected Class[] getContextsInOrder() {
        return mContextsInOrder;
    }

    /** 
     * let subclasses get at the constructor
     */
    protected Constructor getConstructor() {
        return mConstr;
    }

    /**
     * @return the Class of the object returned by createContext.
     */
    public Class getContextType() {
        return mConstr.getDeclaringClass();
    }

    /**
     * a generic method to create context instances 
     */
    public Object createContext(Object param) throws Exception {
            return mConstr.newInstance(new Object[] {
                new MergingContextFactory(param)});
    }

    private class MergingContextFactory 
        implements MergedClass.InstanceFactory {
        
        private final Object mContextParameter;

        MergingContextFactory(Object contextParam) {
            mContextParameter = contextParam;            
        }

        public Object getInstance(int i) {
            try {
                return mSources[i].createContext(mContextParameter);
            }
            catch (Exception e) {
                throw new ContextCreationException(e);
            }
        }
    }
}
