// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend.ast;

import photlinc.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.js.backend.ast.metadata.HasMetadata;
import org.jetbrains.kotlin.js.common.Symbol;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract base class for named JavaScript objects.
 */
public class JsName extends HasMetadata implements Symbol, DebugTagged {
  private static int ordinalGenerator;
  private final JsScope enclosing;
  private final int ordinal;
  @NotNull
  private final String ident;
  private final boolean temporary;
  private String debugTag;

  /**
   * @param ident the unmangled ident to use for this name
   */
  JsName(JsScope enclosing, @NotNull String ident, boolean temporary) {
    this.enclosing = enclosing;
    this.ident = ident;
    this.temporary = temporary;
    ordinal = temporary ? ordinalGenerator++ : 0;
    debugTag = "n" + PhotlincDebugGlobal.INSTANCE.nextDebugTag();
    PhotlincDebugGlobal.INSTANCE.taggedShitCreated(this);
  }

    public int getOrdinal() {
    return ordinal;
  }

  public JsScope getEnclosing() {
    return enclosing;
  }

  public boolean isTemporary() {
    return temporary;
  }

  @NotNull
  public String getIdent() {
    return ident;
  }

  @NotNull
  public JsNameRef makeRef() {
      JsNameRef jsNameRef = new JsNameRef(this);
      return jsNameRef;
  }
  @NotNull

  public JsNameRef makePHPVarRef() {
      return new JsNameRef(this).setKind(PHPNameRefKind.VAR);
  }

  @Override
  public String toString() {
    return ident;
  }

  @Nullable
  @Override
  public String getDebugTag() {
    return debugTag;
  }
}
