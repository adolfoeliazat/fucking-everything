package photlinc

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.js.facade.K2JSTranslator
import org.jetbrains.kotlin.js.translate.context.StaticContext
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.util.AstUtil
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.types.KotlinType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import photlin.*
import vgrechka.*

val KotlinType.typeName: String? get() {
    // TODO:vgrechka More than two levels of containment
    val classifierDescriptor = this.constructor.declarationDescriptor!!
    val name = classifierDescriptor.name
    if (name.isSpecial) return null
    val fuck = name.identifier
    val containingDeclaration = classifierDescriptor.containingDeclaration
    val isRoot = (containingDeclaration as? PackageFragmentDescriptorImpl)?.fqName?.isRoot ?: false
    return when {
        isRoot -> fuck
        else -> containingDeclaration.name.identifier + "." + fuck
    }
}

val JsNode.kotlinTypeName: String? get() {
    this.forcedKotlinTypeName?.let {return it}
    this.kotlinType?.let {return it.typeName}

    val valueDescriptor = this.valueDescriptor
    if (valueDescriptor != null) {
        return valueDescriptor.type.typeName
    }

    val expr = this.ktExpression
    if (expr != null) {
        val exprTypeInfo = StaticContext.current.bindingContext.get(BindingContext.EXPRESSION_TYPE_INFO, expr)
        if (exprTypeInfo != null) {
            val type = exprTypeInfo.type
            if (type != null) {
                return type.typeName
            }
        }
    }

    return null
}


object PhotlincDebugGlobal {
    private var puid = 1L
    val breakOnDebugTag: String = "234"
//    val breakOnDebugTag: String = "6607"
    val breakOnPizdavalue: String = "58248"

    fun nextDebugTag(): String {
        return (puid++).toString()
    }

    fun taggedShitCreated(shit: DebugTagged) {
        // @debug
        if (shit.debugTag == breakOnDebugTag) {
            "break on me"
        }
    }
}

// @extension-properties
var Any.beingDebugged by AttachedShit<Boolean?>()
var ClassDescriptor.phpClass by AttachedShit<PHPClass>()
var ClassDescriptor.phpSuperPrototypeNameRef by AttachedShit<JsNameRef?>()
var PHPClass.classDescriptor by AttachedShit<ClassDescriptor>()
var JsNode.ktExpression by AttachedShit<KtExpression?>()
var JsNode.kotlinType by AttachedShit<KotlinType?>()
var JsNode.forcedKotlinTypeName by AttachedShit<String?>()
var JsNode.declarationDescriptor by AttachedShit<DeclarationDescriptor?>()
var JsNode.callableDescriptor by AttachedShit<CallableDescriptor?>()
var JsNode.valueDescriptor by AttachedShit<ValueDescriptor?>()
var JsNode.translationContext by AttachedShit<TranslationContext?>()
var JsVars.JsVar.propertyDescriptor by AttachedShit<PropertyDescriptor?>()
var JsNameRef.additionalArgDescriptors  by AttachedShit<List<DeclarationDescriptor>?>()
var JsNode.valueParameterDescriptor by AttachedShit<ValueParameterDescriptor?>()


class AttachedShit<T> : ReadWriteProperty<Any, T> {
    data class Key(val obj: Any, val prop: String)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return K2JSTranslator.current.shitToShit[Key(thisRef, property.name)] as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        K2JSTranslator.current.shitToShit[Key(thisRef, property.name)] = value
    }

}


interface DebugTagged {
    val debugTag: String?
}


fun addFieldToPHPClass(context: TranslationContext, classDescriptor: ClassDescriptor, name: String, initExpression: JsExpression?) {
    val scope = context.getScopeForDescriptor(classDescriptor)
    val phpClass = classDescriptor.phpClass
    phpClass.statements += JsVars(JsVars.JsVar(scope.declareName(name), initExpression)-{o->
        o.visibility = "private"
    })
}


