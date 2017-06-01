package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import vgrechka.*
import java.io.File

object AlBackPile0 {
    val locale = AlLocale.UA
    val backResourceRootDir = "E:/fegh/alraune/alraune-back"
//    val frontOutDir = "E:/fegh/out/production/alraune-front"
//    val sharedKJSOutDir = "E:/fegh/out/production/shared-kjs"
    val frontOutDirParent = "E:/fegh/out/production"
    val sharedKJSOutDirParent = "E:/fegh/out/production"
    val tmpDirPath = "c:/tmp"
    val backToFrontCommandsKtSourceFile = "E:/fegh/alraune/alraune-back/src/AlBackToFrontCommand.kt"
    val formPostDataKtSourceFile = "E:/fegh/alraune/alraune-back/symlink--alraune-symlinked-src/alraune-symlinked.kt"
    val tsSrcRoot = "E:/fegh/alraune/alraune-back/ts/src"
    val generatedTSFile = "E:/fegh/alraune/alraune-back/ts/src/generated--by-backend.ts"
    val tsJSOutputFile = "E:/fegh/alraune/alraune-back/ts/out/alraune.js"
    val tsMapOutputFile = "E:/fegh/alraune/alraune-back/ts/out/alraune.js.map"
    val log = LoggerFactory.getLogger(this.javaClass)
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




