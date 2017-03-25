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

package org.jetbrains.kotlin.js.translate.context

import photlinc.*
import org.jetbrains.kotlin.js.backend.ast.metadata.staticRef
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.isInlineOnlyOrReifiable
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils.isLibraryObject
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils.isNativeObject
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.assignment
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils

internal class DeclarationExporter(val context: StaticContext) {
    private val objectLikeKinds = setOf(ClassKind.OBJECT, ClassKind.ENUM_ENTRY)
    private val exportedDeclarations = mutableSetOf<MemberDescriptor>()
    private val localPackageNames = mutableMapOf<FqName, JsName>()
    val statements = mutableListOf<JsStatement>()

    fun export(descriptor: MemberDescriptor, force: Boolean) {
        if (exportedDeclarations.contains(descriptor)) return
        if (descriptor is ConstructorDescriptor && descriptor.isPrimary) return
        if (isNativeObject(descriptor) || isLibraryObject(descriptor)) return
        if (descriptor.isInlineOnlyOrReifiable()) return

        val suggestedName = context.nameSuggestion.suggest(descriptor) ?: return

        val container = suggestedName.scope
        if (!descriptor.shouldBeExported(force)) return
        exportedDeclarations.add(descriptor)

        val qualifier = when {
            container is PackageFragmentDescriptor -> {
                getLocalPackageReference(container.fqName)
            }
            DescriptorUtils.isObject(container) -> {
                JsAstUtils.prototypeOf(context.getInnerNameForDescriptor(container).makeRef())
            }
            else -> {
                context.getInnerNameForDescriptor(container).makeRef()
            }
        }

        when {
            descriptor is ClassDescriptor && descriptor.kind in objectLikeKinds -> {
                exportObject(descriptor, qualifier)
            }
            descriptor is PropertyDescriptor && container is PackageFragmentDescriptor -> {
                exportProperty(descriptor, qualifier)
            }
            else -> {
                // @possibly-needed
                // assign(descriptor, qualifier, context.getInnerNameForDescriptor(descriptor).makeRef().setKind(PHPNameRefKind.STRING_LITERAL))

//                assign(descriptor, qualifier, context.getInnerNameForDescriptor(descriptor).makeRef())
            }
        }
    }

    private fun assign(descriptor: DeclarationDescriptor, qualifier: JsExpression, expression: JsExpression) {
        val propertyName = context.getNameForDescriptor(descriptor)
        if (propertyName.staticRef == null) {
            if (expression !is JsNameRef || expression.name !== propertyName) {
                propertyName.staticRef = expression
            }
        }
        statements += assignment(JsNameRef(propertyName, qualifier), expression).makeStmt()
    }

    // @fucking Export object
    private fun exportObject(declaration: ClassDescriptor, qualifier: JsExpression) {
        val name = context.getNameForDescriptor(declaration)

        if (qualifier is JsNameRef && qualifier.qualifier == null) {
            qualifier.kind = PHPNameRefKind.VAR
        }

        val statement = JsInvocation(
            JsNameRef("__photlin_defineProperty", qualifier),
            JsStringLiteral(name.ident),
            JsObjectLiteral(listOf(JsPropertyInitializer(JsNameRef("get"), JsFunction(context.rootScope, JsBlock(
                JsReturn(JsInvocation(context.getNameForObjectInstance(declaration).makeRef()))
            ), "description?"))))

        ).makeStmt()
        if (qualifier is JsNameRef && qualifier.ident == "prototype") {
            (statement as AbstractNode).commentedOut = true
        }
        statements += statement

//        statements += JsAstUtils.defineGetter(context.program, qualifier, name.ident,
//                                              context.getNameForObjectInstance(declaration).makeRef())
    }

    private fun exportProperty(declaration: PropertyDescriptor, qualifier: JsExpression) {
        val propertyLiteral = JsObjectLiteral(true)

        val name = context.getNameForDescriptor(declaration).ident
        val simpleProperty = JsDescriptorUtils.isSimpleFinalProperty(declaration) &&
                             !TranslationUtils.shouldAccessViaFunctions(declaration)

        val getterBody: JsExpression = if (simpleProperty) {
            val accessToField = JsReturn(context.getInnerNameForDescriptor(declaration).makePHPVarRef())
            JsFunction(context.rootFunction.scope, JsBlock(accessToField), "$declaration getter")
        }
        else {
            JsFunction(context.rootFunction.scope, JsBlock(
                JsReturn(
                    JsInvocation(context.getInnerNameForDescriptor(declaration.getter!!).makeRef()))
            ), "pizda")
        }
        propertyLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef("get"), getterBody)

        if (declaration.isVar) {
            val setterBody: JsExpression = if (simpleProperty) {
                val statements = mutableListOf<JsStatement>()
                val function = JsFunction(context.rootFunction.scope, JsBlock(statements), "$declaration setter")
                val valueName = function.scope.declareTemporaryName("value")
                function.parameters += JsParameter(valueName)
                statements += assignment(context.getInnerNameForDescriptor(declaration).makePHPVarRef(), valueName.makeRef()).makeStmt()
                function
            }
            else {
                val block = JsBlock()
                val function = JsFunction(context.rootFunction.scope, block, "pizda")
                val pizdaParameterName = function.scope.declareFreshName("pizda")
                block.statements += JsReturn(JsInvocation(
                    context.getInnerNameForDescriptor(declaration.setter!!).makeRef(),
                    pizdaParameterName.makePHPVarRef()))
                function.parameters.add(JsParameter(pizdaParameterName))
                function
            }
            propertyLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef("set"), setterBody)
        }


        statements += JsInvocation(
            JsNameRef("__photlin_defineProperty", qualifier),
            JsStringLiteral(name),
            propertyLiteral)
            .makeStmt()


//        statements += JsAstUtils.defineProperty(qualifier, name, propertyLiteral, context.program).makeStmt()
    }

    private fun getLocalPackageReference(packageName: FqName): JsExpression {
        val res = run {
            if (packageName.isRoot) {
                return@run context.rootFunction.scope.declareName(Namer.getRootPackageName()).makePHPVarRef()
            }
            var name = localPackageNames[packageName]
            if (name == null) {
                name = context.rootFunction.scope.declareTemporaryName("package$" + packageName.shortName().asString())
                localPackageNames.put(packageName, name)

                val parentRef = getLocalPackageReference(packageName.parent())
                val selfRef = JsNameRef(packageName.shortName().asString(), parentRef)
                selfRef.suppressWarnings = true
                val rhs = JsAstUtils.elvis(selfRef, assignment(selfRef.deepCopy(), JsObjectLiteral(false)))

                // @fucking packages
                statements.add(JsAstUtils.newVar(name, rhs))
            }
            return@run name.makePHPVarRef()
        }
        return res
    }
}

private fun MemberDescriptor.shouldBeExported(force: Boolean) =
        force || effectiveVisibility(checkPublishedApi = true).publicApi || AnnotationsUtils.getJsNameAnnotation(this) != null
