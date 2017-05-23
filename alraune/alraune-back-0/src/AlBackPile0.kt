package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import vgrechka.*
import java.io.File

object AlBackPile0 {
    val locale = AlLocale.UA
    val backResourceRootDir = "E:/fegh/alraune/alraune-back"
    val frontOutDir = "E:/fegh/out/production/alraune-front"
    val sharedKJSOutDir = "E:/fegh/out/production/shared-kjs"
    val tmpDirPath = "c:/tmp"
    val log = LoggerFactory.getLogger(this.javaClass)
    val orderCreationPagePath = "/order"
    val baseURL = "https://alraune.local"

    val secrets by lazy {
        // TODO:vgrechka Get file name from environment variable
        ObjectMapper().readValue(File("e:/fpebb/alraune/alraune-secrets.json"), JSON_AlrauneSecrets::class.java)!!
    }
}

enum class AlLocale {
    UA, EN
}

@Ser class JSON_AlrauneSecrets(
    val keyStore: String,
    val keyStorePassword: String,
    val keyManagerPassword: String,
    val db: JSON_db
) {
    @Ser class JSON_db(
        val prod: DBConnectionParams,
        val test: DBConnectionParams)
}




