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
import java.io.*;

/******************************************************************************
 * A socket implementation that lazily establishs a connection. It only
 * connects when actually needed. Setting options and getting I/O streams will
 * not force a connection to be established. As soon as a read or write
 * operation is performed, a connection is established.
 * <p>
 * If the first write operation requires a connection to be established, then a
 * recycled connection is requested. The connection is tested by writing the
 * data to it. If this fails, a new connection is requested and the operation
 * is tried again.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/08 <!-- $-->
 */
class LazySocket implements SocketFace {
    private final SocketFactory mFactory;
    private final Object mSession;
    private final long mTimeout;

    private boolean mClosed;
    private CheckedSocket mSocket;

    private InputStream mIn;
    private OutputStream mOut;

    private Object[] mOptions;

    public LazySocket(SocketFactory factory) throws SocketException {
        this(factory, null, factory.getDefaultTimeout());
    }

    public LazySocket(SocketFactory factory, Object session)
        throws SocketException
    {
        this(factory, session, factory.getDefaultTimeout());
    }

    public LazySocket(SocketFactory factory, long timeout)
        throws SocketException
    {
        this(factory, null, timeout);
    }

    public LazySocket(SocketFactory factory, Object session, long timeout)
        throws SocketException
    {
        mFactory = factory;
        mSession = session;
        mTimeout = timeout;
    }

    public InetAddress getInetAddress() {
        if (mSocket != null) {
            return mSocket.getInetAddress();
        }
        else {
            return mFactory.getInetAddressAndPort(mSession).getInetAddress();
        }
    }

    public InetAddress getLocalAddress() {
        if (mSocket != null) {
            return mSocket.getLocalAddress();
        }
        else {
            return null;
        }
    }

    public int getPort() {
        if (mSocket != null) {
            return mSocket.getPort();
        }
        else {
            return mFactory.getInetAddressAndPort(mSession).getPort();
        }
    }

    public int getLocalPort() {
        if (mSocket != null) {
            return mSocket.getLocalPort();
        }
        else {
            return -1;
        }
    }

    public InputStream getInputStream() throws IOException {
        if (mIn == null) {
            mIn = new In();
        }
        return mIn;
    }

    public OutputStream getOutputStream() throws IOException {
        if (mOut == null) {
            mOut = new Out();
        }
        return mOut;
    }

    // Option 0.
    public void setTcpNoDelay(boolean on) throws SocketException {
        if (mSocket != null) {
            mSocket.setTcpNoDelay(on);
        }
        else {
            setOption(0, on ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    public boolean getTcpNoDelay() throws SocketException {
        return createSocket().getTcpNoDelay();
    }

    // Option 1.
    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (mSocket != null) {
            mSocket.setSoLinger(on, linger);
        }
        else {
            Object value;
            if (on) {
                value = new Integer(linger);
            }
            else {
                value = Boolean.FALSE;
            }
            setOption(1, value);
        }
    }

    public int getSoLinger() throws SocketException {
        return createSocket().getSoLinger();
    }

    // Option 2.
    public void setSoTimeout(int timeout) throws SocketException {
        if (mSocket != null) {
            mSocket.setSoTimeout(timeout);
        }
        else {
            setOption(2, new Integer(timeout));
        }
    }

    public int getSoTimeout() throws SocketException {
        return createSocket().getSoTimeout();
    }

    // Option 3.
    public void setSendBufferSize(int size) throws SocketException {
        if (mSocket != null) {
            mSocket.setSendBufferSize(size);
        }
        else {
            setOption(3, new Integer(size));
        }
    }

    public int getSendBufferSize() throws SocketException {
        return createSocket().getSendBufferSize();
    }

    // Option 4.
    public void setReceiveBufferSize(int size) throws SocketException {
        if (mSocket != null) {
            mSocket.setReceiveBufferSize(size);
        }
        else {
            setOption(4, new Integer(size));
        }
    }


    public int getReceiveBufferSize() throws SocketException {
        return createSocket().getReceiveBufferSize();
    }

    // Option 5.
    public void setKeepAlive(boolean on) throws SocketException {
        if (mSocket != null) {
            mSocket.setKeepAlive(on);
        }
        else {
            setOption(5, on ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    public boolean getKeepAlive() throws SocketException {
        return createSocket().getKeepAlive();
    }

    public void shutdownInput() throws IOException {
        createSocket().shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        createSocket().shutdownOutput();
    }

    public void close() throws IOException {
        mClosed = true;
        if (mSocket != null) {
            mSocket.close();
        }
    }

    public String toString() {
        if (mSocket != null) {
            return mSocket.toString();
        }
        InetAddressAndPort ip = mFactory.getInetAddressAndPort(mSession);
        return "Unconnected socket[addr=" + ip.getInetAddress() + ",port=" +
            ip.getPort() + ']';
    }

    /**
     * Returns the internal wrapped socket or null if not connected. After
     * calling recycle, this LazySocket instance is closed.
     */
    CheckedSocket recycle() {
        CheckedSocket s;
        if (mClosed) {
            s = null;
        }
        else {
            s = mSocket;
            mSocket = null;
            mClosed = true;
        }
        return s;
    }

    private SocketFace createSocket() throws SocketException {
        if (mSocket != null) {
            return mSocket;
        }
        
        if (mClosed) {
            throw new SocketException("Socket is closed");
        }
        
        mSocket = mFactory.createSocket(mSession, mTimeout);
        applyOptions();
        
        return mSocket;
    }

    private SocketFace getSocket(byte[] data, int off, int len)
        throws SocketException
    {
        if (mSocket != null) {
            return mSocket;
        }

        if (mClosed) {
            throw new SocketException("Socket is closed");
        }
        
        long timeout = mTimeout;
        long start;
        if (timeout > 0) {
            start = System.currentTimeMillis();
        }
        else {
            start = 0;
        }
        
        try {
            mSocket = mFactory.getSocket(mSession, timeout);
            applyOptions();
            OutputStream out = mSocket.getOutputStream();
            out.write(data, off, len);
            out.flush();
        }
        catch (Exception e) {
            if (mSocket != null) {
                try {
                    mSocket.close();
                }
                catch (Exception e2) {
                }
            }
            
            if (timeout > 0) {
                timeout = timeout - (System.currentTimeMillis() - start);
                if (timeout < 0) {
                    timeout = 0;
                }
            }
            
            mSocket = mFactory.createSocket(mSession, timeout);
            applyOptions();
            try {
                OutputStream out = mSocket.getOutputStream();
                out.write(data, off, len);
                out.flush();
            }
            catch (IOException e2) {
                throw new SocketException(e2.getMessage());;
            }
        }
        
        return mSocket;
    }

    private void setOption(int index, Object value) {
        if (mOptions == null) {
            mOptions = new Object[6];
        }
        mOptions[index] = value;
    }

    private void applyOptions() throws SocketException {
        if (mOptions == null || mSocket == null) {
            return;
        }
        
        Object[] options = mOptions;
        Object value;
        
        if ((value = options[0]) != null) {
            mSocket.setTcpNoDelay(((Boolean)value).booleanValue());
        }
        if ((value = options[1]) != null) {
            if (value instanceof Boolean) {
                mSocket.setSoLinger(((Boolean)value).booleanValue(), 0);
            }
            else {
                mSocket.setSoLinger(true, ((Integer)value).intValue());
            }
        }
        if ((value = options[2]) != null) {
            mSocket.setSoTimeout(((Integer)value).intValue());
        }
        if ((value = options[3]) != null) {
            mSocket.setSendBufferSize(((Integer)value).intValue());
        }
        if ((value = options[4]) != null) {
            mSocket.setReceiveBufferSize(((Integer)value).intValue());
        }
        if ((value = options[5]) != null) {
            mSocket.setKeepAlive(((Boolean)value).booleanValue());
        }
    }
    
    private class In extends InputStream {
        private InputStream mStream;
        
        public int read() throws IOException {
            return getStream().read();
        }
        
        public int read(byte[] b) throws IOException {
            return getStream().read(b);
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
            return getStream().read(b, off, len);
        }
        
        public long skip(long n) throws IOException {
            return getStream().skip(n);
        }
        
        public int available() throws IOException {
            return getStream().available();
        }
            
        public void close() throws IOException {
            if (mStream != null) {
                mStream.close();
            }
            LazySocket.this.close();
        }
        
        public void mark(int readlimit) {
            try {
                getStream().mark(readlimit);
            }
            catch (IOException e) {
            }
        }
        
        public void reset() throws IOException {
            if (mStream == null) {
                throw new IOException("Stream not marked");
            }
            else {
                mStream.reset();
            }
        }
        
        public boolean markSupported() {
            try {
                return getStream().markSupported();
            }
            catch (IOException e) {
                return false;
            }
        }
        
        private InputStream getStream() throws IOException {
            if (mStream == null) {
                mStream = createSocket().getInputStream();
            }
            return mStream;
        }
    }
    
    private class Out extends OutputStream {
        private OutputStream mStream;
        
        public void write(int b) throws IOException {
            write(new byte[] {(byte)b}, 0, 1);
        }
        
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
        
        public void write(byte[] b, int off, int len) throws IOException {
            if (mStream == null) {
                mStream = getSocket(b, off, len).getOutputStream();
            }
            else {
                mStream.write(b, off, len);
            }
        }
        
        public void flush() throws IOException {
            if (mStream != null) {
                mStream.flush();
            }
        }
        
        public void close() throws IOException {
            if (mStream != null) {
                mStream.close();
            }
            LazySocket.this.close();
        }
    }
}
