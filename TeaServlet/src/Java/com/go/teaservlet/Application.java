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

import javax.servlet.ServletException;

/******************************************************************************
 * The main hook into the TeaServlet framework. Implement this interface for
 * instantiating other required components and for providing functions to
 * templates.
 *
 * @author Reece Wilton
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface Application {
    /**
     * Initializes resources used by the Application.
     *
     * @param config the application's configuration object
     */
    public void init(ApplicationConfig config) throws ServletException;
    
    /**
     * Called by the TeaServlet when the application is no longer needed.
     */
    public void destroy();

    /**
     * Creates a context, which defines functions that are callable by
     * templates. Any public method in the context is a callable function,
     * except methods defined in Object. A context may receive a request and
     * response, but it doesn't need to use any of them. They are provided only
     * in the event that a function needs access to these objects.
     * <p>
     * Unless the getContextType method returns null, the createContext method
     * is called once for every request to the TeaServlet, so context creation
     * should have a fairly quick initialization. One way of accomplishing this
     * is to return the same context instance each time. The drawback to this
     * technique is that functions will not be able to access the current
     * request and response.
     * <p>
     * The recommended technique is to construct a new context that simply
     * references this Application and any of the passed in parameters. This
     * way, the Application contains all the resources and "business logic",
     * and the context just provides templates access to it.
     *
     * @param request the client's HTTP request
     * @param response the client's HTTP response
     * @return an object context for the templates
     */
    public Object createContext(ApplicationRequest request,
                                ApplicationResponse response);

    /**
     * The class of the object that the createContext method will return, which
     * does not need to implement any special interface or extend any special
     * class. Returning null indicates that this Application defines no
     * context, and createContext will never be called.
     *
     * @return the class that the createContext method will return
     */
    public Class getContextType();
}
