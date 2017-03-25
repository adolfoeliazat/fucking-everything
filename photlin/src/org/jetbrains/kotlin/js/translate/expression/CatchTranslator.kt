/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.expression

import photlinc.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation.patternTranslator
import org.jetbrains.kotlin.js.translate.general.Translation.translateAsStatementAndMergeInBlockIfNeeded
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.convertToBlock
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils.getNotNull
import org.jetbrains.kotlin.types.isDynamic
import photlin.*
import vgrechka.*

class CatchTranslator(
        val catches: List<KtCatchClause>,
        context: TranslationContext
) : AbstractTranslator(context) {

    /**
     * In JavaScript there is no multiple catches, so we translate
     * multiple catch to single catch with instanceof checks for
     * every catch clause.
     *
     * For example this code:
     *  try {
     *      ...
     *  } catch(e: NullPointerException) {
     *      ...
     *  } catch(e: RuntimeException) {
     *      ...
     *  }
     *
     *  is translated to the following JsCode
     *
     *  try {
     *      ...
     *  } catch(e) {
     *      if (e instanceof NullPointerException) {
     *          ...
     *      } else {
     *          if (e instanceof RuntimeException) {
     *              ...
     *          } else throw e;
     *      }
     *  }
     */
    fun translate(): List<JsCatch> {
        val res = mutableListOf<JsCatch>()
        if (catches.isEmpty()) return listOf()

        val firstCatch = catches.first()
        val catchParameter = firstCatch.catchParameter
        val parameterDescriptor = BindingUtils.getDescriptorForElement(bindingContext(), catchParameter!!)
        val parameterName = context().getNameForDescriptor(parameterDescriptor).ident

        val jsCatch = JsCatch(context().scope(), parameterName)
        val catchParamName = jsCatch.parameter.name
        val parameterRef = catchParamName.makeRef()
        val catchContext = context().innerContextWithAliased(parameterDescriptor, parameterRef)

        val (body, typeExpression) = translateCatches(catchContext, parameterRef, catches.iterator())
        jsCatch.body = JsBlock(body)
        jsCatch.typeExpression = typeExpression
        res += jsCatch
        return res
    }

    private fun translateCatches(
            context: TranslationContext,
            initialCatchParameterRef: JsNameRef,
            catches: Iterator<KtCatchClause>
    ): Pair<JsStatement, JsExpression> {
//        if (!catches.hasNext()) return JsThrow(initialCatchParameterRef)

        var nextContext = context

        val catch = catches.next()
        if (catches.hasNext()) imf("Multiple catch clauses    6f5f11a7-e9df-453a-923f-2f53992e90c6")

        val param = catch.catchParameter!!
        val parameterDescriptor = BindingUtils.getDescriptorForElement(bindingContext(), catch.catchParameter!!)
        val parameterName = context().getNameForDescriptor(parameterDescriptor)
        val paramType = param.typeReference!!

        val additionalStatements = mutableListOf<JsStatement>()
        val parameterRef = if (parameterName.ident != initialCatchParameterRef.ident) {
            val parameterAlias = context.scope().declareTemporaryName(parameterName.ident)
            additionalStatements += JsAstUtils.newVar(parameterAlias, initialCatchParameterRef)
            val ref = JsAstUtils.pureFqn(parameterAlias, null)
            ref
        }
        else {
            initialCatchParameterRef
        }
        nextContext = nextContext.innerContextWithAliased(parameterDescriptor, parameterRef)
        val thenBlock = translateCatchBody(nextContext, catch)
        thenBlock.statements.addAll(0, additionalStatements)

//        if (paramType.isDynamic) return thenBlock

        // translateIsCheck won't ever return `null` if its second argument is `null`
        val typeExpression = with (patternTranslator(nextContext)) {
            translateFuckCheck(initialCatchParameterRef, paramType)
        }!!

        return Pair(thenBlock, typeExpression)

//        val elseBlock = translateCatches(context, initialCatchParameterRef, catches)
//        return JsIf(typeCheck, thenBlock, elseBlock)
    }

    private fun translateCatchBody(context: TranslationContext, catchClause: KtCatchClause): JsBlock {
        val catchBody = catchClause.catchBody
        val jsCatchBody = if (catchBody != null) {
            translateAsStatementAndMergeInBlockIfNeeded(catchBody, context)
        }
        else {
            JsAstUtils.asSyntheticStatement(JsLiteral.NULL)
        }

        return convertToBlock(jsCatchBody)
    }

    private val KtTypeReference.isDynamic: Boolean
        get() = getNotNull(bindingContext(), BindingContext.TYPE, this).isDynamic()
}
