package vgrechka

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import java.io.File
import java.util.jar.JarFile

// Ex: _run vgrechka.CLIFindFileInJars DocumentBuilderFactoryImpl E:\fegh\phizdets\phizdets-idea\eclipse-lib
object CLIFindFileInJars {
    @JvmStatic
    fun main(args: Array<String>) {
        val partOfName = args[0]
        val dirPath = args[1]
        clog("partOfName = $partOfName; dirPath = $dirPath")
        clog()

        AnsiConsole.systemInstall()

        val dir = File(dirPath)
        check(dir.isDirectory) {"c259cbce-a0e5-4e60-9514-ec24a4abfb79"}
        dir.listFiles().forEach {file->
            if (file.extension == "jar") {
                val jar = JarFile(file)
                val matches = jar.entries().toList().filter {it.name.contains(partOfName)}
                if (matches.isNotEmpty()) {
                    clog(file.name)
                    clog("-".repeat(file.name.length))
                    matches.forEach {entry->
                        clog(stringBuild {s->
                            var startIndex = 0
                            while (true) {
                                val index = entry.name.indexOf(partOfName, startIndex)
                                if (index == -1) {
                                    break
                                } else {
                                    s += entry.name.substring(startIndex, index)
                                    s += ansi().fgBright(Ansi.Color.RED).a(partOfName).reset()
                                    startIndex = index + partOfName.length
                                }
                            }
                            s += entry.name.substring(startIndex)
                        })
                    }
                    clog()
                }
            }
        }
    }
}









