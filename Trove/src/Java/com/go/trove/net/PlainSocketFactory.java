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

import java.net.*;

/******************************************************************************
 * Allows client socket connections to be established with a timeout. Calling
 * getSocket will always return a new socket, and recycle will always close the
 * socket. Sessions are ignored on all requests.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/08 <!-- $-->
 */
public class PlainSocketFactory implements SocketFactory {
    protected final InetAddress mAddr;
    protected final int mPort;
    protected final InetAddress mLocalAddr;
    protected final int mLocalPort;
    protected final long mTimeout;

    /**
     * @param addr Address to connect new sockets to.
     * @param port Port to connect new sockets to.
     * @param timeout Maximum time to wait (in milliseconds) for new
     * connections to be established before throwing an exception
     */
    public PlainSocketFactory(InetAddress addr, int port, long timeout) {
        mAddr = addr;
        mPort = port;
        mLocalAddr = null;
        mLocalPort = 0;
        mTimeout = timeout;
    }

    /**
     * @param addr Address to connect new sockets to.
     * @param port Port to connect new sockets to.
     * @param localAddr Local address to bind new sockets to
     * @param localPort Local port to bind new sockets to, 0 for any
     * @param timeout Maximum time to wait (in milliseconds) for new
     * connections to be established before throwing an exception
     */
    public PlainSocketFactory(InetAddress addr, int port,
                              InetAddress localAddr, int localPort,
                              long timeout)
    {
        mAddr = addr;
        mPort = port;
        mLocalAddr = localAddr;
        mLocalPort = localPort;
        mTimeout = timeout;
    }

    public InetAddressAndPort getInetAddressAndPort() {
        return new InetAddressAndPort(mAddr, mPort);
    }
    
    public InetAddressAndPort getInetAddressAndPort(Object session) {
        return getInetAddressAndPort();
    }
    
    public long getDefaultTimeout() {
        return mTimeout;
    }

    public CheckedSocket createSocket()
        throws ConnectException, SocketException
    {
        return createSocket(mTimeout);
    }

    public CheckedSocket createSocket(Object session)
        throws ConnectException, SocketException
    {
        return createSocket(mTimeout);
    }

    public CheckedSocket createSocket(long timeout)
        throws ConnectException, SocketException
    {
        Socket socket = SocketConnector.connect
            (mAddr, mPort, mLocalAddr, mLocalPort, timeout);
        if (socket == null) {
            throw new ConnectException("Connect timeout expired: " + timeout);
        }
        return CheckedSocket.check(socket);
    }
    
    public CheckedSocket createSocket(Object session, long timeout)
        throws ConnectException, SocketException
    {
        return createSocket(timeout);
    }

    public CheckedSocket getSocket() throws ConnectException, SocketException {
        return createSocket(mTimeout);
    }

    public CheckedSocket getSocket(Object session)
        throws ConnectException, SocketException
    {
        return createSocket(mTimeout);
    }

    public CheckedSocket getSocket(long timeout)
        throws ConnectException, SocketException
    {
        return createSocket(timeout);
    }

    public CheckedSocket getSocket(Object session, long timeout)
        throws ConnectException, SocketException
    {
        return createSocket(timeout);
    }

    public void recycleSocket(CheckedSocket socket)
        throws SocketException, IllegalArgumentException
    {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (java.io.IOException e) {
                throw new SocketException(e.getMessage());
            }
        }
    }
    
    public void clear() {
    }
    
    public int getAvailableCount() {
        return 0;
    }
}
