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

import java.util.Enumeration;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

/******************************************************************************
 * A ServletContext wrapper that passes all calls to an internal
 * ServletContext. This class is designed for subclasses to override or
 * hook into the behavior of a ServletContext instance.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 10/09/01 <!-- $-->
 */
public class FilteredServletContext implements ServletContext {
    protected final ServletContext mContext;

    public FilteredServletContext(ServletContext context) {
        mContext = context;
    }

    public void log(String message) {
        mContext.log(message);
    }

    public void log(String message, Throwable t) {
        mContext.log(message, t);
    }

    public String getRealPath(String path) {
        return mContext.getRealPath(path);
    }

    public String getMimeType(String file) {
        return mContext.getMimeType(file);
    }

    public String getServerInfo() {
        return mContext.getServerInfo();
    }

    public Object getAttribute(String name) {
        return mContext.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        mContext.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        mContext.removeAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return mContext.getAttributeNames();
    }

    public ServletContext getContext(String uripath) {
        return mContext.getContext(uripath);
    }

    public RequestDispatcher getRequestDispatcher(String uripath) {
        return mContext.getRequestDispatcher(uripath);
    }

    public int getMajorVersion() {
        return mContext.getMajorVersion();
    }

    public int getMinorVersion() {
        return mContext.getMinorVersion();
    }

    public Set getResourcePaths(String path) {
        return mContext.getResourcePaths(path);
    }

    public URL getResource(String uripath) throws MalformedURLException {
        return mContext.getResource(uripath);
    }

    public InputStream getResourceAsStream(String uripath) {
        return mContext.getResourceAsStream(uripath);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return mContext.getNamedDispatcher(name);
    }

    public Enumeration getInitParameterNames() {
        return mContext.getInitParameterNames();
    }

    public String getInitParameter(String name) {
        return mContext.getInitParameter(name);
    }

    public String getServletContextName() {
        return mContext.getServletContextName();
    }

    /**
     * @deprecated
     */
    public void log(Exception e, String message) {
        log(message, e);
    }

    /**
     * @deprecated
     */
    public Servlet getServlet(String name) throws ServletException {
        return mContext.getServlet(name);
    }

    /**
     * @deprecated
     */
    public Enumeration getServlets() {
        return mContext.getServlets();
    }

    /**
     * @deprecated
     */
    public Enumeration getServletNames() {
        return mContext.getServletNames();
    }
}
