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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.lang.reflect.UndeclaredThrowableException;
import com.go.trove.util.Depot;
import com.go.trove.util.Cache;
import com.go.trove.util.SoftHashMap;
import com.go.trove.util.WrappedCache;
import com.go.trove.util.tq.TransactionQueue;
import com.go.trove.file.*;

/******************************************************************************
 * A bunch of functions that aid in creating peristent collection instances.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/04 <!-- $-->
 */
public class PersistentCollectionKit {
    /**
     * Creates a generic persistent {@link Depot}. Features automatic clean
     * close on system exit and detection of incomplete or failed close on
     * previous use. Depot keys and values must be {@link Serializable}.
     * Depot keys must implement {@link Comparable}, or else a
     * {@link Comparator} must be provided.
     * <p>
     * Three files are used by the Depot: the object repository (.obr), the
     * valid cache index (.ixv), and the invalid cache index (.ixi).
     * <p>
     * The set of parameters that this method accepts is somewhat complicated.
     * Many of the parameters will affect performance and storage efficiency,
     * but they have little effect on correct functionality. If the Depot will
     * have small Object keys and small Object values, then this set should
     * work well:
     *
     * <pre>
     * factory         N/A
     * cacheSize       50 objects
     * tq              Construct TQ with a thread limit of 10.
     * depotTimeout    5000 milliseconds
     * create          false (if true, data files are deleted on startup)
     * dataFilePath    N/A
     * indexBlockSize  2048
     * keyType         Object.class
     * keyLength       100
     * keyComparator   N/A
     * valueBlockSize  512
     * shutdownTimeout 15000 milliseconds
     * builder         null
     * </pre>
     *
     * If the keys are always Strings, then the keyType should be specified as
     * String.class. The indexes will perform better because the keys won't
     * need to be written to an ObjectOutputStream. If the key Strings are
     * usually smaller than 100 bytes (UTF-8 encoded), then set the keyLength
     * smaller and reduce the indexBlockSize.
     * <p>
     * If the Depot values are large objects, increase the size of the
     * valueBlockSize. An upper bound of 4096 is acceptible. If the values
     * are always the same size, set the block size to match it. To determine
     * the storage requirements of a value, write one to a file via an
     * ObjectOutputStream and examine the file size.
     * <p>
     * Its generally a good idea to ensure that the block sizes are a multiple
     * of 512, since hard drive sectors usually hold 512 bytes. If the block
     * size isn't a multiple of this size, no space is wasted necessarily, but
     * there may be some performance degradation.
     *
     * @param factory Optional default Depot factory.
     * @param cacheSize Number of items guaranteed to be in the memory cache.
     * If negative, cache is disabled.
     * @param tq TransactionQueue for scheduling Depot factory invocations.
     * @param depotTimeout Default timeout (in milliseconds) to apply to Depot
     * "get" method.
     * @param create Always creates new data files, destroying anything
     * already there.
     * @param dataFilePath Root file name for data files. Three letter dot
     * suffixes will be added to this path to form the final file names.
     * @param indexBlockSize Block size (in bytes) for B-Tree node allocation.
     * @param keyType Expected Depot key type, i.e. String.class
     * @param keyLength Expected average key length, if key is string or array.
     * @param keyComparator Comparator for ordering keys, or null if keys
     * implement Comparable.
     * @param valueBlockSize Block size (in bytes) for allocation of serialized
     * Depot values.
     * @param shutdownTimeout max milliseconds to wait for clean close when
     * system exits.
     * @param builder optional ObjectStreamBuilder.
     */
    public static Depot createPersistentDepot(Depot.Factory factory,
                                              int cacheSize,
                                              TransactionQueue tq,
                                              long depotTimeout,
                                              boolean create,
                                              String dataFilePath,
                                              int indexBlockSize,
                                              Class keyType,
                                              int keyLength,
                                              Comparator keyComparator,
                                              int valueBlockSize,
                                              long shutdownTimeout,
                                              ObjectStreamBuilder builder)
        throws IOException
    {
        File objectFile, validIndexFile, invalidIndexFile;
        objectFile = new File(dataFilePath + ".obr");
        objectFile.getParentFile().mkdirs();
        validIndexFile = new File(dataFilePath + ".ixv");
        invalidIndexFile = new File(dataFilePath + ".ixi");

        ObjectRepository store = null;
        PersistentMap valid = null;
        PersistentMap invalid = null;

        if (!create) openExisting: {
            try {
                store = createObjectRepository
                    (objectFile, valueBlockSize, false, true, shutdownTimeout,
                     builder);
            }
            catch (CorruptFileException e) {
                create = true;
                break openExisting;
            }
                
            try {
                valid = createSortedMap
                    (validIndexFile, indexBlockSize, keyType, keyLength,
                     keyComparator, false, true, shutdownTimeout, store,
                     builder);
            }
            catch (CorruptFileException e) {
                create = true;
                break openExisting;
            }
            
            try {
                invalid = createSortedMap
                    (invalidIndexFile, indexBlockSize, keyType, keyLength,
                     keyComparator, false, true, shutdownTimeout, store,
                     builder);
            }
            catch (CorruptFileException e) {
                create = true;
                break openExisting;
            }
        }

        if (create) {
            store = createObjectRepository
                (objectFile, valueBlockSize, true, true, shutdownTimeout,
                 builder);
            valid = createSortedMap
                (validIndexFile, indexBlockSize, keyType, keyLength,
                 keyComparator, true, true, shutdownTimeout, store,
                 builder);
            invalid = createSortedMap
                (invalidIndexFile, indexBlockSize, keyType, keyLength,
                 keyComparator, true, true, shutdownTimeout, store,
                 builder);
        }

        if (cacheSize == 0) {
            valid = new CachedPersistentMap(new SoftHashMap(), valid);
            invalid = new CachedPersistentMap(new SoftHashMap(), invalid);
        }
        else if (cacheSize > 0) {
            valid = new CachedPersistentMap(cacheSize, valid);
            invalid = new CachedPersistentMap(cacheSize, invalid);
            //valid = new CachedPersistentMap(new Cache(cacheSize), valid);
            //invalid = new CachedPersistentMap(new Cache(cacheSize), invalid);
        }

        return new Depot
            (factory, new DepotKernel(valid, invalid), tq, depotTimeout);
    }

    /**
     * Creates a generic ObjectRepository. Features automatic clean close on
     * system exit and detection of incomplete or failed close on previous
     * use.
     *
     * @param file File for storing serialized objects
     * @param blockSize block size (in bytes) for allocation of serialized
     * objects
     * @param create always creates a new ObjectRepository in the file,
     * destroying anything already there
     * @param failIfDirty when true, checks if file is clean and throws
     * CorruptFileException if not
     * @param shutdownTimeout max milliseconds to wait for clean close when
     * system exits
     * @param builder optional ObjectStreamBuilder
     *
     * @throws CorruptFileException if failIfDirty is true and file wasn't
     * closed cleanly the last time is was used
     */
    public static ObjectRepository createObjectRepository
        (File file,
         int blockSize,
         boolean create,
         boolean failIfDirty,
         long shutdownTimeout,
         ObjectStreamBuilder builder)
        throws IOException
    {
        create = create || !file.exists();
        FileBuffer fb = new SystemFileBuffer(file, false);
        return createObjectRepository
            (fb, blockSize, create, failIfDirty, shutdownTimeout, builder);
    }

    /**
     * Creates a generic ObjectRepository. Features automatic clean close on
     * system exit and detection of incomplete or failed close on previous
     * use.
     *
     * @param file FileBuffer for storing serialized objects
     * @param blockSize block size (in bytes) for allocation of serialized
     * objects
     * @param create always creates a new ObjectRepository in the file,
     * destroying anything already there
     * @param failIfDirty when true, checks if file is clean and throws
     * CorruptFileException if not
     * @param shutdownTimeout max milliseconds to wait for clean close when
     * system exits
     * @param builder optional ObjectStreamBuilder
     *
     * @throws CorruptFileException if failIfDirty is true and file wasn't
     * closed cleanly the last time is was used
     */
    public static ObjectRepository createObjectRepository
        (FileBuffer file,
         int blockSize,
         boolean create,
         boolean failIfDirty,
         long shutdownTimeout,
         ObjectStreamBuilder builder)
        throws IOException
    {
        TxFileBuffer txfile;
        int reserved;
        if (file instanceof TxFileBuffer) {
            txfile = (TxFileBuffer)file;
            reserved = 0;
        }
        else {
            txfile = new TaggedTxFileBuffer(file, new Bitlist(file), 0);
            // Reserve one byte for the tag bitlist.
            reserved = 1;
        }

        if (failIfDirty && !txfile.isClean()) {
            throw new CorruptFileException
                ("File not closed cleanly; contents may be corrupt");
        }

        addShutdownHook(txfile, shutdownTimeout);

        MultiplexFile mf;
        if (create) {
            file.truncate(0);
            mf = new MultiplexFile(file, reserved, blockSize, 4, 4);
        }
        else {
            mf = new MultiplexFile(file, reserved);
        }

        return new BasicObjectRepository
            (new MultiplexFileRepository(mf), builder);
    }

    /**
     * Creates a generic PersistentSortedMap for use in mapping any kind of
     * Serializable object.
     *
     * <ul>
     * <li>B-Tree index
     * <li>Automatic clean shutdown on system exit
     * <li>Detection of failed shutdown
     * </ul>
     *
     * @param indexFile File for storing B-Tree index
     * @param indexBlockSize block size (in bytes) for B-Tree node allocation
     * @param keyType expected key type, i.e. String.class
     * @param keyLength expected average key length, if key is string or array
     * @param keyComparator Comparator for ordering keys, or null if keys
     * implement Comparable
     * @param createIndex always creates a new B-Tree in the index file,
     * destroying anything already there
     * @param failIfDirty when true, checks if index file is clean and
     * throws CorruptFileException if not
     * @param shutdownTimeout max milliseconds to wait for clean close when
     * system exits
     * @param store ObjectRepository for storing values, which may be shared
     * @param builder optional ObjectStreamBuilder for BTree
     *
     * @throws CorruptFileException if failIfDirty is true and file wasn't
     * closed cleanly the last time is was used
     */
    public static PersistentSortedMap createSortedMap
        (File indexFile,
         int indexBlockSize,
         Class keyType,
         int keyLength,
         Comparator keyComparator,
         boolean createIndex,
         boolean failIfDirty,
         long shutdownTimeout,
         ObjectRepository store,
         ObjectStreamBuilder builder)
        throws IOException
    {
        createIndex = createIndex || !indexFile.exists();
        FileBuffer fb = new SystemFileBuffer(indexFile, false);
        return createSortedMap(fb, indexBlockSize, keyType, keyLength,
                               keyComparator, createIndex,
                               failIfDirty, shutdownTimeout, store, builder);
    }

    /**
     * Creates a generic PersistentSortedMap for use in mapping any kind of
     * Serializable object.
     *
     * <ul>
     * <li>B-Tree index
     * <li>Automatic clean shutdown on system exit
     * <li>Detection of failed shutdown
     * </ul>
     *
     * @param indexFile FileBuffer for storing B-Tree index
     * @param indexBlockSize block size (in bytes) for B-Tree node allocation
     * @param keyType expected key type, i.e. String.class
     * @param keyLength expected average key length, if key is string or array
     * @param keyComparator Comparator for ordering keys, or null if keys
     * implement Comparable
     * @param createIndex always creates a new B-Tree in the index file,
     * destroying anything already there
     * @param failIfDirty when true, checks if index file is clean and
     * throws CorruptFileException if not
     * @param shutdownTimeout max milliseconds to wait for clean close when
     * system exits
     * @param store ObjectRepository for storing values, which may be shared
     * @param builder optional ObjectStreamBuilder for BTree
     *
     * @throws CorruptFileException if failIfDirty is true and file wasn't
     * closed cleanly the last time is was used
     */
    public static PersistentSortedMap createSortedMap
        (FileBuffer indexFile,
         int indexBlockSize,
         Class keyType,
         int keyLength,
         Comparator keyComparator,
         boolean createIndex,
         boolean failIfDirty,
         long shutdownTimeout,
         ObjectRepository store,
         ObjectStreamBuilder builder)
        throws IOException
    {
        TxFileBuffer txfile;
        int reserved;
        if (indexFile instanceof TxFileBuffer) {
            txfile = (TxFileBuffer)indexFile;
            reserved = 0;
        }
        else {
            txfile = new TaggedTxFileBuffer
                (indexFile, new Bitlist(indexFile), 0);
            // Reserve one byte for the tag bitlist.
            reserved = 1;
        }

        if (failIfDirty && !txfile.isClean()) {
            throw new CorruptFileException
                ("Index file not closed cleanly; contents may be corrupt");
        }

        addShutdownHook(txfile, shutdownTimeout);

        if (createIndex) {
            txfile.truncate(reserved);
        }

        BTree.StorageStrategy ss = new MultiplexFileStorageStrategy
            (txfile, reserved, indexBlockSize, keyType, Long.class,
             keyLength, 0, 0, builder);

        PersistentSortedMap index;
        if (keyComparator == null) {
            index = BTree.createMap(ss);
        }
        else {
            index = BTree.createMap(ss, keyComparator);
        }

        return new ObjectRepositoryMap(index, store);
    }

    private static void addShutdownHook(final TxFileBuffer txfile,
                                        final long timeout)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    txfile.close(timeout);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // PersistentDepotKernel
    private static class DepotKernel implements Depot.Kernel {
        private final PersistentMap mValid;
        private final PersistentMap mInvalid;

        DepotKernel(PersistentMap valid, PersistentMap invalid) {
            mValid = valid;
            mInvalid = invalid;
        }

        public Map validCache() {
            return Adapters.wrap(mValid);
        }

        public Map invalidCache() {
            return Adapters.wrap(mInvalid);
        }

        public int size() {
            try {
                mValid.lock().acquireReadLock();
                try {
                    mInvalid.lock().acquireReadLock();
                    return mValid.size() + mInvalid.size();
                }
                finally {
                    mInvalid.lock().releaseLock();
                }
            }
            catch (InterruptedException e) {
                throw new UndeclaredThrowableException(e);
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
            finally {
                mValid.lock().releaseLock();
            }
        }

        public boolean isEmpty() {
            try {
                mValid.lock().acquireReadLock();
                try {
                    mInvalid.lock().acquireReadLock();
                    return mValid.isEmpty() && mInvalid.isEmpty();
                }
                finally {
                    mInvalid.lock().releaseLock();
                }
            }
            catch (InterruptedException e) {
                throw new UndeclaredThrowableException(e);
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
            finally {
                mValid.lock().releaseLock();
            }
        }

        public void invalidateAll(Depot.Filter filter) {
            try {
                // It is assumed that PersistentCollections support iteration
                // without requiring a long-held lock.
                PersistentIterator it = mValid.entrySet().iterator();
                while (it.hasNext()) {
                    PersistentMap.Entry entry = (PersistentMap.Entry)it.next();
                    Object key = entry.getKey();
                    if (filter.accept(key)) {
                        it.remove();
                        mInvalid.put(key, entry.getValue());
                    }
                }
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        public void invalidateAll() {
            try {
                // It is assumed that PersistentCollections support iteration
                // without requiring a long-held lock.
                PersistentIterator it = mValid.entrySet().iterator();
                while (it.hasNext()) {
                    PersistentMap.Entry entry = (PersistentMap.Entry)it.next();
                    it.remove();
                    mInvalid.put(entry.getKey(), entry.getValue());
                }
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        public void removeAll(Depot.Filter filter) {
            try {
                // It is assumed that PersistentCollections support iteration
                // without requiring a long-held lock.
                PersistentIterator it = mValid.keySet().iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    if (filter.accept(key)) {
                        it.remove();
                    }
                }
                it = mInvalid.keySet().iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    if (filter.accept(key)) {
                        it.remove();
                    }
                }
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        public void clear() {
            try {
                mValid.lock().acquireWriteLock();
                try {
                    mInvalid.lock().acquireWriteLock();
                    // The ObjectRepositoryMap iterates over its entries when
                    // clearing because the ObjectRepository is shared.
                    // Since I'm clearing EVERYTHING, this technique isn't
                    // the most efficient.
                    mValid.clear();
                    mInvalid.clear();
                }
                finally {
                    mInvalid.lock().releaseLock();
                }
            }
            catch (InterruptedException e) {
                throw new UndeclaredThrowableException(e);
            }
            catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
            finally {
                mValid.lock().releaseLock();
            }
        }
    }
}
