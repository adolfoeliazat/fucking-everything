/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.psi.impl;

import vgrechka.phizdetsidea.phizdets.psi.PyBoolLiteralExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyNumericLiteralExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyReferenceExpression;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * TODO: Merge PhizdetsDataflowUtil, {@link vgrechka.phizdetsidea.phizdets.psi.impl.PyConstantExpressionEvaluator}  and {@link vgrechka.phizdetsidea.phizdets.psi.impl.PyEvaluator} and all its inheritors and improve Abstract Interpretation
 * @author yole
 */
public class PyConstantExpressionEvaluator {
  private PyConstantExpressionEvaluator() {
  }

  @Nullable
  public static Object evaluate(final PyExpression expr) {
    if (expr instanceof PyNumericLiteralExpression) {
      final PyNumericLiteralExpression numericLiteral = (PyNumericLiteralExpression)expr;
      if (numericLiteral.isIntegerLiteral()) {
        final BigInteger value = numericLiteral.getBigIntegerValue();
        if ((long)value.intValue() == value.longValue()) {
          return value.intValue();
        }
      }
    }
    if (expr instanceof PyBoolLiteralExpression) {
      return ((PyBoolLiteralExpression)expr).getValue();
    }
    if (expr instanceof PyReferenceExpression) {
      final String text = expr.getText();
      if ("true".equals(text) || "True".equals(text)) {
        return true;
      }
      if ("false".equals(text) || "False".equals(text)) {
        return false;
      }
    }
    return null;
  }

  public static boolean evaluateBoolean(final PyExpression expr, boolean defaultValue) {
    Object result = evaluate(expr);
    if (result instanceof Boolean) {
      return (Boolean)result;
    }
    else if (result instanceof Integer) {
      return ((Integer)result) != 0;
    }
    else {
      return defaultValue;
    }
  }

  public static boolean evaluateBoolean(final PyExpression expr) {
    return evaluateBoolean(expr, true);
  }
}