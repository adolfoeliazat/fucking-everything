package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*
import java.io.File
import io.undertow.server.HttpServerExchange
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.util.Headers
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


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
        runUndertow()
    }

    private fun runUndertow() {
        val sslContext = SSLContext.getInstance("TLS")
        val kmf = KeyManagerFactory.getInstance("SunX509")
        val ks = KeyStore.getInstance("JKS")

        ks.load(FileInputStream(AlBack.secrets.keyStore), AlBack.secrets.keyStorePassword.toCharArray())
        kmf.init(ks, AlBack.secrets.keyManagerPassword.toCharArray())
        sslContext.init(kmf.keyManagers, null, null)

        val port = 443
        val server = Undertow.builder()
            .addHttpsListener(port, "localhost", sslContext)
            .setHandler(object : HttpHandler {
                override fun handleRequest(exchange: HttpServerExchange) {
                    exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/html")
                    exchange.responseSender.send("fuuuuuuuuuck")
                }
            }).build()
        server.start()
        clog("[Alraune] Shit is spinning on port $port")
    }



//    private fun runJetty() {
//        class FuckingServlet : HttpServlet() {
//            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
//                req.characterEncoding = "UTF-8"
//                req.queryString
//                res.contentType = "text/html; charset=utf-8"
//                res.writer.println("Fuck you")
//                res.status = HttpServletResponse.SC_OK
//            }
//        }
//
//        //        val httpPort = 80
//        val httpsPort = 443
//        val port = httpsPort
//
//        val server = Server()
//
//        //        val httpConnector = ServerConnector(server)
//        //        httpConnector.setPort(httpPort)
//
//        val https = HttpConfiguration()
//        https.addCustomizer(SecureRequestCustomizer())
//
//        val sslContextFactory = SslContextFactory()
//        sslContextFactory.keyStorePath = AlBack.secrets.keyStore
//        sslContextFactory.setKeyStorePassword(AlBack.secrets.keyStorePassword)
//        sslContextFactory.setKeyManagerPassword(AlBack.secrets.keyManagerPassword)
//
//        val httpsConnector = ServerConnector(server,
//                                             SslConnectionFactory(sslContextFactory, "http/1.1"),
//                                             HttpConnectionFactory(https))
//        httpsConnector.port = httpsPort
//
//        server.connectors = arrayOf(/*httpConnector,*/ httpsConnector)
//
//        server.handler = ServletHandler() - {o ->
//            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
//        }
//        server.start()
//
//        clog("[Alraune] Shit is spinning on port $port")
//        server.join()
//    }
}




