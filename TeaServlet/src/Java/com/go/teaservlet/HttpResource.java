/*
 * HttpResource.java
 * 
 * Copyright (c) 2000 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Brian S O'Neill
 * 
 * $Workfile:: HttpResource.java                                              $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import java.io.*;
import java.net.*;
import java.util.*;
import com.go.trove.net.*;
import com.go.trove.util.*;
import com.go.trove.log.*;

/******************************************************************************
 * Very simple HTTP connection implementation, suitable for use by
 * HttpContextImpl.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
class HttpResource {
    // Default is 10,000 milliseconds.
    private static long DEFAULT_TIMEOUT = 10000;

    // Maps HostPorts to SocketFactories.
    private static Map cSocketFactories;

    // Maps URLs to HttpResources.
    private static Map cHttpResources;
    
    static {
        cSocketFactories = new Cache(10);
        cHttpResources = new Cache(100);
    }

    public static HttpResource get(URL url) {
        synchronized (cHttpResources) {
            HttpResource res = (HttpResource)cHttpResources.get(url);
            if (res == null) {
                HostPort key = (HostPort)Utils.intern(new HostPort(url));

                SocketFactory factory =
                    (SocketFactory)cSocketFactories.get(key);
                if (factory == null) {
                    String[] hosts = {key.mHost};
                    int[] ports = {key.mPort};
                    factory = new MultiPooledSocketFactory
                        (hosts, ports, DEFAULT_TIMEOUT);
                    cSocketFactories.put(key, factory);
                }

                res = new HttpResource(key, url.getFile(), factory);
                cHttpResources.put(url, res);
            }
            return res;
        }
    }

    private HostPort mHostPort;
    private String mURI;
    private SocketFactory mFactory;

    // 0 = HEAD might be supported
    // 1 = HEAD is supported
    // 2 = HEAD is not supported
    private int mHeadState;

    private HttpResource(HostPort hostPort, String uri,
                         SocketFactory factory) {
        mHostPort = hostPort;
        mURI = uri;
        mFactory = factory;
    }

    public boolean exists() throws IOException {
        return exists(mFactory.getDefaultTimeout());
    }

    public boolean exists(long timeout) throws IOException {
        HttpClient client = new HttpClient(mFactory, timeout);
        client.setURI(mURI);

        if (mHeadState == 0) {
            client.setMethod("HEAD");
        }
        else if (mHeadState == 1) {
            client.setMethod("HEAD").setPersistent(true);
        }
        else {
            // Default to GET request, so shutdown socket after receiving
            // response.
        }

        try {
            HttpClient.Response response = client.getResponse();

            // Make sure the input is read, but there shouldn't be any.
            InputStream in = response.getInputStream();
            while (in.read() >= 0);
            
            try {
                int statusCode = response.getStatusCode();
                switch (statusCode) {
                case 200: // OK
                    if (mHeadState == 0) {
                        mHeadState = 1;
                    }
                    return true;

                case 404: // Not Found
                    if (mHeadState == 0) {
                        mHeadState = 1;
                    }
                    return false;

                case 301: // Moved Permanently
                    if (mHeadState == 0) {
                        mHeadState = 1;
                    }
                    mURI = response.getHeaders().getString("Location");
                    client.setURI(mURI);
                    return client.getResponse().getStatusCode() == 200;

                case 302: // Moved Temporarily
                    if (mHeadState == 0) {
                        mHeadState = 1;
                    }
                    client.setURI(response.getHeaders().getString("Location"));
                    return client.getResponse().getStatusCode() == 200;

                case 501: // Not Implemented
                    break;

                case 503: // Service Unavailable
                    if (mHeadState == 0) {
                        mHeadState = 1;
                    }
                    Syslog.debug
                        ("Response from " + mHostPort + mURI + ": " +
                         statusCode + ' ' + response.getStatusMessage());
                    return false;

                default:
                    throw new IOException
                        ("Response from " + mHostPort + mURI + ": " +
                         statusCode + ' ' + response.getStatusMessage());
                }
            }
            finally {
                if (mHeadState == 2) {
                    try {
                        response.getInputStream().close();
                    }
                    catch (IOException e) {
                    }
                }
            }
        }
        catch (IOException e) {
            if (mHeadState != 0) {
                throw e;
            }
        }

        if (mHeadState == 0) {
            Syslog.warn("HEAD request not supported by " + mHostPort + mURI);
            mHeadState = 2;
            return exists(timeout);
        }
        else {
            return false;
        }
    }

    /**
     * @return null if no data
     */
    public HttpClient.Response getResponse() throws IOException {
        return getResponse(mFactory.getDefaultTimeout());
    }

    /**
     * @return null if no data
     */
    public HttpClient.Response getResponse(long timeout) throws IOException {
        HttpClient client = new HttpClient(mFactory, timeout);
        client.setURI(mURI);
        client.setPersistent(true);

        HttpClient.Response response = client.getResponse();

        int statusCode = response.getStatusCode();
        switch (statusCode) {
        case 200: // OK
            return response;

        case 404: // Not Found
            return null;
            
        case 301: // Moved Permanently
            mURI = response.getHeaders().getString("Location");
            client.setURI(mURI);
            response = client.getResponse();
            statusCode = response.getStatusCode();
            if (statusCode == 200) {
                return response;
            }
            else {
                return null;
            }

        case 302: // Moved Temporarily
            client.setURI(response.getHeaders().getString("Location"));
            response = client.getResponse();
            statusCode = response.getStatusCode();
            if (statusCode == 200) {
                return response;
            }
            else {
                return null;
            }
        }

        throw new IOException
            ("Response from " + mHostPort + mURI + ": " +
             statusCode + ' ' + response.getStatusMessage());
    }

    private static class HostPort {
        public final String mHost;
        public final int mPort;

        public HostPort(String host, int port) {
            mHost = host;
            mPort = port < 0 ? 80 : port;
        }

        public HostPort(URL url) {
            this(url.getHost(), url.getPort());
        }
        
        public int hashCode() {
            return mHost.hashCode() + mPort;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof HostPort)) {
                return false;
            }

            HostPort other = (HostPort)obj;
            return mPort == other.mPort && mHost.equals(other.mHost);
        }

        public String toString() {
            return mHost + ':' + mPort;
        }
    }
}
