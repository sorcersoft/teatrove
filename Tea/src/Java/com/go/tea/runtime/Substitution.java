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

package com.go.tea.runtime;

/******************************************************************************
 * A block of code in a template that can be passed as a substitution to
 * another template or to a function, must implement this interface. A
 * function that defines its last parameter as a Substitution can receive
 * a block of code from a template. To execute it, call substitute.
 * <p>
 * Substitution blocks can contain internal state information which may change
 * when the called function returns. Therefore, Substitution objects should
 * never be saved unless explicitly detached.
 * <p>
 * Functions that accept a Substitution appear to extend the template language
 * itself. Condsider the following example, which implements a simple looping
 * function:
 *
 * <pre>
 * public void loop(int count, Substitution s) throws Exception {
 *     while (--count >= 0) {
 *         s.substitute();
 *     }
 * }
 * </pre>
 *
 * The template might invoke this function as:
 *
 * <pre>
 * loop (100) {
 *     "This message is printed 100 times\n"
 * }
 * </pre>
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/05/03 <!-- $-->
 */
public interface Substitution {
    /**
     * Causes the code substitution block to execute against its current
     * output receiver.
     *
     * @throws UnsupportedOperationException if this Substitution was detached.
     */
    public void substitute() throws Exception;

    /**
     * Causes the code substitution block to execute against any context.
     *
     * @throws ClassCastException if context is incompatible with this
     * substitution.
     */
    public void substitute(Context context) throws Exception;

    /**
     * Returns an object that uniquely identifies this substitution block.
     */
    public Object getIdentifier();

    /**
     * Returns a detached substitution that can be saved and re-used. Detaching
     * a substitution provides greater flexibilty when implementing template
     * output caching strategies. One thread may execute the substitution while
     * another thread may, upon timing out, output the previously cached output
     * from this substitution.
     * <p>
     * When calling substitute, a context must be provided or else an
     * UnsupportedOperationException is thrown. In order for multiple threads
     * to safely execute this substitution, each must have its own detached
     * instance.
     */
    public Substitution detach();
}
