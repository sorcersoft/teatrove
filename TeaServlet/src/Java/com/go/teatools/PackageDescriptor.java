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

import java.lang.reflect.Method;
import java.util.Date;

/******************************************************************************
 * Wrapper for information retrieved from a package's <code>PackageInfo</code>
 * class <i>or</i> <code>java.lang.Package</code> object.
 *
 * @author Mark Masse
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/20/01 <!-- $-->
 */
public class PackageDescriptor {

    /**
     * Creates a PackageDescriptor for the named package.
     */
    public static PackageDescriptor forName(String packageName) {
        return forName(packageName, null);
    }

    /**
     * Creates a PackageDescriptor for the named package using the 
     * specified ClassLoader to load the PackageInfo or Package.
     */
    public static PackageDescriptor forName(String packageName,
                                            ClassLoader classLoader) {
        
        PackageDescriptor pd = new PackageDescriptor(packageName);
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        
        if (packageName == null || packageName.trim().length() == 0) {
            return pd;
        }
        
        String packageInfoClassName = packageName;

        if (!packageInfoClassName.endsWith(".")) {
            packageInfoClassName = packageInfoClassName + ".";
        }
        
        packageInfoClassName = packageInfoClassName + "PackageInfo";


        Class packageInfoClass = null;
        
        try {
            if (classLoader != null) {
                packageInfoClass = 
                    classLoader.loadClass(packageInfoClassName);
            }
            else {
                packageInfoClass = Class.forName(packageInfoClassName);
            }
        }
        catch (Throwable t) {
        }

        if (packageInfoClass != null) {
            try {
                initFromPackageInfo(pd, packageInfoClass);
            }
            catch (Throwable t) {
            }
        }
        else {

            Package pkg = null;
            
            if (classLoader != null) {
                // Get around getPackage's protected access
                PackageLoader packageLoader = 
                    new PackageLoader(classLoader);
                
                pkg = packageLoader.getPackage(packageName);
            }
            
            if (pkg == null) {
                pkg = Package.getPackage(packageName);
            }                        

            if (pkg != null) {
                initFromPackage(pd, pkg);
            }
        }

        return pd;
    } 

    // Initialize the PackageDescriptor from the PackageInfo class
    private static void initFromPackageInfo(PackageDescriptor pd,
                                            Class packageInfoClass) 
    throws Exception {


        pd.setExists(true);

        Class[] ca = new Class[0];
        Object[] oa = new Object[0];

        pd.setSpecificationTitle(
            (String) (packageInfoClass.getMethod("getSpecificationTitle", 
                                                 ca)).invoke(null, oa));

        pd.setSpecificationVersion(
            (String) (packageInfoClass.getMethod("getSpecificationVersion", 
                                                 ca)).invoke(null, oa));

        pd.setSpecificationVendor(
            (String) (packageInfoClass.getMethod("getSpecificationVendor", 
                                                 ca)).invoke(null, oa));

        pd.setImplementationTitle(
            (String) (packageInfoClass.getMethod("getImplementationTitle", 
                                                 ca)).invoke(null, oa));

        pd.setImplementationVersion(
            (String) (packageInfoClass.getMethod("getImplementationVersion", 
                                                 ca)).invoke(null, oa));

        pd.setImplementationVendor(
            (String) (packageInfoClass.getMethod("getImplementationVendor", 
                                                 ca)).invoke(null, oa));

        pd.setBaseDirectory(
            (String) (packageInfoClass.getMethod("getBaseDirectory", 
                                                 ca)).invoke(null, oa));

        pd.setRepository(
            (String) (packageInfoClass.getMethod("getRepository", 
                                                 ca)).invoke(null, oa));

        pd.setUsername(
            (String) (packageInfoClass.getMethod("getUsername", 
                                                 ca)).invoke(null, oa));

        pd.setBuildMachine(
            (String) (packageInfoClass.getMethod("getBuildMachine", 
                                                 ca)).invoke(null, oa));

        pd.setGroup(
            (String) (packageInfoClass.getMethod("getGroup", 
                                                 ca)).invoke(null, oa));

        pd.setProject(
            (String) (packageInfoClass.getMethod("getProject", 
                                                 ca)).invoke(null, oa));

        pd.setBuildLocation(
            (String) (packageInfoClass.getMethod("getBuildLocation", 
                                                 ca)).invoke(null, oa));

        pd.setProduct(
            (String) (packageInfoClass.getMethod("getProduct", 
                                                 ca)).invoke(null, oa));

        pd.setProductVersion(
            (String) (packageInfoClass.getMethod("getProductVersion", 
                                                 ca)).invoke(null, oa));

        pd.setBuildNumber(
            (String) (packageInfoClass.getMethod("getBuildNumber", 
                                                 ca)).invoke(null, oa));

        pd.setBuildDate(
            (Date) (packageInfoClass.getMethod("getBuildDate", 
                                               ca)).invoke(null, oa));


    }

    // Initialize the PackageDescriptor from the Package
    private static void initFromPackage(PackageDescriptor pd,
                                        Package pkg) {

        pd.setExists(true);

        String specificationTitle = pkg.getSpecificationTitle();
        String specificationVersion = pkg.getSpecificationVersion(); 
        String specificationVendor = pkg.getSpecificationVendor();

        String implementationTitle = pkg.getImplementationTitle();
        String implementationVersion = pkg.getImplementationVersion(); 
        String implementationVendor = pkg.getImplementationVendor();

        if (implementationTitle == null) {
            implementationTitle = specificationTitle;
        }

        if (implementationVersion == null) {
            implementationVersion = specificationVersion;
        }

        if (implementationVendor == null) {
            implementationVendor = specificationVendor;
        }


        pd.setSpecificationTitle(specificationTitle);
        pd.setSpecificationVersion(specificationVersion);
        pd.setSpecificationVendor(specificationVendor);

        pd.setImplementationTitle(implementationTitle);
        pd.setImplementationVersion(implementationVersion);
        pd.setImplementationVendor(implementationVendor);

        pd.setProduct(implementationTitle);
        pd.setProductVersion(implementationVersion);
    }


    
    protected String mPackageName;
    protected boolean mExists;

    protected String mSpecificationTitle;
    protected String mSpecificationVersion;
    protected String mSpecificationVendor;
    protected String mImplementationTitle;
    protected String mImplementationVersion;
    protected String mImplementationVendor;
    protected String mBaseDirectory;
    protected String mRepository;
    protected String mUsername;
    protected String mBuildMachine;
    protected String mGroup;
    protected String mProject;
    protected String mBuildLocation;
    protected String mProduct;
    protected String mProductVersion;
    protected String mBuildNumber;
    protected Date mBuildDate;

    
    /**
     * Creates a new PackageDescriptor for the specified packageName
     */
    public PackageDescriptor(String packageName) {
        setPackageName(packageName);
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Returns true if this PackageDescriptor was successfully initialized 
     * from PackageInfo or Package data. 
     */
    public boolean getExists() {
        return mExists;
    }

    public void setSpecificationTitle(String s) {
        mSpecificationTitle = s;
    }

    public String getSpecificationTitle() {
        return mSpecificationTitle;
    }

    public void setSpecificationVersion(String s) {
        mSpecificationVersion = s;
    }

    public String getSpecificationVersion() {
        return mSpecificationVersion;
    }

    public void setSpecificationVendor(String s) {
        mSpecificationVendor = s;
    }

    public String getSpecificationVendor() {
        return mSpecificationVendor;
    }

    public void setImplementationTitle(String s) {
        mImplementationTitle = s;
    }

    public String getImplementationTitle() {
        return mImplementationTitle;
    }

    public void setImplementationVersion(String s) {
        mImplementationVersion = s;
    }

    public String getImplementationVersion() {
        return mImplementationVersion;
    }

    public void setImplementationVendor(String s) {
        mImplementationVendor = s;
    }

    public String getImplementationVendor() {
        return mImplementationVendor;
    }

    public void setBaseDirectory(String s) {
        mBaseDirectory = s;
    }

    public String getBaseDirectory() {
        return mBaseDirectory;
    }

    public void setRepository(String s) {
        mRepository = s;
    }

    public String getRepository() {
        return mRepository;
    }

    public void setUsername(String s) {
        mUsername = s;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setBuildMachine(String s) {
        mBuildMachine = s;
    }

    public String getBuildMachine() {
        return mBuildMachine;
    }

    public void setGroup(String s) {
        mGroup = s;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setProject(String s) {
        mProject = s;
    }

    public String getProject() {
        return mProject;
    }

    public void setBuildLocation(String s) {
        mBuildLocation = s;
    }

    public String getBuildLocation() {
        return mBuildLocation;
    }

    public void setProduct(String s) {
        mProduct = s;
    }

    public String getProduct() {
        return mProduct;
    }

    public void setProductVersion(String s) {
        mProductVersion = s;
    }

    public String getProductVersion() {
        return mProductVersion;
    }

    public void setBuildNumber(String s) {
        mBuildNumber = s;
    }

    public String getBuildNumber() {
        return mBuildNumber;
    }

    public void setBuildDate(Date d) {
        mBuildDate = d;
    }

    public Date getBuildDate() {
        return mBuildDate;
    }

    private void setExists(boolean exists) {
        mExists = exists;
    }

    private static class PackageLoader extends ClassLoader {
        
        PackageLoader(ClassLoader parent) {
            super(parent);
        }

        public Package getPackage(String name) {
            return super.getPackage(name);
        }
    }

}
