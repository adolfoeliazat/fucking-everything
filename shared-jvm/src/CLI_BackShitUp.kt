package vgrechka

import com.google.common.hash.Hashing
import java.io.File
import java.time.LocalDateTime

// TODO:vgrechka Back up Postgres
// TODO:vgrechka Upload to Google Drive
// TODO:vgrechka Upload to OneDrive
// TODO:vgrechka Upload to to TFVC

// NOTE: For some reason Bash doesn't want to work from IDEA console, so run this from cmd.exe

// Ex: _run vgrechka.CLI_BackShitUp
object CLI_BackShitUp {
    @JvmStatic
    fun main(args: Array<String>) {
        object {
            val stamp = LocalDateTime.now().format(TimePile.FMT_YMD)
            val outDirWin = "e:/bak/$stamp"
            val tmpDirWin = "c:/tmp"

            val outDirWSL = run {
                var s = outDirWin
                if (s.startsWith("e:/"))
                    s = "/mnt/e/" + s.substring(3)
                else
                    wtf("fddaec58-f69c-4974-bb83-4af4e35555d1")
                s
            }

            init {
                clog("Backing shit up to $outDirWSL")
                copyIDEASettings()
                recreateOutDir()
                zipFuckingEverything()
                zipAPS()
                zipFuckingPrivateEverything()
                hashShit()
                uploadShitToDropbox()
                clog("\nOK")
            }

            private fun hashShit() {
                for (localFile in File(outDirWin).listFiles()) {
                    val sha256 = Hashing.sha256().hashBytes(localFile.readBytes()).toString()
                    File(localFile.path + ".sha256").writeText(sha256)
                }
            }

            private fun uploadShitToDropbox() {
                clog()
                val bs = ConsoleBullshitter("[DROPBOX]")
                val box = Dropbox(BigPile.saucerfulOfSecrets.dropbox.vgrechka)
                bs.say("Account: " + box.account.name.displayName)

                val targetFolder = "/bak/$stamp"

                bs.operation("Creating folder $targetFolder") {
                    box.client.files().createFolder(targetFolder)
                }

                for (localFile in File(outDirWin).listFiles()) {
                    bs.operation("Uploading ${localFile.name}") {
                        localFile.inputStream().use {stm ->
                            box.client.files()
                                .uploadBuilder("$targetFolder/${localFile.name}")
                                .uploadAndFinish(stm)
                        }
                    }
                }
                bs.say()

                bs.section("Listing shit under $targetFolder") {
                    val metas = box.listFolder(targetFolder, recursive = true)
                    for (meta in metas) {
                        bs.say(meta.pathLower)
                    }
                }

                bs.section("Double-checking shit") {
                    for (origFile in File(outDirWin).listFiles()) {
                        val downloadedFile = File("$tmpDirWin/checking--${origFile.name}")
                        if (downloadedFile.exists()) {
                            check(downloadedFile.delete()) {"f683d3b4-d096-40b6-85a6-43395c1181c0"}
                        }

                        bs.operation("Downloading ${origFile.name}") {
                            downloadedFile.outputStream().use {stm->
                                box.client.files()
                                    .download("$targetFolder/${origFile.name}")
                                    .download(stm)
                            }
                        }

                        bs.operation("Comparing with original") {
                            val downloadedBytes = downloadedFile.readBytes()
                            val origBytes = origFile.readBytes()
                            if (downloadedBytes.size != origBytes.size)
                                bitch("Sizes don't match")
                            for (i in downloadedBytes.indices)
                                if (downloadedBytes[i] != origBytes[i])
                                    bitch("Bytes don't batch")
                        }
                    }
                }
            }

            private fun copyIDEASettings() {
                val exportedSettings = "/mnt/c/tmp/idea-settings-$stamp.jar"
                bash("cp $exportedSettings $outDirWSL")
            }

            private fun zipAPS() {
                bash(""  + " cd /mnt/e/work/aps"
                         + " && zip -r $outDirWSL/aps-$stamp.zip ."
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

            private fun zipFuckingEverything() {
                bash(""  + " cd /mnt/e/fegh"
                         + " && zip -r $outDirWSL/fe-$stamp.zip ."
                         + "        -x /.git*"
                         + "        -x /node_modules/*"
                         + "        -x /composer/vendor/*"
                         + "        -x /out/*"
                         + "        -x /lib-gradle/*"
                         + "        -x /.gradle/*"
                         + "        -x /phizdets/phizdets-idea/eclipse-lib/*"
                         + "        -x /okaml/okaml-fucking/1/_build/*"
                         + "        -x /okaml/okaml-fucking/2/_build/*")
            }

            private fun zipFuckingPrivateEverything() {
                bash(""  + " cd /mnt/e/fpebb"
                         + " && zip -r $outDirWSL/fpe-$stamp.zip ."
                         + "        -x /.git*")
            }

            private fun recreateOutDir() {
                bash("rm -rf $outDirWSL")
                bash("mkdir $outDirWSL")
            }

            fun bash(cmd: String) {
                val res = BigPile.runProcessAndWait(listOf(
                    "bash", "-c", cmd
                ), noisy = true)
                if (res.exitValue != 0)
                    bitch("Bash said us 'Fuck you', providing code ${res.exitValue}")
            }
        }
    }
}

