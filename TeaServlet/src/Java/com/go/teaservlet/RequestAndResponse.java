/*
 * RequestAndResponse.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: RequestAndResponse.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import com.go.tea.runtime.OutputReceiver;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/01/02 <!-- $-->
 */
public class RequestAndResponse implements TeaServletTransaction {

    ApplicationRequest mReq;
    ApplicationResponse mResp;
    OutputReceiver mOutRec;
        
    public RequestAndResponse() {
        mResp = null;
        mReq = null;
        mOutRec = null;
    }
        
    public RequestAndResponse(ApplicationRequest req, ApplicationResponse resp) {
        mResp = resp;
        mReq = req;
    }

    
    public RequestAndResponse(OutputReceiver outRec) {
        mOutRec = outRec;
    }
     
    public ApplicationRequest getRequest() {
        return mReq;
    }

    public ApplicationResponse getResponse() {
        return mResp;
    }

    public OutputReceiver getOutputReceiver() {
         return mOutRec;
    }
}
