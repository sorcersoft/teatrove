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

package com.go.trove.file;

import java.io.IOException;
import java.io.InterruptedIOException;
import com.go.trove.util.ReadWriteLock;

/******************************************************************************
 * Various utilities for operating on a file at the bit level.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/04 <!-- $-->
 */
public class Bitlist {
    private static int findSubIndex(byte v) {
        return (v<0)?0:((v<16)?((v<4)?((v<2)?7:6):((v<8)?5:4)):((v<64)?((v<32)?3:2):1));
    }

    private final FileBuffer mFile;

    public Bitlist(FileBuffer file) {
        mFile = file;
    }

    /**
     * Set the bit at the given index to one.
     */
    public void set(long index) throws IOException {
        long pos = index >> 3;
        try {
            lock().acquireUpgradableLock();
            int value = mFile.read(pos);
            int newValue;
            if (value <= 0) {
                newValue = (0x00000080 >> (index & 7));
            }
            else {
                newValue = value | (0x00000080 >> (index & 7));
            }
            if (newValue != value) {
                mFile.write(pos, newValue);
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
    }

    /**
     * Clear the bit at the given index to zero.
     */
    public void clear(long index) throws IOException {
        long pos = index >> 3;
        try {
            lock().acquireUpgradableLock();
            int value = mFile.read(pos);
            int newValue;
            if (value <= 0) {
                newValue = 0;
            }
            else {
                newValue = value & (0xffffff7f >> (index & 7));
            }
            if (newValue != value) {
                mFile.write(pos, newValue);
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
    }

    /**
     * Returns false if bit is clear (zero), true if bit is set (one). If bit
     * is beyond the file buffer size, false is returned.
     */
    public boolean get(long index) throws IOException {
        try {
            lock().acquireReadLock();
            int value = mFile.read(index >> 3);
            return value > 0 && ((value << (index & 7)) & 0x80) != 0;
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
    }

    /**
     * Searches the bitlist for the first set bit and returns the index to it.
     *
     * @param start first index to begin search.
     */
    public long findFirstSet(long start) throws IOException {
        return findFirstSet(start, new byte[128]);
    }

    /**
     * Searches the bitlist for the first set bit and returns the index to it.
     *
     * @param start first index to begin search.
     * @param temp temporary byte array for loading bits.
     * @return index to first set bit or -1 if not found.
     */
    public long findFirstSet(long start, byte[] temp) throws IOException {
        long pos = start >> 3;
        try {
            lock().acquireReadLock();
            while (true) {
                int amt = mFile.read(pos, temp, 0, temp.length);
                if (amt <= 0) {
                    return -1;
                }
                for (int i=0; i<amt; i++, pos++) {
                    byte val;
                    if ((val = temp[i]) != 0) {
                        long index = pos << 3;
                        if (index < start) {
                            // Clear the upper bits to skip check.
                            val &= (0x000000ff >>> (start - index));
                            if (val == 0) {
                                // False alarm.
                                continue;
                            }
                        }
                        return index + findSubIndex(val);
                    }
                }
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
    }

    /**
     * Searches the bitlist for the first clear bit and returns the index to
     * it.
     *
     * @param start first index to begin search.
     */
    public long findFirstClear(long start) throws IOException {
        return findFirstClear(start, new byte[128]);
    }

    /**
     * Searches the bitlist for the first clear bit and returns the index to
     * it.
     *
     * @param start first index to begin search.
     * @param temp temporary byte array for loading bits.
     * @return index to first set bit or -1 if not found.
     */
    public long findFirstClear(long start, byte[] temp) throws IOException {
        long pos = start >> 3;
        try {
            lock().acquireReadLock();
            while (true) {
                int amt = mFile.read(pos, temp, 0, temp.length);
                if (amt <= 0) {
                    return -1;
                }
                for (int i=0; i<amt; i++, pos++) {
                    byte val;
                    if ((val = temp[i]) != -1) {
                        long index = pos << 3;
                        if (index < start) {
                            // Set the upper bits to skip check.
                            val |= (0xffffff00 >> (start - index));
                            if (val == -1) {
                                // False alarm.
                                continue;
                            }
                        }
                        return index + findSubIndex((byte)(val ^ 0xff));
                    }
                }
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
    }

    /**
     * Counts all the set bits.
     */
    public long countSetBits() throws IOException {
        byte[] temp;
        long size = mFile.size();
        if (size > 1024) {
            temp = new byte[1024];
        }
        else {
            temp = new byte[(int)size];
        }

        long pos = 0;
        long count = 0;
        try {
            lock().acquireReadLock();
            while (true) {
                int amt = mFile.read(pos, temp, 0, temp.length);
                if (amt <= 0) {
                    break;
                }
                for (int i=0; i<amt; i++) {
                    byte val = temp[i];
                    switch (val & 15) {
                    default: break;
                    case 1: case 2: case 4: case 8: count++; break;
                    case 3: case 5: case 6: case 9: case 10: case 12:
                        count += 2; break;
                    case 7: case 11: case 13: case 14: count += 3; break;
                    case 15: count += 4; break;
                    }
                    switch ((val >> 4) & 15) {
                    default: break;
                    case 1: case 2: case 4: case 8: count++; break;
                    case 3: case 5: case 6: case 9: case 10: case 12:
                        count += 2; break;
                    case 7: case 11: case 13: case 14: count += 3; break;
                    case 15: count += 4; break;
                    }
                }
                pos += amt;
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
        return count;
    }

    /**
     * Counts all the clear bits.
     */
    public long countClearBits() throws IOException {
        byte[] temp;
        long size = mFile.size();
        if (size > 1024) {
            temp = new byte[1024];
        }
        else {
            temp = new byte[(int)size];
        }

        long pos = 0;
        long count = 0;
        try {
            lock().acquireReadLock();
            while (true) {
                int amt = mFile.read(pos, temp, 0, temp.length);
                if (amt <= 0) {
                    break;
                }
                for (int i=0; i<amt; i++) {
                    byte val = temp[i];
                    switch (val & 15) {
                    case 0: count += 4; break;
                    case 1: case 2: case 4: case 8: count += 3; break;
                    case 3: case 5: case 6: case 9: case 10: case 12:
                        count += 2; break;
                    case 7: case 11: case 13: case 14: count++; break;
                    default: break;
                    }
                    switch ((val >> 4) & 15) {
                    case 0: count += 4; break;
                    case 1: case 2: case 4: case 8: count += 3; break;
                    case 3: case 5: case 6: case 9: case 10: case 12:
                        count += 2; break;
                    case 7: case 11: case 13: case 14: count++; break;
                    default: break;
                    }
                }
                pos += amt;
            }
        }
        catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        finally {
            lock().releaseLock();
        }
        return count;
    }

    /**
     * Counts all the set bits.
     *
     * @param start index to start counting, inclusive.
     * @param end index to stop counting, exclusive. If negative, count to the
     * end.
     * @param temp temporary byte array for loading bits.
     */
    /*
    public long countSetBits(long start, long end, byte[] temp)
        throws IOException
    {
    }
    */

    /**
     * Returns the number of bits in this list.
     */
    public long size() throws IOException {
        return mFile.size() * 8;
    }

    public ReadWriteLock lock() {
        return mFile.lock();
    }

    public boolean force() throws IOException {
        return mFile.force();
    }
}
