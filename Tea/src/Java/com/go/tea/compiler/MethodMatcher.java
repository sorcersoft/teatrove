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

package com.go.tea.compiler;

import java.lang.reflect.Method;

/******************************************************************************
 * This class finds methods that best fit a given description. The compiler
 * will then bind to one of those methods.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class MethodMatcher {
    /**
     * The best result candidates are stored in the Method array passed in.
     * The int returned indicates the number of candidates in the array. Zero
     * is returned if there is no possible match.
     */
    public static int match(Method[] methods, String name, Type[] params) {
        int paramCount = params.length;
        int matchCount = methods.length;
        Method m;

        int[] costs = new int[matchCount];

        // Filter the available methods down to a smaller set, tossing
        // out candidates that could not possibly match because the name 
        // differs, and the number of parameters differ. Also eliminate
        // ones in which the parameter types are not compatible at all
        // because no known conversion could be applied.
        
        int lowestTotalCost = Integer.MAX_VALUE;
        int length = matchCount;
        matchCount = 0;
        for (int i=0; i < length; i++) {
            m = methods[i];
            if (name == null || m.getName().equals(name)) {
                Class[] methodParams = m.getParameterTypes();
                if (methodParams.length == paramCount) {

                    int total = 0;
                    int j;
                    for (j=0; j<paramCount; j++) {
                        int cost = new Type(methodParams[j])
                            .convertableFrom(params[j]);
                        if (cost < 0) {
                            break;
                        }
                        else {
                            total += cost;
                        }
                    }

                    if (j == paramCount) {
                        costs[matchCount] = total;
                        methods[matchCount++] = m;
                        if (total < lowestTotalCost) {
                            lowestTotalCost = total;
                        }
                    }
                }
            }
        }

        if (matchCount <= 1) {
            return matchCount;
        }

        // Filter out those that have a cost higher than lowestTotalCost.
        length = matchCount;
        matchCount = 0;
        for (int i=0; i < length; i++) {
            if (costs[i] <= lowestTotalCost) {
                costs[matchCount] = costs[i];
                methods[matchCount++] = methods[i];
            }
        }

        if (matchCount <= 1) {
            return matchCount;
        }

        // Filter further by matching parameters with the shortest distance
        // in the hierarchy.

        for (int j=0; j<paramCount; j++) {
            Class lastMatch = null;
            Method bestFit = null;

            length = matchCount;
            matchCount = 0;
            for (int i=0; i < length; i++) {
                m = methods[i];
                if (bestFit == null) {
                    bestFit = m;
                }
                Class methodParam = m.getParameterTypes()[j];
                Class param = params[j].getNaturalClass();
                if (methodParam.isAssignableFrom(param)) {
                    if (lastMatch == null ||
                        lastMatch.isAssignableFrom(methodParam)) {
                        
                        bestFit = m;
                        lastMatch = methodParam;
                    }
                }
            }

            methods[matchCount++] = bestFit;
        }

        return matchCount;
    }

    /**
     * Test program.
     */
    /*
    public static void main(String[] arg) throws Exception {
        new Tester().test();
    }

    private static class Tester {
        public Tester() {
        }

        public void test() {
            Type t1 = new Type(boolean.class);
            Type t2 = new Type(int.class);
            Type t3 = new Type(float.class);
            Type t4 = new Type(double.class);

            Type t5 = new Type(Boolean.class);
            Type t6 = new Type(Integer.class);
            Type t7 = new Type(Float.class);
            Type t8 = new Type(Double.class);

            test("test", new Type[] {});

            test("test", new Type[] {t1});
            test("test", new Type[] {t2});
            test("test", new Type[] {t3});
            test("test", new Type[] {t4});
            test("test", new Type[] {t5});
            test("test", new Type[] {t6});
            test("test", new Type[] {t7});
            test("test", new Type[] {t8});

            test("test2", new Type[] {t1});
            test("test2", new Type[] {t2});
            test("test2", new Type[] {t3});
            test("test2", new Type[] {t4});
            test("test2", new Type[] {t5});
            test("test2", new Type[] {t6});
            test("test2", new Type[] {t7});
            test("test2", new Type[] {t8});

            test("test3", new Type[] {t1});
            test("test3", new Type[] {t2});
            test("test3", new Type[] {t3});
            test("test3", new Type[] {t4});
            test("test3", new Type[] {t5});
            test("test3", new Type[] {t6});
            test("test3", new Type[] {t7});
            test("test3", new Type[] {t8});

            test("test4", new Type[] {t1});
            test("test4", new Type[] {t2});
            test("test4", new Type[] {t3});
            test("test4", new Type[] {t4});
            test("test4", new Type[] {t5});
            test("test4", new Type[] {t6});
            test("test4", new Type[] {t7});
            test("test4", new Type[] {t8});

            test("test5", new Type[] {t1});
            test("test5", new Type[] {t2});
            test("test5", new Type[] {t3});
            test("test5", new Type[] {t4});
            test("test5", new Type[] {t5});
            test("test5", new Type[] {t6});
            test("test5", new Type[] {t7});
            test("test5", new Type[] {t8});

            test("test6", new Type[] {t1});
            test("test6", new Type[] {t2});
            test("test6", new Type[] {t3});
            test("test6", new Type[] {t4});
            test("test6", new Type[] {t5});
            test("test6", new Type[] {t6});
            test("test6", new Type[] {t7});
            test("test6", new Type[] {t8});

            test("test7", new Type[] {t2, t6});
            test("test7", new Type[] {t6, t2});
            test("test7", new Type[] {t2, t2});
            test("test7", new Type[] {t6, t6});

            // Should only produce the method that accepts B
            test("test8", new Type[] {new Type(C.class)});
        }
        
        private void test(String name, Type[] params) {
            Method[] methods = this.getClass().getMethods();
            int count = MethodMatcher.match(methods, name, params);
            dump(methods, count);
        }

        private void dump(Method[] methods, int count) {
            for (int i=0; i<count; i++) {
                System.out.println(methods[i]);
            }
            System.out.println();
        }

        public void test(boolean i) {}
        public void test(char i) {}
        public void test(byte i) {}
        public void test(short i) {}
        public void test(int i) {}
        public void test(float i) {}
        public void test(long i) {}
        public void test(double i) {}
        public void test(Boolean i) {}
        public void test(Character i) {}
        public void test(Byte i) {}
        public void test(Short i) {}
        public void test(Integer i) {}
        public void test(Float i) {}
        public void test(Long i) {}
        public void test(Double i) {}
        public void test(Number i) {}
        public void test(Object i) {}
        public void test(String i) {}

        public void test2(boolean i) {}
        public void test2(char i) {}
        public void test2(byte i) {}
        public void test2(short i) {}
        public void test2(int i) {}
        public void test2(float i) {}
        public void test2(long i) {}
        public void test2(double i) {}

        public void test3(Boolean i) {}
        public void test3(Character i) {}
        public void test3(Byte i) {}
        public void test3(Short i) {}
        public void test3(Integer i) {}
        public void test3(Float i) {}
        public void test3(Long i) {}
        public void test3(Double i) {}
        public void test3(Number i) {}

        public void test4(Object i) {}
        public void test4(String i) {}

        public void test5(int i) {}
        public void test5(String i) {}

        public void test6(Number i) {}
        public void test6(Integer i) {}
        public void test6(String i) {}

        public void test7(int i, Integer I) {}
        public void test7(Integer I, int i) {}

        private class A {}
        private class B extends A {}
        private class C extends B {}
        
        public void test8(A a) {}
        public void test8(B b) {}
    }
    */
}
