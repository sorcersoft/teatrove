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

import java.io.*;
import com.go.tea.parsetree.*;

/******************************************************************************
 * A class that prints a parse tree. To print, call the writeTo method.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 */
public class TreePrinter extends CodeGenerator {
    private String mIndentStr;
    private boolean mExtraParens;

    public TreePrinter(Template tree) {
        this(tree, "    ", false);
    }

    public TreePrinter(Template tree, String indentStr) {
        this(tree, indentStr, false);
    }

    public TreePrinter(Template tree, boolean extraParens) {
        this(tree, "    ", extraParens);
    }

    public TreePrinter(Template tree, String indentStr, boolean extraParens) {
        super(tree);
        mIndentStr = indentStr;
        mExtraParens = extraParens;
    }

    public void writeTo(OutputStream out) throws IOException {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
        
        Visitor v = new Visitor(w, mIndentStr, mExtraParens);

        try {
            getParseTree().accept(v);
        }
        catch (IOError e) {
            e.rethrow();
        }
        
        w.flush();
    }

    /**
     * Converts any node to a String.
     */
    public static String toString(Node node) {
        return toString(node, "    ");
    }

    /**
     * Converts any node to a String.
     */
    public static String toString(Node node, String indentStr) {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        Visitor v = new Visitor(w, indentStr, false);
        node.accept(v);
        try {
            w.flush();
        }
        catch (IOException e) {
            throw new IOError(e);
        }
        return sw.toString();
    }

    private static class IOError extends RuntimeException {
        private IOException mException;
        
        public IOError(IOException e) {
            mException = e;
        }
        
        public void rethrow() throws IOException {
            throw mException;
        }
    }

    private static class Visitor implements NodeVisitor {
        private BufferedWriter mWriter;
        private String mIndentStr;
        private boolean mExtraParens;
        private int mIndent;
        private boolean mNeedIndent = true;

        public Visitor(BufferedWriter writer, String indentStr,
                       boolean extraParens) {
            mWriter = writer;
            mIndentStr = indentStr;
            mExtraParens = extraParens;
        }

        public void indent(int amount) {
            mIndent += amount;
            if (mIndent < 0) {
                mIndent = 0;
            }
        }
        
        private void print(String str) throws IOError {
            doIndent();

            try {
                mWriter.write(str);
            }
            catch (IOException e) {
                throw new IOError(e);
            }
        }
        
        private void println() throws IOError {
            mNeedIndent = true;

            try {
                mWriter.newLine();
            }
            catch (IOException e) {
                throw new IOError(e);
            }
        }

        private void doIndent() throws IOError {
            if (mNeedIndent) {
                mNeedIndent = false;

                try {
                    for (int i = mIndent; i > 0; --i) {
                        mWriter.write(mIndentStr);
                    }
                }
                catch (IOException e) {
                    throw new IOError(e);
                }
            }
        }
        
        public Object visit(Template node) {
            print("template ");
            print(node.getName().getName());
            
            Variable[] params = node.getParams();
            print(" (");
            if (params != null) {
                for (int i=0; i<params.length; i++) {
                    if (i > 0) {
                        print(", ");
                    }
                    params[i].accept(this);
                }
            }
            print(") ");
            
            if (node.hasSubstitutionParam()) {
                print("{...} ");
            }

            Statement stmt = node.getStatement();
            if (stmt != null) {
                stmt.accept(this);
            }
            
            return null;
        }
        
        public Object visit(Name node) {
            print(node.getName());
            return null;
        }
        
        public Object visit(TypeName node) {
            print(node.getName());
            int dim = node.getDimensions();
            for (int i=0; i<dim; i++) {
                print("[]");
            }
            return null;
        }
        
        public Object visit(Variable node) {
            TypeName typeName = node.getTypeName();
            if (typeName != null) {
                typeName.accept(this);
                print(" ");
            }
            print(node.getName());
            return null;
        }
        
        public Object visit(ExpressionList node) {
            Expression[] exprs = node.getExpressions();
            for (int i=0; i<exprs.length; i++) {
                if (i > 0) {
                    print(", ");
                }
                exprs[i].accept(this);
            }
            
            return null;
        }
        
        public Object visit(Statement node) {
            return null;
        }
        
        public Object visit(StatementList node) {
            Statement[] stmts = node.getStatements();
            if (stmts != null) {
                for (int i=0; i<stmts.length; i++) {
                    println();
                    stmts[i].accept(this);
                }
                println();
            }
            
            return null;
        }
        
        public Object visit(Block node) {
            print("{ ");
            indent(1);
            visit((StatementList)node);
            indent(-1);
            print("} ");
            
            return null;
        }
        
        public Object visit(AssignmentStatement node) {
            node.getLValue().accept(this);
            print(" = ");
            node.getRValue().accept(this);
            print(" ");
            
            return null;
        }

        public Object visit(BreakStatement node) {
            print("break");
            return null;
        }

        public Object visit(ForeachStatement node) {
            print("foreach (");
            node.getLoopVariable().accept(this);
            print(" in ");
            node.getRange().accept(this);
            if (node.getEndRange() != null) {
                print("..");
                node.getEndRange().accept(this);
            }
            print(") ");
            Statement body = node.getBody();
            if (body != null) {
                body.accept(this);
            }
            
            return null;
        }
        
        public Object visit(IfStatement node) {
            print("if ");
            
            node.getCondition().accept(this);
            
            Statement stmt = node.getThenPart();
            if (stmt != null) {
                print(" ");
                stmt.accept(this);
            }
            else {
                print(" {");
                println();
                print("} ");
            }
        
            stmt = node.getElsePart();
            if (stmt != null) {
                println();
                print("else ");
                stmt.accept(this);
            }

            return null;
        }

        public Object visit(SubstitutionStatement node) {
            print("...");
            return null;
        }

        public Object visit(ExpressionStatement node) {
            node.getExpression().accept(this);
            return null;
        }

        public Object visit(ReturnStatement node) {
            Expression expr = node.getExpression();
            if (expr != null) {
                print("/* return */ ");
                expr.accept(this);
            }
            else {
                print("/* return void */ ");
            }
            return null;
        }

        public Object visit(ExceptionGuardStatement node) {
            print("/* try { */");
            if (node.getGuarded() != null) {
                indent(1);
                node.getGuarded().accept(this);
                indent(-1);
            }
            print("/* } catch (java.lang.Exception e) { */");
            if (node.getReplacement() != null) {
                indent(1);
                node.getReplacement().accept(this);
                indent(-1);
            }
            print("/* } */");

            return null;
        }

        public Object visit(Expression node) {
            print(String.valueOf(node));
            return null;
        }

        public Object visit(ParenExpression node) {
            print("(");
            node.getExpression().accept(this);
            print(")");

            return null;
        }

        public Object visit(NewArrayExpression node) {
            if (node.isAssociative()) {
                print("##(");
            }
            else {
                print("#(");
            }
            node.getExpressionList().accept(this);
            print(")");

            return null;
        }

        public Object visit(FunctionCallExpression node) {
            return visit((CallExpression)node);
        }

        public Object visit(TemplateCallExpression node) {
            print("call ");
            return visit((CallExpression)node);
        }

        private Object visit(CallExpression node) {
            node.getTarget().accept(this);
            print("(");
            node.getParams().accept(this);
            print(")");
        
            Statement subParam = node.getSubstitutionParam();
            if (subParam != null) {
                print(" ");
                subParam.accept(this);
            }

            return null;
        }

        public Object visit(VariableRef node) {
            print(node.getName());
            return null;
        }

        public Object visit(Lookup node) {
            if (mExtraParens) {
                print("(");
                node.getExpression().accept(this);
                print(").");
            }
            else {
                node.getExpression().accept(this);
                print(".");
            }
            print(node.getLookupName().getName());

            return null;
        }

        public Object visit(ArrayLookup node) {
            if (mExtraParens) {
                print("(");
                node.getExpression().accept(this);
                print(")[");
            }
            else {
                node.getExpression().accept(this);
                print("[");
            }
            node.getLookupIndex().accept(this);
            print("]");

            return null;
        }

        public Object visit(NegateExpression node) {
            if (mExtraParens) {
                print("-(");
                node.getExpression().accept(this);
                print(")");
            }
            else {
                print("-");
                node.getExpression().accept(this);
            }

            return null;
        }

        public Object visit(NotExpression node) {
            if (mExtraParens) {
                print("not (");
                node.getExpression().accept(this);
                print(")");
            }
            else {
                print("not ");
                node.getExpression().accept(this);
            }

            return null;
        }

        public Object visit(BinaryExpression node) {
            if (mExtraParens) print("(");
            node.getLeftExpression().accept(this);
            print(" ");
            print(node.getOperator().getImage());
            print(" ");
            node.getRightExpression().accept(this);
            if (mExtraParens) print(")");

            return null;
        }

        public Object visit(ConcatenateExpression node) {
            return visit((BinaryExpression)node);
        }

        public Object visit(ArithmeticExpression node) {
            return visit((BinaryExpression)node);
        }

        public Object visit(RelationalExpression node) {
            if (node.getOperator().getID() != Token.ISA) {
                return visit((BinaryExpression)node);
            }
            else {
                if (mExtraParens) print("(");
                node.getLeftExpression().accept(this);
                print(" ");
                print(node.getOperator().getImage());
                print(" ");
                node.getIsaTypeName().accept(this);
                if (mExtraParens) print(")");
                
                return null;
            }
        }

        public Object visit(AndExpression node) {
            return visit((BinaryExpression)node);
        }

        public Object visit(OrExpression node) {
            return visit((BinaryExpression)node);
        }

        public Object visit(NullLiteral node) {
            print(String.valueOf(node.getValue()));
            return null;
        }

        public Object visit(BooleanLiteral node) {
            print(String.valueOf(node.getValue()));
            return null;
        }

        public Object visit(StringLiteral node) {
            String str = node.getValue().toString();
            int length = str.length();
            StringBuffer buf = new StringBuffer(length);

            for (int i=0; i<length; i++) {
                char c = str.charAt(i);

                if (c > '\"' && c < 128) {
                    buf.append(c);
                }
                else {
                    switch(c) {
                    case ' ':
                    case '!':
                        buf.append(c);
                        break;
                    case '\"':
                        buf.append("\\\"");
                        break;
                    case '\0':
                        buf.append("\\0");
                        break;
                    case '\b':
                        buf.append("\\b");
                        break;
                    case '\t':
                        buf.append("\\t");
                        break;
                    case '\n':
                        buf.append("\\n");
                        break;
                    case '\f':
                        buf.append("\\f");
                        break;
                    case '\r':
                        buf.append("\\r");
                        break;
                    default:
                        buf.append("\\u");
                        String hex = "0000" + Integer.toHexString(c);
                        buf.append(hex.substring(hex.length() - 4));
                        break;
                    }
                }
            }

            print("\"");
            print(buf.toString());
            print("\"");
            return null;
        }

        public Object visit(NumberLiteral node) {
            Number value = (Number)node.getValue();
            print(value.toString());

            if (value instanceof Long) {
                print("L");
            }
            else if (value instanceof Float) {
                print("f");
            }
            else if (value instanceof Double) {
                print("d");
            }

            return null;
        }
    }
}
