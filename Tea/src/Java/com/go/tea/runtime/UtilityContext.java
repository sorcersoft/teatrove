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
 * Extends the basic context to provide generic utility functions that most
 * templates will need to use for formatting.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public interface UtilityContext extends Context {

    /**
     * Returns a Date object with the current date and time.
     */
    public java.util.Date currentDate();

    /**
     * Tests if the given string starts with the given prefix.  
     * Returns true if the given string starts with the given prefix.
     *
     * @param str the source string
     * @param prefix the prefix to test for
     *
     * @return true if the given string starts with the given prefix
     */
    public boolean startsWith(String str, String prefix);

    /**
     * Tests if the given string ends with the given suffix.
     * Returns true if the given string ends with the given suffix.
     *
     * @param str the source string
     * @param suffix the suffix to test for
     * 
     * @return true if the given string ends with the given suffix
     */
    public boolean endsWith(String str, String suffix);

    /**
     * Finds the indices for each occurrence of the given search string in the
     * source string.  Returns an array of indices, which is empty if the 
     * search string wasn't found
     *
     * @param str the source string
     * @param search the string to search for 
     * 
     * @return an array of indices, which is empty if the search string 
     * wasn't found
     */
    public int[] find(String str, String search);

    /**
     * Finds the indices for each occurrence of the given search string in the
     * source string, starting from the given index.
     *
     * @param str the source string
     * @param search the string to search for 
     * @param fromIndex index to start the find
     * 
     * @return an array of indices, which is empty if the search string 
     * wasn't found
     */
    public int[] find(String str, String search, int fromIndex);

    /**
     * Finds the index of the first occurrence of the given search string in
     * the source string, or -1 if not found.
     *
     * @param str the source string
     * @param search the string to search for
     *
     * @return the start index of the found string or -1 if not found
     */
    public int findFirst(String str, String search);

    /**
     * Finds the index of the first occurrence of the given search string in
     * the source string, starting from the given index, or -1 if not found.
     *
     * @param str the source string
     * @param search the string to search for
     * @param fromIndex index to start the find
     *
     * @return the start index of the found string or -1 if not found
     */
    public int findFirst(String str, String search, int fromIndex);

    /**
     * Finds the index of the last occurrence of the given search string in the
     * source string, or -1 if not found.
     * 
     * @param str the source string 
     * @param search the string to search for
     *
     * @return the start index of the found string or -1 if not found
     */
    public int findLast(String str, String search);

    /**
     * Finds the index of the last occurrence of the given search string in the
     * source string, starting from the given index, or -1 if not found.
     *
     * @param str the source string 
     * @param search the string to search for
     * @param fromIndex optional index to start the find
     *
     * @return the start index of the found string or -1 if not found
     */
    public int findLast(String str, String search, int fromIndex);

    /**
     * Returns the trailing end of the given string, starting from the given
     * index.
     *
     * @param str the source string
     * @param startIndex the start index, inclusive 
     * 
     * @return the specified substring.
     */
    public String substring(String str, int startIndex);

    /**
     * Returns a sub-portion of the given string for the characters that are at
     * or after the starting index, and are before the end index.
     *
     * @param str the source string
     * @param startIndex the start index, inclusive 
     * @param endIndex the ending index, exclusive
     * 
     * @return the specified substring.
     */
    public String substring(String str, int startIndex, int endIndex);

    /**
     * Converts all the characters in the given string to lowercase.
     *
     * @param str the string to convert
     * 
     * @return the string converted to lowercase
     */
    public String toLowerCase(String str);

    /**
     * Converts all the characters in the given string to uppercase.
     *
     * @param str the string to convert
     * 
     * @return the string converted to uppercase
     */
    public String toUpperCase(String str);

    /**
     * Trims all leading and trailing whitespace characters from the given
     * string.
     *
     * @param str the string to trim 
     *
     * @return the trimmed string
     */
    public String trim(String str);

    /**
     * Trims all leading whitespace characters from the given string.
     *
     * @param str the string to trim 
     *
     * @return the trimmed string
     */
    public String trimLeading(String str);

    /**
     * Trims all trailing whitespace characters from the given string.
     *
     * @param str the string to trim 
     *
     * @return the trimmed string
     */
    public String trimTrailing(String str);

    /**
     * Replaces all exact matches of the given pattern in the source string 
     * with the provided replacement.
     * 
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns.
     *
     * @return the string with any replacements applied.
     */
    public String replace(String source, String pattern, String replacement);

    /**
     * Replaces all exact matches of the given pattern in the source string 
     * with the provided replacement, starting from the given index.
     * 
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns.
     * @param fromIndex index to start the replace
     *
     * @return the string with any replacements applied.
     */
    public String replace(String source, String pattern,
                          String replacement, int fromIndex);

    /**
     * Applies string replacements using the pattern-replacement pairs provided
     * by the given map (associative array). The longest matching pattern is
     * used for selecting an appropriate replacement.
     *
     * @param source the source string
     * @param patternReplacements pattern-replacement pairs
     */
    public String replace(String source, java.util.Map patternReplacements);

    /**
     * Replaces the first exact match of the given pattern in the source
     * string with the provided replacement.
     *
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns
     * 
     * @return the string with any replacements applied
     */
    public String replaceFirst(String source, String pattern,
                               String replacement);

    /**
     * Replaces the first exact match of the given pattern in the source
     * string with the provided replacement, starting from the given index.
     *
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns
     * @param fromIndex index to start the replace
     * 
     * @return the string with any replacements applied
     */
    public String replaceFirst(String source, String pattern,
                               String replacement, int fromIndex);

    /**
     * Replaces the last exact match of the given pattern in the source
     * string with the provided replacement.
     *
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns
     * 
     * @return the string with any replacements applied
     */
    public String replaceLast(String source, String pattern,
                              String replacement);

    /**
     * Replaces the last exact match of the given pattern in the source
     * string with the provided replacement, starting from the given index.
     *
     * @param source the source string
     * @param pattern the simple string pattern to search for
     * @param replacement the string to use for replacing matched patterns
     * @param fromIndex index to start the replace
     * 
     * @return the string with any replacements applied
     */
    public String replaceLast(String source, String pattern,
                              String replacement, int fromIndex);

    /**
     * A function that converts an integer to a short ordinal value. 
     * i.e. 1st, 2nd, 3rd etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the short ordinal value of the specified 
     * number
     */
    public String shortOrdinal(Long n);

    /**
     * A function that converts an integer to a short ordinal value. 
     * i.e. 1st, 2nd, 3rd etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the short ordinal value of the specified 
     * number
     *
     * @hidden       
     */
    public String shortOrdinal(long n);

    /**
     * A function that converts an integer to an ordinal value. i.e. first,
     * second, third etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the ordinal value of the specified number
     */
    public String ordinal(Long n);

    /**
     * A function that converts an integer to an ordinal value. i.e. first,
     * second, third etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the ordinal value of the specified number
     *
     * @hidden       
     */
    public String ordinal(long n);

    /**
     * A function that converts an integer to a cardinal value. i.e. one,
     * two, three etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the cardinal value of the specified number
     */
    public String cardinal(Long n);

    /**
     * A function that converts an integer to a cardinal value. i.e. one,
     * two, three etc.
     *
     * @param n the number to convert
     * 
     * @return a String containing the cardinal value of the specified number
     *
     * @hidden       
     */
    public String cardinal(long n);
}
