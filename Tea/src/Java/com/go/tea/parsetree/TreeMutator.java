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

package com.go.tea.parsetree;

import java.util.Iterator;
import com.go.tea.compiler.Type;
import com.go.tea.compiler.SourceInfo;

/******************************************************************************
 * TreeMutator is similar to {@link TreeWalker TreeWalker} in that it
 * traverses a parse tree in canonocal order, and only a few visit methods
 * should be overridden. The key difference is that visit methods must
 * return a node of the same type as the one passed in. By returning a node
 * which isn't the same as the one passed in, a node can be replaced.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 */
public abstract class TreeMutator implements NodeVisitor {
    public Object visit(Template node) {
        node.getName().accept(this);

        Variable[] params = node.getParams();
        if (params != null) {
            for (int i=0; i<params.length; i++) {
                params[i] = (Variable)params[i].accept(this);
            }
        }

        Statement stmt = node.getStatement();
        if (stmt != null) {
            node.setStatement((Statement)stmt.accept(this));
        }
        
        return node;
    }
        
    public Object visit(Name node) {
        return node;
    }
        
    public Object visit(TypeName node) {
        return node;
    }

    public Object visit(Variable node) {
        node.getTypeName().accept(this);
        return node;
    }
        
    public Object visit(ExpressionList node) {
        Expression[] exprs = node.getExpressions();
        for (int i=0; i<exprs.length; i++) {
            exprs[i] = visitExpression(exprs[i]);
        }
        
        return node;
    }
        
    public Object visit(Statement node) {
        return node;
    }
        
    public Object visit(StatementList node) {
        Statement[] stmts = node.getStatements();
        if (stmts != null) {
            for (int i=0; i<stmts.length; i++) {
                stmts[i] = (Statement)stmts[i].accept(this);
            }
        }
        
        return node;
    }
        
    public Object visit(Block node) {
        Statement init = node.getInitializer();
        if (init != null) {
            node.setInitializer((Statement)init.accept(this));
        }

        visit((StatementList)node);

        Statement fin = node.getFinalizer();
        if (fin != null) {
            node.setFinalizer((Statement)fin.accept(this));
        }

        return node;
    }
        
    public Object visit(AssignmentStatement node) {
        node.getLValue().accept(this);
        node.setRValue(visitExpression(node.getRValue()));
        
        return node;
    }
        
    public Object visit(BreakStatement node) {
        return node;
    }

    public Object visit(ForeachStatement node) {
        node.getLoopVariable().accept(this);
        node.setRange(visitExpression(node.getRange()));
        Expression endRange = node.getEndRange();
        if (endRange != null) {
            node.setEndRange(visitExpression(endRange));
        }

        Statement init = node.getInitializer();
        if (init != null) {
            node.setInitializer((Statement)init.accept(this));
        }

        Block body = node.getBody();
        if (body != null) {
            node.setBody(visitBlock(body));
        }
        
        return node;
    }
        
    public Object visit(IfStatement node) {
        node.setCondition(visitExpression(node.getCondition()));

        Block block = node.getThenPart();
        if (block != null) {
            node.setThenPart(visitBlock(block));
        }
        
        block = node.getElsePart();
        if (block != null) {
            node.setElsePart(visitBlock(block));
        }
        
        return node;
    }

    public Object visit(SubstitutionStatement node) {
        return node;
    }

    public Object visit(ExpressionStatement node) {
        node.setExpression(visitExpression(node.getExpression()));
        return node;
    }

    public Object visit(ReturnStatement node) {
        Expression expr = node.getExpression();
        if (expr != null) {
            node.setExpression(visitExpression(node.getExpression()));
        }
        return node;
    }

    public Object visit(ExceptionGuardStatement node) {
        node.setGuarded((Statement)node.getGuarded().accept(this));
        Statement stmt = node.getReplacement();
        if (stmt != null) {
            node.setReplacement((Statement)stmt.accept(this));
        }
        return node;
    }

    public Object visit(Expression node) {
        return node;
    }

    public Object visit(ParenExpression node) {
        node.setExpression(visitExpression(node.getExpression()));
        return node;
    }

    public Object visit(NewArrayExpression node) {
        node.setExpressionList
            ((ExpressionList)node.getExpressionList().accept(this));
        return node;
    }

    public Object visit(FunctionCallExpression node) {
        return visit((CallExpression)node);
    }

    public Object visit(TemplateCallExpression node) {
        return visit((CallExpression)node);
    }

    private Object visit(CallExpression node) {
        node.setParams((ExpressionList)node.getParams().accept(this));
        
        Statement init = node.getInitializer();
        if (init != null) {
            node.setInitializer((Statement)init.accept(this));
        }

        Block subParam = node.getSubstitutionParam();
        if (subParam != null) {
            node.setSubstitutionParam(visitBlock(subParam));
        }
        
        return node;
    }

    public Object visit(VariableRef node) {
        Variable v = node.getVariable();
        if (v != null) {
            node.setVariable((Variable)v.accept(this));
        }

        return node;
    }

    public Object visit(Lookup node) {
        node.setExpression(visitExpression(node.getExpression()));
        return node;
    }

    public Object visit(ArrayLookup node) {
        node.setExpression(visitExpression(node.getExpression()));
        node.setLookupIndex(visitExpression(node.getLookupIndex()));
        return node;
    }

    public Object visit(NegateExpression node) {
        node.setExpression(visitExpression(node.getExpression()));
        return node;
    }

    public Object visit(NotExpression node) {
        node.setExpression(visitExpression(node.getExpression()));
        return node;
    }

    private Object visit(BinaryExpression node) {
        node.setLeftExpression(visitExpression(node.getLeftExpression()));
        node.setRightExpression(visitExpression(node.getRightExpression()));
        return node;
    }

    public Object visit(ConcatenateExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(ArithmeticExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(RelationalExpression node) {
        if (node.getIsaTypeName() != null) {
            node.setLeftExpression(visitExpression(node.getLeftExpression()));
            node.getIsaTypeName().accept(this);
            return node;
        }
        else {
            return visit((BinaryExpression)node);
        }
    }

    public Object visit(AndExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(OrExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(NullLiteral node) {
        return node;
    }

    public Object visit(BooleanLiteral node) {
        return node;
    }

    public Object visit(StringLiteral node) {
        return node;
    }

    public Object visit(NumberLiteral node) {
        return node;
    }

    /**
     * All expressions pass through this method to ensure the expression's
     * type is preserved.
     */
    protected Expression visitExpression(Expression expr) {
        if (expr == null) {
            return null;
        }

        Expression newExpr = (Expression)expr.accept(this);
        if (expr != newExpr) {
            Type newType = newExpr.getType();

            if (newType == null || !newType.equals(expr.getType())) {
                Iterator it = expr.getConversionChain().iterator();
                while (it.hasNext()) {
                    Expression.Conversion conv = 
                        (Expression.Conversion)it.next();
                    newExpr.convertTo
                        (conv.getToType(), conv.isCastPreferred());
                }
            }
        }
        return newExpr;
    }

    /**
     * Visit a Block to ensure that new Statement is a Block.
     */
    protected Block visitBlock(Block block) {
        if (block == null) {
            return null;
        }

        Statement stmt = (Statement)block.accept(this);

        if (stmt instanceof Block) {
            return (Block)stmt;
        }
        else if (stmt != null) {
            return new Block(stmt);
        }
        else {
            return new Block(block.getSourceInfo());
        }
    }

    // TODO: create a visitNode for all nodes to ensure scope is preserved?
}
