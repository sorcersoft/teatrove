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
import java.util.Arrays;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/21/02 <!-- $-->
 */
public class PacketHandler implements Runnable {

    public static final boolean DEBUG = false;

    private DatagramPacket mRawPacket;
    private PacketReceiver mReception;

    private boolean mNew;
    private boolean mParsedWithNoErrors;
    private boolean mUbercacheProtocol;
    private Object mKey;
    private String mSender;
    private int mActionCode;
    private long mAbsoluteTTL;
    private int mSeq;
    private int mTotalSeq;
    private int mDataOffset;
    private int mDataLength;
    private byte[] mData;


    PacketHandler(DatagramPacket rawPacket, PacketReceiver reception) {
        mNew = true;
        mRawPacket = rawPacket;
        mReception = reception;
    }

    public void run() {
        try {
            parsePacket(mRawPacket);
            mNew = false;
            if (DEBUG) {
                System.out.println(toString());
            }
            processPacket(mReception);
        }
        catch (Exception e) {
            mParsedWithNoErrors = false;
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public boolean isNew() {
        return mNew;
    }

    public boolean isValid() {
        return mParsedWithNoErrors && !isNew();
    }

    public boolean isUsingUbercacheProtocol() {
        return mUbercacheProtocol;
    }

    public String getActionString() {
        return resolveAction(getActionCode());
    }
    
    public int getActionCode() {
        return mActionCode;
    }

    public Object getKey() {
        return mKey;
    }

    public String getSender() {
        return mSender;
    }

    public long getAbsoluteTTL() throws IllegalStateException {
        if (isNew()) {
            throw new IllegalStateException
                ("This Packet has not yet been processed.");
        }
        return mAbsoluteTTL;
    }

    public int getSequenceCount() throws IllegalStateException {
        if (isNew()) {
            throw new IllegalStateException
                ("This Packet has not yet been processed.");
        }
        return mSeq;
    }

    public int getSequenceTotal() throws IllegalStateException {
        if (isNew()) {
            throw new IllegalStateException
                ("This Packet has not yet been processed.");
        }
        return mTotalSeq;
    }

    public byte[] getRawData() throws IllegalStateException {
        if (isNew()) {
            throw new IllegalStateException
                ("This Packet has not yet been processed.");
        }
        byte[] data = new byte[mDataLength];
        System.arraycopy(mData, mDataOffset, data, 0, mDataLength);
        return data;
    }


    private void processPacket(PacketReceiver rec) 
        throws ClassNotFoundException, IOException, IllegalStateException {
        
        if (isValid()) {
            int actionCode = getActionCode();
            if (actionCode == UbercachePacketFactory.YEP_CODE) {
                rec.receivedYep(getKey(), getAbsoluteTTL(), getSender());
            }
            else if (actionCode == UbercachePacketFactory.DATA_CODE) {
                rec.receivedData(getKey(), getSender(), getAbsoluteTTL(),
                                 getSequenceCount(),
                                 getSequenceTotal(), getRawData());
            }
            else if (actionCode == UbercachePacketFactory.GOT_CODE) {
                rec.receivedGotThis(getKey(), getSender());
            }
            else if (actionCode == UbercachePacketFactory.GIVE_CODE) {
                rec.receivedGimme(getKey(), getSender());
            }
            else if (actionCode == UbercachePacketFactory.XCE_CODE) {
                rec.receivedExpire(getKey());
            }
        }
    }


    private int readField(byte[] buffer, int offset)
        throws InvalidFieldException {

        int pos = offset;
        int len = 0;
        try {
            switch (buffer[pos]) {
                
                
            case 'S':
                switch (buffer[++pos]) {
                case 'N':
                if (DEBUG) {
                    System.out.println("reading SND:");
                }

                    // parse sender
                    len = fieldLength(buffer, pos += 4);
                    mSender = new String(restOfField(buffer, pos, len - 2));
                    pos += len;
                    break;

                case 'Q':
                    if (DEBUG) {
                        System.out.println("reading SQN:");
                    }

                    // parse sequence
                    len = fieldLength(buffer, pos += 4);
                    String sequence = new String(restOfField(buffer, 
                                                             pos, len - 2));
                    pos += len;
                    mSeq = Integer
                        .parseInt(sequence
                                  .substring(0, sequence.indexOf('/')));
                    
                    mTotalSeq = Integer
                        .parseInt(sequence
                                  .substring(sequence.indexOf('/') + 1));
                    break;
                    
            
                default:
                    throw new InvalidFieldException
                        ("Invalid field encountered while parsing the packet.");
                }

                break;

            case 'A':
                if (DEBUG) {
                    System.out.println("reading ACT:");
                }

                // parse action code
                len = fieldLength(buffer, pos += 5);
                String action = new String(restOfField(buffer, pos, len - 2));
                if (DEBUG) {
                    System.out.println(action);
                }
                mActionCode = resolveAction(action);
                pos += len;
                break;
                
            case 'M':
                if (DEBUG) {
                    System.out.println("reading MID:");
                }

                len = buffer[pos += 5] << 24;
                pos++;
                len |= (buffer[pos++] & 0xFF) << 16; 
                len |= (buffer[pos++] & 0xFF) << 8;
                len |= (buffer[pos++] & 0xFF);

                mKey = new ObjectInputStream
                    (new ByteArrayInputStream(buffer, pos, len)).readObject();
                pos += len;
                break;
                
            case 'T':
                if (DEBUG) {
                    System.out.println("reading TTL:");
                }

                // parse hash key
                len = fieldLength(buffer, pos += 5);
                mAbsoluteTTL = System.currentTimeMillis() + Long
                    .parseLong(new String(restOfField(buffer, pos, len - 2)));
                pos += len;
                break;
                
            case 'C':
                // check if the CID is Ubercache 1.0
                len = fieldLength(buffer, pos);
                if (DEBUG) {
                    System.out.println("CID length = " + len);
                }
                mUbercacheProtocol = Arrays
                    .equals(restOfField(buffer, pos , len - 2), 
                            UbercachePacketFactory.CID);
                pos += len;
                break;

            case 'D':
                if (DEBUG) {
                    System.out.println("reading DAT:");
                }

                len = buffer[pos += 5] << 24;
                pos++;
                len |= (buffer[pos++] & 0xFF) << 16; 
                len |= (buffer[pos++] & 0xFF) << 8;
                len |= (buffer[pos++] & 0xFF);
                mData = buffer;
                mDataOffset = pos;
                mDataLength = len;
                pos += len;
                break;

            case '\r':
            case '\n':
                return 0;
                
            default:
                System.out.println("Switched on: " + (buffer[pos] & 0xFF) 
                                   + " at position " + pos);
                System.out.write(buffer, 0, pos + 5);
                
                throw new InvalidFieldException
                    ("Invalid field encountered while parsing the packet.");
            }
        }
        catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            throw new InvalidFieldException
                ("Error encountered while parsing the packet.");
        }
        return pos - offset;
    }



    private void parsePacket(DatagramPacket packet) {
            
        mParsedWithNoErrors = true;
        byte[] data = packet.getData();
        int cursor = 0;
        try {
            for (int lengthRead = 0;
                 (lengthRead = readField(data, cursor)) > 0;) {
                cursor += lengthRead;
            }
        }
        catch (InvalidFieldException ife) {
            ife.printStackTrace();
        }
    }

    // returns the length of the field from the position until the CRLF 
    // (inclusive) or end of buffer
    private int fieldLength(byte[] buffer, int position) {
        int end = position;
        while (++end < buffer.length 
               && buffer[end-1] != '\r' && buffer[end] != '\n') {
            // do nothing since everything happens in the while logic
        }
        return end + 1 - position;
    }

    private byte[] restOfField(byte[] buffer, int position, int len) {
        if (len > 0) {
            byte[] field = new byte[len];
            System.arraycopy(buffer, position, field, 0, len);
            return field;
        }
        else {
            return new byte[0];
        }
    }

    private int resolveAction(String actionStr) {
        if (actionStr != null) {
            if (actionStr.equals(UbercachePacketFactory.GOTSTR)) {
                return UbercachePacketFactory.GOT_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.YEPSTR)) {
                return UbercachePacketFactory.YEP_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.DATASTR)) {
                return UbercachePacketFactory.DATA_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.GIVESTR)) {
                return UbercachePacketFactory.GIVE_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.STOPSTR)) {
                return UbercachePacketFactory.STOP_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.XCESTR)) {
                return UbercachePacketFactory.XCE_CODE;
            }
            else if (actionStr.equals(UbercachePacketFactory.XHESTR)) {
                return UbercachePacketFactory.XHE_CODE;
            }
        }
        return 0;
    }

    private String resolveAction(int actionCode) {
        if (actionCode == UbercachePacketFactory.GOT_CODE) {
            return UbercachePacketFactory.GOTSTR;
        }
        else if (actionCode == UbercachePacketFactory.YEP_CODE) {
            return UbercachePacketFactory.YEPSTR;
        }
        else if (actionCode == UbercachePacketFactory.DATA_CODE) {
            return UbercachePacketFactory.DATASTR;
        }
        else if (actionCode == UbercachePacketFactory.GIVE_CODE) {
            return UbercachePacketFactory.GIVESTR;
        }
        else if (actionCode == UbercachePacketFactory.STOP_CODE) {
            return UbercachePacketFactory.STOPSTR;
        }
        else if (actionCode == UbercachePacketFactory.XCE_CODE) {
            return UbercachePacketFactory.XCESTR;
        }
        else if (actionCode == UbercachePacketFactory.XHE_CODE) {
            return UbercachePacketFactory.XHESTR;
        }
        else {
            return "Unknown action code";
        }
    }

    public String toString() {
        return "Packet Contents " + (mParsedWithNoErrors 
                                     ? "parsed without errors " 
                                     : "contained errors ")
            + "\n" + (mUbercacheProtocol ? "NOT " : "") 
            + "using Ubercache 1.0\n"
            + "Key = " + mKey + "\nSender: " + mSender + "\nAction: "
            + resolveAction(mActionCode) + "\nTTL: " + mAbsoluteTTL 
            + "\nSeq: " + mSeq + '/' + mTotalSeq + "\nData: " 
            + new String(((mData != null) ? mData : 
                          new byte[mDataOffset + mDataLength]), 
                         mDataOffset, 
                         mDataLength);
    }
}

