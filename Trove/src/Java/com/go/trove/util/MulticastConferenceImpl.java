/* ====================================================================
 * Trove - Copyright (c) 1997-2000 Walt Disney Internet Group
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/******************************************************************************
 * Used to confer with peers via the Ubercache protocol.
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/21/02 <!-- $-->
 */
public class MulticastConferenceImpl implements UbercacheConference {
    
    //MulticastSocket mSocket;
    MulticastSocket mSocketWithPortSpecified;
    InetAddress mGroupAddress;
    int mPort;
    UbercachePacketFactory mPacketFactory;
    PacketReceiver mReception;
    PacketReceiver mReceptionWithPortSpecified;
    long mDefaultTimeout;

    public static UbercacheConference createMulticastConference
        (String mcastGroupAddress, int port, 
         UbercacheConference localConference) throws IOException {

        return new MulticastConferenceImpl(InetAddress
                                           .getByName(mcastGroupAddress), 
                                           port, 
                                           InetAddress.getLocalHost(),
                                           localConference);
    }

    public static UbercacheConference createMulticastConference
        (String mcastGroupAddress, int port, String localHost,
         UbercacheConference localConference) throws IOException {

        return new MulticastConferenceImpl(InetAddress
                                           .getByName(mcastGroupAddress), 
                                           port, 
                                           InetAddress.getByName(localHost),
                                           localConference);
    }

    MulticastConferenceImpl(InetAddress mcastGroup, int port, 
                            InetAddress localAddress,
                            UbercacheConference localConference) 
        throws IOException {
        mDefaultTimeout = 1234L;
        //mSocket = new MulticastSocket();
        mSocketWithPortSpecified = new MulticastSocket(port);
        //mSocket.joinGroup(mcastGroup);
        mSocketWithPortSpecified.setInterface(localAddress);
        System.out.println(mSocketWithPortSpecified
                           .getInterface().getHostAddress());

        mSocketWithPortSpecified.joinGroup(mcastGroup);
        mPacketFactory = new UbercachePacketFactory(mcastGroup, port);
        /*   
             mReception = new PacketReceiver(mSocket,
                                        mPacketFactory,
                                        localConference,
                                        new ThreadPool("PacketHandlers", 100));
        */
        mReceptionWithPortSpecified = 
            new PacketReceiver(mSocketWithPortSpecified,
                               mPacketFactory,
                               localConference,
                               new ThreadPool("PacketHandlers", 100));
        // listen up.
        mReceptionWithPortSpecified.setDaemon(true);
        mReceptionWithPortSpecified.start();
        //mReception.start();
    }

    public boolean gotThis(Object key) throws IOException {
        return gotThis(key, mDefaultTimeout);
    }
    
    public boolean gotThis(Object key, long timeout) throws IOException {
        try {
            return mReceptionWithPortSpecified.listenForYep(key, timeout);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
            return false;
        }
    }
    
    public Object gimme(Object key) throws IOException {
        return gimme(key, mDefaultTimeout);
    }

    public Object gimme(Object key, long timeout) throws IOException {
        try {
            return mReceptionWithPortSpecified.listenForData(key, timeout);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }
    }
  
    public void stop(Object key) throws IOException {
        // mSocket.send(mPacketFactory.stop(key));
        mSocketWithPortSpecified.send(mPacketFactory.stop(key));
    }
    
    public void expireCE(Object key) throws IOException {
        //mSocket.send(mPacketFactory.expire(key));
        mSocketWithPortSpecified.send(mPacketFactory.expire(key));
    }  
    
}

