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

package com.go.teaservlet.util.cluster;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

/******************************************************************************
 * A fairly generic implementation of the @see Clustered interface.
 * This class could be used by any sort of server or application that wants to 
 * share information and call methods across a cluster.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  6/26/01 <!-- $-->
 */
public class ClusterHook extends UnicastRemoteObject 
    implements Clustered {

    private List mPeers;
    protected String mClusterName,mServerName;

    public ClusterHook(String clusterName, String serverName) 
        throws RemoteException {
        
        super();
        mPeers = new Vector();
        mClusterName = clusterName;
        if (serverName != null) {
            mServerName = serverName.toLowerCase();
        }
    }

    public String getServerName() throws RemoteException {

        if (mServerName == null) {
            try {
                mServerName = InetAddress.getLocalHost()
                    .getHostName().toLowerCase();
            }
            catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            }
        }
        return mServerName;
    }

    public Clustered[] getKnownPeers() throws RemoteException {

        return (Clustered[])mPeers.toArray(new Clustered[mPeers.size()]);
    }

    public void addPeer(Clustered peer) throws RemoteException {

        mPeers.add(peer);
    }

    public boolean containsPeer(Clustered peer) throws RemoteException {

        return mPeers.contains(peer);
    }
    
    public void removePeer(Clustered peer) throws RemoteException {

        mPeers.remove(peer);
    }

    public String getClusterName() throws RemoteException {
        if (mClusterName == null) {
            mClusterName = "Unnamed_Cluster";
        }
        return mClusterName;
    }
}
