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
import javax.servlet.*;
import javax.servlet.http.*;

/******************************************************************************
 * A HttpServletResponse wrapper that passes all calls to an internal
 * HttpServletResponse. This class is designed for subclasses to override or
 * hook into the behavior of a HttpServletResponse instance.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/03/20 <!-- $-->
 * @deprecated Version 2.3 of the Servlet API contains
 * HttpServletResponseWrapper
 */
public class FilteredHttpServletResponse implements HttpServletResponse {
    protected final HttpServletResponse mResponse;

    public FilteredHttpServletResponse(HttpServletResponse response) {
        mResponse = response;
    }

    // ServletResponse defined methods

    public String getCharacterEncoding() {
        return mResponse.getCharacterEncoding();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return mResponse.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        return mResponse.getWriter();
    }

    public void setContentLength(int length) {
        mResponse.setContentLength(length);
    }

    public void setContentType(String type) {
        mResponse.setContentType(type);
    }

    // HttpServletResponse defined methods

    public void addCookie(Cookie cookie) {
        mResponse.addCookie(cookie);
    }

    public boolean containsHeader(String name) {
        return mResponse.containsHeader(name);
    }

    public String encodeURL(String url) {
        return mResponse.encodeURL(url);
    }

    public String encodeRedirectURL(String url) {
        return mResponse.encodeRedirectURL(url);
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String url) {
        return mResponse.encodeUrl(url);
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String url) {
        return mResponse.encodeRedirectUrl(url);
    }

    public void sendError(int sc, String msg) throws IOException {
        mResponse.sendError(sc, msg);
    }

    public void sendError(int sc) throws IOException {
        mResponse.sendError(sc);
    }

    public void sendRedirect(String location) throws IOException {
        mResponse.sendRedirect(location);
    }

    public void setDateHeader(String name, long date) {
        mResponse.setDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
        mResponse.setHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        mResponse.setIntHeader(name, value);
    }

    public void setStatus(int sc) {
        mResponse.setStatus(sc);
    }

    public void setBufferSize(int size)  {
        mResponse.setBufferSize(size);
    }

    public void setLocale(Locale locale) {
        mResponse.setLocale(locale);
    }

    public void addDateHeader(String name, long date) {
        mResponse.addDateHeader(name,date);
    }

    public void addIntHeader(String name, int value) {
        mResponse.addIntHeader(name,value);
    }

    public void flushBuffer() throws IOException {
        mResponse.flushBuffer();
    }

    public void resetBuffer() {
        mResponse.resetBuffer();
    }

    public int getBufferSize() {
        return mResponse.getBufferSize();
    }

    public boolean isCommitted() {
        return mResponse.isCommitted();
    }

    public void reset() {
        mResponse.reset();
    }

    public void addHeader(String name, String value) {
        mResponse.addHeader(name, value);
    }

    public Locale getLocale() {
        return mResponse.getLocale();
    }

    /**
     * @deprecated
     */
    public void setStatus(int sc, String msg) {
        mResponse.setStatus(sc, msg);
    }

    public String toString() {
        return mResponse.toString();
    }
}
