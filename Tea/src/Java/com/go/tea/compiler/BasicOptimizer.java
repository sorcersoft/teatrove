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

import java.util.Vector;
import com.go.tea.parsetree.*;

/******************************************************************************
 * The BasicOptimizer only performs two optimizations: constant
 * folding and dead code elimination.
 *
 * <p>Expressions that contain known values at compile time can
 * be replaced with a constant expression. This basic optimization is
 * known as constant folding. It improves runtime performance and reduces
 * the size of generated code.
 *
 * <p>Statements that are known at compile time to be unreachable (sometimes
 * as a result of constant folding) can be removed. This is dead code
 * elimination, and it mainly reduces the size of generated code. It can also
 * improve runtime performance.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$--> 6 <!-- $$JustDate:-->  5/31/01 <!-- $-->
 */
public class BasicOptimizer {
    private Node mTree;

    public BasicOptimizer(Node tree) {
        mTree = tree;
    }

    public Node optimize() {
        return (Node)mTree.accept(new Visitor());
    }

    private static class Visitor extends TreeMutator {
        public Object visit(Statement node) {
            return null;
        }
        
        public Object visit(StatementList node) {
            Statement[] stmts = optimizeStatements(node.getStatements());
            if (stmts == null || stmts.length == 0) {
                return null;
            }
            
            if (stmts.length == 1) {
                return stmts[0];
            }
            
            return new StatementList(node.getSourceInfo(), stmts);
        }
        
        public Object visit(Block node) {
            Statement[] stmts = optimizeStatements(node.getStatements());

            Statement init = node.getInitializer();
            if (init != null) {
                init = (Statement)init.accept(this);
            }

            Statement fin = node.getFinalizer();
            if (fin != null) {
                fin = (Statement)fin.accept(this);
            }
            
            if (stmts == null || stmts.length == 0) {
                if (init == null && fin == null) {
                    return null;
                }
                else {
                    node = new Block(node.getSourceInfo());
                }
            }
            else {
                node = new Block(node.getSourceInfo(), stmts);
            }
            
            node.setInitializer(init);
            node.setFinalizer(fin);

            return node;
        }
        
        private Statement[] optimizeStatements(Statement[] stmts) {
            if (stmts == null) {
                return null;
            }
            
            int length = stmts.length;
            Vector v = new Vector(length);
            
            for (int i=0; i<length; i++) {
                Statement stmt = (Statement)stmts[i].accept(this);
                if (stmt != null) {
                    v.addElement(stmt);
                }
            }
            
            Statement[] newStmts = new Statement[v.size()];
            v.copyInto(newStmts);
            
            return newStmts;
        }
        
        public Object visit(BreakStatement node) {
            return (BreakStatement)super.visit(node);
        }

        public Object visit(ForeachStatement node) {
            node = (ForeachStatement)super.visit(node);

            Expression range = node.getRange();
            Expression endRange = node.getEndRange();
            if (endRange != null &&
                range.isValueKnown() && endRange.isValueKnown()) {

                Object rangeValue = range.getValue();
                Object endRangeValue = endRange.getValue();

                if (rangeValue instanceof Number &&
                    endRangeValue instanceof Number) {
                    
                    if (((Number)rangeValue).intValue() >
                        ((Number)endRangeValue).intValue()) {
                        
                        // Will loop zero times. Set the body to null
                        // instead of returning null because the loop
                        // variable still should be set to the first value.
                        // It is possible that the loop variable is
                        // accessed outside the loop, so it must be
                        // set to the correct value.
                        
                        node.setBody(null);
                    }
                }
            }
            
            return node;
        }
        
        public Object visit(IfStatement node) {
            Expression condition = visitExpression(node.getCondition());
            
            Block thenPart;
            Block elsePart;
            
            // If the condition is a constant boolean...
            if (condition.isValueKnown() && 
                condition.getType().getObjectClass() == Boolean.class) {
                // ... and the condition's value is true...
                if ( ((Boolean)condition.getValue()).booleanValue() ) {
                    // ... then the then part will always execute.
                    thenPart = node.getThenPart();
                    if (thenPart != null) {
                        thenPart = (Block)thenPart.accept(this);
                    }
                    
                    return thenPart;
                }
                else {
                    // ... else the else part will always execute.
                    elsePart = node.getElsePart();
                    if (elsePart != null) {
                        elsePart = (Block)elsePart.accept(this);
                    }
                    
                    return elsePart;
                }
            }
            
            thenPart = node.getThenPart();
            if (thenPart != null) {
                thenPart = (Block)thenPart.accept(this);
            }
            
            elsePart = node.getElsePart();
            if (elsePart != null) {
                elsePart = (Block)elsePart.accept(this);
            }
            
            // if there is no then part, but there is an else part,
            // invert the condition and make the else part the then part.
            if (thenPart == null && elsePart != null) {
                thenPart = elsePart;
                elsePart = null;
                condition.convertTo(Type.BOOLEAN_TYPE);
                condition = new NotExpression(condition.getSourceInfo(),
                                              condition);
                condition.convertTo(Type.BOOLEAN_TYPE);
            }

            node.setCondition(condition);
            node.setThenPart(thenPart);
            node.setElsePart(elsePart);

            return node;
        }
        
        public Object visit(ParenExpression node) {
            return node.getExpression().accept(this);
        }
        
        public Object visit(NegateExpression node) {
            SourceInfo info = node.getSourceInfo();
            Expression expr = visitExpression(node.getExpression());
            
            if (expr.isValueKnown()) {
                Object value = expr.getValue();
                
                if (value instanceof Number) {
                    Number number = (Number)value;
                    
                    if (value instanceof Integer) {
                        return new NumberLiteral(info, -number.intValue());
                    }
                    else if (value instanceof Long) {
                        return new NumberLiteral(info, -number.longValue());
                    }
                    else if (value instanceof Float) {
                        return new NumberLiteral(info, -number.floatValue());
                    }
                    else if (value instanceof Double) {
                        return new NumberLiteral(info, -number.doubleValue());
                    }
                }
            }
            else if (expr instanceof NegateExpression) {
                return ((NegateExpression)expr).getExpression();
            }
            
            node.setExpression(expr);
            return node;
        }
        
        public Object visit(NotExpression node) {
            SourceInfo info = node.getSourceInfo();
            Expression expr = visitExpression(node.getExpression());
            
            if (expr.isValueKnown()) {
                Object value = expr.getValue();
                
                if (value instanceof Boolean) {
                    boolean bv = ((Boolean)value).booleanValue();
                    return new BooleanLiteral(info, !bv);
                }
            }
            else if (expr instanceof NotExpression) {
                return ((NotExpression)expr).getExpression();
            }
            
            node.setExpression(expr);
            return node;
        }
        
        public Object visit(ConcatenateExpression node) {
            SourceInfo info = node.getSourceInfo();

            Expression left = visitExpression(node.getLeftExpression());
            Expression right = visitExpression(node.getRightExpression());
            
            if (left.isValueKnown() && left.getValue() instanceof String) {
                String leftValue = (String)left.getValue();
                if (right.isValueKnown() && 
                    right.getValue() instanceof String) {

                    String rightValue = (String)right.getValue();
                    return new StringLiteral(info, leftValue + rightValue);
                }

                if (leftValue.length() == 0) {
                    return right;
                }
            }
            else if (right.isValueKnown() &&
                     right.getValue() instanceof String) {

                if (((String)right.getValue()).length() == 0) {
                    return left;
                }
            }

            node.setLeftExpression(left);
            node.setRightExpression(right);
            return node;
        }

        public strictfp Object visit(ArithmeticExpression node) {
            SourceInfo info = node.getSourceInfo();
            Token operator = node.getOperator();
            
            Expression left = visitExpression(node.getLeftExpression());
            Expression right = visitExpression(node.getRightExpression());
            
            if (node.getType() != null &&
                left.isValueKnown() && right.isValueKnown()) {
                int ID = operator.getID();
                Object leftValue = left.getValue();
                Object rightValue = right.getValue();
                
                Type type = left.getType();

                if (leftValue instanceof Number && 
                    rightValue instanceof Number &&
                    type.equals(right.getType())) {
                    
                    Class clazz = type.getObjectClass();

                    Number lv = (Number)leftValue;
                    Number rv = (Number)rightValue;
                    
                    try {
                        if (clazz == Integer.class) {
                            int i1 = lv.intValue();
                            int i2 = rv.intValue();

                            switch (ID) {
                            case Token.PLUS:
                                return new NumberLiteral(info, i1 + i2);
                            case Token.MINUS:
                                return new NumberLiteral(info, i1 - i2);
                            case Token.MULT:
                                return new NumberLiteral(info, i1 * i2);
                            case Token.DIV:
                                return new NumberLiteral(info, i1 / i2);
                            case Token.MOD:
                                return new NumberLiteral(info, i1 % i2);
                            }
                        }
                        else if (clazz == Float.class) {
                            float f1 = lv.floatValue();
                            float f2 = rv.floatValue();
                            
                            switch (ID) {
                            case Token.PLUS:
                                return new NumberLiteral(info, f1 + f2);
                            case Token.MINUS:
                                return new NumberLiteral(info, f1 - f2);
                            case Token.MULT:
                                return new NumberLiteral(info, f1 * f2);
                            case Token.DIV:
                                return new NumberLiteral(info, f1 / f2);
                            case Token.MOD:
                                return new NumberLiteral(info, f1 % f2);
                            }
                        }
                        else if (clazz == Long.class) {
                            long L1 = lv.longValue();
                            long L2 = rv.longValue();
                            
                            switch (ID) {
                            case Token.PLUS:
                                return new NumberLiteral(info, L1 + L2);
                            case Token.MINUS:
                                return new NumberLiteral(info, L1 - L2);
                            case Token.MULT:
                                return new NumberLiteral(info, L1 * L2);
                            case Token.DIV:
                                return new NumberLiteral(info, L1 / L2);
                            case Token.MOD:
                                return new NumberLiteral(info, L1 % L2);
                            }
                        }
                        else if (clazz == Double.class) {
                            double d1 = lv.doubleValue();
                            double d2 = rv.doubleValue();
                            
                            switch (ID) {
                            case Token.PLUS:
                                return new NumberLiteral(info, d1 + d2);
                            case Token.MINUS:
                                return new NumberLiteral(info, d1 - d2);
                            case Token.MULT:
                                return new NumberLiteral(info, d1 * d2);
                            case Token.DIV:
                                return new NumberLiteral(info, d1 / d2);
                            case Token.MOD:
                                return new NumberLiteral(info, d1 % d2);
                            }
                        }
                    }
                    catch (ArithmeticException e) {
                        // A divide or mod by 0 is ignored, and expression
                        // is left as is. Runtime will detect and throw
                        // divide by zero. The compiler is not required
                        // to detect divide by zero, because it can't
                        // always perform this check.
                    }
                }
            }

            node.setLeftExpression(left);
            node.setRightExpression(right);
            return node;
        }
        
        public Object visit(RelationalExpression node) {
            SourceInfo info = node.getSourceInfo();
            Token operator = node.getOperator();
            int ID = operator.getID();

            Expression left = visitExpression(node.getLeftExpression());
            Type leftType = left.getType();
            Object leftValue = left.getValue();

            if (ID == Token.ISA) {
                Type rightType = node.getIsaTypeName().getType();

                if (rightType.getObjectClass().isAssignableFrom
                    (leftType.getObjectClass())) {
                    // Widening case. i.e. (5 isa Number) is always true.
                    return new BooleanLiteral(info, true);
                }
 
                node.setLeftExpression(left);
                return node;
            }

            Expression right = visitExpression(node.getRightExpression());
            Type rightType = right.getType();
            Object rightValue = right.getValue();

            if (node.getType() != null &&
                left.isValueKnown() && right.isValueKnown() &&
                leftValue != null && rightValue != null) {

                Type type = leftType;

                // TODO: support JDK1.2 Comparable interface
                
                if (leftValue instanceof Number &&
                    rightValue instanceof Number &&
                    type.equals(rightType)) {
                    
                    Class clazz = type.getObjectClass();
                    
                    Number lv = (Number)leftValue;
                    Number rv = (Number)rightValue;
                        
                    if (clazz == Integer.class) {
                        int i1 = lv.intValue();
                        int i2 = rv.intValue();

                        switch (ID) {
                        case Token.EQ:
                            return new BooleanLiteral(info, i1 == i2);
                        case Token.NE:
                            return new BooleanLiteral(info, i1 != i2);
                        case Token.LT:
                            return new BooleanLiteral(info, i1 < i2);
                        case Token.GT:
                            return new BooleanLiteral(info, i1 > i2);
                        case Token.LE:
                            return new BooleanLiteral(info, i1 <= i2);
                        case Token.GE:
                            return new BooleanLiteral(info, i1 >= i2);
                        }
                    }
                    else if (clazz == Float.class) {
                        float f1 = lv.floatValue();
                        float f2 = rv.floatValue();

                        switch (ID) {
                        case Token.EQ:
                            return new BooleanLiteral(info, f1 == f2);
                        case Token.NE:
                            return new BooleanLiteral(info, f1 != f2);
                        case Token.LT:
                            return new BooleanLiteral(info, f1 < f2);
                        case Token.GT:
                            return new BooleanLiteral(info, f1 > f2);
                        case Token.LE:
                            return new BooleanLiteral(info, f1 <= f2);
                        case Token.GE:
                            return new BooleanLiteral(info, f1 >= f2);
                        }
                    }
                    else if (clazz == Long.class) {
                        long L1 = lv.longValue();
                        long L2 = rv.longValue();

                        switch (ID) {
                        case Token.EQ:
                            return new BooleanLiteral(info, L1 == L2);
                        case Token.NE:
                            return new BooleanLiteral(info, L1 != L2);
                        case Token.LT:
                            return new BooleanLiteral(info, L1 < L2);
                        case Token.GT:
                            return new BooleanLiteral(info, L1 > L2);
                        case Token.LE:
                            return new BooleanLiteral(info, L1 <= L2);
                        case Token.GE:
                            return new BooleanLiteral(info, L1 >= L2);
                        }
                    }
                    else if (clazz == Double.class) {
                        double d1 = lv.doubleValue();
                        double d2 = rv.doubleValue();

                        switch (ID) {
                        case Token.EQ:
                            return new BooleanLiteral(info, d1 == d2);
                        case Token.NE:
                            return new BooleanLiteral(info, d1 != d2);
                        case Token.LT:
                            return new BooleanLiteral(info, d1 < d2);
                        case Token.GT:
                            return new BooleanLiteral(info, d1 > d2);
                        case Token.LE:
                            return new BooleanLiteral(info, d1 <= d2);
                        case Token.GE:
                            return new BooleanLiteral(info, d1 >= d2);
                        }
                    }
                }
                else if (leftValue instanceof String &&
                         rightValue instanceof String) {
                    
                    int result = 
                        ((String)leftValue).compareTo((String)rightValue);
                    
                    switch (ID) {
                    case Token.EQ:
                        return new BooleanLiteral(info, result == 0);
                    case Token.NE:
                        return new BooleanLiteral(info, result != 0);
                    case Token.LT:
                        return new BooleanLiteral(info, result < 0);
                    case Token.GT:
                        return new BooleanLiteral(info, result > 0);
                    case Token.LE:
                        return new BooleanLiteral(info, result <= 0);
                    case Token.GE:
                        return new BooleanLiteral(info, result >= 0);
                    }
                }
            }

            if (leftType.isNonNull() && rightType.isNonNull() &&
                leftType.getObjectClass() == Boolean.class &&
                rightType.getObjectClass() == Boolean.class) {

                if (left.isValueKnown() && leftValue != null) {
                    boolean lv = ((Boolean)leftValue).booleanValue();

                    if (right.isValueKnown() && rightValue != null) {
                        boolean rv = ((Boolean)rightValue).booleanValue();
                        
                        if (ID == Token.EQ) {
                            return new BooleanLiteral(info, lv == rv);
                        }
                        else if (ID == Token.NE) {
                            return new BooleanLiteral(info, lv != rv);
                        }
                    }
                    else {
                        if (lv) {
                            // (true == (right)) is always (right)
                            if (ID == Token.EQ) {
                                return right;
                            }
                            // (true != (right)) is always !(right)
                            else if (ID == Token.NE) {
                                right.convertTo(Type.BOOLEAN_TYPE);
                                right = new NotExpression(info, right);
                                right.convertTo(Type.BOOLEAN_TYPE);
                                return right;
                            }
                        }
                        else {
                            // (false == (right)) is always !(right)
                            if (ID == Token.EQ) {
                                right.convertTo(Type.BOOLEAN_TYPE);
                                right = new NotExpression(info, right);
                                right.convertTo(Type.BOOLEAN_TYPE);
                                return right;
                            }
                            // (false != (right)) is always (right)
                            else if (ID == Token.NE) {
                                return right;
                            }
                        }
                    }
                }
                else if (right.isValueKnown() && rightValue != null) {
                    boolean rv = ((Boolean)rightValue).booleanValue();

                    if (rv) {
                        // ((left) == true) is always (left)
                        if (ID == Token.EQ) {
                            return left;
                        }
                        // ((left) != true) is always !(left)
                        else if (ID == Token.NE) {
                            left.convertTo(Type.BOOLEAN_TYPE);
                            left = new NotExpression(info, left);
                            left.setType(Type.BOOLEAN_TYPE);
                            return left;
                        }
                    }
                    else {
                        // ((left) == false) is always !(left)
                        if (ID == Token.EQ) {
                            left.convertTo(Type.BOOLEAN_TYPE);
                            left = new NotExpression(info, left);
                            left.setType(Type.BOOLEAN_TYPE);
                            return left;
                        }
                        // ((left) != false) is always (left)
                        else if (ID == Token.NE) {
                            return left;
                        }
                    }
                }
            }

            // Optimize tests against null.
            
            boolean leftIsNull = left.isValueKnown() && leftValue == null;
            boolean rightIsNull = right.isValueKnown() && rightValue == null;

            if (leftIsNull && rightIsNull) {
                if (ID == Token.EQ) {
                    return new BooleanLiteral(info, true);
                }
                else if (ID == Token.NE) {
                    return new BooleanLiteral(info, false);
                }
            }
            /* This optimization may eliminate expressions with side effects.
            else if (leftIsNull && rightType.isNonNull() ||
                     rightIsNull && leftType.isNonNull()) {
            */
            else if (leftIsNull && rightType.isNonNull() &&
                     (right instanceof Literal ||
                      right instanceof VariableRef) ||
                     rightIsNull && leftType.isNonNull() &&
                     (left instanceof Literal ||
                      left instanceof VariableRef)) {

                if (ID == Token.EQ) {
                    return new BooleanLiteral(info, false);
                }
                else if (ID == Token.NE) {
                    return new BooleanLiteral(info, true);
                }
            }

            node.setLeftExpression(left);
            node.setRightExpression(right);
            return node;
        }
        
        public Object visit(AndExpression node) {
            SourceInfo info = node.getSourceInfo();
             
            Expression left = visitExpression(node.getLeftExpression());
            Expression right = visitExpression(node.getRightExpression());
            
            if (left.isValueKnown()) {
                Object leftValue = left.getValue();
                if (leftValue instanceof Boolean) {
                    if ( ((Boolean)leftValue).booleanValue() ) {
                        // "And"ing with true has no effect. Simply return the
                        // right expression.
                        return right;
                    }
                    else {
                        // Short circuit result if value is false.
                        return new BooleanLiteral(info, false);
                    }
                }
            }
            
            if (right.isValueKnown()) {
                Object rightValue = right.getValue();
                if (rightValue instanceof Boolean) {
                    if ( ((Boolean)rightValue).booleanValue() ) {
                        // "And"ing with true has no effect. Simply return the
                        // left expression.
                        return left;
                    }
                    else {
                        // Don't short cicuit in this case.
                    }
                }
            }
            
            node.setLeftExpression(left);
            node.setRightExpression(right);
            return node;
        }
        
        public Object visit(OrExpression node) {
            SourceInfo info = node.getSourceInfo();
            
            Expression left = visitExpression(node.getLeftExpression());
            Expression right = visitExpression(node.getRightExpression());
            
            if (left.isValueKnown()) {
                Object leftValue = left.getValue();
                if (leftValue instanceof Boolean) {
                    if ( ((Boolean)leftValue).booleanValue() ) {
                        // Short circuit result if value is true.
                        return new BooleanLiteral(info, true);
                    }
                    else {
                        // "Or"ing with false has no effect. Simply return the
                        // right expression.
                        return right;
                    }
                }
            }
            
            if (right.isValueKnown()) {
                Object rightValue = right.getValue();
                if (rightValue instanceof Boolean) {
                    if ( ((Boolean)rightValue).booleanValue() ) {
                        // Don't short cicuit in this case.
                    }
                    else {
                        // "Or"ing with false has no effect. Simply return the
                        // left expression.
                        return left;
                    }
                }
            }
            
            node.setLeftExpression(left);
            node.setRightExpression(right);
            return node;
        }
    }
}
