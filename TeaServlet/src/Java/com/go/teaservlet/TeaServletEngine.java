/* ====================================================================
 * TeaServlet - Copyright (c) 1999-2001 Walt Disney Internet Group
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.go.trove.io.ByteBuffer;
import com.go.trove.log.LogEvent;
import com.go.trove.util.PropertyMap;
import com.go.tea.engine.Template;
import com.go.tea.engine.TemplateSource;

/******************************************************************************
 * This interface allows other servlets to create
 * {@link TeaServletTransaction TeaServletTransactions}. When the TeaServlet is
 * initialized, it adds an attribute to its ServletContext named
 * "com.go.teaservlet.TeaServletEngine". The attribute's value is a
 * TeaServletEngine array. The number of array elements matches the number of
 * times a TeaServlet is configured in. Use TeaServletEngine's name to
 * distinguish between different instances.
 * <p>
 * Servlets that request a TeaServletTransaction should generally let all
 * output be handled by it. This is because it will try to set headers and use 
 * a servlet output stream.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  3/05/02 <!-- $-->
 */
public interface TeaServletEngine extends ApplicationConfig {
    /**
     * Creates a TeaServletTransaction instance for the given request/response 
     * pair and returns it.
     *
     * @param request HttpServletRequest used for building ApplicationRequest.
     * @param response HttpServletResponse used for building
     * ApplicationResponse.
     */
    public TeaServletTransaction createTransaction
        (HttpServletRequest request, HttpServletResponse response)
        throws IOException;

    public TeaServletTransaction createTransaction
        (HttpServletRequest request, HttpServletResponse response, 
         boolean lookupTemplate)
        throws IOException;


    public Template findTemplate(String uri,
                                 HttpServletRequest request,
                                 HttpServletResponse response)            
        throws IOException, ServletException;

    public Template findTemplate(String uri,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 TemplateSource templateSrc)
        throws IOException, ServletException;

    public ApplicationDepot getApplicationDepot();

    public TeaServletTemplateSource getTemplateSource();

    public TeaServletTemplateSource reloadTemplateSource();

    public String[] getTemplatePaths();

    public PropertyMap getProperties(); 

    public LogEvent[] getLogEvents();

    public void destroy();
}
