/*
 * BackchannelSocketFactory.java
 * 
 * Copyright (c) 2002 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: BackchannelSocketFactory.java                                  $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet.util.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/19/02 <!-- $-->
 */
public class BackchannelSocketFactory extends RMISocketFactory {

    private static final int BACKLOG = 50;
    private InetAddress mBCaddr;

    public BackchannelSocketFactory(String host, String localNet) 
        throws IOException {
        
        mBCaddr = LocalNetResolver.resolveLocalNet(host, localNet);
    }

    public InetAddress getInetAddress() {
        return mBCaddr;
    }

    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port, mBCaddr, 0);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port, BACKLOG, mBCaddr);
    }
}
