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
import java.util.Vector;
import com.go.tea.parsetree.*;
import com.go.trove.io.SourceReader;

/******************************************************************************
 * A Parser creates the parse tree for a template by reading tokens emitted by
 * a {@link Scanner}. The parse tree represents the entire template as a
 * data structure composed of specialized nodes. Add an {@link ErrorListener}
 * to capture any syntax errors detected by the Parser.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  5/31/01 <!-- $-->
 */
public class Parser {
    private Scanner mScanner;
    private CompilationUnit mUnit;

    private Vector mListeners = new Vector(1);
    private int mErrorCount = 0;
    private int mEOFErrorCount = 0;

    private MessageFormatter mFormatter;

    public Parser(Scanner scanner) {
        this(scanner, null);
    }

    public Parser(Scanner scanner, CompilationUnit unit) {
        mScanner = scanner;
        mUnit = unit;
        mFormatter = MessageFormatter.lookup(this);
    }

    public void addErrorListener(ErrorListener listener) {
        mListeners.addElement(listener);
    }

    public void removeErrorListener(ErrorListener listener) {
        mListeners.removeElement(listener);
    }

    private void dispatchParseError(ErrorEvent e) {
        mErrorCount++;

        synchronized (mListeners) {
            for (int i = 0; i < mListeners.size(); i++) {
                ((ErrorListener)mListeners.elementAt(i)).compileError(e);
            }
        }
    }

    private void error(String str, Token culprit) {
        str = mFormatter.format(str);

        if (culprit.getID() == Token.EOF) {
            if (mEOFErrorCount++ == 0) {
                str = mFormatter.format("error.at.end", str);
            }
            else {
                return;
            }
        }

        dispatchParseError(new ErrorEvent(this, str, culprit, mUnit));
    }

    private void error(String str, String arg, Token culprit) {
        str = mFormatter.format(str, arg);

        if (culprit.getID() == Token.EOF) {
            if (mEOFErrorCount++ == 0) {
                str = mFormatter.format("error.at.end", str);
            }
            else {
                return;
            }
        }

        dispatchParseError(new ErrorEvent(this, str, culprit, mUnit));
    }

    private void error(String str, SourceInfo info) {
        str = mFormatter.format(str);
        dispatchParseError(new ErrorEvent(this, str, info, mUnit));
    }

    /**
     * Returns a parse tree by its root node. The parse tree is generated
     * from tokens read from the scanner. Any errors encountered while
     * parsing are delivered by dispatching an event. Add an error listener
     * in order to capture parse errors.
     *
     * @return Non-null template node, even if there were errors during 
     * parsing.
     * @see Parser#addErrorListener
     */
    public Template parse() throws IOException {
        Template t = parseTemplate();
        
        if (t != null) {
            return t;
        }

        return new Template(new SourceInfo(0, 0, 0), null, null, false, null);
    }

    public int getErrorCount() {
        return mErrorCount;
    }

    private Token read() throws IOException {
        return mScanner.readToken();
    }

    private Token peek() throws IOException {
        return mScanner.peekToken();
    }

    private void unread(Token token) throws IOException {
        mScanner.unreadToken(token);
    }

    private Template parseTemplate() throws IOException {
        Name name;
        Variable[] params = null;
        StatementList statementList;
        
        Token token = read();
        SourceInfo templateInfo = token.getSourceInfo();

        if (token.getID() != Token.TEMPLATE) {
            if (token.getID() == Token.STRING && 
                peek().getID() == Token.TEMPLATE) {
                
                error("template.start", token);
                token = read();
            }
            else {
                error("template.declaration", token);
            }
        }

        SourceInfo nameInfo = peek().getSourceInfo();
        name = new Name(nameInfo, parseIdentifier());

        params = parseFormalParameters();

        // Check if a block is accepted as a parameter. Pattern is { ... }
        boolean subParam = false;
        token = peek();
        if (token.getID() == Token.LBRACE || token.getID() == Token.ELLIPSIS) {
            if (token.getID() == Token.ELLIPSIS) {
                error("template.substitution.lbrace", token);
            }
            else {
                read();
                token = peek();
            }

            if (token.getID() == Token.ELLIPSIS) {
                read();
                token = peek();
                if (token.getID() == Token.RBRACE) {
                    read();
                    subParam = true;
                }
                else {
                    error("template.substitution.rbrace", token);
                }
            }
            else {
                error("template.substitution.ellipsis", token);
                if (token.getID() == Token.RBRACE) {
                    read();
                    subParam = true;
                }
            }
        }

        // Parse statements until end of file is reached.
        Vector v = new Vector(10, 0);
        
        SourceInfo info = peek().getSourceInfo();
        
        Statement statement = null;
        while (peek().getID() != Token.EOF) {
            statement = parseStatement();
            v.addElement(statement);
        }

        if (statement != null) {
            info = info.setEndPosition(statement.getSourceInfo());
        }
        
        Statement[] statements = new Statement[v.size()];
        v.copyInto(statements);
        
        statementList = new StatementList(info, statements);

        templateInfo = 
            templateInfo.setEndPosition(statementList.getSourceInfo());

        return new Template(templateInfo, name, params, subParam, 
                            statementList);
    }

    private String parseIdentifier() throws IOException {
        Token token = read();
        if (token.getID() != Token.IDENT) {
            if (token.isReservedWord()) {
                error("identifier.reserved.word", token.getImage(), token);
                return token.getImage();
            }
            else {
                error("identifier.expected", token);
                return "";
            }
        }

        return token.getStringValue();
    }

    private Name parseName() throws IOException {
        SourceInfo info = null;
        StringBuffer name = new StringBuffer(20);

        while (true) {
            Token token = read();
            if (token.getID() != Token.IDENT) {
                if (info == null) {
                    info = token.getSourceInfo();
                }
                else {
                    info = info.setEndPosition(token.getSourceInfo());
                }

                if (token.isReservedWord()) {
                    error("name.reserved.word", token.getImage(), token);
                    name.append(token.getImage());
                }
                else {
                    error("name.identifier.expected", token);
                    break;
                }
            }
            else {
                name.append(token.getStringValue());
                if (info == null) {
                    info = token.getSourceInfo();
                }
                else {
                    info = info.setEndPosition(token.getSourceInfo());
                }
            }

            token = peek();
            if (token.getID() != Token.DOT) {
                break;
            }
            else {
                token = read();
                name.append('.');
                info = info.setEndPosition(token.getSourceInfo());
            }
        }

        return new Name(info, name.toString());
    }

    private TypeName parseTypeName() throws IOException {
        Name name = parseName();

        SourceInfo info = name.getSourceInfo();
        int dim = 0;

        while (peek().getID() == Token.LBRACK) {
            dim++;
            Token token = read(); // read the left bracket
            if (peek().getID() == Token.RBRACK) {
                token = read(); // read the right bracket
            }
            else {
                error("name.rbracket", peek());
            }
            info = info.setEndPosition(token.getSourceInfo());
        }

        return new TypeName(info, name, dim);
    }

    private Variable parseVariableDeclaration() throws IOException {
        TypeName typeName = parseTypeName();

        SourceInfo info = peek().getSourceInfo();
        String varName = parseIdentifier();

        return new Variable(info, varName, typeName);
    }

    private Variable[] parseFormalParameters() throws IOException {
        Token token = peek();
        
        if (token.getID() == Token.LPAREN) {
            read(); // read the left paren
            token = peek();
        }
        else {
            error("params.lparen", token);
        }

        Vector vars = new Vector(10, 0);

        if (token.getID() == Token.RPAREN) {
            // Empty list detected.
        }
        else {
            Expression expr = null;
            while (true) {
                if ((token = peek()).getID() == Token.RPAREN) {
                    error("params.premature.end", token);
                    break;
                }

                vars.addElement(parseVariableDeclaration());

                if ((token = peek()).getID() != Token.COMMA) {
                    break;
                }
                else {
                    read(); // read the comma
                }
            }
        }

        if (token.getID() == Token.RPAREN) {
            read(); // read the right paren
        }
        else {
            error("params.rparen.expected", token);
        }

        Variable[] variables = new Variable[vars.size()];
        vars.copyInto(variables);
        
        return variables;
    }

    private VariableRef parseLValue() throws IOException {
        return parseLValue(read());
    }
    
    private VariableRef parseLValue(Token token) throws IOException {
        String loopVarName;
        if (token.getID() != Token.IDENT) {
            if (token.isReservedWord()) {
                error("lvalue.reserved.word", token.getImage(), token);
                loopVarName = token.getImage();
            }
            else {
                error("lvalue.identifier.expected", token);
                loopVarName = "";
            }
        }
        else {
            loopVarName = token.getStringValue();
        }

        return new VariableRef(token.getSourceInfo(), loopVarName);
    }
    
    private Block parseBlock() throws IOException {
        Token token = peek();
        SourceInfo info = token.getSourceInfo();

        if (token.getID() != Token.LBRACE) {
            error("block.lbrace.expected", token);
            if (token.getID() == Token.SEMI) {
                read();
                return new Block(info, new Statement[0]);
            }
        }
        else {
            token = read(); // read the left brace
        }
        
        Vector v = new Vector(10, 0);
        Token p;
        while ((p = peek()).getID() != Token.RBRACE) {
            if (p.getID() == Token.EOF) {
                error("block.rbrace.expected", p);
                break;
            }
            v.addElement(parseStatement());
        }
        token = read(); // read the right brace

        Statement[] statements = new Statement[v.size()];
        v.copyInto(statements);

        info = info.setEndPosition(token.getSourceInfo());

        return new Block(info, statements);
    }

    private Statement parseStatement() throws IOException {
        Statement st = null;

        while (st == null) {
            Token token = read();

            switch (token.getID()) {
            case Token.SEMI:
                // If the token after the semi-colon is a right brace,
                // we can't simply skip it because this method
                // can't properly parse a right brace. Instead, return
                // an empty placeholder statement. The parseBlock method
                // will then be able to parse the right brace properly.
                
                int ID = peek().getID();
                if (ID == Token.RBRACE || ID == Token.EOF) {
                    st = new Statement(token.getSourceInfo());
                }
                else {
                    // Skip this token
                }
                break;
            case Token.BREAK:
                st = parseBreakStatement(token);
                break;
            case Token.IF:
                st = parseIfStatement(token);
                break;
            case Token.FOREACH:
                st = parseForeachStatement(token);
                break;
            case Token.IDENT:
                if (peek().getID() == Token.ASSIGN) {
                    st = parseAssignmentStatement(token);
                }
                else {
                    st = new ExpressionStatement(parseExpression(token));
                }
                break;
            case Token.ELLIPSIS:
                st = new SubstitutionStatement(token.getSourceInfo());
                break;
                
            case Token.EOF:
                error("statement.expected", token);
                st = new Statement(token.getSourceInfo());
                break;
                
                // Handle some error cases in a specialized way so that
                // the error message produced is more meaningful.
            case Token.ELSE:
                error("statement.misuse.else", token);
                st = parseBlock();
                break;
            case Token.IN:
                error("statement.misuse.in", token);
                st = new ExpressionStatement(parseExpression(token));
                break;

            case Token.REVERSE:
                error("statement.misuse.reverse", token);
                st = new ExpressionStatement(parseExpression(token));
                break;
                
            default:
                st = new ExpressionStatement(parseExpression(token));
                break;
            }
        }

        return st;
    }

    // When this is called, the keyword "break" has already been read.
    private BreakStatement parseBreakStatement(Token token) 
        throws IOException {
        return new BreakStatement(token.getSourceInfo());
    }

    // When this is called, the keyword "if" has already been read.
    private IfStatement parseIfStatement(Token token) throws IOException {
        SourceInfo info = token.getSourceInfo();

        Expression condition = parseExpression();

        if (!(condition instanceof ParenExpression)) {
            error("if.condition", condition.getSourceInfo());
        }

        Block thenPart = parseBlock();
        Block elsePart = null;

        token = peek();
        if (token.getID() != Token.ELSE) {
            info = info.setEndPosition(thenPart.getSourceInfo());
        }
        else {
            read(); // read the else keyword
            token = peek();
            if (token.getID() == Token.IF) {
                elsePart = new Block(parseIfStatement(read()));
            }
            else {
                elsePart = parseBlock();
            }

            info = info.setEndPosition(elsePart.getSourceInfo());
        }

        return new IfStatement(info, condition, thenPart, elsePart);
    }

    // When this is called, the keyword "foreach" has already been read.
    private ForeachStatement parseForeachStatement(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();

        token = peek();
        if (token.getID() == Token.LPAREN) {
            read();
        }
        else {
            error("foreach.lparen.expected", token);
        }

        VariableRef loopVar = parseLValue();

        token = peek();
        if (token.getID() == Token.IN) {
            read();
        }
        else {
            error("foreach.in.expected", token);
        }

        Expression range = parseExpression();
        Expression endRange = null;
        
        token = peek();
        if (token.getID() == Token.DOTDOT) {
            read();
            endRange = parseExpression();
            token = peek();
        }

        boolean reverse = false;
        if (token.getID() == Token.REVERSE) {
            read();
            reverse = true;
            token = peek();
        }

        if (token.getID() == Token.RPAREN) {
            read();
        }
        else {
            error("foreach.rparen.expected", token);
        }

        Block body = parseBlock();

        info = info.setEndPosition(body.getSourceInfo());

        return new ForeachStatement
            (info, loopVar, range, endRange, reverse, body);
    }

    // When this is called, the identifier token has already been read.
    private AssignmentStatement parseAssignmentStatement(Token token)
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        VariableRef lvalue = parseLValue(token);

        if (peek().getID() == Token.ASSIGN) {
            read();
        }
        else {
            error("assignment.equals.expected", peek());
        }

        Expression rvalue = parseExpression();
        info = info.setEndPosition(rvalue.getSourceInfo());

        return new AssignmentStatement(info, lvalue, rvalue);
    }

    /**
     * @param bracketed True if the list is bounded by brackets instead of
     * parenthesis.
     */
    private ExpressionList parseList(boolean bracketed) throws IOException {
        int leftID;
        int rightID;

        if (!bracketed) {
            leftID = Token.LPAREN;
            rightID = Token.RPAREN;
        }
        else {
            leftID = Token.LBRACK;
            rightID = Token.RBRACK;
        }

        Token token = peek();
        SourceInfo info = token.getSourceInfo();
        
        if (token.getID() == leftID) {
            read(); // read the left paren
            token = peek();
        }
        else {
            if (!bracketed) {
                error("list.lparen.expected", token);
            }
            else {
                error("list.lbracket.expected", token);
            }
        }

        Vector exprs = new Vector(10, 0);
        boolean done = false;

        if (token.getID() == rightID) {
            // Empty list detected
        }
        else {
            Expression expr = null;
            while (true) {
                token = read();

                if (token.getID() == rightID) {
                    error("list.premature.end", token);
                    info = info.setEndPosition(token.getSourceInfo());
                    done = true;
                    break;
                }

                expr = parseExpression(token);
                exprs.addElement(expr);

                token = peek();

                if (token.getID() != Token.COMMA) {
                    break;
                }
                else {
                    token = read(); // read the comma
                }
            }

            if (!done && expr != null) {
                info = info.setEndPosition(expr.getSourceInfo());
            }
        }

        if (!done) {
            token = peek();
            
            if (token.getID() == rightID) {
                token = read(); // read the right paren
                info = info.setEndPosition(token.getSourceInfo());
            }
            else {
                if (!bracketed) {
                    error("list.rparen.expected", token);
                }
                else {
                    error("list.rbracket.expected", token);
                }
            }
        }

        Expression[] elements = new Expression[exprs.size()];
        exprs.copyInto(elements);
        
        return new ExpressionList(info, elements);
    }

    private Expression parseExpression() throws IOException {
        return parseExpression(read());
    }

    private Expression parseExpression(Token token) throws IOException {
        return parseOrExpression(token);
    }

    private Expression parseOrExpression(Token token) throws IOException {
        SourceInfo info = token.getSourceInfo();
        Expression expr = parseAndExpression(token);
        
    loop:
        while (true) {
            token = peek();

            if (token.getID() == Token.OR) {
                read();
                Expression right = parseAndExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new OrExpression(info, token, expr, right);
            }
            else {
                break loop;
            }
        }

        return expr;
    }

    private Expression parseAndExpression(Token token) throws IOException {
        SourceInfo info = token.getSourceInfo();
        Expression expr = parseEqualityExpression(token);
        
    loop:
        while (true) {
            token = peek();

            if (token.getID() == Token.AND) {
                read();
                Expression right = parseEqualityExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new AndExpression(info, token, expr, right);
            }
            else {
                break loop;
            }
        }

        return expr;
    }

    private Expression parseEqualityExpression(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseRelationalExpression(token);
        
    loop:
        while (true) {
            token = peek();

            switch (token.getID()) {
            case Token.ASSIGN:
                error("equality.misuse.assign", token);
                token = new Token(token.getSourceInfo(), Token.EQ);
            case Token.EQ:
            case Token.NE:
                read();
                Expression right = parseRelationalExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new RelationalExpression(info, token, expr, right);
                break;
            default:
                break loop;
            }
        }

        return expr;
    }

    private Expression parseRelationalExpression(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseConcatenateExpression(token);
        
    loop:
        while (true) {
            token = peek();

            switch (token.getID()) {
            case Token.LT:
            case Token.GT:
            case Token.LE:
            case Token.GE:
                read();
                Expression right = parseConcatenateExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new RelationalExpression(info, token, expr, right);
                break;
            case Token.ISA:
                read();
                TypeName typeName = parseTypeName();
                info = info.setEndPosition(typeName.getSourceInfo());
                expr = new RelationalExpression(info, token, expr, typeName);
                break;
            default:
                break loop;
            }
        }

        return expr;
    }

    private Expression parseConcatenateExpression(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseAdditiveExpression(token);
        
    loop:
        while (true) {
            token = peek();

            if (token.getID() == Token.CONCAT) {
                read();
                Expression right = parseAdditiveExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new ConcatenateExpression(info, token, expr, right);
            }
            else {
                break loop;
            }
        }

        return expr;
    }

    private Expression parseAdditiveExpression(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseMultiplicativeExpression(token);
        
    loop:
        while (true) {
            token = peek();

            switch (token.getID()) {
            case Token.PLUS:
            case Token.MINUS:
                read();
                Expression right = parseMultiplicativeExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new ArithmeticExpression(info, token, expr, right);
                break;
            default:
                break loop;
            }
        }

        return expr;
    }

    private Expression parseMultiplicativeExpression(Token token) 
        throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseUnaryExpression(token);
        
    loop:
        while (true) {
            token = peek();

            switch (token.getID()) {
            case Token.MULT:
            case Token.DIV:
            case Token.MOD:
                read();
                Expression right = parseUnaryExpression(read());
                info = info.setEndPosition(right.getSourceInfo());
                expr = new ArithmeticExpression(info, token, expr, right);
                break;
            default:
                break loop;
            }
        }

        return expr;
    }

    private Expression parseUnaryExpression(Token token) throws IOException {
        SourceInfo info;
        Expression expr;

        switch (token.getID()) {
        case Token.NOT:
            info = token.getSourceInfo();
            expr = parseUnaryExpression(read());
            info = info.setEndPosition(expr.getSourceInfo());
            return new NotExpression(info, expr);
        case Token.MINUS:
            info = token.getSourceInfo();
            expr = parseUnaryExpression(read());
            info = info.setEndPosition(expr.getSourceInfo());
            return new NegateExpression(info, expr);
        }

        return parseLookup(token);
    }

    private Expression parseLookup(Token token) throws IOException {

        SourceInfo info = token.getSourceInfo();
        Expression expr = parseFactor(token);
        
        while (true) {
            token = peek();

            if (token.getID() == Token.DOT) {
                // "dot" lookup i.e.: a.b

                Token dot = read(); // read the dot
                
                token = read();
                
                Name lookupName;
                SourceInfo nameInfo = token.getSourceInfo();
                if (token.getID() != Token.IDENT) {
                    if (token.isReservedWord()) {
                        error("lookup.reserved.word", token.getImage(), token);
                        lookupName = new Name(nameInfo, token.getImage());
                    }
                    else {
                        error("lookup.identifier.expected", token);
                        lookupName = new Name(nameInfo, null);
                    }
                }
                else {
                    lookupName = new Name(nameInfo, token.getStringValue());
                    info = info.setEndPosition(nameInfo);
                }
                
                expr = new Lookup(info, expr, dot, lookupName);
            }
            else if (token.getID() == Token.LBRACK) {
                // array lookup i.e.: a[b]

                Token lbrack = read(); // read the left bracket

                token = read();
                
                if (token.getID() == Token.RBRACK) {
                    info = info.setEndPosition(token.getSourceInfo());

                    error("lookup.empty.brackets", token);

                    expr = new ArrayLookup(info, expr, lbrack, 
                                           new Expression(info));
                    
                    continue;
                }
                
                Expression arrayLookup = parseExpression(token);
                
                token = peek();
                
                if (token.getID() == Token.RBRACK) {
                    read(); // read the right bracket
                    info = info.setEndPosition(token.getSourceInfo());
                }
                else {
                    error("lookup.rbracket.expected", token);
                    info = info.setEndPosition(arrayLookup.getSourceInfo());
                }

                expr = new ArrayLookup(info, expr, lbrack, arrayLookup);
            }
            else {
                break;
            }
        }

        return expr;
    }

    private Expression parseFactor(Token token) throws IOException {
        SourceInfo info = token.getSourceInfo();
        Token value;

        switch (token.getID()) {
        case Token.HASH:
        case Token.DOUBLE_HASH:
            return parseNewArrayExpression(token);
            
        case Token.LPAREN:
            Expression expr;

            token = peek();
            if (token.getID() == Token.RPAREN) {
                expr = null;
            }
            else {
                expr = parseExpression(read());
            }

            token = peek();
            if (token.getID() == Token.RPAREN) {
                read(); // read the right paren
                info = info.setEndPosition(token.getSourceInfo());
            }
            else {
                error("factor.rparen.expected", token);
                info = info.setEndPosition(expr.getSourceInfo());
            }

            if (expr == null) {
                error("factor.empty.parens", info);
                expr = new Expression(info);
            }

            return new ParenExpression(info, expr);

        case Token.NULL:
            return new NullLiteral(info);

        case Token.TRUE:
            return new BooleanLiteral(info, true);

        case Token.FALSE:
            return new BooleanLiteral(info, false);

        case Token.CALL:
            Name target = parseName();
            info.setEndPosition(target.getSourceInfo());

            ExpressionList list = parseList(false);
            info = info.setEndPosition(list.getSourceInfo());

            // Check if a block is being passed in the call.
            Block subParam = null;
            if (peek().getID() == Token.LBRACE) {
                subParam = parseBlock();
                info = info.setEndPosition(subParam.getSourceInfo());
            }

            return new TemplateCallExpression(info, target, list, subParam);

        case Token.NUMBER:
            if (token.getNumericType() == 0) {
                error("factor.number.invalid", token);
            }
            
            switch (token.getNumericType()) {
            case 1:
                return new NumberLiteral(info, token.getIntValue());
            case 2:
                return new NumberLiteral(info, token.getLongValue());
            case 3:
                return new NumberLiteral(info, token.getFloatValue());
            case 4:
            default:
                return new NumberLiteral(info, token.getDoubleValue());
            }

        case Token.STRING:
            return new StringLiteral(info, token.getStringValue());

        case Token.IDENT:
            FunctionCallExpression call = parseFunctionCallExpression(token);
            if (call != null) {
                return call;
            }
            else {
                return new VariableRef(info, token.getStringValue());
            }

        case Token.EOF:
            error("factor.expression.expected", token);
            break;

        case Token.RPAREN:
            error("factor.rparen.unmatched", token);
            break;

        case Token.RBRACE:
            error("factor.rbrace.unmatched", token);
            break;

        case Token.RBRACK:
            error("factor.rbracket.unmatched", token);
            break;

        case Token.ASSIGN:
            error("factor.illegal.assignment", token);
            break;
            
        case Token.DOTDOT:
            error("factor.misuse.dotdot", token);
            break;

        default:
            if (token.isReservedWord()) {
                error("factor.reserved.word", token.getImage(), token);
            }
            else {
                error("factor.unexpected.token", token);
            }
            break;
        }

        return new Expression(token.getSourceInfo());
    }

    private Expression parseNewArrayExpression(Token token) 
        throws IOException {

        boolean associative = (token.getID() == Token.DOUBLE_HASH);

        SourceInfo info = token.getSourceInfo();
        ExpressionList list = parseList(false);
        info = info.setEndPosition(list.getSourceInfo());
        return new NewArrayExpression(info, list, associative);
    }

    // Special parse method in that it may return null if it couldn't parse
    // a FunctionCallExpression. Token passed in must be an identifier.
    private FunctionCallExpression parseFunctionCallExpression(Token token) 
        throws IOException {
        
        SourceInfo info = token.getSourceInfo();

        // Search for pattern <ident> {<dot> <ident>} <lparen>
        Vector lookahead = new Vector();
        StringBuffer name = new StringBuffer(token.getStringValue());
        Name target = null;

        while (true) {
            token = read();
            lookahead.addElement(token);

            if (token.getID() == Token.DOT) {
                name.append('.');
                info = info.setEndPosition(token.getSourceInfo());
            }
            else if (token.getID() == Token.LPAREN) {
                target = new Name(info, name.toString());
                unread(token);
                break;
            }
            else {
                break;
            }

            token = read();
            lookahead.addElement(token);

            if (token.getID() == Token.IDENT) {
                name.append(token.getStringValue());
                info = info.setEndPosition(token.getSourceInfo());
            }
            else {
                break;
            }
        }

        if (target == null) {
            // Pattern not found, unread all lookahead tokens.
            for (int i = lookahead.size() - 1; i >= 0; --i) {
                unread((Token)lookahead.elementAt(i));
            }
            return null;
        }

        ExpressionList list = parseList(false);
        info = info.setEndPosition(list.getSourceInfo());

        // Check if a block is being passed in the call.
        Block subParam = null;
        if (peek().getID() == Token.LBRACE) {
            subParam = parseBlock();
            info = info.setEndPosition(subParam.getSourceInfo());
        }

        return new FunctionCallExpression(info, target, list, subParam);
    }

    /** Test program */
    public static void main(String[] arg) throws Exception {
        Tester.test(arg);
    }

    /**************************************************************************
     * 
     * @author Brian S O'Neill
     * @version
     * <!--$$Revision$--> 36 <!-- $$JustDate:-->  5/31/01 <!-- $-->
     */
    private static class Tester implements ErrorListener {
        String mFilename;

        public static void test(String[] arg) throws Exception {
            new Tester(arg[0]);
        }

        public Tester(String filename) throws Exception {
            mFilename = filename;
            Reader file = new BufferedReader(new FileReader(filename));
            Scanner scanner = new Scanner(new SourceReader(file, "<%", "%>"));
            scanner.addErrorListener(this);
            Parser parser = new Parser(scanner);
            parser.addErrorListener(this);
            Template tree = parser.parse();

            if (tree != null) {
                TreePrinter printer = new TreePrinter(tree);
                printer.writeTo(System.out);
            }
        }

        public void compileError(ErrorEvent e) {
            System.out.println(e.getDetailedErrorMessage());
        }
    }
}
