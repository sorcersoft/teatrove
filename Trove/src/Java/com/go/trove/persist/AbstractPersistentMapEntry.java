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

package com.go.trove.persist;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/******************************************************************************
 * Abstract PersistentMap.Entry implementation that makes it easier to
 * define new Map entries.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/01/03 <!-- $-->
 */
public abstract class AbstractPersistentMapEntry
    implements PersistentMap.Entry
{
    /**
     * Always throws UnsupportedOperationException.
     */
    public Object setValue(Object value) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof PersistentMap.Entry)) {
            return false;
        }
        
        PersistentMap.Entry e = (PersistentMap.Entry)obj;
        
        try {
            Object key = getKey();
            Object value = getValue();
            return 
                (key == null ?
                 e.getKey() == null : key.equals(e.getKey())) &&
                (value == null ?
                 e.getValue() == null : value.equals(e.getValue()));
        }
        catch (IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
    
    public int hashCode() {
        try {
            Object key = getKey();
            Object value = getValue();
            return 
                (key == null ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    public String toString() {
        String key;
        try {
            key = String.valueOf(getKey());
        }
        catch (IOException e) {
            key = e.toString();
        }
        
        String value;
        try {
            value = String.valueOf(getValue());
        }
        catch (IOException e) {
            value = e.toString();
        }
        
        return key + '=' + value;
    }
}