package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.project.ProjectManager
import vgrechka.*
import vgrechka.idea.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HotReloadableIdeaPieceOfShit {
    fun serve(req: HttpServletRequest, res: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        req.queryString
        val rawRequest = req.reader.readText()
        clog("Got request:", rawRequest)

        val requestClass = Class.forName(this::class.java.`package`.name + "." + req.getParameter("proc"))
        val om = ObjectMapper()
        val servant = om.readValue(rawRequest, requestClass) as Servant
        val response = servant.serve()
        val rawResponse = om.writeValueAsString(response)

        res.contentType = "application/json; charset=utf-8"
        res.writer.println(rawResponse)
        res.status = HttpServletResponse.SC_OK
    }
}

private interface Servant {
    fun serve(): Any
}

@Ser class PrintToBullshitter(
    val projectName: String,
    val message: String
) : Servant {
    override fun serve(): Any {
        val project = ProjectManager.getInstance().openProjects.find {it.name == projectName}
            ?: return Response(status = "No fucking project `$projectName`")

        project.bullshitter.mumble("Someone said: " + message)
        return Response(status = "Cool")
    }

    class Response(val status: String)
}














