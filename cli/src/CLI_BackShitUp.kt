package vgrechka

import com.google.api.client.http.FileContent
import com.google.common.hash.Hashing
import vgrechka.BigPile.exitingProcessDespitePossibleJavaFXThread
import vgrechka.CLIPile.regexpTag
import vgrechka.db.*
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

// NOTE: For some reason WSL Bash doesn't want to work from IDEA console, so run this from cmd.exe

object CLI_BackShitUp__CheckShitUploadedToOneDrive {
    @JvmStatic
    fun main(args: Array<String>) {
        val stamp = args[0]
        BackShitUpStuff(stamp) {
            it.uploadShitToOneDrive {
                it.doubleCheck()
            }
        }
    }
}

// Ex: _run vgrechka.CLI_BackShitUp
object CLI_BackShitUp {
    @JvmStatic
    fun main(args: Array<String>) {
        BackShitUpStuff {
            it.recreateTFVCOutDir()
            it.recreateReducedOutDir()
            it.copyIDEASettings()
            it.dumpPostgres()
            it.zipFuckingEverything()
            it.zipAPS()
            it.zipFuckingPrivateEverything()
            it.hashShit()
            it.uploadShitToDropbox()
            it.uploadShitToGoogleDrive()
            it.uploadShitToOneDrive()
            it.checkInToTFVC()
        }
    }
}


private class BackShitUpStuff(stamp: String? = null, block: (BackShitUpStuff) -> Unit) {
    val stamp = stamp ?: LocalDateTime.now().format(TimePile.FMT_YMD)!!
    val tfvcOutDirWin = "e:/febig/bak/$stamp"
    val tfvcOutDirWSL = BigPile.win2WSL(tfvcOutDirWin)
    val reducedOutDirWin = "e:/bak/$stamp"
    val reducedOutDirWSL = BigPile.win2WSL(reducedOutDirWin)
    val tmpDirWin = "c:/tmp"

    init {
        exitingProcessDespitePossibleJavaFXThread {
            clog("reducedOutDirWin = $reducedOutDirWSL")
            block(this)
            clog("\nOK")
        }
    }

    fun runTFAndCheckOutput(cmdPieces: List<String>, expectedLines: List<String>) {
        val res = BigPile.runProcessAndWait(
            listOf("cmd", "/c", "tf") + cmdPieces.toTypedArray(),
            workingDirectory = File("e:/febig"),
            inheritIO = false,
            noisy = true)

        if (res.exitValue != 0)
            bitch("TF said us 'Fuck you', providing code ${res.exitValue}")

        CLIPile.printAndCheckProcessResult(res, expectedLines)
    }

    inner class checkInToTFVC {
        private var files = File(tfvcOutDirWin).listFiles()
        private var fileNames: List<String>
        private var filePaths: List<String>

        init {
            files.sortWith(Comparator {a, b -> a.name.compareTo(b.name)})
            fileNames = files.map {it.name}
            filePaths = files.map {
                val s = it.path
                check(s.matches(Regex("^\\w:.*")))
                s[0].toUpperCase() + s.substring(1)
            }

            runStatus()

            runTFAndCheckOutput(cmdPieces = listOf("add") + filePaths, expectedLines = mutableListOf<String>()-{
                it += "bak\\$stamp:"
                for (name in fileNames)
                    it += name
                it += ""
            })

            runTFAndCheckOutput(cmdPieces = listOf("checkin") + filePaths, expectedLines = mutableListOf<String>()-{
                it += "bak\\$stamp:"
                for (name in fileNames)
                    it += "Checking in add: $name"
                it += ""
                it += "${regexpTag}Changeset #\\d+ checked in."
                it += ""
            })
        }

        private fun runStatus() {
            runTFAndCheckOutput(cmdPieces = listOf("status", tfvcOutDirWin + "/*"), expectedLines = run {
                val maxFileNameLength = fileNames.maxBy {it.length}!!.length
                // val maxFilePathLength = filePaths.maxBy {it.length}!!.length


                val width = 79

                var fileNameColumnTitle = "File name"
                if (maxFileNameLength > fileNameColumnTitle.length) {
                    fileNameColumnTitle += " ".repeat(maxFileNameLength - fileNameColumnTitle.length)
                }

                var columnLine = "-".repeat(fileNameColumnTitle.length) + " ------ "
                if (columnLine.length <= width)
                    columnLine += "-".repeat(width - columnLine.length)
                else
                    columnLine += "-"

                val expectedLines = mutableListOf(
                    "",
                    "-".repeat(width),
                    "Detected Changes:",
                    "-".repeat(width),
                    "$fileNameColumnTitle Change Local path",
                    columnLine,
                    "\$/febig/bak/$stamp")

                for (index in files.indices) {
                    expectedLines += (""
                        + fileNames[index].padEnd(maxFileNameLength + 1)
                        + "add    "
                        + filePaths[index])
                }

                expectedLines += listOf(
                    "",
                    "0 change(s), ${files.size} detected change(s)",
                    "")

                expectedLines
            })
        }
    }

    fun dumpPostgres() {
        clog("Dumping fepg")
        val dumpFileName = "fepg-$stamp.pg_dump"
        val dumpFile = "$reducedOutDirWin/$dumpFileName"
        DBPile.pg_dump(BigPile.saucerfulOfSecrets.fepg.prod, toFile = dumpFile)
        bash("cd $reducedOutDirWSL && zip $dumpFileName.zip $dumpFileName")
        bash("cp $reducedOutDirWSL/$dumpFileName.zip $tfvcOutDirWSL")
        check(File(dumpFile).delete()) {"b41fd906-e8ba-4b1c-babd-b3310d2c465e"}
    }

    fun hashShit() {
        fun jerk(dirWin: String) {
            for (localFile in File(dirWin).listFiles()) {
                val sha256 = Hashing.sha256().hashBytes(localFile.readBytes()).toString()
                File(localFile.path + ".sha256").writeText(sha256)
            }
        }

        jerk(reducedOutDirWin)
        jerk(tfvcOutDirWin)
    }

    fun uploadShitToDropbox() {
        val box = Dropbox(BigPile.saucerfulOfSecrets.dropbox.vgrechka)
        object : UploadShitSomewhereTemplate() {
            override fun getOutputLabel() = "[DROPBOX]"

            override fun getAccountName(): String {
                return box.account.name.displayName
            }

            override fun createFolder(remotePath: String) {
                box.client.files().createFolder(remotePath)
            }

            override fun uploadFile(file: File, remoteFilePath: String) {
                file.inputStream().use {stm->
                    box.client.files()
                        .uploadBuilder(remoteFilePath)
                        .uploadAndFinish(stm)
                }
            }

            override fun listFolder(removeFolder: String, recursive: Boolean): List<Meta> {
                return box.listFolder(removeFolder, recursive).map {
                    object : Meta() {
                        override val path get() = it.pathLower
                    }
                }
            }

            override fun downloadFile(remotePath: String, stm: FileOutputStream) {
                box.client.files()
                    .download(remotePath)
                    .download(stm)
            }
        }
    }

    fun uploadShitToOneDrive(block: ((UploadShitSomewhereTemplate) -> Unit)? = null) {
        val drive = OneDrive()

        object : UploadShitSomewhereTemplate(block) {
            override fun getOutputLabel() = "[ONEDRIVE]"

            override fun getAccountName(): String {
                return drive.account.displayName
            }

            override fun createFolder(remotePath: String) {
                drive.createFolder(remotePath)
            }

            override fun uploadFile(file: File, remoteFilePath: String) {
                drive.uploadFile(file, remoteFilePath)
            }

            override fun listFolder(remoteFolder: String, recursive: Boolean): List<Meta> {
                imf("0e7e2391-83aa-4d0c-8d05-07262d893305")
            }

            override fun downloadFile(remotePath: String, stm: FileOutputStream) {
                drive.downloadFile(remotePath, stm)
            }
        }
    }

    fun uploadShitToGoogleDrive() {
        val g = GoogleDrive()

        object : UploadShitSomewhereTemplate() {
            override fun getOutputLabel() = "[GDRIVE]"

            override fun getAccountName(): String {
                return g.drive.about().get().setFields("user").execute()
                    .user.displayName
            }

            inner class FilePathShit(val parentID: String, val name: String)

            override fun createFolder(remotePath: String) {
                val fps = getFilePathShit(remotePath)

                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = fps.name
                fileMetadata.parents = listOf(fps.parentID)
                fileMetadata.mimeType = "application/vnd.google-apps.folder"

                g.drive.files().create(fileMetadata)
                    .execute()
            }

            fun getFilePathShit(remotePath: String): FilePathShit {
                check(remotePath.startsWith("/"))
                val theRemotePath = remotePath.substring(1)

                val steps = theRemotePath.split("/")
                val name = steps.last()
                val parentID = getRemoteFileID(steps.dropLast(1))
                return FilePathShit(parentID, name)
            }

            private fun getRemoteFile(pathParts: List<String>): com.google.api.services.drive.model.File {
                var parentID = "root"
                val parentsSoFar = mutableListOf(parentID)
                var file: com.google.api.services.drive.model.File? = null
                for (name in pathParts) {
                    val q = "name = '$name' and '$parentID' in parents and trashed = false"
                    // clog("q = $q")
                    val list = g.drive.files().list()
                        .setQ(q)
                        .execute()
                    if (list.files.size == 0)
                        bitch("File `$name` not found under `${parentsSoFar.joinToString("/")}`")
                    check(list.files.size == 1)
                    val f = list.files.first()
                    file = f
                    parentID = f.id
                    parentsSoFar += parentID
                }
                return file!!
            }

            private fun getRemoteFileID(pathParts: List<String>): String {
                return getRemoteFile(pathParts).id
            }

            override fun uploadFile(file: File, remoteFilePath: String) {
                val fps = getFilePathShit(remoteFilePath)

                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = fps.name
                fileMetadata.parents = listOf(fps.parentID)

                val fileContent = FileContent(HTTPPile.contentType.octetStream, file)

                val insert = g.drive.files().create(fileMetadata, fileContent)
                val uploader = insert.mediaHttpUploader
                uploader.isDirectUploadEnabled = true
                // uploader.setProgressListener(...)
                insert.execute()
            }

            override fun listFolder(remoteFolder: String, recursive: Boolean): List<Meta> {
                imf("5f958cdc-5fdc-4767-8f66-a446a4e76b5a")
            }

            override fun downloadFile(remotePath: String, stm: FileOutputStream) {
                check(remotePath.startsWith("/"))
                val theRemotePath = remotePath.substring(1)
                val id = getRemoteFileID(theRemotePath.split("/"))
                val remoteFile = g.drive.files().get(id)
                remoteFile.executeMediaAndDownloadTo(stm)
            }
        }
    }

    abstract inner class UploadShitSomewhereTemplate(
        block: ((UploadShitSomewhereTemplate) -> Unit)? = null
    ) {
        abstract fun getOutputLabel(): String
        abstract fun getAccountName(): String
        abstract fun createFolder(remotePath: String)
        abstract fun uploadFile(file: File, remoteFilePath: String)
        abstract fun listFolder(remoteFolder: String, recursive: Boolean): List<Meta>
        abstract fun downloadFile(remotePath: String, stm: FileOutputStream)

        val bs = ConsoleBullshitter(getOutputLabel())
        val targetFolder = "/bak/$stamp"

        abstract inner class Meta {
            abstract val path: String
        }

        init {
            clog()
            bs.say("Account: " + getAccountName())
            when (block) {
                null -> all()
                else -> block(this)
            }
        }

        fun all() {
            createFolder()
            uploadShit()
            doubleCheck()
        }

        fun listShit() {
            bs.say()
            bs.section("Listing shit under $targetFolder") {
                val metas = listFolder(targetFolder, recursive = true)
                for (meta in metas) {
                    bs.say(meta.path)
                }
            }
        }

        fun doubleCheck() {
            bs.section("Double-checking shit") {
                for (origFile in File(reducedOutDirWin).listFiles()) {
                    val downloadedFile = File("$tmpDirWin/checking--${origFile.name}")
                    if (downloadedFile.exists()) {
                        check(downloadedFile.delete()) {"f683d3b4-d096-40b6-85a6-43395c1181c0"}
                    }

                    bs.operation("Downloading ${origFile.name}") {
                        downloadedFile.outputStream().use {stm ->
                            downloadFile("$targetFolder/${origFile.name}", stm)
                        }
                    }

                    bs.operation("Comparing with original") {
                        val downloadedBytes = downloadedFile.readBytes()
                        val origBytes = origFile.readBytes()
                        if (downloadedBytes.size != origBytes.size)
                            bitch("Sizes don't match. Expected ${origBytes.size}, got ${downloadedBytes.size}")
                        for (i in downloadedBytes.indices)
                            if (downloadedBytes[i] != origBytes[i])
                                bitch("Bytes don't batch")
                    }
                }
            }
        }

        fun uploadShit() {
            for (localFile in File(reducedOutDirWin).listFiles()) {
                bs.operation("Uploading ${localFile.name}") {
                    uploadFile(localFile, "$targetFolder/${localFile.name}")
                }
            }
        }

        fun createFolder() {
            bs.operation("Creating folder $targetFolder") {
                createFolder(targetFolder)
            }
        }

    }

    fun copyIDEASettings() {
        val exportedSettings = "/mnt/c/tmp/idea-settings-$stamp.jar"
        bash("cp $exportedSettings $reducedOutDirWSL")
        bash("cp $exportedSettings $tfvcOutDirWSL")
    }

    fun zipAPS() {
        fun jerk(subName: String, outDirWSL: String, opts: String) {
            bash(""  + " cd /mnt/e/work/aps"
                     + " && zip -r $outDirWSL/aps-$subName-$stamp.zip . $opts")
        }

        jerk(subName = "full", outDirWSL = tfvcOutDirWSL, opts = "")

        jerk(subName = "reduced", outDirWSL = reducedOutDirWSL, opts = ""
            + "        -x /.git*"
            + "        -x /node_modules/*"
            + "        -x /back/.gradle/*"
            + "        -x /back/built/*"
            + "        -x /back/lib-gradle/*"
            + "        -x /back/out/*"
            + "        -x /front/out/*"
            + "        -x /javaagent/out/*"
            + "        -x /kotlin-js-playground/node_modules/*"
            + "        -x /kotlin-js-playground/out/*"
            + "        -x /kotlin-jvm-playground/out/*"
            + "        -x /tools/out/*")
    }

    fun zipFuckingEverything() {
        fun jerk(subName: String, outDirWSL: String, opts: String) {
            bash(""  + " cd /mnt/e/fegh"
                     + " && zip -r $outDirWSL/fe-$subName-$stamp.zip . $opts")
        }

        jerk(subName = "full", outDirWSL = tfvcOutDirWSL, opts = "")

        jerk(subName = "reduced", outDirWSL = reducedOutDirWSL, opts = ""
            + " -x /.git*"
            + " -x /node_modules/*"
            + " -x /composer/vendor/*"
            + " -x /out/*"
            + " -x /lib-gradle/*"
            + " -x /.gradle/*"
            + " -x /phizdets/phizdets-idea/eclipse-lib/*"
            + " -x /okaml/okaml-fucking/1/_build/*"
            + " -x /okaml/okaml-fucking/2/_build/*")
    }

    fun zipFuckingPrivateEverything() {
        fun jerk(subName: String, outDirWSL: String, opts: String) {
            bash(""  + " cd /mnt/e/fpebb"
                     + " && zip -r $outDirWSL/fpe-$subName-$stamp.zip . $opts")
        }

        jerk(subName = "full", outDirWSL = tfvcOutDirWSL, opts = "")

        jerk(subName = "reduced", outDirWSL = reducedOutDirWSL, opts = ""
            + " -x /.git*")
    }

    private fun recreateDir(pathWin: String) {
        bash("rm -rf ${BigPile.win2WSL(pathWin)}")
        val file = File(pathWin)
        if (file.exists())
            bitch("Unable to remove directory $pathWin")
        bash("mkdir ${BigPile.win2WSL(pathWin)}")
        if (!file.exists() || !file.isDirectory)
            bitch("Unable to create directory $pathWin")
    }

    fun recreateReducedOutDir() {
        recreateDir(reducedOutDirWin)
    }

    fun recreateTFVCOutDir() {
        recreateDir(tfvcOutDirWin)
    }

    fun bash(cmd: String) {
        val res = BigPile.runProcessAndWait(listOf(
            "bash", "-c", cmd
        ), noisy = true)
        if (res.exitValue != 0)
            bitch("Bash said us 'Fuck you', providing code ${res.exitValue}")
    }

}











