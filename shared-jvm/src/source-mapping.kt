package vgrechka

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.debugging.sourcemap.SourceMapConsumerFactory
import com.google.debugging.sourcemap.SourceMapping
import java.io.File
import kotlin.reflect.KProperty

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

    val sourceLineToGeneratedLine: MutableMap<Int, Int> by lazy {
        val res = mutableMapOf<Int, Int>()
        for ((generatedLine, entries) in generatedLineToDugEntries) {
            val sourceLine = entries.first().sourceLine
            for (entry in entries) {
                if (entry.sourceLine != sourceLine || entry.generatedLine != generatedLine)
                    wtf("8ea09bef-be33-4a6f-8eb4-5cd2b8502e02")
            }
            res[sourceLine] = generatedLine
        }
        res
    }

    fun dumpSourceLineToGeneratedLine() {
        if (sourceLineToGeneratedLine.isEmpty()) return clog("Freaking source map is empty")
        val entries = sourceLineToGeneratedLine.entries.toMutableList()
        entries.sortBy {it.key}
        for ((sourceLine, generatedLine) in entries) {
            clog("sourceLine = $sourceLine; generatedLine = $generatedLine")
        }
    }
}

object SourceMappingCache {
    private val mappingCache =
        /*if (CACHE_MAPPINGS_BETWEEN_REQUESTS) sharedMappingCache
        else*/ makeMappingCache()

    fun getMapping(mapFilePath: String) = mappingCache[mapFilePath]

    private fun makeMappingCache(): LoadingCache<String, SourceMapping> {
        return CacheBuilder.newBuilder().build(object:CacheLoader<String, SourceMapping>() {
            override fun load(mapPath: String): SourceMapping {
                return SourceMapConsumerFactory.parse(File(mapPath).readText())
            }
        })
    }
}








