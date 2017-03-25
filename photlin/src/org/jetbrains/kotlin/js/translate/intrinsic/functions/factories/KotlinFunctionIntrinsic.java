/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.intrinsic.functions.factories;

import photlinc.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.js.backend.ast.JsExpression;
import org.jetbrains.kotlin.js.backend.ast.JsInvocation;
import org.jetbrains.kotlin.js.backend.ast.JsNameRef;
import org.jetbrains.kotlin.js.translate.context.Namer;
import org.jetbrains.kotlin.js.translate.context.TranslationContext;
import org.jetbrains.kotlin.js.translate.intrinsic.functions.basic.FunctionIntrinsicWithReceiverComputed;
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils;
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils;

import java.util.ArrayList;
import java.util.List;

public class KotlinFunctionIntrinsic extends FunctionIntrinsicWithReceiverComputed {
    @NotNull
    private final String functionName;
    private final JsExpression[] additionalArguments;
    public boolean beingDebugged2;

    public KotlinFunctionIntrinsic(@NotNull String functionName, JsExpression... additionalArguments) {
        this.functionName = functionName;
        this.additionalArguments = additionalArguments;
    }

    @NotNull
    @Override
    public JsExpression apply(
            @Nullable JsExpression receiver,
            @NotNull List<? extends JsExpression> arguments,
            @NotNull TranslationContext context
    ) {
        // @debug
//        if (beingDebugged2) {
//            "break on me".toString();
//        }

        JsExpression function = JsAstUtils.pureFqn(functionName, Namer.kotlinObject());
        if (additionalArguments.length > 0) {
            List<JsExpression> newArguments = new ArrayList<JsExpression>(arguments);
            for (JsExpression e : additionalArguments) {
                newArguments.add(e.deepCopy());
            }
            arguments = newArguments;
        }

        JsExpression res = null;
        if (function instanceof JsNameRef) {
            JsNameRef jsNameRef = (JsNameRef) function;
            JsExpression qualifier = jsNameRef.getQualifier();
            if (qualifier instanceof JsNameRef) {
                JsNameRef qualifierNameRef = (JsNameRef) qualifier;
                if ("Kotlin".equals(qualifierNameRef.getIdent())) {
                    String ident = jsNameRef.getIdent();
                    if ("toString".equals(ident)) {
                        res = new PHPStaticMethodCall("Kotlin", "toString", receiver == null ? arguments : TranslationUtils.generateInvocationArguments(receiver, arguments));
                        ShebangKt.setForcedKotlinTypeName(res, "kotlin.String");
                    }
                    else if ("equals".equals(ident)) {
                        res = new PHPStaticMethodCall("Kotlin", "equals", receiver == null ? arguments : TranslationUtils.generateInvocationArguments(receiver, arguments));
                    }
                    else if ("hashCode".equals(ident)) {
                        res = new PHPStaticMethodCall("Kotlin", "hashCode", receiver == null ? arguments : TranslationUtils.generateInvocationArguments(receiver, arguments));
                    }
                }
            }
        }
        if (res == null) {
            throw new IllegalStateException("Implement c7364168-86c4-4f3e-bfba-0e69029207a7");
        }

        return res;
//        return new JsInvocation(function, receiver == null ? arguments : TranslationUtils.generateInvocationArguments(receiver, arguments));
    }
}
