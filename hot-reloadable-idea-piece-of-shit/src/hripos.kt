package vgrechka.idea.hripos

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.psi.*
import vgrechka.*
import vgrechka.idea.*
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.concurrent.thread

@Suppress("Unused")
class HotReloadableIdeaPieceOfShit {
    fun serve(req: HttpServletRequest, res: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        req.queryString
        val rawRequest = req.reader.readText()
        clog("Got request:", rawRequest)
        val requestClass = Class.forName(this::class.java.`package`.name + ".Command_" + req.getParameter("proc"))
        val servant = relaxedObjectMapper.readValue(rawRequest, requestClass) as Servant

        var response by notNullOnce<Any>()
        ApplicationManager.getApplication().invokeAndWait {
            response = try {
                servant.serve()
            } catch (e: Throwable) {
                ErrorResponse(e.message ?: "Obscure error")
            }
        }

        val rawResponse = relaxedObjectMapper.writeValueAsString(response)
        res.contentType = "application/json; charset=utf-8"
        res.writer.println(rawResponse)
        res.status = HttpServletResponse.SC_OK
    }
}

internal interface Servant {
    fun serve(): Any
}

class SimpleResponse(val status: String)

class ErrorResponse(val error: String)

internal fun withProjectNamed(projectName: String, block: (Project) -> Any): Any {
    val project = ProjectManager.getInstance().openProjects.find {it.name == projectName}
        ?: bitch("No fucking project `$projectName`")
    return block(project)
}

private fun coolResponse() = SimpleResponse("Cool")

@Ser @Suppress("Unused")
class Command_MessAround(val projectName: String) : Servant {
    override fun serve() = withProjectNamed(projectName) {project->
        val title = this::class.simpleName!!.substring("Command_".length)
//        val bs = Bullshitter(project, title = title)
//        bs.mumble("Just messing around... (${Date()})")

        IDEAPile.showingErrorOnException {
            serve1(project, title)
//            serve2(project)
        }
    }

    private fun serve2(project: Project) {
        thread {
            IDEAPile.showingErrorOnException {
                run { // Generate shit
                    val name = "vgrechka.spew.SpewSomeShit2"
                    IDEAPile.runConfiguration(project, name, debug = false)
                    IDEAPile.waitForConfigurationToRunAndThenTerminate(project, name, debug = false, runTimeout = 5000, terminationTimeout = 10000)
                }
                IDEAPile.runConfiguration(project, "BotinokTest1.test1", debug = true)
            }
        }
    }

    private fun serve1(project: Project, title: String): Any {
        val p = HriposDebugOutput(project)
        p.println("Fuck")
        p.println("Shit")
        p.println("Bitch")
        p.println("Like that...")
        p.showDialog(title = title)
        return object {
            val output = p.output
        }
    }
}

class HriposDebugOutput(val project: Project) {
    val sw = StringWriter()
    val p = PrintWriter(sw)

    val output get() = sw.toString()

    fun println(s: Any?) = p.println(s)

    fun showDialog(title: String? = null) {
        Messages.showMultilineInputDialog(project, "So, here is what was bullshitted...", title ?: "A Little Bullshit", output, null, null)
    }
}

@Ser @Suppress("Unused")
class Command_PrintToBullshitter(val projectName: String, val message: String) : Servant {
    override fun serve() = withProjectNamed(projectName) {project->
        val bs = Bullshitter(project)
        bs.mumble("Someone said: " + message)
        coolResponse()
    }
}

//@Ser @Suppress("Unused")
//class Command_DebugConfiguration(val projectName: String, val configurationName: String) : Servant {
//    override fun serve(): Any {
//        return withProjectNamed(projectName) {project->
//            val error = debugConfiguration(project, configurationName)
//            when (error) {
//                null -> coolResponse()
//                else -> SimpleResponse(error)
//            }
//        }
//    }
//
//}


@Ser @Suppress("Unused")
class Command_Photlin_BreakOnDebugTag(val debugTag: String) : Servant {
    override fun serve() {
        withProjectNamed("fegh") {project->
            val p = HriposDebugOutput(project)

            run { // Change `breakOnDebugTag`
                val path = "E:/fegh/photlin/src/photlinc/shebang.kt"
                val file = LocalFileSystem.getInstance().findFileByPath(path) ?: wtf("e8df55f6-bb15-42fe-8bfc-e33634b06d33")
                val ktFile = PsiUtil.getPsiFile(project, file) as KtFile
                val globalObject = ktFile.declarations.find {it is KtObjectDeclaration && it.name == "PhotlincDebugGlobal"} as KtObjectDeclaration ?: wtf("0202ea08-d79c-4e8b-b156-98f90bb42dbc")
                val globalObjectClassBody = globalObject.getBody() ?: wtf("6e3bf3c9-b163-4847-b405-65b53b7e2111")
                val breakOnDebugTagProperty = globalObjectClassBody.properties.find {it.name == "breakOnDebugTag"} ?: wtf("7ece59a2-507e-46df-bf11-0a76baec99ca")
                p.println("Found ${breakOnDebugTagProperty.name}")
                val initializer = breakOnDebugTagProperty.initializer ?: wtf("cfe4f552-5f87-41ce-aac1-144a321a5a43")
                WriteCommandAction.runWriteCommandAction(project) {
                    initializer.replace(KtPsiFactory(project).createStringTemplate(debugTag))
                }
            }

            IDEAPile.runConfiguration(project, "photlinc.TryPhotlin", debug = true)

            //p.showDialog()
        }
    }
}

@Ser @Suppress("Unused")
class Command_IdeaBackdoorTesting_CheckKotlinClassesAreLoadedByPluginClassLoader : Servant {
    override fun serve() {
        withProjectNamed("idea-backdoor-fucking") {project->
            val p = HriposDebugOutput(project)
            val path = "E:/work/idea-backdoor-fucking/src/fuck.kt"
            val file = LocalFileSystem.getInstance().findFileByPath(path) ?: bitch("c7f4046b-0a5d-4f09-94fe-712542170f12")
            val psiFile = PsiUtil.getPsiFile(project, file)
            p.println("psiFile = $psiFile")
            p.println(psiFile.javaClass)
            p.println("Passed: ${psiFile is KtFile}")
            p.showDialog()
        }
    }
}
















