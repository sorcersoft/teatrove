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

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import com.go.tea.parsetree.Variable;
import com.go.tea.parsetree.VariableRef;
import com.go.tea.parsetree.TypeName;

/******************************************************************************
 * A Scope encapsulates a set of declared variables and references to them.
 * Scopes can be nested, and child scopes have access to variables in the 
 * parent scope.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 00/11/16 <!-- $-->
 */
public class Scope {
    // Shared variables, maps Variable objects to Variable objects.
    private Map mVariables;
    private Scope mParent;
    private Collection mChildren;
    
    // Set of private Variables declared in this scope.
    private Set mPrivateVars;

    // Maps String variable names to Variable objects for locally available
    // variables.
    private Map mDeclared = new HashMap(11);

    // Contains a list of all the VariableRefs used in this scope.
    private Collection mVariableRefs = new ArrayList();
    
    public Scope() {
        this(null);
    }
    
    public Scope(Scope parent) {
        if ((mParent = parent) != null) {
            mVariables = parent.mVariables;
            if (parent.mChildren == null) {
                parent.mChildren = new ArrayList(5);
            }
            parent.mChildren.add(this);
        }
        else {
            mVariables = new HashMap(53);
        }
    }

    /**
     * Returns null if this scope has no parent.
     */
    public Scope getParent() {
        return mParent;
    }
    
    /**
     * Returns an empty array if this scope has no children.
     */
    public Scope[] getChildren() {
        if (mChildren == null) {
            return new Scope[0];
        }
        else {
            return (Scope[])mChildren.toArray(new Scope[mChildren.size()]);
        }
    }

    /**
     * Declare a variable for use in this scope. If no variable of this name
     * and type has been defined, it is added to the shared set of pooled
     * variables. Returns the actual Variable object that should be used.
     */
    public Variable declareVariable(Variable var) {
        return declareVariable(var, false);
    }

    /**
     * Declare a variable for use in this scope. If no variable of this name
     * and type has been defined, it is added to the shared set of pooled
     * variables. Returns the actual Variable object that should be used
     * instead.
     *
     * @param isPrivate when true, variable declaration doesn't leave this
     * scope during an intersection or promotion
     */
    public Variable declareVariable(Variable var, boolean isPrivate) {
        if (mVariables.containsKey(var)) {
            var = (Variable)mVariables.get(var);
        }
        else {
            mVariables.put(var, var);
        }

        mDeclared.put(var.getName(), var);

        if (isPrivate) {
            if (mPrivateVars == null) {
                mPrivateVars = new HashSet(7);
            }
            mPrivateVars.add(var);
        }
        else {
            if (mPrivateVars != null) {
                mPrivateVars.remove(var);
            }
        }

        return var;
    }

    /**
     * Declare new variables in this scope. Entries in the array are replaced
     * with actual Variable objects that should be used instead.
     */
    public void declareVariables(Variable[] vars) {
        for (int i=0; i<vars.length; i++) {
            vars[i] = declareVariable(vars[i]);
        }
    }
    
    /**
     * Returns a declared variable by name. Search begins in this scope and
     * moves up into parent scopes. If not found, null is returned. The
     * returned variable may be private or public to a scope.
     *
     * @return Null if no declared variable found with the given name
     */
    public Variable getDeclaredVariable(String name) {
        return getDeclaredVariable(name, false);
    }

    /**
     * Returns a declared variable by name. Search begins in this scope and
     * moves up into parent scopes. If not found, null is returned. A public-
     * only variable can be requested.
     *
     * @return Null if no declared variable found with the given name
     */
    public Variable getDeclaredVariable(String name, boolean publicOnly) {
        //private Set mPrivateVars;

        Variable var = (Variable)mDeclared.get(name);
        if (var != null) {
            // If its okay to be private or its public then...
            if (!publicOnly || mPrivateVars == null ||
                !mPrivateVars.contains(var)) {
                return var;
            }
        }
        
        if (mParent != null) {
            return mParent.getDeclaredVariable(name);
        }
        
        return null;
    }

    /**
     * Returns all the variables declared in this scope.
     *
     * @return non-null array of locally declared variables
     */
    private Variable[] getLocallyDeclaredVariables() {
        Collection vars = mDeclared.values();
        return (Variable[])vars.toArray(new Variable[vars.size()]);
    }
    
    /**
     * Attempt to bind variable reference to a variable in this scope or a
     * parent scope. If the variable to bind to isn't available or doesn't
     * exist, false is returned.
     *
     * @return true if reference has been bound
     */    
    public boolean bindToVariable(VariableRef ref) {
        String name = ref.getName();
        Variable var = getDeclaredVariable(name);
        
        if (var != null) {
            ref.setType(null);
            ref.setVariable(var);
            mVariableRefs.add(ref);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns all the variable references made from this scope and all child
     * scopes.
     *
     * @return non-null array of VariableRefs.
     */
    public VariableRef[] getVariableRefs() {
        Collection allRefs = new ArrayList();
        fillVariableRefs(allRefs, this);
        return (VariableRef[])allRefs.toArray(new VariableRef[allRefs.size()]);
    }

    private static void fillVariableRefs(Collection refs, Scope scope) {
        refs.addAll(scope.mVariableRefs);

        Collection children = scope.mChildren;
        if (children != null) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                fillVariableRefs(refs, (Scope)it.next());
            }
        }
    }

    /**
     * Returns all the references made from this scope to variables declared
     * in this scope or in a parent.
     *
     * @return non-null array of VariableRefs.
     */
    public VariableRef[] getLocalVariableRefs() {
        VariableRef[] refs = new VariableRef[mVariableRefs.size()];
        return (VariableRef[])mVariableRefs.toArray(refs);
    }

    /**
     * Returns all the references made from this scope and all child scopes to
     * variables declared outside of this scope.
     */
    public VariableRef[] getOutOfScopeVariableRefs() {
        Scope parent;
        if ((parent = getParent()) == null) {
            return new VariableRef[0];
        }

        Collection allRefs = new ArrayList();
        fillVariableRefs(allRefs, this);

        Collection refs = new ArrayList(allRefs.size());

        Iterator it = allRefs.iterator();
        while (it.hasNext()) {
            VariableRef ref = (VariableRef)it.next();
            Variable var = ref.getVariable();
            if (var != null &&
                parent.getDeclaredVariable(var.getName()) == var) {
                refs.add(ref);
            }
        }

        VariableRef[] refsArray = new VariableRef[refs.size()];
        return (VariableRef[])refs.toArray(refsArray);
    }

    /**
     * Returns all the references made from this scope to variables declared
     * outside of this scope.
     */
    public VariableRef[] getLocalOutOfScopeVariableRefs() {
        Scope parent;
        if ((parent = getParent()) == null) {
            return new VariableRef[0];
        }

        Collection refs = new ArrayList(mVariableRefs.size());

        Iterator it = mVariableRefs.iterator();
        while (it.hasNext()) {
            VariableRef ref = (VariableRef)it.next();
            Variable var = ref.getVariable();
            if (var != null &&
                parent.getDeclaredVariable(var.getName()) == var) {
                refs.add(ref);
            }
        }
        
        VariableRef[] refsArray = new VariableRef[refs.size()];
        return (VariableRef[])refs.toArray(refsArray);
    }

    /**
     * Returns true if this scope is the same as or a parent of the one given.
     */
    public boolean isEnclosing(Scope scope) {
        for (; scope != null; scope = scope.getParent()) {
            if (this == scope) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the innermost enclosing scope of this and the one given. If no
     * enclosing scope exists, null is returned.
     */
    public Scope getEnclosingScope(Scope scope) {
        for (Scope s = this; s != null; s = s.getParent()) {
            if (s.isEnclosing(scope)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Returns the intersection of this scope against the one given. An
     * intersection operates on each scope's locally declared variables,
     * producing a list of variables that both scopes may inherit.
     * <p>
     * The list may contain undeclared variables, and so all returned variables
     * must be re-declared in a common parent scope. This responsibility is
     * left to the caller, intersect does not alter the internal state of
     * either scope.
     * <p>
     * This method is designed specifically for combining the locally
     * declared variables of the "then" and "else" parts of an if statement.
     *
     * @return variables representing the intersection
     */
    public Variable[] intersect(Scope scope) {
        Collection intersection = new ArrayList();
        
        // A set of variable names that have been moved into the intersection.
        Set matchedNames = new HashSet(7);

        intersectFrom(this, scope, matchedNames, intersection);
        intersectFrom(scope, this, matchedNames, intersection);

        Variable[] vars = new Variable[intersection.size()];
        return (Variable[])intersection.toArray(vars);
    }

    /**
     * Returns variables to promote from this scope to a parent scope.
     * Promote is similar to intersect, except it operates on this scope and
     * its parent (if it has one).
     * <p>
     * The list may contain undeclared variables, and so all returned variables
     * must be re-declared in a common parent scope. This responsibility is
     * left to the caller, promote does not alter the internal state of this
     * scope or its parent.
     * <p>
     * This method is designed specifically for promoting the locally
     * declared variables of a loop statement's body.
     *
     * @return variables to promote
     */
    public Variable[] promote() { 
        Scope parent = getParent();
        if (parent == null) {
            return new Variable[0];
        }

        Collection promotion = new ArrayList();
        
        // A set of variable names that have been moved into the promotion.
        Set matchedNames = new HashSet(7);

        intersectFrom(this, parent, matchedNames, promotion);

        Variable[] vars = new Variable[promotion.size()];
        return (Variable[])promotion.toArray(vars);
    }

    private static void intersectFrom(Scope scope1, Scope scope2,
                                      Set matchedNames,
                                      Collection vars) {
        Set privates1 = scope1.mPrivateVars;

        Variable[] vars1 = scope1.getLocallyDeclaredVariables();
        for (int i=0; i<vars1.length; i++) {
            Variable var1 = vars1[i];
            if (privates1 != null && privates1.contains(var1)) {
                continue;
            }

            String varName = var1.getName();

            if (matchedNames.contains(varName)) {
                // This variable has already been moved into the intersection.
                continue;
            }
            else {
                matchedNames.add(varName);
            }

            Variable var2 = scope2.getDeclaredVariable(varName, true);

            if (var2 == null) {
                // No matching public variable in scope2, so continue.
                continue;
            }

            Type type1 = var1.getType();
            Type type2 = var2.getType();

            // Find common type.
            Type type = type1.getCompatibleType(type2);
            if (type == null) {
                continue;
            }
            Class clazz = type.getNaturalClass();

            // Find a variable to hold common type.
            Variable var;
            if (type.equals(type1)) {
                var = var1;
            }
            else if (type.equals(type2)) {
                var = var2;
            }
            else {
                // Create a new variable with the common type.
                var = new Variable(var1.getSourceInfo(), varName, type);
            }

            vars.add(var);
        }
    }

    /**
     * Delete this scope by detaching it from its parent.
     */
    public void delete() {
        Scope parent = getParent();
        if (parent != null) {
            parent.mChildren.remove(this);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append('\n');
        append(buf, this, "");
        return buf.toString();
    }

    private void append(StringBuffer buf, Scope scope, String indent) {
        buf.append(indent);
        buf.append("{\n");

        String indentMore = indent + "    ";

        Variable[] vars = scope.getLocallyDeclaredVariables();
        for (int i=0; i<vars.length; i++) {
            Variable var = vars[i];

            buf.append(indentMore);
            
            Set privateVars = scope.mPrivateVars;
            if (privateVars != null && privateVars.contains(var)) {
                buf.append("private ");
            }

            Type type = var.getType();
            if (type != null) {
                buf.append(type.getFullName());
            }
            else {
                buf.append("<null>");
            }

            buf.append(' ');
            buf.append(var.getName());

            buf.append(";  // ");
            buf.append(var);
            buf.append('\n');
        }

        VariableRef[] refs = scope.getLocalVariableRefs();
        for (int i=0; i<refs.length; i++) {
            VariableRef ref = refs[i];

            buf.append(indentMore);
            buf.append(ref.getName());

            buf.append(";  // ");
            buf.append(ref);
            buf.append(" to ");
            buf.append(ref.getVariable());
            buf.append('\n');
        }
        
        Scope[] children = scope.getChildren();
        for (int i=0; i<children.length; i++) {
            append(buf, children[i], indentMore);
        }
        
        buf.append(indent);
        buf.append("}\n");
    }
}
