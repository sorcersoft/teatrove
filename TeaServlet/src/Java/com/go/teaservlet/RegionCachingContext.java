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

import java.util.Map;
import java.util.Arrays;
import com.go.tea.runtime.Substitution;
import com.go.trove.io.*;
import com.go.trove.util.Depot;
import com.go.trove.util.Utils;
import com.go.trove.util.MultiKey;

/******************************************************************************
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface RegionCachingContext {

    /**
     * Caches and reuses a region of a page. The cached region expies after
     * a default time-to-live period has elapsed.
     *
     * @param s substitution block whose contents will be cached
     */
    public void cache(Substitution s) throws Exception;

    /**
     * Caches and reuses a region of a page. The cached region expies after
     * a the specified time-to-live period has elapsed.
     *
     * @param ttlMillis maximum time to live of cached region, in milliseconds
     * @param s substitution block whose contents will be cached
     */
    public void cache(long ttlMillis, Substitution s) throws Exception;

    /**
     * Caches and reuses a region of a page. The cached region expies after
     * a the specified time-to-live period has elapsed. An additional parameter
     * is specified which helps to identify the uniqueness of the region.
     *
     * @param ttlMillis maximum time to live of cached region, in milliseconds
     * @param key key to further identify cache region uniqueness
     * @param s substitution block whose contents will be cached
     */
    public void cache(long ttlMillis, Object key, Substitution s)
        throws Exception;

    public void nocache(Substitution s) throws Exception;

    public RegionCacheInfo getRegionCacheInfo();
    
    public RegionCachingApplication.ClusterCacheInfo getClusterCacheInfo();
}
