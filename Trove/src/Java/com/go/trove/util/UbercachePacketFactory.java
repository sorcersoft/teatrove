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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.go.trove.io.ByteBuffer;
import com.go.trove.io.DefaultByteBuffer;
import com.go.trove.io.CharToByteBuffer;
import com.go.trove.io.FastCharToByteBuffer;

/******************************************************************************
 *  
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/21/02 <!-- $-->
 */
public class UbercachePacketFactory {
  
    public static final int MAX_PACKET_LENGTH = 32768;

    // Channel ID
    public static final String CIDSTR = "CID: Ubercache 1.0";
    public static final byte[] CID = CIDSTR.getBytes();

    // Message ID  a.k.a. Hash
    public static final String MIDSTR = "MID: ";
    public static final byte[] MID_TAG = MIDSTR.getBytes();

    // Actions
    public static final String ACTSTR = "ACT: ";
    public static final String GOTSTR = "got this?";
    public static final String YEPSTR = "yep";
    public static final String XCESTR = "expire CE";
    public static final String XHESTR = "expire HE";
    public static final String GIVESTR = "gimme";
    public static final String STOPSTR = "stop";
    public static final String DATASTR = "data";

    public static final byte[] ACT_TAG = ACTSTR.getBytes();
    public static final byte[] GOT = GOTSTR.getBytes();
    public static final byte[] YEP = YEPSTR.getBytes();
    public static final byte[] XCE = XCESTR.getBytes();
    public static final byte[] XHE = XHESTR.getBytes();
    public static final byte[] GIVE = GIVESTR.getBytes();
    public static final byte[] STOP = STOPSTR.getBytes();
    public static final byte[] DATA = DATASTR.getBytes();

    // Time to live and sender tags
    public static final String TTLSTR = "TTL: ";
    public static final String SNDSTR = "SND: ";
    public static final byte[] TTL_TAG = TTLSTR.getBytes();
    public static final byte[] SND_TAG = SNDSTR.getBytes();

    // Data and sequence tag
    public static final String DATSTR = "DAT: ";
    public static final String SQNSTR = "SQN: ";
    public static final byte[] DATA_TAG = DATSTR.getBytes();
    public static final byte[] SQN_TAG = SQNSTR.getBytes();

    // CRLF
    public static final byte[] CRLF = "\r\n".getBytes();

    //int action codes
    public static final int GOT_CODE = 
        (GOT[0] << 24) + (GOT[1] << 16) + (GOT[2] << 8) + GOT[3]; 
    public static final int YEP_CODE =      
        (YEP[0] << 20) + (YEP[1] << 10) + YEP[2];
    public static final int XCE_CODE = 
        (XCE[5] << 24) + (XCE[6] << 16) + (XCE[7] << 8) + XCE[8]; 
    public static final int XHE_CODE = 
        (XHE[8] << 24) + (XCE[7] << 16) + (XCE[6] << 8) + XCE[5]; 
    public static final int GIVE_CODE = 
        (GIVE[1] << 24) + (GIVE[2] << 16) + (GIVE[3] << 8) + GIVE[4]; 
    public static final int STOP_CODE = 
        (STOP[0] << 24) + (STOP[1] << 16) + (STOP[2] << 8) + STOP[3]; 
    public static final int DATA_CODE = 
        (DATA[0] << 24) + (DATA[1] << 16) + (DATA[2] << 8) + DATA[3]; 

    private InetAddress mAddress;
    private int mPort;

    UbercachePacketFactory(InetAddress address, int port) {
        mAddress = address;
        mPort = port;
    }

    public InetAddress getTargetAddress() {
        return mAddress;
    }

    public int getTargetPort() {
        return mPort;
    }

    public DatagramPacket gotThis(Object key, String sender) 
        throws IOException {
        CharToByteBuffer buf = createPacketHeader(key, GOT);
        appendSender(sender, buf);
        return createDatagramPacket(buf);
    }

    public DatagramPacket yep(Object key, int ttl, String sender)
        throws IOException {

        CharToByteBuffer buf = createPacketHeader(key, YEP);
        appendTTL(ttl, buf);
        appendSender(sender, buf);
        return createDatagramPacket(buf);
    }

    public DatagramPacket expire(Object key) throws IOException {
        CharToByteBuffer buf = createPacketHeader(key, XCE);
        return createDatagramPacket(buf);
    }

    public DatagramPacket gimme(Object key) throws IOException {
        return gimme(key, null);
    }

    public DatagramPacket gimme(Object key, String sender) throws IOException {
        CharToByteBuffer buf = createPacketHeader(key, GIVE);
        appendSender(sender, buf);
        return createDatagramPacket(buf);
    }

    public DatagramPacket stop(Object key) throws IOException {
        CharToByteBuffer buf = createPacketHeader(key, STOP);
        return createDatagramPacket(buf);
    }

    public DatagramPacket[] bean(Object key, int ttl, 
                                 String sender, Object obj) 
        throws IOException {
        

        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteOS);
        oos.writeObject(obj);
        oos.flush();
        byte[] objBuf = byteOS.toByteArray();
        
        int sent = 0;
        int remaining = objBuf.length;
        int totalPackets = (remaining / MAX_PACKET_LENGTH) + 1;

        CharToByteBuffer head = createPacketHeader(key, DATA);
        appendTTL(ttl, head);
        appendSender(sender, head);
            
        DatagramPacket[] packets = 
            new DatagramPacket[totalPackets];

        for (int j = 0; remaining > 0; j++) {
            CharToByteBuffer buf =
                new FastCharToByteBuffer(new DefaultByteBuffer(), 
                                         "ISO-8859-1");
            buf.appendSurrogate(head);
            appendSequence(j+1, totalPackets, buf);
            appendData(objBuf, sent, ((MAX_PACKET_LENGTH > remaining) ? 
                                      remaining : MAX_PACKET_LENGTH), buf); 
            packets[j] = createDatagramPacket(buf);

            sent += MAX_PACKET_LENGTH;
            remaining -= MAX_PACKET_LENGTH;
        }
        return packets;
    }

    protected CharToByteBuffer createPacketHeader(Object key, byte[] action) 
        throws IOException {

        CharToByteBuffer buf =
            new FastCharToByteBuffer(new DefaultByteBuffer(), "ISO-8859-1");
        
        buf.append(CID);
        buf.append(CRLF);
        buf.append(MID_TAG);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(key);
        oos.flush();
        byte[] serial = baos.toByteArray();
        int length = serial.length;
        buf.append((byte)((length >> 24) & 0xFF));
        buf.append((byte)((length >> 16) & 0xFF));
        buf.append((byte)((length >> 8) & 0xFF));
        buf.append((byte)(length & 0xFF));
        buf.append(serial, 0, length);
        buf.append(ACT_TAG);
        buf.append(action);
        buf.append(CRLF);

        return buf;
    }

    protected void appendTTL(int ttl, CharToByteBuffer buf) 
        throws IOException {
    
        buf.append(TTL_TAG);
        buf.append(Integer.toString(ttl));
        buf.append(CRLF);
    }

    protected void appendSender(String sender, CharToByteBuffer buf) 
        throws IOException {
        if (sender != null) {
            buf.append(SND_TAG);
            buf.append(sender);
            buf.append(CRLF);
        }
    }

    protected void appendSequence(int count, int total, CharToByteBuffer buf)
        throws IOException {
        if (count > 0 && total > 1) {
            buf.append(SQN_TAG);
            buf.append(Integer.toString(count) + "/" 
                       + Integer.toString(total));
            buf.append(CRLF);
        }
    }
     
    protected void appendData(byte[] data, int offset, int length, 
                              CharToByteBuffer buf) throws IOException {

        buf.append(DATA_TAG);
        buf.append((byte)((length >> 24) & 0xFF));
        buf.append((byte)((length >> 16) & 0xFF));
        buf.append((byte)((length >> 8) & 0xFF));
        buf.append((byte)(length & 0xFF));
        buf.append(data, offset, length);
    }
    
    protected DatagramPacket createDatagramPacket(CharToByteBuffer buf) 
        throws IOException {

        ByteArrayOutputStream baos = 
            new ByteArrayOutputStream(((int)buf.getByteCount()) + 2);
        buf.writeTo(baos);
        // write empty field to indicate end of packet.
        baos.write(CRLF);
        byte[] data = baos.toByteArray();
        return new DatagramPacket(data, data.length, 
                                  getTargetAddress(), 
                                  getTargetPort());
    }
}

