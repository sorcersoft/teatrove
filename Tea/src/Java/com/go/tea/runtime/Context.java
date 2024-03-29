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
 * Interface establishes basic printing and formatting functions for a template
 * runtime context.
 * 
 * @see com.go.tea.compiler.Compiler#getRuntimeContext
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/04/19 <!-- $-->
 * @see DefaultContext
 */
public interface Context extends OutputReceiver {

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
    public void print(Object obj) throws Exception;

    /**
     * @hidden
     */
    public void print(java.util.Date date) throws Exception;

    /**
     * @hidden
     */
    public void print(Number n) throws Exception;

    /**
     * @hidden
     */
    public void print(int n) throws Exception;

    /**
     * @hidden
     */
    public void print(float n) throws Exception;

    /**
     * @hidden
     */
    public void print(long n) throws Exception;

    /**
     * @hidden
     */
    public void print(double n) throws Exception;

    /**
     * A function that converts an object to a string, applying any applicable
     * formatting settings. The returned String is never null.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(Object obj);

    /**
     * A function that converts a string to the null format string if it is
     * null. Otherwise, it is returned unchanged.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(String str);

    /**
     * A function that converts a date to a string, using the current
     * date format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(java.util.Date date);

    /**
     * A function that converts a number to a string, using the current
     * number format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(Number n);

    /**
     * A function that converts a number to a string, using the current
     * number format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(int n);

    /**
     * A function that converts a number to a string, using the current
     * number format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(float n);

    /**
     * A function that converts a number to a string, using the current
     * number format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(long n);

    /**
     * A function that converts a number to a string, using the current
     * number format.
     *
     * @see com.go.tea.compiler.Compiler#getRuntimeStringConverter
     * @hidden
     */
    public String toString(double n);

    /**
     * Setting the locale resets date and number formats to the default for
     * that locale. Setting a locale of null resets date and number formats
     * to the system defaults.
     * <p>
     * See getAvailableLocales to get all the available language, country 
     * and variant codes. 
     *
     * @param locale pre-constructed locale object
     */
    public void setLocale(java.util.Locale locale);

    /**
     * Setting the locale resets date and number formats to the default for
     * that locale.
     *
     * @param language language code
     * @param country country code
     */
    public void setLocale(String language, String country);

    /**
     * Setting the locale resets date and number formats to the default for
     * that locale.
     *
     * @param language language code
     * @param country country code
     * @param variant optional variant code
     */
    public void setLocale(String language, String country, String variant);

    /**
     * Returns the current locale setting.
     */
    public java.util.Locale getLocale();

    /**
     * Returns a list of all the available locales.
     */
    public java.util.Locale[] getAvailableLocales();

    /**
     * A function that sets the formatted value of null object references.
     *
     * @param format string to print in place of "null".
     */
    public void nullFormat(String format);

    /**
     * Returns the current null format specification.
     */
    public String getNullFormat();

    /**
     * Defines a format to use when printing dates from templates. 
     * Passing null sets the format back to the default.
     * <p> 
     * <strong>Time Format Syntax:</strong> 
     * <p> 
     * To specify the time format use a <em>time pattern</em> string. 
     * In this pattern, all ASCII letters are reserved as pattern letters, 
     * which are defined as the following: 
     *
     * <blockquote> 
     * <pre> 
     * Symbol   Meaning                 Presentation        Example 
     * ------   -------                 ------------        ------- 
     * G        era designator          (Text)              AD 
     * y        year                    (Number)            1996 
     * M        month in year           (Text & Number)     July & 07 
     * d        day in month            (Number)            10 
     * h        hour in am/pm (1~12)    (Number)            12 
     * H        hour in day (0~23)      (Number)            0 
     * m        minute in hour          (Number)            30 
     * s        second in minute        (Number)            55 
     * S        millisecond             (Number)            978 
     * E        day in week             (Text)              Tuesday 
     * D        day in year             (Number)            189 
     * F        day of week in month    (Number)            2 (2nd Wed in July)
     * w        week in year            (Number)            27 
     * W        week in month           (Number)            2 
     * a        am/pm marker            (Text)              PM 
     * k        hour in day (1~24)      (Number)            24 
     * K        hour in am/pm (0~11)    (Number)            0 
     * z        time zone               (Text)              Pacific Standard Time 
     * '        escape for text         (Delimiter) 
     * ''       single quote            (Literal)           ' 
     * </pre> 
     * </blockquote> 
     * The count of pattern letters determine the format. 
     * <p> 
     * <strong>(Text)</strong>: 4 or more pattern letters--use full form, 
     * less than 4--use short or abbreviated form if one exists. 
     * <p> 
     * <strong>(Number)</strong>: the minimum number of digits. Shorter 
     * numbers are zero-padded to this amount. Year is handled specially; 
     * that is, if the count of 'y' is 2, the Year will be truncated to 2
     * digits. 
     * <p> 
     * <strong>(Text & Number)</strong>: 3 or over, use text, otherwise use 
     * number.
     * <p> 
     * Any characters in the pattern that are not in the ranges of 
     * ['a'..'z'] and ['A'..'Z'] will be treated as quoted text. For instance,
     * characters like ':', '.', ' ', '#' and '@' will appear in the resulting
     * time text even they are not embraced within single quotes. 
     * <p> 
     * A pattern containing any invalid pattern letter will result in a thrown
     * exception during formatting or parsing. 
     * <p> 
     * <strong>Examples:</strong> 
     * <blockquote> 
     * <pre> 
     * Format Pattern                         Result 
     * --------------                         ------- 
     * "yyyy.MM.dd G 'at' hh:mm:ss z"    ->>  1996.07.10 AD at 15:08:56 PDT 
     * "EEE, MMM d, ''yy"                ->>  Wed, July 10, '96 
     * "h:mm a"                          ->>  12:08 PM 
     * "hh 'o''clock' a, zzzz"           ->>  12 o'clock PM, Pacific Daylight Time 
     * "K:mm a, z"                       ->>  0:00 PM, PST 
     * "yyyyy.MMMMM.dd GGG hh:mm aaa"    ->>  1996.July.10 AD 12:08 PM 
     * </pre> 
     * </blockquote> 
     *
     * @param format date format specification string
     */
    public void dateFormat(String format);

    /**
     * A function that sets the formatted value of dates.
     *
     * @param format date format specification string
     * @param timeZoneID time zone ID, i.e. "PST"
     */
    public void dateFormat(String format, String timeZoneID);

    /**
     * Returns the current date format specification.
     */
    public String getDateFormat();

    /**
     * Returns the current date format time zone.
     */
    public String getDateFormatTimeZone();

    /**
     * Returns a list of all the available time zones.
     */
    public java.util.TimeZone[] getAvailableTimeZones();

    /**
     * A function that sets the formatted value of numbers.
     *
     * Defines a format to use when printing numbers from templates. 
     * Passing null sets the format back to the default. The format string is
     * of the form "#.#". 
     * <p> 
     * Here are the special characters used in the parts of the format string,
     * with notes on their usage. 
     * <pre> 
     * Symbol   Meaning 
     * ------   ------- 
     * 0        a digit 
     * #        a digit, zero shows as absent 
     * .        placeholder for decimal separator. 
     * ,        placeholder for grouping separator. 
     * ;        separates formats. 
     * -        default negative prefix. 
     * %        multiply by 100 and show as percentage 
     * ?        multiply by 1000 and show as per mille 
     * \u00a4        currency sign; replaced by currency symbol;
     * &nbsp;        if doubled, replaced by international currency 
     * &nbsp;        symbol. If present in a pattern, the monetary
     * &nbsp;        decimal separator is used instead of the decimal 
     * &nbsp;        separator. (Unicode escape is \\u00a4) 
     * X        any other characters can be used in the prefix or suffix 
     * '        used to quote special characters in a prefix or suffix. 
     * </pre> 
     * 
     * @param format number format specification string
     */
    public void numberFormat(String format);

    /**
     * A function that sets the formatted value of numbers.
     *
     * Defines a format to use when printing numbers from templates. 
     * Passing null sets the format back to the default. The format string is
     * of the form "#.#". 
     * <p> 
     * Here are the special characters used in the parts of the format string,
     * with notes on their usage. 
     * <pre> 
     * Symbol   Meaning 
     * ------   ------- 
     * 0        a digit 
     * #        a digit, zero shows as absent 
     * .        placeholder for decimal separator. 
     * ,        placeholder for grouping separator. 
     * ;        separates formats. 
     * -        default negative prefix. 
     * %        multiply by 100 and show as percentage 
     * ?        multiply by 1000 and show as per mille 
     * \u00a4        currency sign; replaced by currency symbol;
     * &nbsp;        if doubled, replaced by international currency 
     * &nbsp;        symbol. If present in a pattern, the monetary
     * &nbsp;        decimal separator is used instead of the decimal 
     * &nbsp;        separator. (Unicode escape is \\u00a4) 
     * X        any other characters can be used in the prefix or suffix 
     * '        used to quote special characters in a prefix or suffix. 
     * </pre> 
     * 
     * @param format number format specification string
     * @param infinity display string for infinity
     * @param NaN display string for not-a-number, resulting from zero divided
     * by zero.
     */
    public void numberFormat(String format, String infinity, String NaN);

    /**
     * Returns the current number format specification.
     */
    public String getNumberFormat();

    /**
     * Returns the current number format for infinity.
     */
    public String getNumberFormatInfinity();

    /**
     * Returns the current number format for NaN.
     */
    public String getNumberFormatNaN();
}
