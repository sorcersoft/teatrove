/* ====================================================================
 * Trove - Copyright (c) 1997-2000 Walt Disney Internet Group
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

package com.go.trove.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/******************************************************************************
 * Wraps Depot.Kernel to directly expose the cached objects to other 
 * processes via the UbercacheConference interface.
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  2/21/02 <!-- $-->
 */
public class LocalConferenceImpl implements UbercacheConference, 
                                            Depot.Kernel {
    

    private Depot.Kernel mKernel;
    
    public LocalConferenceImpl() {
        this(0);
    }

    public LocalConferenceImpl(int cacheSize) {
        mKernel = new DefaultKernel(cacheSize);
    }
    
    public LocalConferenceImpl(Depot.Kernel kernel) {
        mKernel = kernel;
    }

    //Conference Methods
    
    public boolean gotThis(Object key) {
        return validCache().containsKey(key);
    }
    
    public Object gimme(Object key) {
        Object obj =  validCache().get(key);
        return (obj != null) ? obj : invalidCache().get(key);
    }
    
    public void stop(Object key) {
        // do nothing
    }
    
    public void expireCE(Object key) {
        Map valid = validCache();
        synchronized(valid) {
            Object value = valid.remove(key);
            if (value != null) {
                System.out.println("expired " + key + ':' + value);
                invalidCache().put(key, value);
            }
        }
    }

    public void expireHE(Object key) {
        // do nothing
    }

    // Kernel Methods 
    public Map validCache() {
        return mKernel.validCache();
    }
        
    public Map invalidCache() {
        return mKernel.invalidCache();
    }

    public int size() {
        return mKernel.size();
    }

    public boolean isEmpty() {
        return mKernel.isEmpty();
    }
        
    public void invalidateAll(Depot.Filter filter) {
        mKernel.invalidateAll(filter);
    }
        
    public void invalidateAll() {
        mKernel.invalidateAll();
    }

    public void removeAll(Depot.Filter filter) {
        mKernel.removeAll(filter);
    }
        
    public void clear() {
        mKernel.clear();
    }    
    
    private class DefaultKernel implements Depot.Kernel {
    
        private Map mValidCache;
        private Map mInvalidCache;
            
        DefaultKernel(int cacheSize) {
            Map valid;
            Map invalid;
            
            if (cacheSize < 0) {
                valid = Utils.VOID_MAP;
                invalid = Utils.VOID_MAP;
            }
            else if (cacheSize > 0) {
                valid = new Cache(cacheSize);
                invalid = new Cache((Cache)valid);
            }
            else {
                valid = new SoftHashMap();
                invalid = new SoftHashMap();
            }   
            
            mValidCache = Collections.synchronizedMap(valid);
            mInvalidCache = Collections.synchronizedMap(invalid);
        }

        public Map validCache() {
        
            return mValidCache;
        }
        
        public Map invalidCache() {
            return mInvalidCache;
        }
        
        public int size() {
            return mValidCache.size() + mInvalidCache.size();
        }
        
        public boolean isEmpty() {
            return mValidCache.isEmpty() && mInvalidCache.isEmpty();
        }
        
        public void invalidateAll(Depot.Filter filter) {
            Map valid = mValidCache;
            Map invalid = mInvalidCache;
            synchronized (valid) {
                Iterator it = valid.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    Object key = entry.getKey();
                    if (filter.accept(key)) {
                        it.remove();
                        invalid.put(key, entry.getValue());
                    }
                }
            }
        }
        
        public void invalidateAll() {
            synchronized (mValidCache) {
                synchronized (mInvalidCache) {
                    mInvalidCache.putAll(mValidCache);
                    mValidCache.clear();
                }
            }
        }
        
        public void removeAll(Depot.Filter filter) {
            Map valid = mValidCache;
            Map invalid = mInvalidCache;
            synchronized (valid) {
                synchronized (invalid) {
                    Iterator it = valid.keySet().iterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        if (filter.accept(key)) {
                            it.remove();
                        }
                    }
                    it = invalid.keySet().iterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        if (filter.accept(key)) {
                            it.remove();
                        }
                    }
                }
            }
        }

        public void clear() {
            synchronized (mValidCache) {
                synchronized (mInvalidCache) {
                    mValidCache.clear();
                    mInvalidCache.clear();
                }
            }
        }
    }
}










