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

import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.go.teaservlet.util.FilteredHttpServletRequest;
import com.go.tea.runtime.TemplateLoader;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
class ApplicationRequestImpl extends FilteredHttpServletRequest
    implements ApplicationRequest
{
    private final Object mTemplateOrLoader;
    private final Map mAppContextMap;
    private Object mID;
    // 0 = don't know, 1 = no, 2 = yes
    private int mCompression;

    public ApplicationRequestImpl(HttpServletRequest request,
                                  Map appContextMap,
                                  TemplateLoader.Template template) {
        super(request);
        mAppContextMap = appContextMap;
        mTemplateOrLoader = template;
    }

    public ApplicationRequestImpl(HttpServletRequest request,
                                  Map appContextMap,
                                  TemplateLoader loader) {
        super(request);
        mAppContextMap = appContextMap;
        mTemplateOrLoader = loader;
    }

    public TemplateLoader.Template getTemplate() {
        if (mTemplateOrLoader instanceof TemplateLoader.Template) {
            return (TemplateLoader.Template)mTemplateOrLoader;
        }
        else {
            return null;
        }
    }

    public TemplateLoader getTemplateLoader() {
        if (mTemplateOrLoader instanceof TemplateLoader.Template) {
            return ((TemplateLoader.Template)mTemplateOrLoader)
                .getTemplateLoader();
        }
        else {
            return (TemplateLoader)mTemplateOrLoader;
        }
    }

    public synchronized Object getIdentifier() {
        if (mID == null) {
            mID = new Object();
        }
        return mID;
    }

    public boolean isCompressionAccepted() {
        if (mCompression != 0) {
            return mCompression == 2;
        }
        
        // MSIE 4.x doesn't support the compression format produced
        // by the TeaServlet, even though it is still a legal GZIP stream.
        String userAgent = getHeader("User-Agent");
        if (userAgent != null) {
            int index = userAgent.indexOf("MSIE ");
            if (index > 0) {
                int index2 = userAgent.indexOf('.', index + 5);
                if (index2 > index) {
                    try {
                        int majorVersion = Integer.parseInt
                            (userAgent.substring(index + 5, index2));
                        if (majorVersion <= 4) {
                            mCompression = 1;
                            return false;
                        }
                    }
                    catch (NumberFormatException e) {
                    }
                }
            }
        }

        String value = getHeader("Accept-Encoding");

        if (value != null) {
            if (value.indexOf("gzip") >= 0) {
                mCompression = 2;
                return true;
            }
            Enumeration enum = getHeaders("Accept-Encoding");
            while (enum.hasMoreElements()) {
                value = (String)enum.nextElement();
                if ("gzip".equals(value)) {
                    mCompression = 2;
                    return true;
                }
            }
        }

        mCompression = 1;
        return false;
    }

    public Map getApplicationContextTypes() {
        return mAppContextMap;
    }
}
