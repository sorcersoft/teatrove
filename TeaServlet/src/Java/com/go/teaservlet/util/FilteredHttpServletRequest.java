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

package com.go.teaservlet.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

/******************************************************************************
 * A HttpServletRequest wrapper that passes all calls to an internal
 * HttpServletRequest. This class is designed for subclasses to override or
 * hook into the behavior of a HttpServletRequest instance.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/03/20 <!-- $-->
 * @deprecated Version 2.3 of the Servlet API contains
 * HttpServletRequestWrapper
 */
public class FilteredHttpServletRequest implements HttpServletRequest {
    protected final HttpServletRequest mRequest;

    public FilteredHttpServletRequest(HttpServletRequest request) {
        mRequest = request;
    }

    // ServletRequest defined methods

    public Object getAttribute(String name) {
        return mRequest.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return mRequest.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return mRequest.getCharacterEncoding();
    }

    public void setCharacterEncoding(String encoding)
        throws UnsupportedEncodingException
    {
        mRequest.setCharacterEncoding(encoding);
    }

    public int getContentLength() {
        return mRequest.getContentLength();
    }

    public String getContentType() {
        return mRequest.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return mRequest.getInputStream();
    }

    public String getParameter(String name) {
        return mRequest.getParameter(name);
    }

    public Enumeration getParameterNames() {
        return mRequest.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return mRequest.getParameterValues(name);
    }

    public Map getParameterMap() {
        return mRequest.getParameterMap();
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

    public BufferedReader getReader() throws IOException {
        return mRequest.getReader();
    }

    public String getRemoteAddr() {
        return mRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return mRequest.getRemoteHost();
    }

    public void setAttribute(String key, Object obj) {
        mRequest.setAttribute(key, obj);
    }

    /**
     * @deprecated
     */
    public String getRealPath(String path) {
        return mRequest.getRealPath(path);
    }

    // HttpServletRequest defined methods

    public String getAuthType() {
        return mRequest.getAuthType();
    }

    public Cookie[] getCookies() {
        return mRequest.getCookies();
    }

    public long getDateHeader(String name) {
        return mRequest.getDateHeader(name);
    }

    public String getHeader(String name) {
        return mRequest.getHeader(name);
    }

    public Enumeration getHeaderNames() {
        return mRequest.getHeaderNames();
    }

    public int getIntHeader(String name) {
        return mRequest.getIntHeader(name);
    }

    public String getMethod() {
        return mRequest.getMethod();
    }

    public String getPathInfo() {
        return mRequest.getPathInfo();
    }

    public String getPathTranslated() {
        return mRequest.getPathTranslated();
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

    public String getRequestURI() {
        return mRequest.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return mRequest.getRequestURL();
    }

    public String getServletPath() {
        return mRequest.getServletPath();
    }

    public HttpSession getSession(boolean create) {
        return mRequest.getSession(create);
    }

    public HttpSession getSession() {
        return mRequest.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return mRequest.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return mRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return mRequest.isRequestedSessionIdFromURL();
    }

    public void removeAttribute(String name) {
        mRequest.removeAttribute(name);
    }

    public String getContextPath() {
        return mRequest.getContextPath();
    }

    public boolean isSecure() {
        return mRequest.isSecure();
    }

    public boolean isUserInRole(String role) {
        return mRequest.isUserInRole(role);
    }

    public java.security.Principal getUserPrincipal() {
        return mRequest.getUserPrincipal();
    }

    public Enumeration getHeaders(String name) {
        return mRequest.getHeaders(name);
    }

    public Locale getLocale() {
        return mRequest.getLocale();
    }

    public Enumeration getLocales() {
        return mRequest.getLocales();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return mRequest.getRequestDispatcher(path);
    }

    /**
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {
        return mRequest.isRequestedSessionIdFromUrl();
    }

    public String toString() {
        return mRequest.toString();
    }
}
