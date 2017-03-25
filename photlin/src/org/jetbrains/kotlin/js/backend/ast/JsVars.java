// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend.ast;

import org.jetbrains.kotlin.js.common.Symbol;
import org.jetbrains.kotlin.js.util.AstUtil;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A JavaScript <code>var</code> statement.
 */
public class JsVars extends SourceInfoAwareJsNode implements JsStatement, Iterable<JsVars.JsVar> {
    private final List<JsVar> vars;

    private final boolean multiline;

    public JsVars() {
        this(new SmartList<JsVar>(), false);
    }

    public JsVars(boolean multiline) {
        this(new SmartList<JsVar>(), multiline);
    }

    public JsVars(List<JsVar> vars, boolean multiline) {
        this.vars = vars;
        this.multiline = multiline;
        initDebugTag();
    }

    public JsVars(JsVar var) {
        this(new SmartList<JsVar>(var), false);
    }

    public JsVars(JsVar... vars) {
        this(new SmartList<JsVar>(vars), false);
    }

    public boolean isMultiline() {
        return multiline;
    }


    /**
     * A var declared using the JavaScript <code>var</code> statement.
     */
    public static class JsVar extends SourceInfoAwareJsNode implements HasName {
        private JsName name;
        private JsExpression initExpression;
        public @Nullable  String visibility;

        public JsVar(JsName name) {
            this.name = name;
            constructed();
        }

        public JsVar(JsName name, @Nullable JsExpression initExpression) {
            this.name = name;
            this.initExpression = initExpression;
            constructed();
        }

        private void constructed() {
            initDebugTag();
        }

        public JsExpression getInitExpression() {
            return initExpression;
        }

        @Override
        public JsName getName() {
            return name;
        }

        @Override
        public void setName(JsName name) {
            this.name = name;
        }

        @Override
        public Symbol getSymbol() {
            return name;
        }

        public void setInitExpression(JsExpression initExpression) {
            this.initExpression = initExpression;
        }

        @Override
        public void accept(JsVisitor v) {
            v.visit(this);
        }

        @Override
        public void acceptChildren(JsVisitor visitor) {
            if (initExpression != null) {
                visitor.accept(initExpression);
            }
        }

        @Override
        public void traverse(JsVisitorWithContext v, JsContext ctx) {
            if (v.visit(this, ctx)) {
                if (initExpression != null) {
                    initExpression = v.accept(initExpression);
                }
            }
            v.endVisit(this, ctx);
        }

        @NotNull
        @Override
        public JsVar deepCopy() {
            if (initExpression == null) return new JsVar(name);

            JsVar copy = new JsVar(name, initExpression.deepCopy()).withMetadataFrom(this);
            copy.visibility = visibility;
            return copy;
        }

    }

    public void add(JsVar var) {
        vars.add(var);
    }

    public void addAll(Collection<? extends JsVars.JsVar> vars) {
        this.vars.addAll(vars);
    }

    public void addAll(JsVars otherVars) {
        this.vars.addAll(otherVars.vars);
    }

    public void addIfHasInitializer(JsVar var) {
        if (var.getInitExpression() != null) {
            add(var);
        }
    }

    public boolean isEmpty() {
        return vars.isEmpty();
    }

    @Override
    public Iterator<JsVar> iterator() {
        return vars.iterator();
    }

    public List<JsVar> getVars() {
        return vars;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitVars(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.acceptWithInsertRemove(vars);
    }

    @Override
    public void traverse(JsVisitorWithContext v, JsContext ctx) {
        if (v.visit(this, ctx)) {
            v.acceptList(vars);
        }
        v.endVisit(this, ctx);
    }

    @NotNull
    @Override
    public JsVars deepCopy() {
        JsVars copy = new JsVars(AstUtil.deepCopy(vars), multiline).withMetadataFrom(this);
        return copy;
    }
}