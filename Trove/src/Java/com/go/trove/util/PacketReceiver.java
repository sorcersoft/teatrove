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
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/21/02 <!-- $-->
 */
public class PacketReceiver extends Thread {
        
    public static final boolean DEBUG = true;
    
    MulticastSocket mSock;
    ThreadPool mPacketHandlerThreads;
    UbercachePacketFactory mFactory;
    UbercacheConference mLocalConference;
    Map mYepTable;
    Map mSenderMap;
    Map mDataTable;
    Map mDataMap;
    Map mReseqMap;
    String mSender;
    ExpireThread mAutoExpire;
        
    PacketReceiver(MulticastSocket sock, UbercachePacketFactory factory,
                   UbercacheConference localConference,
                   ThreadPool handlerThreads) {
        super("PacketReceiver");
        mSock = sock;
        try {
            mSender = mSock.getInterface().getHostAddress();
        }
        catch (java.net.SocketException se) {
        }
        mLocalConference = localConference;
        mFactory = factory;
        mPacketHandlerThreads = handlerThreads;
        mYepTable = Collections.synchronizedMap(new SoftHashMap());
        mSenderMap = Collections.synchronizedMap(new SoftHashMap());
        mDataTable = Collections.synchronizedMap(new SoftHashMap());
        mDataMap = Collections.synchronizedMap(new SoftHashMap());
        mAutoExpire = new ExpireThread();
        mAutoExpire.start();
    }

    public boolean listenForYep(Object key, long timeout) 
        throws InterruptedException, IOException {
        
        Object sync = new Object();
        mYepTable.put(key, sync);
        if (DEBUG) {
            System.out.println("listening for yep " + key + '/' + sync);
        }
        long now;
        synchronized(sync) {
            mSock.send(mFactory.gotThis(key, mSender));
            sync.wait(timeout);
        }
        return mSenderMap.containsKey(key);
    }

    public Object listenForData(Object key, long timeout)
        throws InterruptedException, IOException {
        Object sync = new Object();
        mDataTable.put(key, sync);
        if (DEBUG) {
            System.out.println("listening for data " + key + '/' + sync);
        }
        synchronized(sync) {
            mSock.send(mFactory.gimme(key, (String)mSenderMap.remove(key)));
            sync.wait(timeout/2);
            if (!mDataMap.containsKey(key)) {
                mSock.send(mFactory.gimme(key));
            }
            sync.wait(timeout/2);
        }
        return mDataMap.remove(key);
    }

    public void run() {
        boolean cruisin = true;

        while (cruisin) {
            try {
                DatagramPacket packet = 
                    new DatagramPacket(new byte[UbercachePacketFactory
                                               .MAX_PACKET_LENGTH], 
                                       UbercachePacketFactory
                                       .MAX_PACKET_LENGTH);
                mSock.receive(packet);
                PacketHandler handler = new PacketHandler(packet,
                                                          this);
                mPacketHandlerThreads.start(handler);
            }
            catch (Exception e) {
                e.printStackTrace();
                cruisin = false;
                System.err.println("No longer listening for incoming packets.");
            }
        }
    }

    public void receivedYep(Object key, long absTTL,  String sender) {
        mSenderMap.put(key, sender);
        Object sync = mYepTable.get(key);
        if (sync != null) {
            if (DEBUG) {
                System.out.println("Notifying " + sync);
            }
            synchronized(sync) {
                sync.notifyAll();
            }
        }
    }


    public void receivedData(Object key, String sender,long absTTL, 
                             int seqCount, int seqTotal, byte[] data) 
        throws ClassNotFoundException, IOException, IllegalStateException {

        
        if (seqTotal > 1) {
            byte[][] rcvd;
            Object[] keys = {key, sender};
            Object mulKey = new MultiKey(keys);
            if (mReseqMap.containsKey(mulKey)) {
                rcvd = ((byte[][])mReseqMap.get(mulKey));
            }
            else {
                rcvd = new byte[seqTotal][];
                mReseqMap.put(mulKey, rcvd);                      
            }
            rcvd[seqCount] = data;
            boolean done = true;
            int totalBytes = 0;
            for (int j = 0; done && j < rcvd.length; j++) {
                if (rcvd[j] != null) {
                    totalBytes += rcvd[j].length;
                }
                else {
                    done = false;
                }
            }

            if (!done) {
                return;
            }
            else {
                data = new byte[totalBytes];
                int cursor = 0;
                for (int j = 0; j < rcvd.length; j++) {
                    System.arraycopy(rcvd[j], 0, data, cursor, rcvd[j].length);
                    cursor += rcvd[j].length;
                }
            }
        }
        
        Object sync = mDataTable.remove(key);
        if (sync != null) {
            mAutoExpire.addExpiration(key, absTTL);
            mDataMap.put(key, deserializeData(data));
            if (DEBUG) {
                System.out.println("Notifying " + sync);
            }
            synchronized(sync) {
                sync.notifyAll();
            }
        }
    }

    public void receivedGotThis(Object key, String sender) {
        try {
            if (sender == null || !sender.equalsIgnoreCase(mSender)) {
                if (DEBUG) {
                    System.out.println("Seeing if I have " + key);
                }
                if (mLocalConference.gotThis(key)) {
                    mSock.send(mFactory.yep(key, Integer.MAX_VALUE, mSender));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receivedExpire(Object key) {
        try {
            if (DEBUG) {
                System.out.println("Expiring " + key);
            }
            mLocalConference.expireCE(key);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void receivedGimme(Object key, String sender) {
        try {
            if (sender == null || sender.equalsIgnoreCase(mSender)) {
                if (DEBUG) {
                    System.out.println("trying to provide " + key);
                }
                Object obj = mLocalConference.gimme(key);
                if (obj != null) {
                    DatagramPacket[] packets = mFactory.bean(key, 
                                                             Integer.MAX_VALUE,
                                                             mSender, obj);
                    for (int j = 0; j < packets.length; j++) {
                        mSock.send(packets[j]);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object deserializeData(byte[] data) 
        throws ClassNotFoundException, IOException, IllegalStateException {
        
        ObjectInputStream ois = new ObjectInputStream
            (new ByteArrayInputStream(data));
        return ois.readObject();
    }

    private class ExpireThread extends Thread {


        TreeSet mExpirationSet;
        
        ExpireThread() {
            super("DistributedExpiration");
            setDaemon(true);
            mExpirationSet = new TreeSet();
        }

        public void addExpiration(Object key, long ttl) {
            synchronized(mExpirationSet) {
                mExpirationSet.add(new Expiration(key, ttl));
                mExpirationSet.notify();
            }
        }

        public void run() {
            boolean cruisin = true;
            synchronized(mExpirationSet) {
                while (cruisin) {
                    try {
                        Expiration exp = null;
                        try {
                            exp = (Expiration)mExpirationSet
                                .first();
                        }
                        catch (java.util.NoSuchElementException nsee) {
                            mExpirationSet.wait();
                        }
                        if (exp != null) {                        
                            long diff = exp.getTimeOfDying() - 
                                System.currentTimeMillis();
                            if (diff <= 0) {
                                mExpirationSet.remove(exp);
                                exp.expire();
                            }
                            else {
                                mExpirationSet.wait(diff);
                            }
                        }
                        else {
                            mExpirationSet.wait();
                        }
                    }
                    catch (Exception e) {
                        System.err.println("Distributed expiration thread halted.");
                        e.printStackTrace();
                        cruisin = false;
                    }
                }
            }
        }
    }

    private class Expiration implements Comparable {
        
        Object mObject;
        long mTimeOfDying;

        Expiration(Object obj, long ttl) {
            mObject = obj;
            mTimeOfDying = ttl;
        }

        public int compareTo(Object comp) throws ClassCastException {
            Expiration exp = (Expiration)comp;
            int diff = (int)(mTimeOfDying - exp.getTimeOfDying());
            if (diff == 0) {
                diff = mObject.hashCode() - exp.getObject().hashCode();
            }
            return diff;
        }

        public boolean equals(Object equ) {
            try {
                Expiration exp = (Expiration)equ;
                return mTimeOfDying == exp.getTimeOfDying() 
                    && mObject.equals(exp.getObject());
            }
            catch (Exception e) {
                return false;
            }
        }

        public Object getObject() {
            return mObject;
        }

        public long getTimeOfDying() {
            return mTimeOfDying;
        }

        public void expire() throws IOException {
            mLocalConference.expireCE(mObject);
        }
    }
}
