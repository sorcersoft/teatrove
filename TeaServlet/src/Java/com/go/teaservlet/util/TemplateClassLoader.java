/*
 * TemplateClassLoader.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: TemplateClassLoader.java                                       $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/09/01 <!-- $-->
 */
public class TemplateClassLoader extends URLClassLoader {

    private static URL[]  makeURLs(String classpath) {
        if (classpath == null) {
            return new URL[0];
        }
        StringTokenizer st = new StringTokenizer(classpath, ",; ");
        URL[] urls = new URL[st.countTokens()];
        for (int j = 0; st.hasMoreTokens();j++) {
            try {
                urls[j] = new URL(st.nextToken());
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return urls;
    }

    public TemplateClassLoader(ClassLoader parent, String classpath) {
        super(makeURLs(classpath), parent);
    }

    protected Class loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {

        //System.out.println("\nLoading " + name);

        Class clazz = findLoadedClass(name);

        if (clazz == null) {
            synchronized (this) {
                try {
                    clazz = findClass(name);
                }   
                catch (Throwable wuteva) {
                    clazz = null;
                }
                    
                if (clazz == null) {
                    clazz = getParent().loadClass(name);
                    //   System.out.println("Parent Loaded " + name);
                }
                /*
                  else {
                  System.out.println("Loaded " + name + " successfully.");
                  }
                */
            }
        }
   
        
        if (resolve) {
            resolveClass(clazz);
        }
        
        return clazz;
        
    }
}
