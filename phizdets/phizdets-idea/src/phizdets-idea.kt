package vgrechka.phizdetsidea

import com.intellij.ide.actions.ShowStructureSettingsAction
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.options.newEditor.SettingsDialog
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.util.lang.UrlClassLoader
import vgrechka.*
import vgrechka.idea.*
import java.io.File
import kotlin.concurrent.thread
import vgrechka.*
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class PhizdetsIDEAPlugin : ApplicationComponent {
    override fun getComponentName(): String {
        return this::class.qualifiedName!!
    }

    override fun disposeComponent() {
    }

    override fun initComponent() {
        val pm = ProjectManager.getInstance()
        pm.addProjectManagerListener(object : ProjectManagerListener {
            override fun projectOpened(project: Project) {
                clog("Opened project", project.name)
            }
        })

        val am = ActionManager.getInstance()
        val group = am.getAction("ToolsMenu") as DefaultActionGroup
        group.addSeparator()

        run {
            val action = object : AnAction("Phizdets: _Mess Around") {
                override fun actionPerformed(event: AnActionEvent) {
                    thread {
                        val robot = Robot()
                        val origLocation = MouseInfo.getPointerInfo().location

                        fun key(code: Int) {
                            robot.keyPress(code)
                            robot.keyRelease(code)
                        }

                        fun click() {
                            Thread.sleep(500)
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                            Thread.sleep(500)
                        }

                        robot.keyPress(KeyEvent.VK_CONTROL)
                        robot.keyPress(KeyEvent.VK_ALT)
                        robot.keyPress(KeyEvent.VK_SHIFT)
                        key('S'.toInt())
                        robot.keyRelease(KeyEvent.VK_SHIFT)
                        robot.keyRelease(KeyEvent.VK_ALT)
                        robot.keyRelease(KeyEvent.VK_CONTROL)

                        robot.mouseMove(334, 314)
                        click()

                        robot.mouseMove(480, 90)
                        click()

                        key(KeyEvent.VK_DOWN)
                        Thread.sleep(250)
                        key(KeyEvent.VK_ENTER)
                        Thread.sleep(250)
                        key(KeyEvent.VK_ENTER)
                    }

//                    object : SingleConfigurableEditor(event.project, ProjectStructureConfigurable.getInstance(event.project), SettingsDialog.DIMENSION_KEY) {
//                        override fun getStyle(): DialogWrapper.DialogStyle {
//                            return DialogWrapper.DialogStyle.COMPACT
//                        }
//                    }.show()


//                    Messages.showInfoMessage("Fuck you", "aaaaaa")
                }
            }
            group.add(action)
        }
    }
}







