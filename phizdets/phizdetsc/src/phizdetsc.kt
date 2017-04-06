package phizdets.compiler

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.kotlin.cli.js.K2PhizdetsCompiler
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.facade.K2JSTranslator
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.array
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.index
import photlin.devtools.*
import vgrechka.*
import java.io.File
import java.io.StringWriter
import java.util.*
import javax.xml.bind.JAXB
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.*
import kotlin.properties.Delegates.notNull
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.system.exitProcess
import jdk.nashorn.internal.ir.*
import jdk.nashorn.internal.ir.visitor.NodeVisitor
import jdk.nashorn.internal.parser.Parser
import jdk.nashorn.internal.parser.TokenType
import jdk.nashorn.internal.runtime.Context
import jdk.nashorn.internal.runtime.ErrorManager
import jdk.nashorn.internal.runtime.Source
import jdk.nashorn.internal.runtime.options.Options
import org.jetbrains.kotlin.js.sourceMap.PhizdetsSourceGenerationVisitor
import org.jetbrains.kotlin.js.util.TextOutputImpl
import phizdets.Boobs
import phizdets.TestParams

val shitToShit = mutableMapOf<Any, Any?>()
var JsNode.declarationDescriptor by AttachedShit<DeclarationDescriptor?>(shitToShit)

object PhizdetscGlobal {
    var debugTagPrefix = ""
    var reportTranslationStage: (num: Int, program: JsProgram) -> Unit = {_,_->}
}


class AttachedShit<T>(val shitToShit: MutableMap<Any, Any?>) : ReadWriteProperty<Any, T> {
    data class Key(val obj: Any, val prop: String)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return shitToShit[Key(thisRef, property.name)] as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        shitToShit[Key(thisRef, property.name)] = value
    }

}


























