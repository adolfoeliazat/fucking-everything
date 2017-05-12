package vgrechka

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.Metadata

class Dropbox(val appAccessConfig: JSON_DropboxAppAccessConfig) {
    val config = DbxRequestConfig.newBuilder("fuck/shit")!!
    val client = DbxClientV2(config.build(), appAccessConfig.accessToken)
    val account = client.users().currentAccount!!

    fun listFolder(path: String, recursive: Boolean = false): List<Metadata> {
        val list = mutableListOf<Metadata>()

        var result = client.files().listFolderBuilder(path).withRecursive(recursive).start()
        while (true) {
            for (metadata in result.entries)
                list += metadata
            if (!result.hasMore)
                break
            result = client.files().listFolderContinue(result.cursor)
        }

        return list
    }
}