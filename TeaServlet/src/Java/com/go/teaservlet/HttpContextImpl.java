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

package com.go.teaservlet;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.go.trove.log.Log;
import com.go.trove.io.*;
import com.go.trove.net.*;

import com.go.teaservlet.util.DecodedRequest;

import com.go.tea.runtime.Substitution;
import com.go.tea.runtime.OutputReceiver;

/******************************************************************************
 * The context that is used by the template to return its data. This class 
 * provides some additional HTTP-specific template functions.
 *
 * @author Reece Wilton, Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
 */
class HttpContextImpl extends com.go.tea.runtime.DefaultContext
implements HttpContext {

    private static final int FILE_SPILLOVER = 65000;

    protected final ServletContext mServletContext;

    protected final Log mLog;

    /** The client's HTTP request */
    protected final HttpServletRequest mRequest;

    /** The client's HTTP response */
    protected final HttpServletResponse mResponse;

    /** Buffer that receives template output */
    protected final CharToByteBuffer mBuffer;

    private Request mReq;

    private OutputReceiver mOutputReceiver;

    private boolean mOutputOverridePermitted;

    // Default is 10,000 milliseconds.
    private long mURLTimeout = 10000;

    /**
     * Constructs the HttpContext which provides HTTP-specific template
     * functions.
     *
     * @param request the client's HTTP request
     * @param response the client's HTTP response
     * @param buffer receives the template output
     */
    public HttpContextImpl(ServletContext context,
                           Log log,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           CharToByteBuffer buffer,
                           OutputReceiver outputReceiver) {
        mServletContext = context;
        mLog = log;
        mRequest = request;
        mResponse = response;
        mBuffer = buffer;
        mOutputReceiver = outputReceiver;
        mOutputOverridePermitted = (outputReceiver != null);
    }

    /**
     * This method is called when the template outputs data. This
     * implementation calls this.toString(Object) on the object and then
     * appends the result to the internal CharToByteBuffer.
     *
     * @param obj the object to output
     *
     * @hidden
     */
    public final void print(Object obj) throws Exception {
            
        if ((mOutputOverridePermitted || mBuffer == null) 
            && mOutputReceiver != null) { 
            mOutputReceiver.print(obj);
        }
        else if (mBuffer != null) {
            mBuffer.append(toString(obj));
        }
    }

    public void overrideOutput(boolean overridePermitted) {
        mOutputOverridePermitted = overridePermitted;
    }
    
    public HttpContext.Request getRequest() {
        if (mReq == null) {
            mReq = new Request(mRequest);
        }
        return mReq;
    }

    public HttpContext.Request getRequest(String encoding) {
        return new Request(new DecodedRequest(mRequest, encoding));
    }

    public void setStatus(int code) {
        mResponse.setStatus(code);
    }

    public void sendError(int code, String message)
        throws AbortTemplateException, IOException
    {
        mResponse.sendError(code, message);
        throw new AbortTemplateException();
    }

    public void sendError(int code)
        throws AbortTemplateException, IOException
    {
        mResponse.sendError(code);
        throw new AbortTemplateException();
    }

    public void sendRedirect(String url)
        throws AbortTemplateException, IOException
    {
        mResponse.sendRedirect(url);
        throw new AbortTemplateException();
    }

    public void setContentType(String type) throws IOException {
        mResponse.setContentType(type);
    }

    public void setHeader(String name, String value) {
        mResponse.setHeader(name, value);
    }

    public void setHeader(String name, int value) {
        mResponse.setIntHeader(name, value);
    }

    public void setHeader(String name, Date value) {
        mResponse.setDateHeader(name, value.getTime());
    }

    public String encodeParameter(String str) {
        return java.net.URLEncoder.encode(str);
    }

    public boolean fileExists(String path) {
        if (path != null) {
            return absoluteFile(path).exists();
        }
        return false;
    }
    
    public void insertFile(String path) throws IOException {
        if (path != null) {
            File file = absoluteFile(path);
            ByteData data = null;
            try {
                data = new FileByteData(file);
                long length = data.getByteCount();
                if (length > FILE_SPILLOVER) {
                    // If its big enough, don't save contents.
                    mBuffer.appendSurrogate(data);
                    data = null;
                }
                else {
                    data.writeTo(new ByteBufferOutputStream(mBuffer));
                }
            }
            catch (IOException e) {
                mLog.warn(e);
            }
            finally {
                if (data != null) {
                    data.reset();
                }
            }
        }
    }

    public String readFile(String path) throws IOException {
        if (path != null) {
            File file = absoluteFile(path);
            ByteData data = null;
            try {
                data = new FileByteData(file);
                long length = data.getByteCount();
                if (length > Integer.MAX_VALUE) {
                    throw new IOException
                        ("File is too long: " + file + ", " + length);
                }
                ByteArrayOutputStream baos = 
                    new ByteArrayOutputStream((int)length);
                data.writeTo(baos);
                return baos.toString();
            }
            catch (IOException e) {
                mLog.warn(e);
            }
            finally {
                if (data != null) {
                    data.reset();
                }
            }
        }
        return "";
    }

    public String readFile(String path, String encoding) throws IOException {
        if (path != null) {
            File file = absoluteFile(path);
            ByteData data = null;
            try {
                data = new FileByteData(file);
                long length = data.getByteCount();
                if (length > Integer.MAX_VALUE) {
                    throw new IOException
                        ("File is too long: " + file + ", " + length);
                }
                ByteArrayOutputStream baos = 
                    new ByteArrayOutputStream((int)length);
                data.writeTo(baos);
                return baos.toString(encoding);
            }
            catch (IOException e) {
                mLog.warn(e);
            }
            finally {
                if (data != null) {
                    data.reset();
                }
            }
        }
        return "";
    }

    private File absoluteFile(String path) {
        String originalPath = path;

        if (path.length() > 0 && path.charAt(0) != '/') {
            // Relative path.
            String requestURI = mRequest.getRequestURI();
            int index = requestURI.lastIndexOf('/');
            if (index > 0) {
                if (index < requestURI.length() - 1) {
                    path = requestURI.substring(0, index + 1) + path;
                }
                else {
                    path = requestURI + path;
                }
            }
            else {
                path = '/' + path;
            }
        }
        else {
            String servletPath = mRequest.getServletPath();
            if (servletPath != null) {
                int length = servletPath.length();
                if (length > 0 && servletPath.charAt(length - 1) == '/') {
                    path = servletPath.substring(0, length - 1) + path;
                }
                else {
                    path = servletPath + path;
                }
            }
        }

        String realPath = mServletContext.getRealPath(path);
        if (realPath != null) {
            return new File(realPath);
        }

        return new File(originalPath);
    }

    public boolean URLExists(String url) {
        if (url == null) {
            return false;
        }

        try {
            return HttpResource.get(absoluteURL(url)).exists(mURLTimeout);
        }
        catch (UnknownHostException e) {
        }
        catch (IOException e) {
            mLog.warn(e);
        }

        return false;
    }

    public void insertURL(String url) throws IOException {
        if (url == null) {
            return;
        }

        // Unlike insertFile, the URL contents are read into the buffer
        // immediately. Not all resources report a content length, and
        // I can't lock the resource to guarantee the length remains fixed.

        try {
            HttpResource resource = HttpResource.get(absoluteURL(url));
            HttpClient.Response response = resource.getResponse(mURLTimeout);
            if (response == null) {
                return;
            }

            Integer contentLength =
                response.getHeaders().getInteger("Content-Length");

            byte[] buffer;
            if (contentLength == null || contentLength.intValue() >= 1024) {
                buffer = new byte[1024];
            }
            else {
                buffer = new byte[contentLength.intValue()];
            }

            InputStream in = response.getInputStream();

            int count;
            while ((count = in.read(buffer)) > 0) {
                mBuffer.append(buffer, 0, count);
            }
        }
        catch (UnknownHostException e) {
        }
        catch (IOException e) {
            mLog.warn(e);
        }
    }

    public String readURL(String url) throws IOException {
        return readURL(url, "iso-8859-1");
    }

    public String readURL(String url, String encoding) throws IOException {
        if (url == null) {
            return "";
        }

        try {
            HttpResource resource = HttpResource.get(absoluteURL(url));
            HttpClient.Response response = resource.getResponse(mURLTimeout);
            if (response == null) {
                return "";
            }

            Reader in = new InputStreamReader
                (response.getInputStream(), encoding);

            StringBuffer sb = new StringBuffer(1024);
            char[] buffer = new char[1024];
            int count;
            while ((count = in.read(buffer, 0, 1024)) > 0) {
                sb.append(buffer, 0, count);
            }
            return new String(sb);
        }
        catch (UnknownHostException e) {
        }
        catch (IOException e) {
            mLog.warn(e);
        }

        return "";
    }

    public void setURLTimeout(long timeout) {
        mURLTimeout = timeout;
    }

    private URL absoluteURL(String path) throws MalformedURLException {
        int colonIndex = path.indexOf(':');
        if (colonIndex > 0) {
            if (path.lastIndexOf('/', colonIndex - 1) < 0) {
                // Protocol pattern detected, URL is already absolute.
                return new URL(path);
            }
        }

        if (path.length() > 0 && path.charAt(0) != '/') {
            // Relative path.
            String requestURI = mRequest.getRequestURI();
            int index = requestURI.lastIndexOf('/');
            if (index > 0) {
                if (index < requestURI.length() - 1) {
                    path = requestURI.substring(0, index + 1) + path;
                }
                else {
                    path = requestURI + path;
                }
            }
            else {
                path = '/' + path;
            }
        }

        return new URL(mRequest.getScheme(), mRequest.getServerName(), 
                       mRequest.getServerPort(), path);
    }

    public void stealOutput(OutputReceiver receiver, Substitution s)
        throws Exception
    {
        if (receiver == this) {
            // Avoid stack overflow if this method is accidentally misused.
            receiver = null;
        }

        OutputReceiver original = mOutputReceiver;
        mOutputReceiver = receiver;
        boolean override = mOutputOverridePermitted;
        overrideOutput(true);
        try {
            s.substitute();
        }
        finally {
            overrideOutput(override);
            mOutputReceiver = original;
        }
    }

    private static class Request implements HttpContext.Request {

        private final HttpServletRequest mRequest;

        private Parameters mParameters;
        private Headers mHeaders;
        private Cookies mCookies;
        private Attributes mAttributes;
        
        Request(HttpServletRequest request) {
            mRequest = request;
        }

        public String getProtocol() {
            return mRequest.getProtocol();
        }

        public String getScheme() {
            return mRequest.getScheme();
        }

        public String getServerName() {
            return mRequest.getServerName();
        }

        public int getServerPort() {
            return mRequest.getServerPort();
        }

        public String getRemoteAddr() {
            return mRequest.getRemoteAddr();
        }

        public String getRemoteHost() {
            return mRequest.getRemoteHost();
        }

        public String getAuthType() {
            return mRequest.getAuthType();
        }

        public String getMethod() {
            return mRequest.getMethod();
        }

        public String getRequestURI() {
            return mRequest.getRequestURI();
        }

        public String getContextPath() {
            return mRequest.getContextPath();
        }

        public String getServletPath() {
            return mRequest.getServletPath();
        }

        public String getPathInfo() {
            return mRequest.getPathInfo();
        }

        public String getQueryString() {
            return mRequest.getQueryString();
        }
        
        public String getRemoteUser() {
            return mRequest.getRemoteUser();
        }

        public String getRequestedSessionId() {
            return mRequest.getRequestedSessionId();
        }

        public boolean isRequestedSessionIdValid() {
            return mRequest.isRequestedSessionIdValid();
        }

        public HttpContext.Parameters getParameters() {
            if (mParameters == null) {
                mParameters = new Parameters(mRequest);
            }
            return mParameters;
        }

        public HttpContext.Headers getHeaders() {
            if (mHeaders == null) {
                mHeaders = new Headers(mRequest);
            }
            return mHeaders;
        }

        public HttpContext.Cookies getCookies() {
            if (mCookies == null) {
                mCookies = new Cookies(mRequest);
            }
            return mCookies;
        }

        public HttpContext.Attributes getAttributes() {
            if (mAttributes == null) {
                mAttributes = new Attributes(mRequest);
            }
            return mAttributes;
        }
    }

    private static class Parameters implements HttpContext.Parameters {

        private final HttpServletRequest mRequest;

        private HttpContext.StringArrayList mNames;
        
        Parameters(HttpServletRequest request) {
            mRequest = request;
        }
        
        public HttpContext.ParameterValues get(String name) {
            String value = mRequest.getParameter(name);
            return (value == null) ? null :
                new ParameterValues(mRequest, name, value);
        }
        
        public HttpContext.StringArrayList getNames() {
            if (mNames == null) {
                mNames = new HttpContext.StringArrayList();
                Enumeration enum = mRequest.getParameterNames();
                while (enum.hasMoreElements()) {
                    mNames.add(enum.nextElement());
                }
            }
            return mNames;
        }
    }

    private static class ParameterValues extends AbstractList 
        implements HttpContext.ParameterValues
    {
        private final HttpServletRequest mRequest;
        private final String mName;
        private final String mValue;
        private String[] mParameterValues;

        ParameterValues(HttpServletRequest request,
                        String name, String value) {
            mRequest = request;
            mName = name;
            mValue = value;
        }

        public Object get(int index) {
            return new Parameter(getParameterValues()[index]);
        }

        public int size() {
            return getParameterValues().length;
        }

        public Integer getAsInteger() {
            try {
                return new Integer(mValue);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        public String getAsString() {
            return mValue;
        }

        public String toString() {
            return mValue;
        }

        private String[] getParameterValues() {
            if (mParameterValues == null) {
                mParameterValues = mRequest.getParameterValues(mName);
            }
            return mParameterValues;
        }
    }

    private static class Parameter implements HttpContext.Parameter {

        private final String mValue;

        Parameter(String value) {
            mValue = value;
        }

        public Integer getAsInteger() {
            try {
                return new Integer(mValue);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        public String getAsString() {
            return mValue;
        }

        public String toString() {
            return mValue;
        }
    }

    private static class Headers implements HttpContext.Headers {

        private final HttpServletRequest mRequest;
        private HttpContext.StringArrayList mNames;
        
        Headers(HttpServletRequest request) {
            mRequest = request;
        }
        
        public HttpContext.Header get(String name) {
            return new Header(mRequest, name);
        }
        
        public HttpContext.StringArrayList getNames() {
            if (mNames == null) {
                mNames = new HttpContext.StringArrayList();
                Enumeration enum = mRequest.getHeaderNames();
                while (enum.hasMoreElements()) {
                    mNames.add(enum.nextElement());
                }
            }
            return mNames;
        }
    }

    private static class Header implements HttpContext.Header {

        private final HttpServletRequest mRequest;
        private final String mName;

        Header(HttpServletRequest request, String name) {
            mRequest = request;
            mName = name;
        }

        public Integer getAsInteger() {
            try {
                int value = mRequest.getIntHeader(mName);
                if (value >= 0) {
                    return new Integer(value);
                }
            }
            catch (NumberFormatException e) {
            }
            return null;
        }

        public Date getAsDate() {
            try {
                long date = mRequest.getDateHeader(mName);
                if (date >= 0) {
                    return new Date(date);
                }
            }
            catch (IllegalArgumentException e) {
            }
            return null;
        }

        public String getAsString() {
            return mRequest.getHeader(mName);
        }

        public String toString() {
            return mRequest.getHeader(mName);
        }
    }

    private static class Cookies implements HttpContext.Cookies {

        private final HttpServletRequest mRequest;

        Cookies(HttpServletRequest request) {
            mRequest = request;
        }
        
        public Cookie get(String name) {
            Cookie[] cookies = mRequest.getCookies();
            for (int i=cookies.length - 1; i >= 0; i--) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
            return null;
        }
        
        public Cookie[] getAll() {
            return mRequest.getCookies();
        }
    }

    private static class Attributes implements HttpContext.Attributes {

        private final HttpServletRequest mRequest;
        private HttpContext.StringArrayList mNames;
        
        Attributes(HttpServletRequest request) {
            mRequest = request;
        }
        
        public Object get(String name) {
            return mRequest.getAttribute(name);
        }
        
        public HttpContext.StringArrayList getNames() {
            if (mNames == null) {
                mNames = new HttpContext.StringArrayList();
                Enumeration enum = mRequest.getAttributeNames();
                while (enum.hasMoreElements()) {
                    mNames.add(enum.nextElement());
                }
            }
            return mNames;
        }
    }
}
