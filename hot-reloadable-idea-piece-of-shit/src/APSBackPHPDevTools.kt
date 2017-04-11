@file:Suppress("Unused")
package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.WindowManager
import com.intellij.project.stateStore
import com.intellij.unscramble.AnnotateStackTraceAction
import com.intellij.util.containers.ConcurrentIntObjectMap
import org.jetbrains.kotlin.utils.stackTraceStr
import java.awt.BorderLayout
import javax.swing.JPanel
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import vgrechka.*
import vgrechka.idea.*
import java.awt.Frame
import java.util.*
import javax.swing.JFrame
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KFunction0

object APSBackPHPDevTools {
    class InterestingFile(val shortNames: List<String>, val fullPath: String, val firstAdditionalFile: String? = null) {
        val shortName get() = shortNames.first()
    }

    val interestingFiles = listOf(
        InterestingFile(listOf("aps-back.php"), "E:/fegh/phizdets/phizdetsc/src/phizdets/php/fuckaroundapsback/aps-back.php", firstAdditionalFile = "E:/fegh/out/phi-tests/aps-back/aps-back.php--1"),
        InterestingFile(listOf("phizdets-stdlib.php"), "E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib.php", firstAdditionalFile = "E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib.php--1"),
        InterestingFile(listOf("phi-engine.php"), "E:/fegh/phizdets/phizdetsc/src/phizdets/php/phi-engine.php")
    )

    fun findInterestingFile(shortName: String): InterestingFile? =
        interestingFiles.find {it.shortNames.contains(shortName)}


    fun link(con: Mumbler, item: FileLine) {
        val path = findInterestingFile(item.file)?.fullPath
            ?: item.file
        con.link(item.file + ":" + item.line, path, item.line)
    }
}

