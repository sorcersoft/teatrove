/* ====================================================================
 * Tea - Copyright (c) 1997-2000 Walt Disney Internet Group
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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;

/******************************************************************************
 * Used with the RemoteCompiler to allow templates to be loaded over http. 
 * This servlet should be running on the host specified in the template.path
 * parameter of the teaservlet.
 *
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$--> 21 <!-- $$JustDate:--> 10/01/01 <!-- $-->
 * 
 */
public class TemplateServerServlet extends HttpServlet {
    File mTemplateRoot;
    ServletConfig mConfig;
    
    public void init(ServletConfig conf) {
        mConfig = conf;
        mTemplateRoot = new File(conf.getInitParameter("template.root"));
    }
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) {
        doGet(req,res);
    }
    
    /**
     * Retrieves all the templates that are newer that the timestamp specified
     * by the client.  The pathInfo from the request specifies which templates 
     * are desired.  QueryString parameters "timeStamp" and ??? provide
     */
    public void doGet(HttpServletRequest req,HttpServletResponse res) {
        getTemplateData(res,req.getPathInfo());
    }
    
    public void getTemplateData(HttpServletResponse resp,String path) {
        try {
            OutputStream out = resp.getOutputStream();
            File templateFile;
            if (path != null) {
                templateFile = new File(mTemplateRoot,path);
                
            }
            else {
                templateFile = mTemplateRoot;
            }
            if (templateFile != null) {
                if (templateFile.isFile()) {
                    resp.setIntHeader("Content-Length", (int)templateFile.length());
                    InputStream fis = new BufferedInputStream(
                          new FileInputStream(templateFile));
                    for(int nextChar = -1;(nextChar = fis.read()) >= 0;) {
                        out.write(nextChar);
                    }
                    fis.close();
                }
                else if (templateFile.isDirectory()) {
                    Vector tempVec = new Vector();
                    File[] dirlist = templateFile.listFiles();
                    for (int j=0;j<dirlist.length;j++) {
                        listTemplates(dirlist[j],tempVec,"/");
                    }
                    Iterator tempIt = tempVec.iterator();
                    while (tempIt.hasNext()) {
                        out.write((byte[])tempIt.next());
                    }
                }
                else {
                    mConfig.getServletContext().log(path
                     + " doesn't map to an existing template or directory.");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }     
    }

    public void listTemplates(File root,Vector storage,String path) throws IOException {
        if (root.isDirectory()) {
            File[] dirlist = root.listFiles();
            for (int j=0;j<dirlist.length;j++) {
                listTemplates(dirlist[j],storage,
                                path + root.getName() + "/");
            }
        } 
        else if (root.isFile()) {
            String templateName = root.getName();
            if (templateName.endsWith(".tea")) {
                storage.add(("|" + path + templateName.substring(0,templateName.length()-4)+ "|" 
                        + Long.toString(root.lastModified())).getBytes());
            }
        }
    }
}
