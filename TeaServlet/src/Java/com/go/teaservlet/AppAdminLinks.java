/*
 * AppAdminLinks.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: AppAdminLinks.java                                             $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import java.util.List;
import java.util.Vector;

/******************************************************************************
 *  
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public class AppAdminLinks {
    
    String mAppName;
    List mLinks;

    public AppAdminLinks(String appName) {
        super();
        mAppName = appName;
        mLinks = new Vector();
    }

    public String getAppName() {
        return mAppName;
    }

    public AdminLink[] getLinks() {
        return (AdminLink[])mLinks.toArray(new AdminLink[mLinks.size()]);
    }

    public void addAdminLink(String name,String location) {
        mLinks.add(new AdminLink(name,location));
    }

    public void addAdminLink(AdminLink link) {
        mLinks.add(link);
    }   

    public class AdminLink {

        String mName,mLocation;
        
        public AdminLink(String name,String location) {
            mName = name;
            mLocation = location;
        }

        public String getName() {
            return mName;
        }

        public String getLocation() {
            return mLocation;
        }
    }
}
