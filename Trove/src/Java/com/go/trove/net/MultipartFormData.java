/* ====================================================================
 * Trove - Copyright (c) 1997-2001 Walt Disney Internet Group
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
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import com.go.trove.net.HttpHeaderMap;
import com.go.trove.io.FastBufferedInputStream;

/******************************************************************************
 * Utility class for reading multipart/form-data encoded HTML forms. This
 * "experimental" technique is described in
 * <a href="http://www.cis.ohio-state.edu/htbin/rfc/rfc1867.html">RFC1867</a>,
 * "Form-based File Upload in HTML".
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public class MultipartFormData extends HttpServletRequestWrapper {
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String BOUNDARY = "boundary";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String NAME = "name";
    private static final String FILENAME = "filename";

    private static final int NO_BOUNDARY = 1;
    private static final int START_BOUNDARY = 2;
    private static final int END_BOUNDARY = 3;

    private ServletInputStream mIn;
    private MultiInputStream mMultiIn;
    private byte[] mBoundary;
    private HttpHeaderMap mHeaders;
    private transient String[][] mDispositionFields;

    /**
     * @throws IllegalArgumentException if the request content type doesn't
     * describe a multipart/form-data encoded message.
     */
    public MultipartFormData(HttpServletRequest request)
        throws IOException, IllegalArgumentException
    {
        super(request);

        String contentType = readContentTypeAndBoundary();

        if (contentType == null) {
            throw new IllegalArgumentException("No content type set");
        }

        if (!MULTIPART_CONTENT_TYPE.equals(contentType)) {
            throw new IllegalArgumentException
                ("Content type of request not " + MULTIPART_CONTENT_TYPE +
                 ": " + contentType);
        }

        if (mBoundary == null) {
            throw new IllegalArgumentException
                ("No boundary set: " + contentType);
        }

        if (!(mIn = request.getInputStream()).markSupported()) {
            mIn = new ServletInputStreamImpl(new FastBufferedInputStream(mIn));
        }
    }

    /**
     * Moves to the next encoded form data part, but returns false if no
     * more to read.
     */
    public boolean next() throws IOException {
        if (mMultiIn != null) {
            mMultiIn.mEOF = true;
        }

        int length = mBoundary.length;
        do {
            int result = readBoundary(false);
            if (result == START_BOUNDARY) {
                mHeaders = new HttpHeaderMap();
                mHeaders.readFrom(mIn);
                mDispositionFields = null;
                readContentTypeAndBoundary();
                mMultiIn = new MultiInputStream();
                return true;
            }
            else if (result == END_BOUNDARY) {
                break;
            }
        } while (mIn.read() >= 0);

        mHeaders = null;
        mDispositionFields = null;
        mMultiIn = null;
        return false;
    }

    /**
     * Returns an InputStream for the current encoded part.
     * When the stream has no more data, call next and request another stream.
     */
    public ServletInputStream getInputStream() throws IOException {
        if (mMultiIn == null) {
            next();
            if (mMultiIn == null) {
                return mIn;
            }
        }
        return mMultiIn;
    }

    /**
     * Returns the current boundary sequence between each part, which can
     * change after reading a message body part.
     */
    public byte[] getBoundary() {
        return (byte[])mBoundary.clone();
    }

    private String readContentTypeAndBoundary()
        throws UnsupportedEncodingException
    {
        String[][] fields = HttpUtils.parseHeaderFields(getContentType(), ",;");

        if (fields.length > 1 && BOUNDARY.equals(fields[1][0])) {
            mBoundary = ("--" + fields[1][1]).getBytes("8859_1");
        }

        if (fields.length > 0) {
            return fields[0][0];
        }
        else {
            return null;
        }
    }

    /**
     * Returns the content disposition type of the current form data part or
     * null if there isn't any current form data.
     */
    public String getContentDispositionType() {
        parseDispositionFields();
        if (mDispositionFields != null && mDispositionFields.length > 0) {
            return mDispositionFields[0][0];
        }
        else {
            return null;
        }
    }

    /**
     * Returns the content disposition name of the current form data part or
     * null if there isn't any.
     */
    public String getContentDispositionName() {
        return getContentDispositionField(NAME);
    }

    /**
     * Returns the content disposition filename of the current form data part
     * or null if there isn't any.
     */
    public String getContentDispositionFilename() {
        return getContentDispositionField(FILENAME);
    }

    /**
     * Returns a field from the current content disposition or null if there
     * isn't any current content disposition or the field wasn't found.
     */
    public String getContentDispositionField(String name) {
        parseDispositionFields();

        if (mDispositionFields != null && mDispositionFields.length > 1) {
            for (int i=1; i<mDispositionFields.length; i++) {
                if (name.equals(mDispositionFields[i][0])) {
                    String value = mDispositionFields[i][1];
                    int length = value.length();
                    if (length >= 2 &&
                        value.charAt(0) == '"' &&
                        value.charAt(length - 1) == '"') {

                        value = value.substring(1, length - 1);
                    }

                    return value;
                }
            }
        }

        return null;
    }

    private void parseDispositionFields() {
        if (mDispositionFields == null) {
            String disposition = getHeader(CONTENT_DISPOSITION);
            if (disposition != null) {
                mDispositionFields = HttpUtils.parseHeaderFields(disposition, ";");
            }
        }
    }

    /**
     * @return NO_BOUNDARY, START_BOUNDARY or END_BOUNDARY.
     */
    private int readBoundary(boolean alwaysReset) throws IOException {
        int length = mBoundary.length;
        mIn.mark(2 + length + 2 + 2);

        if (mIn.read() == '\r' && mIn.read() == '\n') {
            // May have just read in last linefeed from previous part
        }
        else {
            mIn.reset();
        }

        for (int i=0; i<length; i++) {
            if (mIn.read() != mBoundary[i]) {
                mIn.reset();
                return NO_BOUNDARY;
            }
        }

        // "\r\n" at tail indicates a start boundary, "--" indicates a
        // end boundary. If anything else, not a boundary.

        int readAhead1 = mIn.read();
        int readAhead2 = mIn.read();

        if (readAhead1 == '\r' && readAhead2 == '\n') {
            if (alwaysReset) {
                mIn.reset();
            }
            return START_BOUNDARY;
        }
        else if (readAhead1 == '-' && readAhead2 == '-' &&
                 mIn.read() == '\r' && mIn.read() == '\n') {
            if (alwaysReset) {
                mIn.reset();
            }
            return END_BOUNDARY;
        }
        else {
            mIn.reset();
            return NO_BOUNDARY;
        }
    }

    public String getHeader(String name) {
        if (mHeaders != null) {
            String header = mHeaders.getString(name);
            if (header != null) {
                return header;
            }
        }
        return super.getHeader(name);
    }

    public int getIntHeader(String name) {
        if (mHeaders != null && mHeaders.containsKey(name)) {
            Integer value = mHeaders.getInteger(name);
            if (value != null) {
                return value.intValue();
            }
        }
        return super.getIntHeader(name);
    }

    public long getDateHeader(String name) {
        if (mHeaders != null && mHeaders.containsKey(name)) {
            Date value = mHeaders.getDate(name);
            if (value != null) {
                return value.getTime();
            }
        }
        return super.getDateHeader(name);
    }

    public Enumeration getHeaderNames() {
        if (mHeaders == null) {
            return super.getHeaderNames();
        }

        Set allNames = new HashSet();

        Enumeration enum = super.getHeaderNames();
        while (enum.hasMoreElements()) {
            allNames.add(enum.nextElement());
        }

        allNames.addAll(mHeaders.keySet());

        return Collections.enumeration(allNames);
    }

    public String getContentType() {
        if (mHeaders != null) {
            return mHeaders.getString("Content-Type");
        }
        return super.getContentType();
    }

    public String toString() {
        return super.toString();
    }

    private class MultiInputStream extends ServletInputStream {
        private boolean mEOF;

        public MultiInputStream() throws IOException {
        }

        public int read() throws IOException {
            if (mEOF) {
                return -1;
            }
            int b;
            if ((b = readInternal()) < 0) {
                mEOF = true;
                return -1;
            }
            else {
                return b;
            }
        }

        private int readInternal() throws IOException {
            int result = readBoundary(true);
            if (result == START_BOUNDARY || result == END_BOUNDARY) {
                return -1;
            }
            else {
                return mIn.read();
            }
        }

        public int read(byte bytes[], int off, int len) throws IOException {
            int i;
            for (i=0; i<len; i++) {
                int b = read();
                if (b < 0) {
                    if (i == 0) {
                        return -1;
                    }
                    else {
                        break;
                    }
                }
                bytes[i] = (byte)b;
            }
            return i;
        }

        public long skip(long n) throws IOException {
            long i;
            for (i=0; i<n; i++) {
                int b = read();
                if (b < 0) {
                    if (i == 0) {
                        return -1;
                    }
                    else {
                        break;
                    }
                }
            }
            return i;
        }

        public int available() throws IOException {
            if (mEOF) {
                return 0;
            }
            else {
                return mIn.available();
            }
        }

        public void close() throws IOException {
            mIn.close();
        }
    }
}
