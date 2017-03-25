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

package org.jetbrains.kotlin.js.translate.expression

import photlinc.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.descriptorUtils.isCoroutineLambda
import org.jetbrains.kotlin.js.inline.util.getInnerFunction
import org.jetbrains.kotlin.js.inline.util.rewriters.NameReplacingVisitor
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.context.getNameForCapturedDescriptor
import org.jetbrains.kotlin.js.translate.context.hasCapturedExceptContaining
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getFunctionDescriptor
import org.jetbrains.kotlin.js.translate.utils.FunctionBodyTranslator.setDefaultValueForArguments
import org.jetbrains.kotlin.js.translate.utils.FunctionBodyTranslator.translateFunctionBody
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils.simpleReturnFunction
import org.jetbrains.kotlin.js.translate.utils.fillCoroutineMetadata
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import photlin.*
import vgrechka.*
import java.util.*

class LiteralFunctionTranslator(context: TranslationContext) : AbstractTranslator(context) {
    fun translate(declaration: KtDeclarationWithBody): JsExpression {
        val invokingContext = context()
        val descriptor = getFunctionDescriptor(invokingContext.bindingContext(), declaration)

        val lambda = invokingContext.getFunctionObject(descriptor)

        val aliases = mutableMapOf<DeclarationDescriptor, JsExpression>()
        if (descriptor.isCoroutineLambda) {
            aliases.put(descriptor, JsAstUtils.stateMachineReceiver())
        }

        val functionContext = invokingContext
                .newFunctionBodyWithUsageTracker(lambda, descriptor)
                .innerContextWithDescriptorsAliased(aliases)
                .translateAndAliasParameters(descriptor, lambda.parameters)

        descriptor.valueParameters.forEach {
            if (it is ValueParameterDescriptorImpl.WithDestructuringDeclaration) {
                lambda.body.statements.add(it.translate(functionContext))
            }
        }

        lambda.body.statements += setDefaultValueForArguments(descriptor, functionContext)
        lambda.body.statements += translateFunctionBody(descriptor, declaration, functionContext)
        lambda.functionDescriptor = descriptor

        val tracker = functionContext.usageTracker()!!

        if (tracker.hasCapturedExceptContaining()) {
//            lambda.beingDebugged = true
            val lambdaCreator = simpleReturnFunction(invokingContext.scope(), lambda)
            lambdaCreator.name = invokingContext.getInnerNameForDescriptor(descriptor)
            lambdaCreator.isLocal = true
            if (descriptor in tracker.capturedDescriptors && !descriptor.isCoroutineLambda) {
                lambda.name = tracker.getNameForCapturedDescriptor(descriptor)
            }
            lambdaCreator.name.staticRef = lambdaCreator
            lambdaCreator.fillCoroutineMetadata(invokingContext, descriptor)

            val res = lambdaCreator.withCapturedParameters(
                descriptor, descriptor.wrapContextForCoroutineIfNecessary(functionContext),
                invokingContext)
            lambda.useNames = mutableListOf()

            val lambdaCreatorRef = (res as JsInvocation).qualifier as JsNameRef
            lambdaCreatorRef.kind = PHPNameRefKind.LAMBDA_CREATOR
            val additionalArgDescriptors = lambdaCreatorRef.additionalArgDescriptors!!
            check(additionalArgDescriptors.size == lambdaCreator.parameters.size){"41ea2df7-8f06-48af-9559-428513421bbf"}

            for ((i, argDescriptor) in additionalArgDescriptors.withIndex()) {
                val ref = lambdaCreator.parameters[i].name.makeRef()
                if (argDescriptor is ValueDescriptor) {
                    ref.valueDescriptor = argDescriptor
                }
                lambda.useNames.add(ref)
            }

            return res
        }

        lambda.name = invokingContext.getInnerNameForDescriptor(descriptor)
        if (descriptor in tracker.capturedDescriptors) {
            val capturedName = tracker.getNameForCapturedDescriptor(descriptor)!!
            val globalName = invokingContext.getInnerNameForDescriptor(descriptor)
            val replacingVisitor = NameReplacingVisitor(mapOf(capturedName to JsAstUtils.pureFqn(globalName, null)))
            replacingVisitor.accept(lambda)
        }

        lambda.isLocal = true

        invokingContext.addDeclarationStatement(lambda.makeStmt())
        lambda.fillCoroutineMetadata(invokingContext, descriptor)
        lambda.name.staticRef = lambda
        return getReferenceToLambda(invokingContext, descriptor, lambda.name)
    }

    fun JsFunction.fillCoroutineMetadata(context: TranslationContext, descriptor: FunctionDescriptor) {
        if (!descriptor.isSuspend) return

        fillCoroutineMetadata(context, descriptor, hasController = descriptor.extensionReceiverParameter != null, isLambda = true)
    }

    fun ValueParameterDescriptorImpl.WithDestructuringDeclaration.translate(context: TranslationContext): JsVars {
        val destructuringDeclaration =
                (DescriptorToSourceUtils.descriptorToDeclaration(this) as? KtParameter)?.destructuringDeclaration
                ?: error("Destructuring declaration for descriptor $this not found")

        val jsParameter = JsParameter(context.getNameForDescriptor(this))
        return DestructuringDeclarationTranslator.translate(destructuringDeclaration, jsParameter.name, null, context)
    }
}

private fun CallableMemberDescriptor.wrapContextForCoroutineIfNecessary(context: TranslationContext): TranslationContext {
    return if (isCoroutineLambda) {
        context.innerContextWithDescriptorsAliased(mapOf(this to JsAstUtils.stateMachineReceiver()))
    }
    else {
        context
    }
}

fun JsFunction.withCapturedParameters(
        descriptor: CallableMemberDescriptor,
        context: TranslationContext,
        invokingContext: TranslationContext
): JsExpression {
    context.addDeclarationStatement(makeStmt())
    val ref = getReferenceToLambda(invokingContext, descriptor, name)
    val invocation = JsInvocation(ref).apply { sideEffects = SideEffectKind.PURE }

    val invocationArguments = invocation.arguments
    val functionParameters = this.parameters
    val additionalArgDescriptors = mutableListOf<DeclarationDescriptor>()

    val tracker = context.usageTracker()!!

    for ((capturedDescriptor, name) in tracker.capturedDescriptorToJsName) {
        if (capturedDescriptor == tracker.containingDescriptor && !capturedDescriptor.isCoroutineLambda) continue

//        if (capturedDescriptor !is ValueDescriptor)
//            wtf("199d715c-166f-4b00-a75d-fb8a37e8649c")
        val capturedRef = invokingContext.getArgumentForClosureConstructor(capturedDescriptor)
        var additionalArgs = listOf(capturedRef)
        var additionalParams = listOf(JsParameter(name)-{o->
            if (capturedDescriptor is ValueDescriptor) {
                o.valueDescriptor = capturedDescriptor
            }
        })
        additionalArgDescriptors += capturedDescriptor

        if (capturedDescriptor is TypeParameterDescriptor && capturedDescriptor.isReified) {
            wtf("reified    12cfeb36-e3b7-4c3c-b948-46f4be1d96b3")
            // Preserve the usual order
            additionalArgs = listOf(invokingContext.getNameForDescriptor(capturedDescriptor).makeRef()) + additionalArgs
            additionalParams = listOf(JsParameter(context.getNameForDescriptor(capturedDescriptor))) + additionalParams
        }

        if (capturedDescriptor is CallableDescriptor && isLocalInlineDeclaration(capturedDescriptor)) {
            wtf("capturedDescriptor is CallableDescriptor    7f090ce5-063e-48af-b4f9-81b160e804b0")
            val aliasRef = capturedRef as? JsNameRef
            val localFunAlias = aliasRef?.getStaticRef() as? JsExpression

            if (localFunAlias != null) {
                val (args, params) = moveCapturedLocalInside(this, name, localFunAlias)
                additionalArgs = args
                additionalParams = params
            }
        }

        functionParameters.addAll(additionalParams)
        invocationArguments.addAll(additionalArgs)
    }

    if (ref is JsNameRef) {
        ref.additionalArgDescriptors = additionalArgDescriptors
    }

    return invocation
//    val temp = context.defineTemporary(invocation)
//    return temp
}

private fun getReferenceToLambda(context: TranslationContext, descriptor: CallableMemberDescriptor, name: JsName): JsExpression {
    return if (context.isPublicInlineFunction) {
        val fqn = context.getQualifiedReference(descriptor)
        if (fqn is JsNameRef) {
            fqn.name?.let { it.staticRef = name.staticRef }
        }
        fqn
    }
    else {
//        val argsVarName = context.scope().declareName("args")
//        val lambdaFunctionName = JsNameRef(name)-{o->
//            o.representedAsStringLiteral = true
//        }
//        JsFunction(context.scope(), JsBlock(
//            // XXX func_get_args() result should be stored to temporary variable
//            newPHPVar(argsVarName, JsInvocation(phpNameRef("func_get_args"), listOf())),
//            JsReturn(JsInvocation(phpNameRef("call_user_func_array"), listOf<JsExpression>(lambdaFunctionName, JsNameRef(argsVarName).setPHPVarRef(true))))
//        ), "<fuck>")

        val res = JsAstUtils.pureFqn(name, null).setKind(PHPNameRefKind.LAMBDA)
        res.callableDescriptor = descriptor
        res
    }
}

private data class CapturedArgsParams(val arguments: List<JsExpression> = listOf(), val parameters: List<JsParameter> = listOf())

/**
 * Moves captured local inline function inside capturing function.
 *
 * For example:
 *  var inc = _.foo.inc(closure) // local fun that captures closure
 *  capturingFunction(inc)
 *
 * Is transformed to:
 *  capturingFunction(closure) // var inc = _.foo.inc(closure) is moved inside capturingFunction
 */
private fun moveCapturedLocalInside(capturingFunction: JsFunction, capturedName: JsName, localFunAlias: JsExpression): CapturedArgsParams =
    when (localFunAlias) {
        is JsNameRef -> {
            /** Local inline function does not capture anything, so just move alias inside */
            declareAliasInsideFunction(capturingFunction, capturedName, localFunAlias)
            CapturedArgsParams()
        }
        is JsInvocation ->
            moveCapturedLocalInside(capturingFunction, capturedName, localFunAlias)
        else ->
            throw AssertionError("Local function reference has wrong alias $localFunAlias")
    }

/**
 * Processes case when local inline function with capture
 * is captured by capturingFunction.
 *
 * In this case, capturingFunction should
 * capture arguments captured by localFunAlias,
 * and localFunAlias declaration is moved inside.
 *
 * For example:
 *
 * ```
 *  val x = 0
 *  inline fun id() = x
 *  val lambda = {println(id())}
 * ```
 *
 * `lambda` should capture x in this case
 */
private fun moveCapturedLocalInside(capturingFunction: JsFunction, capturedName: JsName, localFunAlias: JsInvocation): CapturedArgsParams {
    val capturedArgs = localFunAlias.arguments

    val scope = capturingFunction.getInnerFunction()?.scope!!
    val freshNames = getTemporaryNamesInScope(scope, capturedArgs)

    val aliasCallArguments = freshNames.map { it.makeRef() }
    val alias = JsInvocation(localFunAlias.qualifier, aliasCallArguments)
    declareAliasInsideFunction(capturingFunction, capturedName, alias)

    val capturedParameters = freshNames.map(::JsParameter)
    return CapturedArgsParams(capturedArgs, capturedParameters)
}

private fun declareAliasInsideFunction(function: JsFunction, name: JsName, alias: JsExpression) {
    name.staticRef = alias
    function.getInnerFunction()?.addDeclaration(name, alias)
}

private fun getTemporaryNamesInScope(scope: JsScope, suggested: List<JsExpression>): List<JsName> {
    val freshNames = arrayListOf<JsName>()

    for (suggestion in suggested) {
        if (suggestion !is JsNameRef) {
            throw AssertionError("Expected suggestion to be JsNameRef")
        }

        val ident = suggestion.ident
        val name = scope.declareTemporaryName(ident)
        freshNames.add(name)
    }

    return freshNames
}

private fun JsFunction.addDeclaration(name: JsName, value: JsExpression?) {
    val declaration = JsAstUtils.newVar(name, value)
    this.body.statements.add(0, declaration)
}

private fun HasName.getStaticRef(): JsNode? {
    return this.name?.staticRef
}

private fun isLocalInlineDeclaration(descriptor: CallableDescriptor): Boolean {
    return descriptor is FunctionDescriptor
           && descriptor.getVisibility() == Visibilities.LOCAL
           && InlineUtil.isInline(descriptor)
}
