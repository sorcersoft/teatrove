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

package com.go.trove.net;

import java.io.*;
import java.net.*;

/******************************************************************************
 * A Socket wrapper that passes all calls to an internal Socket. This class is
 * designed for subclasses to override or hook into the behavior of a Socket
 * instance.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/08 <!-- $-->
 * @see SocketWrapper
 */
public class SocketFaceWrapper implements SocketFace {
    protected final SocketFace mSocket;

    public SocketFaceWrapper(SocketFace socket) {
        mSocket = socket;
    }

    public SocketFaceWrapper(Socket socket) {
        mSocket = new SocketWrapper(socket);
    }

    public InetAddress getInetAddress() {
        return mSocket.getInetAddress();
    }

    public InetAddress getLocalAddress() {
        return mSocket.getLocalAddress();
    }

    public int getPort() {
        return mSocket.getPort();
    }

    public int getLocalPort() {
        return mSocket.getLocalPort();
    }

    public InputStream getInputStream() throws IOException {
        return mSocket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        mSocket.setTcpNoDelay(on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return mSocket.getTcpNoDelay();
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        mSocket.setSoLinger(on, linger);
    }

    public int getSoLinger() throws SocketException {
        return mSocket.getSoLinger();
    }

    public void setSoTimeout(int timeout) throws SocketException {
        mSocket.setSoTimeout(timeout);
    }

    public int getSoTimeout() throws SocketException {
        return mSocket.getSoTimeout();
    }

    public void setSendBufferSize(int size) throws SocketException {
        mSocket.setSendBufferSize(size);
    }

    public int getSendBufferSize() throws SocketException {
        return mSocket.getSendBufferSize();
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        mSocket.setReceiveBufferSize(size);
    }

    public int getReceiveBufferSize() throws SocketException {
        return mSocket.getReceiveBufferSize();
    }

    public void setKeepAlive(boolean on) throws SocketException {
        mSocket.setKeepAlive(on);
    }

    public boolean getKeepAlive() throws SocketException {
        return mSocket.getKeepAlive();
    }

    public void close() throws IOException {
        mSocket.close();
    }

    public void shutdownInput() throws IOException {
        mSocket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        mSocket.shutdownOutput();
    }

    public String toString() {
        return mSocket.toString();
    }
}
