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
 * A NodeVisitor enables operations to be performed on a parse tree.
 * The Visitor design pattern is discussed in detail in <i>Design Patterns</i>
 * (ISBN 0-201-63361-2) by Gamma, Helm, Johnson and Vlissides.
 *
 * <p>The traditional operations performed on parse trees are type checking
 * and code generation. The Visitor allows those operations to all be
 * encapsulated into one place instead of having that functionality spread
 * out into every Node subclass. It also makes it easy to target multiple
 * languages by allowing any kind of code generation Visitor to be
 * designed.
 *
 * <p>When using a Visitor to traverse a parse tree, the code responsible
 * for moving the traversal into children nodes can either be placed in
 * the nodes or in the Visitor implementation. The NodeVisitor places that
 * responsibility onto NodeVisitor implementations because it is much
 * more flexible. As a result, all Nodes have the same simple implementation
 * for their "accept" method, but do not inherit it.
 *
 * <p>Every visit method in this interface returns an Object. The definition
 * of that returned Object is left up to the specific implementation of the
 * NodeVisitor. Most NodeVisitors can simply return null, but those that
 * are modifying a parse tree or are using one to create another can use the
 * returned Object to pass around newly created Nodes.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 * @see Node#accept(NodeVisitor)
 */
public interface NodeVisitor {
    public Object visit(Template node);
    public Object visit(Name node);
    public Object visit(TypeName node);
    public Object visit(Variable node);
    public Object visit(ExpressionList node);

    public Object visit(Statement node);
    public Object visit(StatementList node);
    public Object visit(Block node);
    public Object visit(AssignmentStatement node);
    public Object visit(ForeachStatement node);
    public Object visit(IfStatement node);
    public Object visit(SubstitutionStatement node);
    public Object visit(ExpressionStatement node);
    public Object visit(ReturnStatement node);
    public Object visit(ExceptionGuardStatement node);
    public Object visit(BreakStatement node);

    public Object visit(Expression node);
    public Object visit(ParenExpression node);
    public Object visit(NewArrayExpression node);
    public Object visit(FunctionCallExpression node);
    public Object visit(TemplateCallExpression node);
    public Object visit(VariableRef node);
    public Object visit(Lookup node);
    public Object visit(ArrayLookup node);
    public Object visit(NegateExpression node);
    public Object visit(NotExpression node);
    public Object visit(ConcatenateExpression node);
    public Object visit(ArithmeticExpression node);
    public Object visit(RelationalExpression node);
    public Object visit(AndExpression node);
    public Object visit(OrExpression node);

    public Object visit(NullLiteral node);
    public Object visit(BooleanLiteral node);
    public Object visit(StringLiteral node);
    public Object visit(NumberLiteral node);
}
