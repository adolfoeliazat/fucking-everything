// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend.ast;

import photlinc.*;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.ValueDescriptor;
import org.jetbrains.kotlin.js.common.Symbol;
import org.jetbrains.kotlin.js.util.AstUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a JavaScript expression that references a name.
 */
public final class JsNameRef extends JsExpression implements HasName {
    private String ident;
    private JsName name;
    private JsExpression qualifier;
//    public boolean representedAsStringLiteral;

    private PHPNameRefKind kind = PHPNameRefKind.DUNNO;

    public JsNameRef(@NotNull JsName name) {
        this.name = name;
        constructed();
    }

    public JsNameRef(@NotNull String ident) {
        this.ident = ident;
        constructed();
    }

    public JsNameRef(@NotNull String ident, JsExpression qualifier) {
        this.ident = ident;
        this.qualifier = qualifier;
        constructed();
    }

    public JsNameRef(@NotNull String ident, @NotNull String qualifier) {
        this(ident, new JsNameRef(qualifier));
    }

    public JsNameRef(@NotNull JsName name, JsExpression qualifier) {
        this.name = name;
        this.qualifier = qualifier;
        constructed();
    }

    private void constructed() {
        initDebugTag();
    }

    public JsNameRef setKind(PHPNameRefKind kind) {
//        if (getDebugTag().equals(DebugGlobal.INSTANCE.getBreakOnDebugTag())) {
//            "break on me".toString();
//        }
        this.kind = kind;
        return this;
    }

    public PHPNameRefKind getKind() {
        return kind;
    }

    @NotNull
    public String getIdent() {
        return (name == null) ? ident : name.getIdent();
    }

    @Nullable
    @Override
    public JsName getName() {
        return name;
    }

    @Override
    public void setName(JsName name) {
        this.name = name;
    }

    @Nullable
    @Override
    public Symbol getSymbol() {
        return name;
    }

    @Nullable
    public JsExpression getQualifier() {
        return qualifier;
    }

    @Override
    public boolean isLeaf() {
        return qualifier == null;
    }

    public void resolve(JsName name) {
        this.name = name;
        ident = null;
    }

    public void setQualifier(JsExpression qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitNameRef(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (qualifier != null) {
           visitor.accept(qualifier);
        }
    }

    @Override
    public void traverse(JsVisitorWithContext v, JsContext ctx) {
        if (v.visit(this, ctx)) {
            if (qualifier != null) {
                qualifier = v.accept(qualifier);
            }
        }
        v.endVisit(this, ctx);
    }

    @NotNull
    @Override
    public JsNameRef deepCopy() {
        JsExpression qualifierCopy = AstUtil.deepCopy(qualifier);

        JsNameRef copy;
        if (name != null) {
            copy = new JsNameRef(name, qualifierCopy).withMetadataFrom(this);
        } else {
            copy = new JsNameRef(ident, qualifierCopy).withMetadataFrom(this);
        }

//        copy.representedAsStringLiteral = representedAsStringLiteral;
        copy.kind = kind;
        return copy;
    }

}


