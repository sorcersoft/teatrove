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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.go.trove.classfile.*;

/******************************************************************************
 * Provides fast matching of strings against patterns containing wildcards.
 * An ordinary map must be supplied in order to create a PatternMatcher. The
 * map keys must be strings. Asterisks (*) are treated as wildcard characters.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 01/12/31 <!-- $-->
 */
public abstract class PatternMatcher {
    private static final int[] NO_POSITIONS = new int[0];

    // Maps pattern sets to auto-generated classes.
    private static Map cPatternMatcherClasses = new SoftHashMap();

    public static synchronized PatternMatcher forPatterns(Map patternMap) {
        Maker maker = new Maker(patternMap);
        Class clazz = (Class)cPatternMatcherClasses.get(maker.getKey());

        if (clazz == null) {
            Class patternMatcherClass = PatternMatcher.class;

            ClassInjector injector = ClassInjector.getInstance
                (patternMatcherClass.getClassLoader());
            
            int id = maker.getKey().hashCode();
            
            String baseName = patternMatcherClass.getName() + '$';
            String className = baseName;
            try {
                while (true) {
                    className = baseName + (id & 0xffffffffL);
                    try {
                        injector.loadClass(className);
                    }
                    catch (LinkageError e) {
                    }
                    id++;
                }
            }
            catch (ClassNotFoundException e) {
            }

            ClassFile cf = maker.createClassFile(className);
            
            /*
            try {
                String name = cf.getClassName();
                name = name.substring(name.lastIndexOf('.') + 1) + ".class";
                System.out.println(name);
                OutputStream out = new FileOutputStream(name);
                cf.writeTo(out);
                out.close();
            }
            catch (IOException e) {
            }
            */

            try {
                OutputStream stream = injector.getStream(cf.getClassName());
                cf.writeTo(stream);
                stream.close();
            }
            catch (IOException e) {
                throw new InternalError(e.toString());
            }
            
            try {
                clazz = injector.loadClass(cf.getClassName());
            }
            catch (ClassNotFoundException e) {
                throw new InternalError(e.toString());
            }

            cPatternMatcherClasses.put(maker.getKey(), clazz);
        }

        try {
            Constructor ctor =
                clazz.getConstructor(new Class[]{Object[].class});
            return (PatternMatcher)ctor.newInstance
                (new Object[]{maker.getMappedValues()});
        }
        catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        }
        catch (InstantiationException e) {
            throw new InternalError(e.toString());
        }
        catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        }
        catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    protected final Object[] mValues;

    protected PatternMatcher(Object[] values) {
        mValues = values;
    }

    /**
     * Returns null if no match.
     */
    public Result getMatch(String lookup) {
        int strLen = lookup.length();
        char[] chars = new char[strLen + 1];
        lookup.getChars(0, strLen, chars, 0);
        chars[strLen] = '\uffff';
        
        TinyList resultList = new TinyList();
        fillMatchResults(chars, 1, resultList);

        return (Result)resultList.mElement;
    }

    /**
     * Returns an empty array if no matches.
     *
     * @param limit maximum number of results to return
     */
    public Result[] getMatches(String lookup, int limit) {
        int strLen = lookup.length();
        char[] chars = new char[strLen + 1];
        lookup.getChars(0, strLen, chars, 0);
        chars[strLen] = '\uffff';
        
        List resultList = new ArrayList();
        fillMatchResults(chars, limit, resultList);

        return (Result[])resultList.toArray(new Result[resultList.size()]);
    }

    protected abstract void fillMatchResults(char[] lookup,
                                             int limit, List results);

    // Returns false if no more results should be added.
    protected static boolean addMatchResult(int limit,
                                            List results,
                                            String pattern,
                                            Object value,
                                            int[] positions,
                                            int len)
    {
        int size = results.size();
        if (size < limit) {
            if (positions == null || len == 0) {
                positions = NO_POSITIONS;
            }
            else {
                int[] original = positions;
                positions = new int[len];
                for (int i=0; i<len; i++) {
                    positions[i] = original[i];
                }
            }
            results.add(new Result(pattern, value, positions));
            return size + 1 < limit;
        }
        else {
            return false;
        }
    }

    public static class Result {
        private final String mPattern;
        private final Object mValue;
        private final int[] mPositions;

        Result(String pattern, Object value, int[] positions) {
            mPattern = pattern;
            mValue = value;
            mPositions = positions;
        }

        public String getPattern() {
            return mPattern;
        }

        /**
         * Returns the value associated with the matched pattern.
         */
        public Object getValue() {
            return mValue;
        }

        /**
         * Returns the indexes used to parse the lookup string at wildcard
         * positions in order for it to match the pattern. Array length is
         * always double the number of wildcards in the pattern. Every even
         * element is the start index (inclusive) of a wildcard match, and
         * every odd element is the end index (exclusive) of a wildcard match.
         */
        public int[] getWildcardPositions() {
            return mPositions;
        }
    }

    private static class Maker {
        private PatternNode mPatternRoot;
        private Object mKey;
        private Object[] mMappedValues;
        private int mMaxWildPerKey;

        private TypeDesc mIntType;
        private TypeDesc mBooleanType;
        private TypeDesc mListType;
        private TypeDesc mStringType;
        private TypeDesc mObjectType;
        private TypeDesc mIntArrayType;

        private CodeBuilder mBuilder;
        private LocalVariable mLookupLocal;
        private LocalVariable mLimitLocal;
        private LocalVariable mResultsLocal;
        private LocalVariable mPositionsLocal;
        private LocalVariable mIndexLocal;
        private Stack mTempLocals;
        private Label mReturnLabel;

        private int mReferenceLine;

        Maker(Map patternMap) {
            String[] keys =
                (String[])patternMap.keySet().toArray(new String[0]);

            for (int i=0; i<keys.length; i++) {
                String key = keys[i];
                // Ensure terminating patterns end in the special
                // terminator char.
                if (!key.endsWith("*")) {
                    keys[i] = key.concat("\uffff");
                }
            }

            // Sort the keys in a special order that ensures correct
            // "closest match" semantics.
            Arrays.sort(keys, new PatternComparator());

            mMappedValues = new Object[keys.length];
            for (int i=0; i<keys.length; i++) {
                String key = keys[i];
                if (key.endsWith("\uffff")) {
                    key = key.substring(0, key.length() - 1);
                }
                mMappedValues[i] = patternMap.get(key);
            }
            
            // Build tree structure for managing pattern matching.
            mPatternRoot = new PatternNode();
            for (int i=0; i<keys.length; i++) {
                String key = keys[i];
                mPatternRoot.buildPathTo(key, i);
            }

            mMaxWildPerKey = mPatternRoot.getMaxWildcardCount();

            mKey = new MultiKey(keys);
        }

        public Object getKey() {
            return mKey;
        }

        public Object getMappedValues() {
            return mMappedValues;
        }

        public ClassFile createClassFile(String className) {
            ClassFile cf = new ClassFile(className, PatternMatcher.class);
            cf.setSourceFile("PatternMatcher");
            
            // constructor
            Modifiers publicAccess = new Modifiers();
            publicAccess.setPublic(true);
            TypeDesc objectArrayType = TypeDesc.OBJECT.toArrayType();
            TypeDesc[] params = {objectArrayType};
            MethodInfo mi = cf.addConstructor(publicAccess, params);
            mBuilder = new CodeBuilder(mi);
            mBuilder.loadThis();
            mBuilder.loadLocal(mBuilder.getParameters()[0]);
            mBuilder.invokeSuperConstructor(params);
            mBuilder.returnVoid();

            mIntType = TypeDesc.INT;
            mBooleanType = TypeDesc.BOOLEAN;
            mListType = TypeDesc.forClass(List.class);
            mStringType = TypeDesc.STRING;
            mObjectType = TypeDesc.OBJECT;
            mIntArrayType = TypeDesc.INT.toArrayType();

            // fillMatchResults method
            TypeDesc charArrayType = TypeDesc.CHAR.toArrayType();
            params = new TypeDesc[]{charArrayType, mIntType, mListType};
            mi = cf.addMethod(publicAccess, "fillMatchResults", null, params);
            mBuilder = new CodeBuilder(mi);

            mLookupLocal = mBuilder.getParameters()[0];
            mLimitLocal = mBuilder.getParameters()[1];
            mResultsLocal = mBuilder.getParameters()[2];
            mPositionsLocal = mBuilder.createLocalVariable("positions", mIntArrayType);
            mIndexLocal = mBuilder.createLocalVariable("index", mIntType);

            mBuilder.mapLineNumber(++mReferenceLine);

            mBuilder.loadConstant(mMaxWildPerKey * 2);
            mBuilder.newObject(mIntArrayType);
            mBuilder.storeLocal(mPositionsLocal);

            mBuilder.loadConstant(0);
            mBuilder.storeLocal(mIndexLocal);
            mTempLocals = new Stack();
            mReturnLabel = mBuilder.createLabel();

            generateBranches(mPatternRoot, -1, 0);

            mReturnLabel.setLocation();
            mBuilder.returnVoid();

            return cf;
        }

        private void generateBranches(PatternNode node, int depth,
                                      int posIndex) {
            generateBranches(node, depth, posIndex, null);
        }

        private void generateBranches(PatternNode node, int depth,
                                      int posIndex,
                                      LocalVariable tempChar) {
            int c = node.mChar;
            List subNodes = node.mSubNodes;

            mBuilder.mapLineNumber(++mReferenceLine);

            if (c == '*') {
                LocalVariable savedIndex;
                
                if (mTempLocals.isEmpty()) {
                    savedIndex =
                        mBuilder.createLocalVariable("temp", mIntType);
                }
                else {
                    savedIndex = (LocalVariable)mTempLocals.pop();
                }
                
                mBuilder.loadLocal(mIndexLocal);
                mBuilder.storeLocal(savedIndex);
                
                // Save position of wildcard start.
                mBuilder.loadLocal(mPositionsLocal);
                mBuilder.loadConstant(posIndex);
                mBuilder.loadLocal(mIndexLocal);
                if (depth > 0) {
                    mBuilder.loadConstant(depth);
                    mBuilder.math(Opcode.IADD);
                }
                mBuilder.storeToArray(TypeDesc.INT);
                
                if (subNodes == null) {
                    generateWildcard(null, depth, posIndex + 2);
                }
                else {
                    int size = subNodes.size();
                    for (int i=0; i<size; i++) {
                        generateWildcard((PatternNode)subNodes.get(i),
                                         depth, posIndex + 2);
                        mBuilder.loadLocal(savedIndex);
                        mBuilder.storeLocal(mIndexLocal);
                    }
                }
                
                mTempLocals.push(savedIndex);

                if (node.mPattern != null) {
                    generateAddMatchResult(node);
                }

                return;
            }

            Label noMatch = mBuilder.createLabel();

            if (c >= 0) {
                if (tempChar != null) {
                    mBuilder.loadLocal(tempChar);
                    mTempLocals.push(tempChar);
                }
                else {
                    mBuilder.loadLocal(mLookupLocal);
                    mBuilder.loadLocal(mIndexLocal);
                    if (depth > 0) {
                        mBuilder.loadConstant(depth);
                        mBuilder.math(Opcode.IADD);
                    }
                    mBuilder.loadFromArray(TypeDesc.CHAR);
                }
                
                mBuilder.loadConstant((char)c);
                mBuilder.ifComparisonBranch(noMatch, "!=");
            }
            
            if (subNodes != null) {
                int size = subNodes.size();
                for (int i=0; i<size; i++) {
                    generateBranches
                        ((PatternNode)subNodes.get(i), depth + 1, posIndex);
                }
            }
            
            if (node.mPattern != null) {
                // Matched pattern; save results.
                generateAddMatchResult(node);
            }

            noMatch.setLocation();
        }
        
        private void generateWildcard(PatternNode node, int depth,
                                      int posIndex) {
            Label loopStart = mBuilder.createLabel().setLocation();
            Label loopEnd = mBuilder.createLabel();
            Label loopContinue = mBuilder.createLabel();

            // Save position of wildcard end.
            mBuilder.loadLocal(mPositionsLocal);
            mBuilder.loadConstant(posIndex - 1);
            mBuilder.loadLocal(mIndexLocal);
            if (depth > 0) {
                mBuilder.loadConstant(depth);
                mBuilder.math(Opcode.IADD);
            }
            mBuilder.storeToArray(TypeDesc.INT);

            mBuilder.loadLocal(mLookupLocal);
            mBuilder.loadLocal(mIndexLocal);
            if (depth > 0) {
                mBuilder.loadConstant(depth);
                mBuilder.math(Opcode.IADD);
            }
            mBuilder.loadFromArray(TypeDesc.CHAR);

            if (node == null) {
                mBuilder.loadConstant('\uffff');
                mBuilder.ifComparisonBranch(loopEnd, "==");
            }
            else {
                LocalVariable tempChar;
                if (mTempLocals.isEmpty()) {
                    tempChar =
                        mBuilder.createLocalVariable("temp", mIntType);
                }
                else {
                    tempChar = (LocalVariable)mTempLocals.pop();
                }
                mBuilder.storeLocal(tempChar);
                mBuilder.loadLocal(tempChar);

                mBuilder.loadConstant('\uffff');
                mBuilder.ifComparisonBranch(loopEnd, "==");

                generateBranches(node, depth, posIndex, tempChar);
            }

            loopContinue.setLocation();
            mBuilder.integerIncrement(mIndexLocal, 1);
            mBuilder.branch(loopStart);
            loopEnd.setLocation();
        }

        private void generateAddMatchResult(PatternNode node) {
            mBuilder.mapLineNumber(++mReferenceLine);

            mBuilder.loadLocal(mLimitLocal);
            mBuilder.loadLocal(mResultsLocal);
            mBuilder.loadConstant(node.mPattern);
            mBuilder.loadThis();
            mBuilder.loadField("mValues", TypeDesc.OBJECT.toArrayType());
            mBuilder.loadConstant(node.mOrder);
            mBuilder.loadFromArray(TypeDesc.OBJECT);
            mBuilder.loadLocal(mPositionsLocal);
            mBuilder.loadConstant(node.getWildcardCount() * 2);

            TypeDesc[] params = {
                mIntType,
                mListType,
                mStringType,
                mObjectType,
                mIntArrayType,
                mIntType
            };
            
            mBuilder.invokeStatic(PatternMatcher.class.getName(),
                                  "addMatchResult", mBooleanType, params);
            mBuilder.ifZeroComparisonBranch(mReturnLabel, "==");
        }
    }

    private static class PatternNode {
        public final int mChar;
        public String mPattern;
        public int mOrder;
        public List mSubNodes;

        public PatternNode() {
            mChar = -1;
        }

        public PatternNode(char c) {
            mChar = c;
        }

        public void buildPathTo(String pattern, int order) {
            buildPathTo(pattern, order, 0);
        }

        public int getHeight() {
            int height = 1;
            if (mSubNodes != null) {
                int size = mSubNodes.size();
                for (int i=0; i<size; i++) {
                    int subH = ((PatternNode)mSubNodes.get(i)).getHeight();
                    if (subH > height) {
                        height = subH;
                    }
                }
            }
            return height;
        }

        public int getWildcardCount() {
            int wildCount = 0;
            String pattern = mPattern;
            if (pattern != null) {
                int len = pattern.length();
                for (int i=0; i<len; i++) {
                    if (pattern.charAt(i) == '*') {
                        wildCount++;
                    }
                }
            }
            return wildCount;
        }

        public int getMaxWildcardCount() {
            int wildCount = getWildcardCount();
            
            if (mSubNodes != null) {
                for (int i=0; i<mSubNodes.size(); i++) {
                    int count = 
                        ((PatternNode)mSubNodes.get(i)).getMaxWildcardCount();
                    if (count > wildCount) {
                        wildCount = count;
                    }
                }
            }

            return wildCount;
        }

        private void buildPathTo(String pattern, int order, int index) {
            if (index >= pattern.length()) {
                if (pattern.endsWith("\uffff")) {
                    // Trim off the '\uffff'.
                    pattern = pattern.substring(0, pattern.length() - 1);
                }
                mPattern = pattern;
                mOrder = order;
                return;
            }

            char c = pattern.charAt(index);

            if (mSubNodes == null) {
                mSubNodes = new ArrayList(10);
            }

            int size = mSubNodes.size();
            for (int i=0; i<size; i++) {
                PatternNode node = (PatternNode)mSubNodes.get(i);
                if (node.mChar == c) {
                    node.buildPathTo(pattern, order, index + 1);
                    return;
                }
            }

            PatternNode node = new PatternNode(c);
            mSubNodes.add(node);
            node.buildPathTo(pattern, order, index + 1);

            return;
        }

        public void dump(PrintStream out, String indent) {
            if (mSubNodes != null) {
                String subIndent = indent.concat(" ");
                for (int i=0; i<mSubNodes.size(); i++) {
                    ((PatternNode)mSubNodes.get(i)).dump(out, subIndent);
                }
            }
            out.print(indent);
            out.print('\'');
            out.print((char)mChar);
            out.print('\'');
            if (mPattern != null) {
                out.print(" -> ");
                out.print(mPattern);
            }
            out.println();
        }
    }

    private static class PatternComparator implements Comparator {
        public int compare(Object a, Object b) {
            String sa = (String)a;
            String sb = (String)b;
            
            int alen = sa.length();
            int blen = sb.length();
            int mlen = Math.min(alen, blen);
            
            for (int i=0; i<mlen; i++) {
                char ca = sa.charAt(i);
                char cb = sb.charAt(i);
                if (ca == '*') {
                    if (cb != '*') {
                        // Wildcard sorted high.
                        return 1;
                    }
                }
                else if (cb == '*') {
                    // Wildcard sorted high.
                    return -1;
                }
                else if (ca < cb) {
                    return -1;
                }
                else if (ca > cb) {
                    return 1;
                }
            }
            
            // The shorter string is sorted high.
            if (alen < blen) {
                return 1;
            }
            else if (alen > blen) {
                return -1;
            }
            
            return 0;
        }
    }

    private static class TinyList extends AbstractList {
        public Object mElement;

        public int size() {
            return mElement == null ? 0 : 1;
        }

        public boolean add(Object obj) {
            if (mElement == null) {
                mElement = obj;
                return true;
            }
            else {
                throw new UnsupportedOperationException();
            }
        }

        public Object get(int index) {
            if (index == 0 && mElement != null) {
                return mElement;
            }
            else {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /* Sample auto-generated method.
    protected void fillMatchResults(char[] lookup, int limit, List results) {
        int[] positions = new int[2]; // At least as large as number of wildcards, times 2.
        int i = 0;
        if (lookup[i + 0] == '/') {
            if (lookup[i + 1] == 'a') {
                if (lookup[i + 2] == 'd') {
                    if (lookup[i + 3] == 'm') {
                        if (lookup[i + 4] == 'i') {
                            if (lookup[i + 5] == 'n') {
                                if (lookup[i + 6] == '2') {
                                    if (lookup[i + 7] == '\uffff') {
                                        addMatchResult(limit, results, "/admin2", mValues[0], null, 0);
                                    }
                                }
                                else if (lookup[i + 6] == '\uffff') {
                                    addMatchResult(limit, results, "/admin", mValues[1], null, 0);
                                }
                            }
                        }
                    }
                }
            }
            else if (lookup[i + 1] == 't') {
                if (lookup[i + 2] == 'e') {
                    if (lookup[i + 3] == 'a') {
                        if (lookup[i + 4] == '/') {
                            // Wildcard pattern. Consume characters until match found.
                            int saved_i = i;
                            positions[0] = i + 5;
                            while (true) {
                                positions[1] = i + 5;
                                char c = lookup[i + 5];
                                if (c == '\uffff') {
                                    break;
                                }
                                else if (c == '.') {
                                    if (lookup[i + 6] == 'h') {
                                        if (lookup[i + 7] == 't') {
                                            if (lookup[i + 8] == 'm') {
                                                if (lookup[i + 9] == 'l') {
                                                    if (lookup[i + 10] == '\uffff') {
                                                        addMatchResult(limit, results, "/tea/*.html", mValues[2], positions, 2);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                i++;
                            }
                            i = saved_i;
                            addMatchResult(limit, results, "/tea/*", mValues[3], positions, 2);
                        }
                    }
                }
            }
        }
        // Wildcard pattern. Consume characters until match found.
        int saved_i = i;
        positions[0] = i;
        while (true) {
            positions[1] = i;
            char c = lookup[i];
            if (c == '\uffff') {
                break;
            }
            else if (c == '.') {
                if (lookup[i + 1] == 'h') {
                    if (lookup[i + 2] == 't') {
                        if (lookup[i + 3] == 'm') {
                            if (lookup[i + 4] == 'l') {
                                if (lookup[i + 5] == '\uffff') {
                                    addMatchResult(limit, results, "*.html", mValues[4], positions, 2);
                                }
                            }
                        }
                    }
                }
            }
            i++;
        }
        i = saved_i;
        addMatchResult(limit, results, "*", mValues[5], positions, 2);
    }
    */
}
