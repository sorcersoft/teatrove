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

package com.go.tea.parsetree;

import com.go.tea.compiler.SourceInfo;

/******************************************************************************
 * The superclass of all parse tree nodes. Every Node contains source
 * information and can accept a NodeVisitor.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 * @see NodeVisitor
 */
public abstract class Node implements Cloneable, java.io.Serializable {
    private static final String cPackage;
    private static final int cPackageLength;

    static {
        String className = Node.class.getName();
        int index = className.lastIndexOf('.');
        if (index >= 0) {
            cPackage = className.substring(0, index + 1);
        }
        else {
            cPackage = "";
        }

        cPackageLength = cPackage.length();
    }
    
    private SourceInfo mInfo;

    protected Node(SourceInfo info) {
        mInfo = info;
    }

    public final SourceInfo getSourceInfo() {
        return mInfo;
    }

    /**
     * Every subclass of Node must override this method with the following:
     * <code>return visitor.visit(this)</code>.
     *
     * @param visitor A visitor of this Node
     * @return Node The Node returned by the visitor
     * @see NodeVisitor
     */
    public abstract Object accept(NodeVisitor visitor);

    /**
     * Returns a clone of this Node and all its children. Immutable child
     * objects are not necessarily cloned
     */
    public Object clone() {
        try {
            return (Node)super.clone();
        }
        catch (CloneNotSupportedException e) {
            // Should never happen, since all Nodes are Cloneable.
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Returns a String that contains the type of this Node and source
     * information.
     */
    public String toString() {
        String name = getClass().getName();
        int index = name.indexOf(cPackage);
        if (index >= 0) {
            name = name.substring(cPackageLength);
        }

        String identityCode = 
            Integer.toHexString(System.identityHashCode(this));

        if (mInfo == null) {
            return name + '@' + identityCode;
        }
        else {
            return 
                name + 
                '(' + 
                mInfo.getLine() + ',' + ' ' +
                mInfo.getStartPosition() + ',' + ' ' +
                mInfo.getEndPosition() + 
                ')' + '@' + identityCode;
        }
    }
}
