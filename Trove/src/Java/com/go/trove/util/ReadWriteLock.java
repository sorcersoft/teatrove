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

/******************************************************************************
 * The ReadWriteLock interface provides a more flexible locking mechanism than
 * Java's monitors. If there are many threads that wish to only read a
 * resource, then they may get a read lock which only blocks writers. A
 * resource can be shared more efficiently in this way.
 *
 * <p>When using the locking mechanisms, its a good idea to release the lock 
 * inside a finally statement to ensure the lock is always released. Example:
 * 
 * <pre>
 *     private Lock fileLock = new ReadWriteLock();
 *
 *     public String readFile() {
 *         try {
 *             fileLock.acquireReadLock();
 *             ...
 *         }
 *         finally {
 *             fileLock.releaseLock();
 *         }
 *     }
 *
 *     public void writeFile(String str) {
 *         try {
 *             fileLock.acquireWriteLock();
 *             ...
 *         }
 *         finally {
 *             fileLock.releaseLock();
 *         }
 *     }
 *
 *     public void deleteFile(String name) {
 *         try {
 *             fileLock.acquireUpgradableLock();
 *             if (exists(name)) {
 *                 try {
 *                     fileLock.acquireWriteLock();
 *                     delete(name);
 *                 }
 *                 finally {
 *                     fileLock.releaseLock();
 *                 }
 *             }
 *         }
 *         finally {
 *             fileLock.releaseLock();
 *         }
 *     }
 * </pre>
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/11/26 <!-- $-->
 */
public interface ReadWriteLock {
    public final int NONE = 0, READ = 1, UPGRADABLE = 2, WRITE = 3;

    /**
     * Same as calling acquireReadLock(getDefaultTimeout()).
     */
    public void acquireReadLock() throws InterruptedException;

    /**
     * A read lock is obtained when no threads currently hold a write lock.
     * When a thread has a read lock, it only blocks threads that wish to
     * acquire a write lock.
     * <p>
     * A read lock is the weakest form of lock.
     *
     * @param timeout milliseconds to wait for lock acquisition. If negative,
     * timeout is infinite.
     * @return true if the lock was acquired.
     */
    public boolean acquireReadLock(long timeout) throws InterruptedException;

    /**
     * Same as calling acquireUpgradableLock(getDefaultTimeout()).
     */
    public void acquireUpgradableLock()
        throws InterruptedException, IllegalStateException;

    /**
     * An upgradable lock is obtained when no threads currently hold write or
     * upgradable locks. When a thread has an upgradable lock, it blocks 
     * threads that wish to acquire upgradable or write locks.
     * <p>
     * Upgradable locks are to be used when a thread needs a read lock, but
     * while that read lock is held, it may need to be upgraded to a write
     * lock. If a thread acquired a read lock and then attempted to
     * acquire a write lock while the first lock was held, the thread
     * would be deadlocked with itself.
     * <p>
     * To prevent deadlock, threads that may need to upgrade a read lock
     * to a write lock should acquire an upgradable lock instead of a read 
     * lock. Upgradable locks will not block threads that wish to only read.
     * <p>
     * To perform an upgrade, call acquireWriteLock while the upgradable 
     * lock is still held.
     *
     * @param timeout milliseconds to wait for lock acquisition. If negative,
     * timeout is infinite.
     * @return true if the lock was acquired.
     * @throws IllegalStateException if thread holds a read lock.
     */
    public boolean acquireUpgradableLock(long timeout)
        throws InterruptedException, IllegalStateException;

    /**
     * Same as calling acquireWriteLock(getDefaultTimeout()).
     */
    public void acquireWriteLock()
        throws InterruptedException, IllegalStateException;

    /**
     * A write lock is obtained only when there are no read, upgradable or 
     * write locks held by any other thread. When a thread has a write lock,
     * it blocks any thread that wishes to acquire any kind of lock.
     * <p>
     * A write lock is the strongest form of lock. When a write lock is
     * held by a thread, then that thread alone has a lock. Requests for
     * write locks are granted the highest priority.
     *
     * @param timeout milliseconds to wait for lock acquisition. If negative,
     * timeout is infinite.
     * @return true if the lock was acquired.
     * @throws IllegalStateException if thread holds a read lock.
     */
    public boolean acquireWriteLock(long timeout)
        throws InterruptedException, IllegalStateException;

    /**
     * Release the lock held by the current thread.
     *
     * @return false if this thread doesn't hold a lock.
     */
    public boolean releaseLock();

    /**
     * Returns the default timeout used for acquiring locks. If negative,
     * the timeout is infinite.
     */
    public long getDefaultTimeout();
}
