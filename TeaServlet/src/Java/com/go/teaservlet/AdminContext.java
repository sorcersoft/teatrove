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

import java.beans.*;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

import com.go.tea.engine.TemplateCompilationResults;
import com.go.teaservlet.util.ServerNote;

/******************************************************************************
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  4/03/02 <!-- $-->
 */
public interface AdminContext extends TeaToolsContext {

    /**
     * Gets the admin information for the TeaServlet. The user also can
     * reload the application or reload templates.
     * <p>
     * This function processes the following HTTP request parameters:
     * <ul>
     * <li>reloadTemplates - reloads the changed templates
     * <li>reloadTemplates=all - reloads all templates
     * <li>log - the id of the log
     * <li>enabled - turns on/off the log (boolean)
     * <li>debug - turns on/off log debug messages (boolean)
     * <li>info - turns on/off log info messages (boolean)
     * <li>warn - turns on/off log warning messages (boolean)
     * <li>error - turns on/off log error messages (boolean)
     * </ul>
     * @return the admin information
     */
    public TeaServletAdmin getTeaServletAdmin() throws ServletException;

    /**
     * If the call to getTeaServletAdmin() caused a reload to occur,
     * a call to this method will return the results of that reload.
     */
    public TemplateCompilationResults getCompilationResults();

    /**
     * Returns a String that uniquely identifies the given Object.
     */
    public String getObjectIdentifier(Object obj);
    
    /**
     * Returns a Class object for a given name.
     * it basically lets templates perform Class.forName(classname);
     */
    public Class getClassForName(String classname);
  
    /**
     * Streams the structural bytes of the named class via the HttpResponse.
     */
    public void streamClassBytes(String className) throws ServletException;


    /** 
     * allows a template to dynamically call another template
     */
    public void dynamicTemplateCall(String templateName) throws Exception;
     
    /** 
     * allows a template to dynamically call another template
     * this time with parameters.
     */
    public void dynamicTemplateCall(String templateName, Object[] params) 
        throws Exception;

    /**
     * returns a context for the specified application instance by name.  
     * this is useful when dynamically calling a function in that context.
     */
    public Object obtainContextByName(String appName) throws ServletException;

    /**
     * allows users to leave notes to each other from admin templates.
     * when called with a null contents parameter, this will not update the 
     * messages but will still return the contents of the message list.  
     * if the ID parameter is null, a list of all known IDs will be returned.
     */
    public Set addNote(String ID, String contents, int lifespan);

}
