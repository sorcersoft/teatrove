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

import java.util.*;
import javax.servlet.http.*;
import com.go.teaservlet.util.FilteredHttpServletRequest;

/******************************************************************************
 * Allows HTTP requests to be 'spiderable', that is, support parameters
 * without using '?', '&' and '=' characters so that search engines will
 * index the request URL. The request URI is broken down, providing additional
 * parameters, and possibly altering the PathInfo. If the URL contains the
 * normal query separator, '?', then any parameters specified after it are
 * preserved.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
class SpiderableRequest extends FilteredHttpServletRequest {
    private final String mQuerySeparator;
    private final String mParameterSeparator;
    private final String mValueSeparator;

    private final int mQuerySepLen;
    private final int mParamSepLen;
    private final int mValSepLen;

    private String mPathInfo;
    private Map mParameters;

    public SpiderableRequest(HttpServletRequest request,
                             String querySeparator,
                             String parameterSeparator,
                             String valueSeparator) {
        super(request);
        mQuerySeparator = querySeparator;
        mParameterSeparator = parameterSeparator;
        mValueSeparator = valueSeparator;

        mQuerySepLen = querySeparator.length();
        mParamSepLen = parameterSeparator.length();
        mValSepLen = valueSeparator.length();

        mPathInfo = request.getPathInfo();
        mParameters = new HashMap(37);
        
        fillParameters();
        fillDefaultParameters();
    }

    public String getPathInfo() {
        return mPathInfo;
    }
    
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    public String[] getParameterValues(String name) {
        return (String[])mParameters.get(name);
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(mParameters.keySet());
    }

    private void fillParameters() {
        String str;
        int startIndex, midIndex, endIndex;
        
        if ("?".equals(mQuerySeparator)) {
            if ((str = mRequest.getQueryString()) == null) {
                return;
            }
            startIndex = 0;
        }
        else {
            if (mPathInfo != null) {
                startIndex = mPathInfo.indexOf(mQuerySeparator);
                if (startIndex >= 0) {
                    mPathInfo = mPathInfo.substring(0, startIndex);
                }
            }

            str = mRequest.getRequestURI();
            startIndex = str.indexOf(mQuerySeparator);
            if (startIndex < 0) {
                return;
            }
            startIndex += mQuerySepLen;
        }
        
        int length = str.length();
        
        for (; startIndex < length; startIndex = endIndex + mParamSepLen) {
            endIndex = str.indexOf(mParameterSeparator, startIndex);
            
            if (endIndex < 0) {
                endIndex = length;
            }
            
            midIndex = str.indexOf(mValueSeparator, startIndex);
            
            String key;
            String value;
            
            if (midIndex < 0 || midIndex > endIndex) {
                if (endIndex - startIndex > 1) {
                    key = str.substring(startIndex, endIndex);
                    value = "";
                }
                else {
                    continue;
                }
            }
            else if (midIndex - startIndex > 1) {
                key = str.substring(startIndex, midIndex);
                value = str.substring(midIndex + mValSepLen, endIndex);
            }
            else {
                continue;
            }
            
            putParameter(key, value);
        }
    }

    private void fillDefaultParameters() {
        Enumeration names = mRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            putParameters(name, mRequest.getParameterValues(name));
        }
    }

    private void putParameter(String key, String value) {
        String[] currentValues = (String[])mParameters.get(key);
        if (currentValues == null) {
            currentValues = new String[] {value};
        }
        else {
            String[] newValues = new String[currentValues.length + 1];
            int i;
            for (i=0; i<currentValues.length; i++) {
                newValues[i] = currentValues[i];
            }
            currentValues = newValues;
            currentValues[i] = value;
        }

        mParameters.put(key, currentValues);
    }


    private void putParameters(String key, String[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        else if (values.length == 1) {
            putParameter(key, values[0]);
            return;
        }

        String[] currentValues = (String[])mParameters.get(key);
        if (currentValues == null) {
            currentValues = values;
        }
        else {
            String[] newValues =
                new String[currentValues.length + values.length];
            int i, j;
            for (i=0; i<currentValues.length; i++) {
                newValues[i] = currentValues[i];
            }
            for (j=0; j<values.length; j++) {
                newValues[i + j] = values[j];
            }
            currentValues = newValues;
        }
        
        mParameters.put(key, currentValues);
    }
}
