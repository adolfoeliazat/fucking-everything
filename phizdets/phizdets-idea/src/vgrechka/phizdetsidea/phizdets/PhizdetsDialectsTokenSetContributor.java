/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vgrechka.phizdetsidea.phizdets;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes element types of various kinds specific for a particular Phizdets dialect.
 *
 * @author vlan
 */
public interface PhizdetsDialectsTokenSetContributor {
  ExtensionPointName<PhizdetsDialectsTokenSetContributor> EP_NAME = ExtensionPointName.create("Phizdetsid.dialectsTokenSetContributor");

  /**
   * Returns element types that are subclasses of {@link vgrechka.phizdetsidea.phizdets.psi.PyStatement}.
   */
  @NotNull
  TokenSet getStatementTokens();

  /**
   * Returns element types that are subclasses of {@link vgrechka.phizdetsidea.phizdets.psi.PyExpression}.
   */
  @NotNull
  TokenSet getExpressionTokens();

  /**
   * Returns element types that are language keywords.
   */
  @NotNull
  TokenSet getKeywordTokens();

  /**
   * Returns element types that are subclasses of {@link vgrechka.phizdetsidea.phizdets.psi.PyParameter}.
   */
  @NotNull
  TokenSet getParameterTokens();

  /**
   * Returns element types that are subclasses of {@link vgrechka.phizdetsidea.phizdets.psi.PyFunction}.
   */
  @NotNull
  TokenSet getFunctionDeclarationTokens();

  /**
   * Returns element types that can be used as unbalanced braces recovery tokens in the lexer.
   */
  @NotNull
  TokenSet getUnbalancedBracesRecoveryTokens();

  /**
   * Returns element types that are subclasses of {@link vgrechka.phizdetsidea.phizdets.psi.PyReferenceExpression}.
   */
  @NotNull
  TokenSet getReferenceExpressionTokens();
}
