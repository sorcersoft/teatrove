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

import java.io.Serializable;
import javax.servlet.http.*;
import com.go.teaservlet.io.CharToByteBuffer;
import com.go.tea.runtime.OutputReceiver;
import com.go.tea.runtime.Substitution;

/******************************************************************************
 * An ordinary HttpServletResponse, but with additional operations specific
 * to the TeaServlet.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public interface ApplicationResponse extends HttpServletResponse {
    /**
     * Returns whether a redirect or error will be sent to the client. This
     * is set to true when sendError or sendRedirect is called on the response.
     */
    public boolean isRedirectOrError();

    /**
     * Returns the internal buffer that stores the response. Applications can
     * use this to directly supply character or byte data to be output.
     */
    public CharToByteBuffer getResponseBuffer();

    /**
     * Provides direct access to the HttpContext that the template has access
     * to. This allows functions to directly control locale and formatting
     * settings as well as perform string conversion against the current
     * format settings.
     * <p>
     * The returned HttpContext instance is actually an auto-generated class
     * that merges all the application contexts into one. Application context
     * instances are requested only when the merged context first needs to
     * invoke a function from that context.
     * <p>
     * Application functions can access functions provided by other
     * applications, possibly using the Java reflection APIs. Also, the merged
     * class implements as many context interfaces as possible. Therefore,
     * casting the context to an expected type can also be used to access other
     * application functions.
     */
    public HttpContext getHttpContext();

    /**
     * Execute the given template substitution, but steal any output that would
     * have gone directly to the response buffer. Applications can use this
     * to specially handle the output of special objects. For example, an
     * application can supply functions that allow templates to create
     * downloadable images. By stealing the output, printed text can go to the
     * image instead of corrupting the encoding.
     * <p>
     * Note: stealOutput is designed to run in the same thread that executed
     * the main template. If the substitution needs to run in a separate
     * thread, consider {@link #execDetached} instead.
     *
     * @param s template substitution block that will be executed
     * @param receiver receives all the output generated by the substitution
     * @throws any exception thrown by the substitution or receiver
     */
    public void stealOutput(Substitution s, OutputReceiver receiver)
        throws Exception;

    /**
     * Execute the given template substitution and detach everything sent to
     * the ApplicationResponse in a way that's safe to do asynchronously.
     * This is useful for certain caching strategies. The substitution is
     * detached and executed with a new context instance and output buffer.
     *
     * @param s template substitution block that will be executed
     * @return the detached response data, which is essentially a copy
     * @throws any exception thrown by the substitution or buffer
     */
    public DetachedData execDetached(Substitution s) throws Exception;

    /**
     * Execute the given command and detach everything sent to the
     * ApplicationResponse in a way that's safe to do asynchronously.
     * This is useful for certain caching strategies. The ApplicationResponse
     * given to the command contains a new context instance and output buffer.
     *
     * @param s template substitution block that will be executed
     * @return the detached response data, which is essentially a copy
     * @throws any exception thrown by the command or buffer
     */
    public DetachedData execDetached(Command command) throws Exception;

    /**
     * Insert an arbitrary command into this response. If this response is
     * saving its state in a DetachedData instance, then the command
     * will be executed every time the DetachedData is played back.
     * If this response isn't detached, the command is not executed and false
     * is returned.
     *
     * @return false if command cannot be inserted because no detached
     * execution is in progress.
     */
    public boolean insertCommand(Command command) throws Exception;

    /**
     * Commits this response and fully writes out the buffered contents unless
     * there was a redirect or an error. Subsequent calls to finish have no
     * affect.
     *
     * @throws IllegalArgumentException if a Writer was already used for
     * writing to the response.
     */
    public void finish() throws java.io.IOException;

    public interface DetachedData extends Serializable {
        public void playback(ApplicationRequest request,
                             ApplicationResponse response) throws Exception;
        
        /**
         * Compresses all the byte buffers using a deflater, unless already
         * called before. When this DetachedData is later played back for a
         * request that accepts a compressed encoding, the compressed data is
         * output. If the request doesn't accept compression, the original
         * uncompressed data is output.
         * <p>
         * The deflater will usually use default strategy, default compression.
         * If the amount of data to compress is small, the "no compression" (0)
         * level is used.
         */
        public void compress();

        /**
         * Compresses all the byte buffers using a deflater, unless already
         * called before. When this DetachedData is later played back for a
         * request that accepts a compressed encoding, the compressed data is
         * output. If the request doesn't accept compression, the original
         * uncompressed data is output.
         * <p>
         * If the amount of data to compress is small, the "no compression" (0)
         * level is used.
         *
         * @param level compression level 1 to 9. 1 is fastest, 9 offers best
         * compression. 6 is default.
         */
        public void compress(int level);
    }

    public interface Command {
        public void execute(ApplicationRequest request,
                            ApplicationResponse response) throws Exception;
    }
}
