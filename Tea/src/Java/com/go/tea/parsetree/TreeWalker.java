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

/******************************************************************************
 * A TreeWalker traverses a parse tree in its canonical order. By overriding
 * a visit method, individual nodes can be captured and processed based on
 * their type. Call super.visit inside the overriden visit method to ensure
 * that the node's children are properly traversed.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 */
public abstract class TreeWalker implements NodeVisitor {
    public Object visit(Template node) {
        node.getName().accept(this);

        Variable[] params = node.getParams();
        if (params != null) {
            for (int i=0; i<params.length; i++) {
                params[i].accept(this);
            }
        }

        Statement stmt = node.getStatement();
        if (stmt != null) {
            stmt.accept(this);
        }
        
        return null;
    }
        
    public Object visit(Name node) {
        return null;
    }

    public Object visit(TypeName node) {
        return null;
    }
        
    public Object visit(Variable node) {
        node.getTypeName().accept(this);
        return null;
    }
        
    public Object visit(ExpressionList node) {
        Expression[] exprs = node.getExpressions();
        for (int i=0; i<exprs.length; i++) {
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
                stmts[i].accept(this);
            }
        }
        
        return null;
    }
        
    public Object visit(Block node) {
        Statement init = node.getInitializer();
        if (init != null) {
            init.accept(this);
        }

        visit((StatementList)node);

        Statement fin = node.getFinalizer();
        if (fin != null) {
            fin.accept(this);
        }

        return null;
    }
        
    public Object visit(AssignmentStatement node) {
        node.getLValue().accept(this);
        node.getRValue().accept(this);
        
        return null;
    }
        
    public Object visit(BreakStatement node) {
        return null;
    }

    public Object visit(ForeachStatement node) {
        node.getLoopVariable().accept(this);
        node.getRange().accept(this);
        Expression endRange = node.getEndRange();
        if (endRange != null) {
            endRange.accept(this);
        }

        Statement init = node.getInitializer();
        if (init != null) {
            init.accept(this);
        }

        Block body = node.getBody();
        if (body != null) {
            body.accept(this);
        }
        
        return null;
    }
        
    public Object visit(IfStatement node) {
        node.getCondition().accept(this);
            
        Block block = node.getThenPart();
        if (block != null) {
            block.accept(this);
        }
        
        block = node.getElsePart();
        if (block != null) {
            block.accept(this);
        }
        
        return null;
    }

    public Object visit(SubstitutionStatement node) {
        return null;
    }

    public Object visit(ExpressionStatement node) {
        node.getExpression().accept(this);
        return null;
    }

    public Object visit(ReturnStatement node) {
        Expression expr = node.getExpression();
        if (expr != null) {
            expr.accept(this);
        }
        return null;
    }

    public Object visit(ExceptionGuardStatement node) {
        Statement stmt = node.getGuarded();
        if (stmt != null) {
            stmt.accept(this);
        }
        stmt = node.getReplacement();
        if (stmt != null) {
            stmt.accept(this);
        }
        return null;
    }

    public Object visit(Expression node) {
        return null;
    }

    public Object visit(ParenExpression node) {
        node.getExpression().accept(this);
        return null;
    }

    public Object visit(NewArrayExpression node) {
        node.getExpressionList().accept(this);
        return null;
    }

    public Object visit(FunctionCallExpression node) {
        return visit((CallExpression)node);
    }

    public Object visit(TemplateCallExpression node) {
        return visit((CallExpression)node);
    }

    private Object visit(CallExpression node) {
        node.getParams().accept(this);
        
        Statement init = node.getInitializer();
        if (init != null) {
            init.accept(this);
        }

        Block subParam = node.getSubstitutionParam();
        if (subParam != null) {
            subParam.accept(this);
        }
        
        return null;
    }

    public Object visit(VariableRef node) {
        Variable v = node.getVariable();
        if (v != null) {
            v.accept(this);
        }

        return null;
    }

    public Object visit(Lookup node) {
        node.getExpression().accept(this);
        return null;
    }

    public Object visit(ArrayLookup node) {
        node.getExpression().accept(this);
        node.getLookupIndex().accept(this);
        return null;
    }

    public Object visit(NegateExpression node) {
        node.getExpression().accept(this);
        return null;
    }

    public Object visit(NotExpression node) {
        node.getExpression().accept(this);
        return null;
    }

    private Object visit(BinaryExpression node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);
        return null;
    }

    public Object visit(ConcatenateExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(ArithmeticExpression node) {
        return visit((BinaryExpression)node);
    }

    public Object visit(RelationalExpression node) {
        if (node.getIsaTypeName() != null) {
            node.getLeftExpression().accept(this);
            node.getIsaTypeName().accept(this);
            return null;
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
        return null;
    }

    public Object visit(BooleanLiteral node) {
        return null;
    }

    public Object visit(StringLiteral node) {
        return null;
    }

    public Object visit(NumberLiteral node) {
        return null;
    }
}
