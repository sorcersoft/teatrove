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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

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
public interface HttpContext extends com.go.tea.runtime.UtilityContext {

    /**
     * Gets an object that contains all the request information from the
     * client.
     */
    public Request getRequest();

    /**
     * Gets an object that contains all the request information from the
     * client and all the parameters are converted using the specified
     * character encoding.
     *
     * @param encoding the character encoding
     */
    public Request getRequest(String encoding);

    /**
     * Sets the response's HTTP status code, like 200 for OK or 404 for not
     * found.
     *
     * @param code the status code int
     */
    public void setStatus(int code);

    /**
     * Causes an error page to be generated using the given status code.
     * Further template processing is aborted and any output already
     * provided by the template is not sent.
     *
     * @param code the error code int
     */
    public void sendError(int code)
    throws AbortTemplateException, IOException;

    /**
     * Causes an error page to be generated using the given status code and
     * message. Further template processing is aborted and any output already
     * provided by the template is not sent.
     *
     * @param code the error code int
     * @param message the error message
     */
    public void sendError(int code, String message)
    throws AbortTemplateException, IOException;

    /**
     * Creates a response that forces the client to redirect to the given URL.
     * Further template processing is aborted and any output already provided
     * by the template is not sent.
     *
     * @param url the url to redirect to
     */
    public void sendRedirect(String url)
    throws AbortTemplateException, IOException;

    /**
     * Sets the MIME type of the page, like "text/html" or "text/plain". If
     * another character encoding is desired, for internationalization, append
     * charset property like this: "text/html; charset=iso-8859-1".
     *
     * @param type the MIME type of the page
     */
    public void setContentType(String type)
    throws UnsupportedEncodingException, IOException;

    /**
     * An advanced function that sets the string value of an
     * arbitrary HTTP response header.
     *
     * @param name the name of the header
     * @param value the string value of the header
     *
     * @expert
     */
    public void setHeader(String name, String value);

    /**
     * An advanced function that sets the integer value of an
     * arbitrary HTTP response header.
     *
     * @param name the name of the header
     * @param value the int value of the header
     *
     * @expert
     */
    public void setHeader(String name, int value);

    /**
     * An advanced function that sets the date value of an
     * arbitrary HTTP response header.
     *
     * @param name the name of the header
     * @param value the date value of the header
     *
     * @expert
     */
    public void setHeader(String name, Date value);

    /**
     * Encodes the given string so that it can be safely used in a URL. For
     * example, '?' is encoded to '%3f'.
     *
     * @param str the string to encode
     */
    public String encodeParameter(String str);

    /**
     * Tests if a file at the given path exists. If the servlet that is running
     * the template has a root directory, it is used to determine the physical
     * file path. If the servlet does not have a root directory set, it is
     * assumed to be the root of the file system. If the path given to this
     * function does not lead with a slash, the path is relative to the
     * pathInfo variable from the request.
     *
     * @param path the name of the file to test
     */
    public boolean fileExists(String path) throws IOException;

    /**
     * Inserts the contents of the given file into the page output or not at
     * all if the file does not exist or cannot be read. This function inserts
     * the raw bytes from the file - no character conversion is applied.
     * Consider using {@link readFile} in order to use character conversion.
     * <p>
     * If the servlet that is running the template has a root directory, it is 
     * used to determine the physical file path. If the servlet does not have
     * a root directory set, it is assumed to be the root of the file system.
     * If the path given to this function does not lead with a slash, the path
     * is relative to the pathInfo variable from the request.
     *
     * @param path the name of the file to insert
     */    
    public void insertFile(String path) throws IOException;

    /**
     * Reads and returns the contents of the given file. If the file does not
     * exist or cannot be read, an empty string is returned. This function
     * differs from {@link insertFile} in that the character conversion is
     * applied and the file's contents are returned.
     * <p>
     * If the servlet that is running the template has a root directory, it is 
     * used to determine the physical file path. If the servlet does not have
     * a root directory set, it is assumed to be the root of the file system.
     * If the path given to this function does not lead with a slash, the path
     * is relative to the pathInfo variable from the request.
     *
     * @param path the name of the file to insert
     */    
    public String readFile(String path) throws IOException;

    /**
     * Reads and returns the contents of the given file. If the file does not
     * exist or cannot be read, an empty string is returned. This function
     * differs from {@link insertFile} in that the character conversion is
     * applied and the file's contents are returned.
     * <p>
     * If the servlet that is running the template has a root directory, it is 
     * used to determine the physical file path. If the servlet does not have
     * a root directory set, it is assumed to be the root of the file system.
     * If the path given to this function does not lead with a slash, the path
     * is relative to the pathInfo variable from the request.
     *
     * @param path the name of the file to insert
     * @param encoding character encoding
     */    
    public String readFile(String path, String encoding) throws IOException;

    /**
     * Tests if a resource at the given URL exists. If the URL has no protocol
     * specified, then a relative lookup is performed. If the relative URL
     * begins with a slash, the lookup is performed with an absolute path on
     * the this server. If the relative URL does not begin with a slash, then
     * the lookup is performed with a relative path.
     *
     * @param url the resource URL
     */
    public boolean URLExists(String URL) throws IOException;

    /**
     * Inserts the contents of the resource at the given URL into the page
     * output. If the resource does not exist or cannot be read, nothing is
     * inserted. The given URL can be absolute or relative. This function
     * inserts the raw bytes from the URL - no character conversion is applied.
     * Consider using {@link readURL} in order to use character conversion.
     *
     * @param url the resource URL
     */
    public void insertURL(String URL) throws IOException;

    /**
     * Reads and returns the contents of the given URL. If the URL does not
     * exist or cannot be read, an empty string is returned. This function
     * differs from {@link insertURL} in that the character conversion is
     * applied and the resource's contents are returned.
     *
     * @param path the name of the file to insert
     */    
    public String readURL(String URL) throws IOException;

    /**
     * Reads and returns the contents of the given URL. If the URL does not
     * exist or cannot be read, an empty string is returned. This function
     * differs from {@link insertURL} in that the character conversion is
     * applied and the resource's contents are returned.
     *
     * @param path the name of the file to insert
     * @param encoding character encoding
     */    
    public String readURL(String URL, String encoding) throws IOException;

    /**
     * Requests to check, insert, or read URLs will timeout if the remote
     * hosts doesn't respond in time. Call this function to explicitly set
     * the timeout value.
     *
     * @param timeout max time to wait for URL operation to complete, in
     * milliseconds
     */
    public void setURLTimeout(long timeout);

    /**
     * @hidden
     * If set to true, output may be redirected to an alternate destination,
     * otherwise, output is forced to the client.
     */
    public void overrideOutput(boolean overridePermitted); 

    /**
     * @hidden
     */
    public void stealOutput(OutputReceiver receiver, Substitution s) 
        throws Exception;

    /**************************************************************************
     * The Request interface provides access to the data that is passed to an 
     * HTTP servlet.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Request {

        /**
         * Returns the name and version of the protocol the request uses in 
         * the form protocol/majorVersion.minorVersion, for example, 
         * HTTP/1.1. 
         */
        public String getProtocol();

        /**
         * Returns the name of the scheme used to make this request, for 
         * example, http, https, or ftp.
         */
        public String getScheme();

        /**
         * Returns the host name of the server that received the request. 
         */
        public String getServerName();

        /**
         * Returns the port number on which this request was received. 
         */
        public int getServerPort();

        /**
         * Returns the Internet Protocol (IP) address of the client that 
         * sent the request. 
         */
        public String getRemoteAddr();

        /**
         * Returns the fully qualified name of the client that sent the 
         * request. 
         */
        public String getRemoteHost();

        /**
         * Returns the name of the authentication scheme the server uses, 
         * for example, "BASIC" or "SSL," or null if the server does not 
         * have an authentication scheme. 
         */
        public String getAuthType();

        /**
         * Returns the name of the HTTP method with which this request was 
         * made, for example, GET, POST, or PUT. The returned String is the 
         * same as the value of the CGI variable REQUEST_METHOD.
         */
        public String getMethod();

        /**
         * Returns the part of this request's URL from the protocol
         * name up to the query string in the first line of the HTTP request.
         * For example:
         *
         * <blockquote>
         * <table>
         * <tr align=left><th>First line of HTTP request<th>
         * <th>Returned Value
         * <tr><td>POST /some/path.html HTTP/1.1<td><td>/some/path.html
         * <tr><td>GET http://foo.bar/a.html HTTP/1.0
         * <td><td>http://foo.bar/a.html
         * <tr><td>HEAD /xyz?a=b HTTP/1.1<td><td>/xyz
         * </table>
         * </blockquote>
         */
        public String getRequestURI();

        /**
         * Returns the portion of the request URI that indicates the context 
         * of the request. The context path always comes first in a request 
         * URI. The path starts with a "/" character but does not end with a 
         * "/" character. 
         */
        public String getContextPath();

        /**
         * Returns the part of this request's URL that calls the servlet. 
         * This includes either the servlet name or a path to the servlet, 
         * but does not include any extra path information or a query string. 
         */
        public String getServletPath();

        /**
         * Returns any extra path information associated with the URL the 
         * client sent when it made this request. The extra path information
         * follows the servlet path but precedes the query string.
         * Returns null if there was no extra path information. 
         */
        public String getPathInfo();

        /**
         * Returns the query string that is contained in the request URL 
         * after the path.  Returns null if the URL does not 
         * have a query string. 
         */
        public String getQueryString();
        

        /**
         * Returns the login of the user making this request, if the user has 
         * been authenticated, or null if the user has not been authenticated.
         * Whether the user name is sent with each subsequent request depends
         * on the browser and type of authentication. 
         */
        public String getRemoteUser();

        /**
         * Returns the session ID specified by the client. This may not be 
         * the same as the ID of the actual session in use. For example, if 
         * the request specified an old (expired) session ID and the server 
         * has started a new session, this method gets a new session with a 
         * new ID. If the request did not specify a session ID, this method 
         * returns null.
         */
        public String getRequestedSessionId();

        /**
         * Checks whether the requested session ID is still valid.
         */
        public boolean isRequestedSessionIdValid();

        /**
         * Returns a Parameters object containing the request parameters.
         */
        public Parameters getParameters();

        /**
         * Returns a Headers object containing the request headers.
         */
        public Headers getHeaders();

        /**
         * Returns a Cookies object containing the request cookies.
         */
        public Cookies getCookies();

        /**
         * Returns an Attributes object containing the request attributes.
         */
        public Attributes getAttributes();
    }

    /**************************************************************************
     * The Parameters interface provides access to the request parameters.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Parameters {

        /**
         * Returns a ParameterValues object containing all of the values for
         * the named parameter.
         *
         * @param name the name of the parameter
         */
        public ParameterValues get(String name);
        
        /**
         * Returns a StringArrayList containing the names of the parameters 
         * contained in the request. 
         */
        public StringArrayList getNames();
    }

    /**************************************************************************
     * The ParameterValues interface provides access to the request parameter 
     * values.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface ParameterValues extends List {

        /** The element type for this List is Parameter.class */
        public static final Class ELEMENT_TYPE = Parameter.class;

        /**
         * Returns the parameter value as an Integer.
         */
        public Integer getAsInteger();

        /**
         * Returns the parameter value as a String.
         */
        public String getAsString();

        public String toString();
    }

    /**************************************************************************
     * The Parameter interface provides access to a request parameter 
     * value.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Parameter {

        /**
         * Returns the parameter value as an Integer.
         */
        public Integer getAsInteger();

        /**
         * Returns the parameter value as a String.
         */
        public String getAsString();

        public String toString();
    }

    /**************************************************************************
     * The Headers interface provides access to the request headers.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Headers {

        /**
         * Returns a Header object containing the value of
         * the named Header.
         *
         * @param name the name of the header
         */
        public Header get(String name);
        
        /**
         * Returns a StringArrayList containing the names of the headers
         * contained in the request. 
         */
        public StringArrayList getNames();
    }

    /**************************************************************************
     * The Header interface provides access to a request header 
     * value.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Header {

        /**
         * Returns the parameter value as an Integer.
         */
        public Integer getAsInteger();

        /**
         * Returns the parameter value as a String.
         */
        public String getAsString();

        /**
         * Returns the parameter value as a Date.
         */
        public Date getAsDate();

        public String toString();
    }

    /**************************************************************************
     * The Cookies interface provides access to the request cookies.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Cookies {

        /**
         * Returns a javax.servlet.http.Cookie object containing the value of
         * the named Cookie.
         *
         * @param name the name of the cookie
         */
        public Cookie get(String name);

        /**
         * Returns an array of javax.servlet.http.Cookie representing all
         * of the request's cookies. 
         */    
        public Cookie[] getAll();
    }

    /**************************************************************************
     * The Attributes interface provides access to the request attributes.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public interface Attributes {

        /**
         * Returns the value of the named attribute.
         *
         * @param name the name of the attribute
         */
        public Object get(String name);

        /**
         * Returns a StringArrayList containing the names of the attributes
         * contained in the request. 
         */
        public StringArrayList getNames();
    }

    /**************************************************************************
     * An ArrayList of Strings.
     *
     * @author Reece Wilton
     * @version
     * <!--$$Revision$-->, <!--$$JustDate:-->  2/25/02 <!-- $-->
     */
    public static class StringArrayList extends ArrayList {

        /** The element type is String */
        public static final Class ELEMENT_TYPE = String.class;
        
        StringArrayList() {
        }
    }
}
