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
import java.util.*;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/10/09 <!-- $-->
 */
public class HttpUtils {
    private static SimpleDateFormat cCookieFormatter;

    static {
        cCookieFormatter =
            new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
        cCookieFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     */
    public static String readLine(InputStream in, byte[] buffer)
        throws IllegalArgumentException, IOException
    {
        return readLine(in, buffer, -1);
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     * @throws LineTooLongException when line is longer than the limit
     */
    public static String readLine(InputStream in, byte[] buffer, int limit)
        throws IllegalArgumentException, IOException, LineTooLongException
    {
        if (!in.markSupported()) {
            throw new IllegalArgumentException
                ("InputStream doesn't support marking: " + in.getClass());
        }

        String line = null;

        int cursor = 0;
        int len = buffer.length;

        int count = 0;
        int c;
    loop:
        while ((c = in.read()) >= 0) {
            if (limit >= 0 && ++count > limit) {
                throw new LineTooLongException(limit);
            }

            switch (c) {
            case '\r':
                in.mark(1);
                if (in.read() != '\n') {
                    in.reset();
                }
                // fall through
            case '\n':
                if (line == null && cursor == 0) {
                    return "";
                }
                break loop;
            default:
                if (cursor >= len) {
                    if (line == null) {
                        line = new String(buffer, "8859_1");
                    }
                    else {
                        line = line.concat(new String(buffer, "8859_1"));
                    }
                    cursor = 0;
                }
                buffer[cursor++] = (byte)c;
            }
        }

        if (cursor > 0) {
            if (line == null) {
                line = new String(buffer, 0, cursor, "8859_1");
            }
            else {
                line = line.concat(new String(buffer, 0, cursor, "8859_1"));
            }
        }

        return line;
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     */
    public static String readLine(InputStream in, char[] buffer)
        throws IllegalArgumentException, IOException
    {
        return readLine(in, buffer, -1);
    }

    /**
     * Reads a line from an HTTP InputStream, using the given buffer for
     * temporary storage.
     *
     * @param in stream to read from
     * @param buffer temporary buffer to use
     * @throws IllegalArgumentException if the given InputStream doesn't
     * support marking
     * @throws LineTooLongException when line is longer than the limit
     */
    public static String readLine(InputStream in, char[] buffer, int limit)
        throws IllegalArgumentException, IOException, LineTooLongException
    {
        if (!in.markSupported()) {
            throw new IllegalArgumentException
                ("InputStream doesn't support marking: " + in.getClass());
        }

        String line = null;

        int cursor = 0;
        int len = buffer.length;

        int count = 0;
        int c;
    loop:
        while ((c = in.read()) >= 0) {
            if (limit >= 0 && ++count > limit) {
                throw new LineTooLongException(limit);
            }

            switch (c) {
            case '\r':
                in.mark(1);
                if (in.read() != '\n') {
                    in.reset();
                }
                // fall through
            case '\n':
                if (line == null && cursor == 0) {
                    return "";
                }
                break loop;
            default:
                if (cursor >= len) {
                    if (line == null) {
                        line = new String(buffer);
                    }
                    else {
                        line = line.concat(new String(buffer));
                    }
                    cursor = 0;
                }
                buffer[cursor++] = (char)c;
            }
        }

        if (cursor > 0) {
            if (line == null) {
                line = new String(buffer, 0, cursor);
            }
            else {
                line = line.concat(new String(buffer, 0, cursor));
            }
        }

        return line;
    }

    /**
     * Returns a description of the given HTTP status code or the empty
     * String if code isn't known.
     *
     * @see javax.servlet.http.HttpServletResponse
     */
    public static String decodeStatusCode(int sc) {
        switch (sc) {
        case HttpServletResponse.SC_CONTINUE:
            return "Continue";
        case HttpServletResponse.SC_SWITCHING_PROTOCOLS:
            return "Switching Protocols";
        case HttpServletResponse.SC_OK:
            return "OK";
        case HttpServletResponse.SC_CREATED:
            return "Created";
        case HttpServletResponse.SC_ACCEPTED:
            return "Accepted";
        case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION:
            return "Non-Authoritative Information";
        case HttpServletResponse.SC_NO_CONTENT:
            return "No Content";
        case HttpServletResponse.SC_RESET_CONTENT:
            return "Reset Content";
        case HttpServletResponse.SC_PARTIAL_CONTENT:
            return "Partial Content";
        case HttpServletResponse.SC_MULTIPLE_CHOICES:
            return "Multiple Choices";
        case HttpServletResponse.SC_MOVED_PERMANENTLY:
            return "Moved Permanently";
        case HttpServletResponse.SC_MOVED_TEMPORARILY:
            return "Moved Temporarily";
        case HttpServletResponse.SC_SEE_OTHER:
            return "See Other";
        case HttpServletResponse.SC_NOT_MODIFIED:
            return "Not Modified";
        case HttpServletResponse.SC_USE_PROXY:
            return "Use Proxy";
        case 307:
            return "Temporary Redirect";
        case HttpServletResponse.SC_BAD_REQUEST:
            return "Bad Request";
        case HttpServletResponse.SC_UNAUTHORIZED:
            return "Unauthorized";
        case HttpServletResponse.SC_PAYMENT_REQUIRED:
            return "Payment Required";
        case HttpServletResponse.SC_FORBIDDEN:
            return "Forbidden";
        case HttpServletResponse.SC_NOT_FOUND:
            return "Not Found";
        case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
            return "Method Not Allowed";
        case HttpServletResponse.SC_NOT_ACCEPTABLE:
            return "Not Acceptable";
        case HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
            return "Proxy Authentication Required";
        case HttpServletResponse.SC_REQUEST_TIMEOUT:
            return "Request Timeout";
        case HttpServletResponse.SC_CONFLICT:
            return "Conflict";
        case HttpServletResponse.SC_GONE:
            return "Gone";
        case HttpServletResponse.SC_LENGTH_REQUIRED:
            return "Length Required";
        case HttpServletResponse.SC_PRECONDITION_FAILED:
            return "Precondition Failed";
        case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE:
            return "Request Entity Too Large";
        case HttpServletResponse.SC_REQUEST_URI_TOO_LONG:
            return "Request-URI Too Long";
        case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
            return "Unsupported Media Type";
        case 416:
            return "Requested Range Not Satisfiable";
        case 417:
            return "Expectation Failed";
        case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
            return "Internal Server Error";
        case HttpServletResponse.SC_NOT_IMPLEMENTED:
            return "Not Implemented";
        case HttpServletResponse.SC_BAD_GATEWAY:
            return "Bad Gateway";
        case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
            return "Service Unavailable";
        case HttpServletResponse.SC_GATEWAY_TIMEOUT:
            return "Gateway Timeout";
        case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED:
            return "HTTP Version Not Supported";
        default:
            return "";
        }
    }

    /**
     * Parses a query string or POST data. Keys are strings, and values may be
     * strings or string arrays. The map entries are not stored in any
     * particular order.
     *
     * @param s query string or POST data.
     * @param map results stored here.
     */
    public static void parseQueryString(String s, Map map) {
        if (s == null || s.length() == 0) {
            return;
        }

        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(s, "&");

        while (st.hasMoreTokens()) {
            String pair = (String)st.nextToken();
            String key, value;
            int pos = pair.indexOf('=');
            int length = pair.length();

            if (pos < 0) {
                if (length > 0) {
                    key = parseName(pair, sb);
                    value = "";
                }
                else {
                    continue;
                }
            }
            else {
                key = parseName(pair.substring(0, pos), sb);
                value = parseName(pair.substring(pos + 1, length), sb);
            }

            Object obj = map.get(key);
            if (obj == null) {
                map.put(key, value);
            }
            else if (obj instanceof String) {
                map.put(key, new String[]{(String)obj, value});
            }
            else if (obj instanceof String[]) {
                String[] oldVals = (String[])obj;
                int len = oldVals.length;
                String[] newVals = new String[len + 1];
                for (int i=0; i<len; i++) {
                    newVals[i] = oldVals[i];
                }
                newVals[len] = value;
                map.put(key, newVals);
            }
        }
    }

    /**
     * Parse a name or value in a query string.
     *
     * @param s name or value.
     * @param sb Temporary StringBuffer.
     */
    private static String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '+':
                sb.append(' ');
                break;

            case '%':
                try {
                    sb.append((char)Integer.parseInt(s.substring(i+1,i+3),16));
                    i += 2;
                }
                catch (NumberFormatException e) {
                    sb.append(c);
                }
                catch (StringIndexOutOfBoundsException e) {
                    String rest = s.substring(i);
                    sb.append(rest);
                    if (rest.length() == 2) {
                        i++;
                    }
                }
                break;

            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Parse a HTTP header whose value contains multiple names and/or
     * name-value pairs, each delimited by a ';' or ',' and optionally
     * separated by space characters.
     *
     * <p>Exmaple:
     * <pre>
     * form-data; name="file1"; filename="test"
     * </pre>
     * is parsed to:
     * <pre>
     * {
     *     {"form-data", ""},
     *     {"name", "\"file1\""},
     *     {"filename", "\"test\""}
     * }
     * </pre>
     *
     * @param headerValue if null, an empty array is returned
     * @param delimiters usually ";", "," or ";,"
     * @return String[n][2]
     */
    public static String[][] parseHeaderFields(String headerValue,
                                               String delimiters) {
        if (headerValue == null) {
            return new String[0][0];
        }

        List entries = new ArrayList();

        int length = headerValue.length();
        StringBuffer name = new StringBuffer();
        StringBuffer value = new StringBuffer();

        // 0 == separator, 1 == name, 2 == value
        int scanState = 1;

        for (int i=0; i<length; i++) {
            char c = headerValue.charAt(i);
            switch (scanState) {
            default:
                if (c == ' ') {
                    scanState = 1;
                    break;
                }
                // Fall through.
            case 1:
                if (c == '=') {
                    scanState = 2;
                    break;
                }
                else if (delimiters.indexOf(c) < 0) {
                    name.append(c);
                    break;
                }
                // Fall through.
            case 2:
                if (delimiters.indexOf(c) >= 0) {
                    scanState = 0;
                    entries.add(new String[]
                                {name.toString(), value.toString()});
                    name.setLength(0);
                    value.setLength(0);
                }
                else {
                    value.append(c);
                }
                break;
            }
        }

        if (scanState == 2 || name.length() > 0) {
            entries.add(new String[] {name.toString(), value.toString()});
        }

        return (String[][])entries.toArray(new String[entries.size()][]);
    }

    /**
     * Adds a value to a parameter map. If another value is already present,
     * both values will be put into the map. Values may be single Strings or
     * arrays of Strings. Any added value will be stored in a String array.
     */
    public static void addToParameterMap(Map parameters,
                                         Object key, Object newValue) {
        if (newValue == null) {
            return;
        }

        Object oldValue = parameters.get(key);

        if (oldValue == null) {
            if (!(newValue instanceof String[])) {
                newValue = new String[]{newValue.toString()};
            }
            parameters.put(key, newValue);
            return;
        }

        // Four possible combinations need to be merged:
        //
        // String   + String
        // String   + String[]
        // String[] + String
        // String[] + String[]

        String[] strings;
        
        if (oldValue instanceof String) {
            String oldString = (String)oldValue;
            if (newValue instanceof String) {
                strings = new String[] {oldString, (String)newValue};
            }
            else {
                String[] newStrings = (String[])newValue;
                strings = new String[1 + newStrings.length];
                strings[0] = oldString;
                for (int i=0; i<newStrings.length; i++) {
                    strings[i + 1] = newStrings[i];
                }
            }
        }
        else {
            String[] oldStrings = (String[])oldValue;
            if (newValue instanceof String) {
                strings = new String[oldStrings.length + 1];
                int i;
                for (i=0; i<oldStrings.length; i++) {
                    strings[i] = oldStrings[i];
                }
                strings[i] = (String)newValue;
            }
            else {
                String[] newStrings = (String[])newValue;
                strings = new String[oldStrings.length + newStrings.length];
                
                int i;
                for (i=0; i<oldStrings.length; i++) {
                    strings[i] = oldStrings[i];
                }
                
                for (int j=0; j<newStrings.length; j++) {
                    strings[i + j] = newStrings[j];
                }
            }
        }

        parameters.put(key, strings);
    }

    /**
     * Ensures the given parameter map only consists of String array values.
     * Otherwise, it converts single entries to one element String arrays.
     */
    public static void ensureOnlyStringArrayValues(Map parameters) {
        // Ensure parameter map only consists of String arrays.
        Iterator it = parameters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Object value = entry.getValue();
            if (!(value instanceof String[])) {
                entry.setValue(new String[]{value.toString()});
            }
        }
    }

    /**
     * Cleans up the given path parameter such that all ".." and "." parameters
     * are collapsed. Any servlet that translates a request path into a
     * file path should clean up the path, otherwise a malicious user can
     * easily access files anywhere in the file system.
     * <p>
     * If the system separator character isn't '/', then those separators are
     * converted to '/'. This conversion is applied before searching for the
     * dot patterns, which must be adjacent to '/' characters in order to be
     * collapsed.
     * <p>
     * The path is not URL decoded, because request.getPathInfo() should have
     * already taken care of that. Decoding the path multiple times exposes
     * a way in which a malicious user can still go up a dir. The pattern
     * "..%255c..", when decoded first becomes: "..%5c..". On the second
     * decode, it becomes "..\..". This method will not detect this secret
     * "updir" pattern.
     * <p>
     * For additional security, compare the canonical path to the root
     * directory allowed to serve files. Make sure that the canonical path is
     * within this directory.
     */
    public static String cleanPath(String path) {
        if (path.length() == 0) {
            return path;
        }

        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        
        if (path.indexOf("./") < 0 && path.indexOf("/.") < 0) {
            return path;
        }

        LinkedList pathParts = new LinkedList();

        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreTokens()) {
            String pathPart = st.nextToken();
            if ("..".equals(pathPart)) {
                if (pathParts.size() > 0) {
                    pathParts.removeLast();
                }
                continue;
            }
            else if (".".equals(pathPart)) {
                continue;
            }
            else {
                pathParts.addLast(pathPart);
            }
        }

        StringBuffer newPath = new StringBuffer(path.length());
        Iterator parts = pathParts.iterator();
        while (parts.hasNext()) {
            newPath.append('/');
            newPath.append((String)parts.next());
        }
        if (path.endsWith("/")) {
            newPath.append('/');
        }

        return newPath.toString();
    }

    /**
     * Adds a Cookie to the given HttpHeaderMap.
     *
     * @throws UnsupportedOperationException if cookie version is unsupported
     */
    public static void addCookie(HttpHeaderMap headers, Cookie cookie) {
        StringBuffer valueBuf = new StringBuffer(200);
        int version = cookie.getVersion();
        
        String cookieValue = cookie.getValue();
        if (cookieValue == null) {
            cookieValue = "";
        }

        if (version == 0) {
            valueBuf.append(cookie.getName())
                .append('=')
                .append(cookieValue);
            int maxAge = cookie.getMaxAge();
            if (maxAge >= 0) {
                Date expire = new Date(System.currentTimeMillis() +
                                       (long)maxAge * 1000);
                synchronized (cCookieFormatter) {
                    valueBuf.append("; expires=")
                        .append(cCookieFormatter.format(expire));
                }
            }

            String path = cookie.getPath();
            if (path != null) {
                valueBuf.append("; path=").append(path);
            }
            
            String domain = cookie.getDomain();
            if (domain != null) {
                valueBuf.append("; domain=").append(domain);
            }
            
            if (cookie.getSecure()) {
                valueBuf.append("; secure");
            }
            
            valueBuf.append(';');
            
            headers.add("Set-Cookie", valueBuf.toString());
        }
        else if (version == 1) {
            valueBuf.append(cookie.getName())
                .append('=')
                .append(cookieValue);
            
            String path = cookie.getPath();
            if (path != null) {
                valueBuf.append("; Path=").append(path);
            }

            int maxAge = cookie.getMaxAge();
            if (maxAge >= 0) {
                valueBuf.append("; Max-Age=").append(maxAge);
            }
                        
            String domain = cookie.getDomain();
            if (domain != null) {
                valueBuf.append("; Domain=").append(domain);
            }
            
            if (cookie.getSecure()) {
                valueBuf.append("; Secure");
            }
            
            valueBuf.append("; Version=").append(version);
            
            String comment = cookie.getComment();
            if (comment != null) {
                valueBuf.append("; Comment=").append(comment);
            }

            valueBuf.append(';');
            
            headers.add("Set-Cookie", valueBuf.toString());
        }
        else {
            throw new UnsupportedOperationException
                ("Cookie version not supported: " + version);
        }
    }
}
