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

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;

/******************************************************************************
 * A convenience HttpServletRequest wrapper that automatically decodes request
 * parameters using the provided character encoding.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class DecodedRequest extends FilteredHttpServletRequest {
    private static final byte[] TEST_BYTES = {65};

    private static Set cGoodEncodings = new HashSet(7);

    private static synchronized String checkEncoding(String encoding) {
        if (!cGoodEncodings.contains(encoding)) {
            // Test the encoding.
            try {
                new String(TEST_BYTES, encoding);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException
                    ("Unsupported character encoding: " + encoding);
            }
            cGoodEncodings.add(encoding);
        }
        return encoding;
    }

    private String mEncoding;
    private String mOriginalEncoding;

    /**
     * @param request wrapped request
     * @param encoding character encoding to apply to request parameters
     * @throws IllegalArgumentException when the encoding isn't supported
     */
    public DecodedRequest(HttpServletRequest request, String encoding) {
        super(request);
        mEncoding = checkEncoding(encoding);
        mOriginalEncoding = request.getCharacterEncoding();
    }

    public String getCharacterEncoding() {
        return mEncoding;
    }

    public String getParameter(String name) {
        String value;
        if ((value = mRequest.getParameter(name)) != null) {
            try {
                return new String
                    (value.getBytes(mOriginalEncoding), mEncoding);
            }
            catch (UnsupportedEncodingException e) {
            }
        }
        return value;
    }

    public String[] getParameterValues(String name) {
        String[] values = (String[])mRequest.getParameterValues(name).clone();
        try {
            String enc = mEncoding;
            String orig = mOriginalEncoding;
            for (int i = values.length; --i >= 0; ) {
                String value;
                if ((value = values[i]) != null) {
                    values[i] = new String(value.getBytes(orig), enc);
                }
            }
        }
        catch (UnsupportedEncodingException e) {
        }
        return values;
    }
}
