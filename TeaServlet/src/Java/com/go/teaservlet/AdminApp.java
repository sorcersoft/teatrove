/*
 * AdminApp.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: AdminApp.java                                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

/******************************************************************************
 * This interface lets the ApplicationDepot know that this application will be
 *  providing links to the TeaservletAdmin Object for use on the admin pages.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface AdminApp extends Application {
    
    /*
     * Retrieves the administrative links for this application.
     */
    public AppAdminLinks getAdminLinks();

}
