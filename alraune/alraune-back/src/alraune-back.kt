package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import vgrechka.*
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.File
import org.eclipse.jetty.server.*

object AlBack {
    val secrets by lazy {
        // TODO:vgrechka Get file name from environment variable
        ObjectMapper().readValue(File("e:/fpebb/alraune/alraune-secrets.json"), JSON_AlrauneSecrets::class.java)!!
    }
}


@Ser class JSON_AlrauneSecrets(
    val keyStore: String,
    val keyStorePassword: String,
    val keyManagerPassword: String
)

object StartAlrauneBack {
    @JvmStatic
    fun main(args: Array<String>) {
        class FuckingServlet : HttpServlet() {
            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
                req.characterEncoding = "UTF-8"
                req.queryString
                res.contentType = "text/html; charset=utf-8"
                res.writer.println("Fuck you")
                res.status = HttpServletResponse.SC_OK
            }
        }

//        val httpPort = 80
        val httpsPort = 443
        val port = httpsPort

        val server = Server()

//        val httpConnector = ServerConnector(server)
//        httpConnector.setPort(httpPort)

        val https = HttpConfiguration()
        https.addCustomizer(SecureRequestCustomizer())

        val sslContextFactory = SslContextFactory()
        sslContextFactory.keyStorePath = AlBack.secrets.keyStore
        sslContextFactory.setKeyStorePassword(AlBack.secrets.keyStorePassword)
        sslContextFactory.setKeyManagerPassword(AlBack.secrets.keyManagerPassword)

        val httpsConnector = ServerConnector(server,
                                             SslConnectionFactory(sslContextFactory, "http/1.1"),
                                             HttpConnectionFactory(https))
        httpsConnector.port = httpsPort

        server.connectors = arrayOf(/*httpConnector,*/ httpsConnector)

        server.handler = ServletHandler()-{o->
            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
        }
        server.start()

        clog("[Alraune] Shit is spinning on port $port")
        server.join()
    }
}




