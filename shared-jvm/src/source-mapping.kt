package vgrechka

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.debugging.sourcemap.*
import java.io.File

val SourceMapping.penetration by AttachedComputedShit<SourceMapping, SourceMapConsumerPenetration> {sourceMapping->
    val penetration = SourceMapConsumerPenetration()

    val privateLines = run {
        val f = sourceMapping.javaClass.getDeclaredField("lines")
        f.isAccessible = true
        f.get(sourceMapping) as List<List<Any>?>
    }
    val privateSources = run {
        val f = sourceMapping.javaClass.getDeclaredField("sources")
        f.isAccessible = true
        f.get(sourceMapping) as Array<String>
    }

    for ((generatedLine, privateLine) in privateLines.withIndex()) {
        if (privateLine != null) {
            for (privateEntry in privateLine) {
                val sourceLine = run {
                    val m = privateEntry.javaClass.getMethod("getSourceLine")
                    m.isAccessible = true
                    m.invoke(privateEntry) as Int
                }
                val sourceColumn = run {
                    val m = privateEntry.javaClass.getMethod("getSourceColumn")
                    m.isAccessible = true
                    m.invoke(privateEntry) as Int
                }
                val sourceFileId = run {
                    val m = privateEntry.javaClass.getMethod("getSourceFileId")
                    m.isAccessible = true
                    m.invoke(privateEntry) as Int
                }

                val entry = SourceMapConsumerPenetration.DugEntry(
                    file = privateSources[sourceFileId],
                    generatedLine = generatedLine + 1,
                    sourceLine = sourceLine + 1,
                    sourceColumn = sourceColumn + 1)

                val entries = penetration.generatedLineToDugEntries.getOrPut(generatedLine + 1) {mutableListOf()}
                entries += entry
            }
        }
    }
    penetration
}

class SourceMapConsumerPenetration {
    val generatedLineToDugEntries = mutableMapOf<Int, MutableList<DugEntry>>()

    class DugEntry(val file: String,
                   val generatedLine: Int,
                   val sourceLine: Int,
                   val sourceColumn: Int)

    val sourceFileLineToGeneratedLine: MutableMap<FileLine, Int> by lazy {
        val res = mutableMapOf<FileLine, Int>()
        for ((generatedLine, entries) in generatedLineToDugEntries) {
            val firstEntry = entries.first()
            for (entry in entries) {
                if (entry.sourceLine != firstEntry.sourceLine
                    || entry.file != firstEntry.file
                    || entry.generatedLine != generatedLine)
                    wtf("8ea09bef-be33-4a6f-8eb4-5cd2b8502e02")
            }
            res[FileLine(firstEntry.file, firstEntry.sourceLine)] = generatedLine
        }
        res
    }

    fun dumpSourceLineToGeneratedLine() {
        if (sourceFileLineToGeneratedLine.isEmpty()) return clog("Freaking source map is empty")
        val entries = sourceFileLineToGeneratedLine.entries.toMutableList()
        entries.sortWith(Comparator<Map.Entry<FileLine, Int>> {a, b ->
            val c = a.key.file.compareTo(b.key.file)
            if (c != 0)
                c
            else
                a.key.line.compareTo(b.key.line)
        })
        for ((sourceFileLine, generatedLine) in entries) {
            val fullFilePath = true
            val fileDesignator = when {
                fullFilePath -> sourceFileLine.file
                else -> {
                    val sourceFile = File(sourceFileLine.file)
                    sourceFile.name
                }
            }
            val source = fileDesignator + ":" + sourceFileLine.line
            clog("source = $source; generatedLine = $generatedLine")
        }
    }
}

val theSourceMappings = SourceMappings()

class SourceMappings(val allowInexactMatches: Boolean = true) {
    private val cache = CacheBuilder.newBuilder().build(object:CacheLoader<String, SourceMapping>() {
        override fun load(mapFilePath: String): SourceMapping {
            val text = File(mapFilePath).readText()
            return when {
                allowInexactMatches -> SourceMapConsumerFactory.parse(text)
                else -> parse(text, null)
            }
        }
    })

    fun getCached(mapFilePath: String): SourceMapping = cache[mapFilePath.replace("\\", "/")]

    private fun parse(contents: String, supplier: SourceMapSupplier?): SourceMapping {
        // Version 1, starts with a magic string
        if (contents.startsWith("/** Begin line maps. **/")) {
            throw SourceMapParseException(
                "This appears to be a V1 SourceMap, which is not supported.")
        } else if (contents.startsWith("{")) {
            val sourceMapObject = SourceMapObjectParser.parse(contents)

            // Check basic assertions about the format.
            when (sourceMapObject.version) {
                3 -> {
                    val consumer = SourceMapConsumerV3Hack()
                    consumer.allowInexactMatches = allowInexactMatches
                    consumer.parse(sourceMapObject, supplier)
                    return consumer
                }
                else -> throw SourceMapParseException(
                    "Unknown source map version:" + sourceMapObject.version)
            }
        }

        throw SourceMapParseException("unable to detect source map format")
    }
}

object DumpSourceMapTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val mapFilePath = args[0]
        val mapping = SourceMappings().getCached(mapFilePath)
        mapping.penetration.dumpSourceLineToGeneratedLine()
    }
}





















