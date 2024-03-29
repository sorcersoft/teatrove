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

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.TimeZone;
import java.util.Locale;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import com.go.tea.util.BeanAnalyzer;
import com.go.trove.util.Pair;
import com.go.trove.util.FastDateFormat;

/******************************************************************************
 * The default runtime context class that Tea templates get compiled to use.
 * All functions callable from a template are defined in the context. To add
 * more or override existing ones, do so when extending this class.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/07/03 <!-- $-->
 */
public abstract class DefaultContext implements UtilityContext {

    private static final String DEFAULT_NULL_FORMAT = "null";

    // Although the Integer.toString method keeps getting more optimized
    // with each release, it still isn't very fast at converting small values.
    private static final String[] INT_VALUES = {
         "0",  "1",  "2",  "3",  "4",  "5",  "6",  "7",  "8",  "9", 
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", 
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", 
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", 
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", 
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", 
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", 
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", 
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", 
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", 
    };

    private static final int FIRST_INT_VALUE = 0;
    private static final int LAST_INT_VALUE = 99;

    private static Map cLocaleCache;
    private static Map cNFormatCache;

    static {
        cLocaleCache = Collections.synchronizedMap(new HashMap(7));
        cNFormatCache = Collections.synchronizedMap(new HashMap(47));
    }

    private Locale mLocale;
    private String mNullFormat = DEFAULT_NULL_FORMAT;
    private FastDateFormat mDateFormat;
    private NFormat mNFormat;


    public DefaultContext() {
    }

    /**
     * Method that is the runtime receiver. Implementations should call one
     * of the toString methods when converting this object to a string.
     * <p>
     * NOTE:  This method should <b>not</b> be called directly within a 
     * template.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeReceiver
     * @hidden
     */
    public abstract void print(Object obj) throws Exception;


    /**
     * @hidden
     */
    public void print(Date date) throws Exception {
        print(toString(date));
    }

    /**
     * @hidden
     */
    public void print(Number n) throws Exception {
        print(toString(n));
    }

    /**
     * @hidden
     */
    public void print(int n) throws Exception {
        print(toString(n));
    }

    /**
     * @hidden
     */
    public void print(float n) throws Exception {
        print(toString(n));
    }

    /**
     * @hidden
     */
    public void print(long n) throws Exception {
        print(toString(n));
    }

    /**
     * @hidden
     */
    public void print(double n) throws Exception {
        print(toString(n));
    }

    /**
     * @hidden
     */
    public String toString(Object obj) {
        if (obj == null) {
            return mNullFormat;
        }
        else if (obj instanceof String) {
            return (String)obj;
        }
        else if (obj instanceof Date) {
            return toString((Date)obj);
        }
        else if (obj instanceof Number) {
            return toString((Number)obj);
        }
        else {
            String str = obj.toString();
            return (str == null) ? mNullFormat : str;
        }
    }

    /**
     * @hidden
     */
    public String toString(String str) {
        return (str == null) ? mNullFormat : str;
    }

    /**
     * @hidden
     */
    public String toString(Date date) {
        if (date == null) {
            return mNullFormat;
        }

        if (mDateFormat == null) {
            dateFormat(null);
        }

        return mDateFormat.format(date);
    }

    /**
     * @hidden
     */
    public String toString(Number n) {
        if (n == null) {
            return mNullFormat;
        }
        else if (mNFormat == null) {
            if (n instanceof Integer) {
                return toString(((Integer)n).intValue());
            }
            else if (n instanceof Long) {
                return toString(((Long)n).longValue());
            }
            else {
                return n.toString();
            }
        }
        else {
            return mNFormat.format(n);
        }
    }

    /**
     * @hidden
     */
    public String toString(int n) {
        if (mNFormat == null) {
            if (n <= LAST_INT_VALUE && n >= FIRST_INT_VALUE) {
                return INT_VALUES[n];
            }
            else {
                return Integer.toString(n);
            }
        }
        else {
            return mNFormat.format(n);
        }
    }

    /**
     * @hidden
     */
    public String toString(float n) {
        return (mNFormat == null) ? Float.toString(n) : mNFormat.format(n);
    }

    /**
     * @hidden
     */
    public String toString(long n) {
        if (mNFormat == null) {
            if (n <= LAST_INT_VALUE && n >= FIRST_INT_VALUE) {
                return INT_VALUES[(int)n];
            }
            else {
                return Long.toString(n);
            }
        }
        else {
            return mNFormat.format(n);
        }
    }

    /**
     * @hidden
     */
    public String toString(double n) {
        return (mNFormat == null) ? Double.toString(n) : mNFormat.format(n);
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            mLocale = null;
            mDateFormat = null;
            mNFormat = null;
        }
        else {
            synchronized (cLocaleCache) {
                Locale cached = (Locale)cLocaleCache.get(locale);
                if (cached == null) {
                    cLocaleCache.put(locale, locale);
                }
                else {
                    locale = cached;
                }
            }
            
            mLocale = locale;
            dateFormat(null);
            numberFormat(null);
        }
    }

    public void setLocale(String language, String country) {
        setLocale(new Locale(language, country));
    }

    public void setLocale(String language, String country, String variant) {
        setLocale(new Locale(language, country, variant));
    }

    public java.util.Locale getLocale() {
        return mLocale;
    }

    public Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    public void nullFormat(String format) {
        mNullFormat = (format == null) ? DEFAULT_NULL_FORMAT : format;
    }

    public String getNullFormat() {
        return mNullFormat;
    }

    public void dateFormat(String format) {
        dateFormat(format, null);
    }

    public void dateFormat(String format, String timeZoneID) {
        TimeZone timeZone;
        if (timeZoneID != null) {
            timeZone = TimeZone.getTimeZone(timeZoneID);
        }
        else {
            timeZone = null;
        }

        if (format == null) {
            mDateFormat = FastDateFormat.getDateTimeInstance
                (FastDateFormat.LONG, FastDateFormat.LONG, timeZone, mLocale);
        }
        else {
            mDateFormat = FastDateFormat.getInstance
                (format, timeZone, mLocale);
        }
    }

    public String getDateFormat() {
        if (mDateFormat == null) {
            dateFormat(null);
        }
        return mDateFormat.getPattern();
    }

    public String getDateFormatTimeZone() {
        if (mDateFormat == null) {
            dateFormat(null);
        }
        TimeZone timeZone = mDateFormat.getTimeZone();
        return timeZone == null ? null : timeZone.getID();
    }

    public TimeZone[] getAvailableTimeZones() {
        String[] IDs = TimeZone.getAvailableIDs();
        TimeZone[] zones = new TimeZone[IDs.length];
        for (int i=zones.length; --i >= 0; ) {
            zones[i] = TimeZone.getTimeZone(IDs[i]);
        }
        return zones;
    }

    public void numberFormat(String format) {
        numberFormat(format, null, null);
    }

    public void numberFormat(String format, String infinity, String NaN) {
        if (format == null && infinity == null && NaN == null) {
            if (mLocale == null) {
                mNFormat = null;
            }
            else {
                mNFormat =
                    new NFormat(NumberFormat.getNumberInstance(mLocale));
            }
            return;
        }

        Object key;
        if (mLocale == null) {
            key = format;
        }
        else {
            key = new Pair(format, mLocale);
        }

        if (infinity != null || NaN != null) {
            key = new Pair(key, infinity);
            key = new Pair(key, NaN);
        }
        
        if ((mNFormat = 
             (NFormat)cNFormatCache.get(key)) == null) {

            DecimalFormat df;
            
            if (mLocale == null) {
                if (format == null) {
                    df = new DecimalFormat();
                }
                else {
                    df = new DecimalFormat(format);
                }
            }
            else {
                DecimalFormatSymbols symbols =
                    new DecimalFormatSymbols(mLocale);
                if (format == null) {
                    df = new DecimalFormat();
                    df.setDecimalFormatSymbols(symbols);
                }
                else {
                    df = new DecimalFormat(format, symbols);
                }
            }
            
            if (infinity != null || NaN != null) {
                DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                symbols = (DecimalFormatSymbols)symbols.clone();
                if (infinity != null) {
                    symbols.setInfinity(infinity);
                }
                if (NaN != null) {
                    symbols.setNaN(NaN);
                }
                df.setDecimalFormatSymbols(symbols);
            }

            mNFormat = new NFormat(df);
            cNFormatCache.put(key, mNFormat);
        }
    }

    public String getNumberFormat() {
        return mNFormat == null ? null : mNFormat.getNumberFormat();
    }

    public String getNumberFormatInfinity() {
        return mNFormat == null ? null : mNFormat.getNumberFormatInfinity();
    }

    public String getNumberFormatNaN() {
        return mNFormat == null ? null : mNFormat.getNumberFormatNaN();
    }

    public Date currentDate() {
        return new Date();
    }

    public boolean startsWith(String str, String prefix) {
        return (str == null || prefix == null) ? (str == prefix) :
            str.startsWith(prefix);
    }

    public boolean endsWith(String str, String suffix) {
        return (str == null || suffix == null) ? (str == suffix) :
            str.endsWith(suffix);
    }

    public int[] find(String str, String search) {
        return find(str, search, 0);
    }

    public int[] find(String str, String search, int fromIndex) {
        if (str == null || search == null) {
            return new int[0];
        }

        int[] indices = new int[10];
        int size = 0;

        int index = fromIndex;
        while ((index = str.indexOf(search, index)) >= 0) {
            if (size >= indices.length) {
                // Expand capacity.
                int[] newArray = new int[indices.length * 2];
                System.arraycopy(indices, 0, newArray, 0, indices.length);
                indices = newArray;
            }
            indices[size++] = index;
            index += search.length();
        }

        if (size < indices.length) {
            // Trim capacity.
            int[] newArray = new int[size];
            System.arraycopy(indices, 0, newArray, 0, size);
            indices = newArray;
        }

        return indices;
    }

    public int findFirst(String str, String search) {
        return (str == null || search == null) ? -1 :
            str.indexOf(search);
    }

    public int findFirst(String str, String search, int fromIndex) {
        return (str == null || search == null) ? -1 :
            str.indexOf(search, fromIndex);
    }

    public int findLast(String str, String search) {
        return (str == null || search == null) ? -1 :
            str.lastIndexOf(search);
    }

    public int findLast(String str, String search, int fromIndex) {
        return (str == null || search == null) ? -1 :
            str.lastIndexOf(search, fromIndex);
    }

    public String substring(String str, int startIndex) {
        return (str == null) ? null : str.substring(startIndex);
    }

    public String substring(String str, int startIndex, int endIndex) {
        return (str == null) ? null : str.substring(startIndex, endIndex);
    }

    public String toLowerCase(String str) {
        return (str == null) ? null : str.toLowerCase();
    }

    public String toUpperCase(String str) {
        return (str == null) ? null : str.toUpperCase();
    }

    public String trim(String str) {
        return (str == null) ? null : str.trim();
    }

    public String trimLeading(String str) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        for (int i=0; i<length; i++) {
            if (str.charAt(i) > ' ') {
                return str.substring(i);
            }
        }

        return "";
    }

    public String trimTrailing(String str) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        for (int i=length-1; i>=0; i--) {
            if (str.charAt(i) > ' ') {
                return str.substring(0, i + 1);
            }
        }

        return "";
    }

    public String replace(String source, String pattern, String replacement) {
        return replace(source, pattern, replacement, 0);
    }

    public String replace(String source, String pattern,
                          String replacement, int fromIndex) {
        if (source == null) {
            if (pattern == null) {
                return replacement;
            }
            else {
                return source;
            }
        }

        int patternLength;
        if (pattern == null || (patternLength = pattern.length()) == 0) {
            return source;
        }

        if (replacement == null) {
            replacement = toString(replacement);
        }

        int sourceLength = source.length();

        StringBuffer buf;
        if (fromIndex <= 0) {
            fromIndex = 0;
            buf = new StringBuffer(sourceLength);
        }
        else if (fromIndex < sourceLength) {
            buf = new StringBuffer(sourceLength);
            buf.append(source.substring(0, fromIndex));
        }
        else {
            return source;
        }
        
    sourceScan:
        for (int s = fromIndex; s < sourceLength; ) {
            int k = s;
            for (int j=0; j<patternLength; j++, k++) {
                if (k >= sourceLength || 
                    source.charAt(k) != pattern.charAt(j)) {

                    buf.append(source.charAt(s));
                    s++;
                    continue sourceScan;
                }
            }

            buf.append(replacement);
            s = k;
        }

        return buf.toString();
    }

    public String replace(String source, Map patternReplacements) {
        if (source == null) {
            return null;
        }

        int mapSize = patternReplacements.size();
        String[] patterns = new String[mapSize];
        String[] replacements = new String[mapSize];

        Iterator it = patternReplacements.entrySet().iterator();
        for (int i=0; it.hasNext(); i++) {
            Map.Entry entry = (Map.Entry)it.next();
            
            patterns[i] = toString(entry.getKey());
            replacements[i] = toString(entry.getValue());
        }

        return replace(source, patterns, replacements);
    }

    private static String replace(String source, String[] patterns, 
                                  String[] replacements)
    {
        int patternsLength = patterns.length;

        int sourceLength = source.length();
        StringBuffer buf = new StringBuffer(sourceLength);

        for (int s=0; s<sourceLength; ) {
            int longestPattern = 0;
            int closestPattern = -1;

        patternScan:
            for (int i=0; i<patternsLength; i++) {
                String pattern = patterns[i];
                int patternLength = pattern.length();

                if (patternLength > 0) {
                    for (int j=0, k=s; j<patternLength; j++, k++) {
                        if (k >= sourceLength || 
                            source.charAt(k) != pattern.charAt(j)) {
                            
                            continue patternScan;
                        }
                    }

                    if (patternLength > longestPattern) {
                        longestPattern = patternLength;
                        closestPattern = i;
                    }
                }
            }

            if (closestPattern >= 0) {
                buf.append(replacements[closestPattern]);
                s += longestPattern;
            }
            else {
                buf.append(source.charAt(s));
                s++;
            }
        }

        return buf.toString();
    }

    public String replaceFirst(String source, String pattern,
                               String replacement) {
        return replaceOne(source, pattern, replacement,
                          findFirst(source, pattern));
    }

    public String replaceFirst(String source, String pattern,
                               String replacement, int fromIndex) {
        return replaceOne(source, pattern, replacement,
                          findFirst(source, pattern, fromIndex));
    }

    public String replaceLast(String source, String pattern,
                              String replacement) {
        return replaceOne(source, pattern, replacement,
                          findLast(source, pattern));
    }

    public String replaceLast(String source, String pattern,
                              String replacement, int fromIndex) {
        return replaceOne(source, pattern, replacement,
                          findLast(source, pattern, fromIndex));
    }

    private String replaceOne(String source, String pattern,
                              String replacement, int atIndex) {
        if (atIndex < 0) {
            if (source == null && pattern == null) {
                return replacement;
            }
            else {
                return source;
            }
        }

        if (replacement == null) {
            replacement = toString(replacement);
        }

        StringBuffer buf = new StringBuffer
            (source.length() - pattern.length() + replacement.length());
        buf.append(source.substring(0, atIndex));
        buf.append(replacement);
        buf.append(source.substring(atIndex + pattern.length()));

        return buf.toString();
    }

    public String shortOrdinal(Long n) {
        return (n == null) ? null : shortOrdinal(n.longValue());
    }

    /**
     * @hidden
     */
    public String shortOrdinal(long n) {
        String str = Long.toString(n);

        if (n < 0) {
            n = -n;
        }

        n %= 100;

        if (n >= 10 && n <= 20) {
            str += "th";
        }
        else {
            if (n > 20) n %= 10;
            
            switch ((int)n) {
            case 1:
                str += "st"; break;
            case 2:
                str += "nd"; break;
            case 3:
                str += "rd"; break;
            default:
                str += "th"; break;
            }
        }

        return str;
    }

    public String ordinal(Long n) {
        return (n == null) ? null : ordinal(n.longValue());
    }

    /**
     * @hidden
     */
    public String ordinal(long n) {
        if (n == 0) {
            return "zeroth";
        }
        
        StringBuffer buf = new StringBuffer(20);

        if (n < 0) {
            buf.append("negative ");
            n = -n;
        }

        n = cardinalGroup(buf, n, 1000000000000000000L, "quintillion");
        n = cardinalGroup(buf, n, 1000000000000000L, "quadrillion");
        n = cardinalGroup(buf, n, 1000000000000L, "trillion");
        n = cardinalGroup(buf, n, 1000000000L, "billion");
        n = cardinalGroup(buf, n, 1000000L, "million");
        n = cardinalGroup(buf, n, 1000L, "thousand");

        if (n == 0) {
            buf.append("th");
        }
        else {
            cardinal999(buf, n, true);
        }

        return buf.toString();
    }

    public String cardinal(Long n) {
        return (n == null) ? null : cardinal(n.longValue());
    }

    /**
     * @hidden
     */
    public String cardinal(long n) {
        if (n == 0) {
            return "zero";
        }
        
        StringBuffer buf = new StringBuffer(20);

        if (n < 0) {
            buf.append("negative ");
            n = -n;
        }

        n = cardinalGroup(buf, n, 1000000000000000000L, "quintillion");
        n = cardinalGroup(buf, n, 1000000000000000L, "quadrillion");
        n = cardinalGroup(buf, n, 1000000000000L, "trillion");
        n = cardinalGroup(buf, n, 1000000000L, "billion");
        n = cardinalGroup(buf, n, 1000000L, "million");
        n = cardinalGroup(buf, n, 1000L, "thousand");

        cardinal999(buf, n, false);

        return buf.toString();
    }

    private static long cardinalGroup(StringBuffer buf, long n, 
                                      long threshold, String groupName) {
        if (n >= threshold) {
            cardinal999(buf, n / threshold, false);
            buf.append(' ');
            buf.append(groupName);
            n %= threshold;
            if (n >= 100) {
                buf.append(", ");
            }
            else if (n != 0) {
                buf.append(" and ");
            }
        }

        return n;
    }

    private static void cardinal999(StringBuffer buf, long n, 
                                    boolean ordinal) {
        n = cardinalGroup(buf, n, 100L, "hundred");

        if (n == 0) {
            if (ordinal) {
                buf.append("th");
            }
            return;
        }

        if (n >= 20) {
            switch ((int)n / 10) {
            case 2:
                buf.append("twen");
                break;
            case 3:
                buf.append("thir");
                break;
            case 4:
                buf.append("for");
                break;
            case 5:
                buf.append("fif");
                break;
            case 6:
                buf.append("six");
                break;
            case 7:
                buf.append("seven");
                break;
            case 8:
                buf.append("eigh");
                break;
            case 9:
                buf.append("nine");
                break;
            }

            n %= 10;
            if (n != 0) {
                buf.append("ty-");
            }
            else {
                if (!ordinal) {
                    buf.append("ty");
                }
                else {
                    buf.append("tieth");
                }
            }
        }
        
        switch ((int)n) {
        case 1:
            if (!ordinal) {
                buf.append("one");
            }
            else {
                buf.append("first");
            }
            break;
        case 2:
            if (!ordinal) {
                buf.append("two");
            }
            else {
                buf.append("second");
            }
            break;
        case 3:
            if (!ordinal) {
                buf.append("three");
            }
            else {
                buf.append("third");
            }
            break;
        case 4:
            if (!ordinal) {
                buf.append("four");
            }
            else {
                buf.append("fourth");
            }
            break;
        case 5:
            if (!ordinal) {
                buf.append("five");
            }
            else {
                buf.append("fifth");
            }
            break;
        case 6:
            if (!ordinal) {
                buf.append("six");
            }
            else {
                buf.append("sixth");
            }
            break;
        case 7:
            if (!ordinal) {
                buf.append("seven");
            }
            else {
                buf.append("seventh");
            }
            break;
        case 8:
            if (!ordinal) {
                buf.append("eight");
            }
            else {
                buf.append("eighth");
            }
            break;
        case 9:
            if (!ordinal) {
                buf.append("nine");
            }
            else {
                buf.append("ninth");
            }
            break;
        case 10:
            if (!ordinal) {
                buf.append("ten");
            }
            else {
                buf.append("tenth");
            }
            break;
        case 11:
            if (!ordinal) {
                buf.append("eleven");
            }
            else {
                buf.append("eleventh");
            }
            break;
        case 12:
            if (!ordinal) {
                buf.append("twelve");
            }
            else {
                buf.append("twelfth");
            }
            break;
        case 13:
            buf.append("thirteen");
            if (ordinal) buf.append("th");
            break;
        case 14:
            buf.append("fourteen");
            if (ordinal) buf.append("th");
            break;
        case 15:
            buf.append("fifteen");
            if (ordinal) buf.append("th");
            break;
        case 16:
            buf.append("sixteen");
            if (ordinal) buf.append("th");
            break;
        case 17:
            buf.append("seventeen");
            if (ordinal) buf.append("th");
            break;
        case 18:
            buf.append("eighteen");
            if (ordinal) buf.append("th");
            break;
        case 19:
            buf.append("nineteen");
            if (ordinal) buf.append("th");
            break;
        }
    }

    private static class NFormat {
        private final NumberFormat mFormat;
        private final boolean mFormatInteger;

        public NFormat(NumberFormat format) {
            mFormat = format;
            boolean formatInteger = true;

            if (format instanceof DecimalFormat) {
                DecimalFormat dformat = (DecimalFormat)format;

                // Check if the number format actually does anything special
                // for integers.

                formatInteger =
                    dformat.isDecimalSeparatorAlwaysShown() ||
                    dformat.isGroupingUsed() ||
                    dformat.getMinimumIntegerDigits() > 1 ||
                    dformat.getMinimumFractionDigits() > 0 ||
                    dformat.getPositiveSuffix().length() > 0 ||
                    dformat.getPositivePrefix().length() > 0 ||
                    dformat.getNegativeSuffix().length() > 0 ||
                    (!("-".equals(dformat.getNegativePrefix())));

                if (!formatInteger) {
                    formatInteger = dformat.getDecimalFormatSymbols()
                        .getZeroDigit() != '0';
                }
            }

            mFormatInteger = formatInteger;
        }

        public String format(int value) {
            return (!mFormatInteger) ?
                Integer.toString(value) : mFormat.format(value);
        }

        public String format(float value) {
            return mFormat.format(value);
        }

        public String format(long value) {
            return (!mFormatInteger) ?
                Long.toString(value) : mFormat.format(value);
        }

        public String format(double value) {
            return mFormat.format(value);
        }

        public String format(Number value) {
            if (value instanceof Integer) {
                return format(((Integer)value).intValue());
            }
            else if (value instanceof Long) {
                return format(((Long)value).longValue());
            }
            else {
                return mFormat.format(value);
            }
        }

        public String getNumberFormat() {
            if (mFormat instanceof DecimalFormat) {
                return ((DecimalFormat)mFormat).toPattern();
            }
            return null;
        }

        public String getNumberFormatInfinity() {
            if (mFormat instanceof DecimalFormat) {
                DecimalFormatSymbols symbols =
                    ((DecimalFormat)mFormat).getDecimalFormatSymbols();
                return symbols.getInfinity();
            }
            return null;
        }

        public String getNumberFormatNaN() {
            if (mFormat instanceof DecimalFormat) {
                DecimalFormatSymbols symbols =
                    ((DecimalFormat)mFormat).getDecimalFormatSymbols();
                return symbols.getNaN();
            }
            return null;
        }
    }
}
