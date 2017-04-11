/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.sourceMap

import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.PhizdetsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.util.TextOutput
import com.intellij.util.SmartList
import vgrechka.*

class PhizdetsSourceGenerationVisitor(out: TextOutput, taggedGenOut: TextOutput, private val sourceMapBuilder: SourceMapBuilder?) : PhizdetsToStringGenerationVisitor(out, taggedGenOut), TextOutput.OutListener {

    private val pendingSources = SmartList<Any>()

    init {
        out.setOutListener(this)
    }

    override fun visitProgramFragment(x: JsProgramFragment) {
        x.acceptChildren(this)
    }

    override fun newLined() {
        sourceMapBuilder?.newLine()
    }

    override fun indentedAfterNewLine() {
        if (pendingSources.isEmpty()) return

        assert(sourceMapBuilder != null)
        for (source in pendingSources) {
            sourceMapBuilder!!.processSourceInfo(source)
        }
        pendingSources.clear()
    }

    override fun <T: JsNode?> accept(node: T) {
        if (node !is JsNameRef && node !is JsLiteral.JsThisRef) {
            mapSource(node)
        }
        super.accept(node)
    }

    // @debug-source-map
    private fun mapSource(node: JsNode?) {
        if (sourceMapBuilder != null) {
            val sourceInfo = node!!.source

//            run { // @debug
//                if (node is JsConditional) {
//                    "break on me"
//                }
//            }


            // @debug
//            val shit = node.debug_attachedShit("replacedNode")
//            if (shit != null) {
//                "break on me"
//            }

            // @debug
//            if ("dt-pizda" == sourceInfo.debug_tag) {
//                "break on me".toString()
//            }

            // @debug
//            if (node.toString().contains("QUERY_STRING")) {
//                "break on me".toString()
//            }

            if (sourceInfo != null) {
                if (p.isJustNewlined) {
                    pendingSources.add(sourceInfo)
                } else {
                    sourceMapBuilder.processSourceInfo(sourceInfo)
                }
            }
        }
    }

    override fun beforeNodePrinted(node: JsNode) {
        mapSource(node)
    }

    override fun visitProgram(program: JsProgram) {
        // @phi-1

        p.print("<?php \$GLOBALS['myFuckingDir'] = __DIR__; require_once('phi-engine.php'); @define('PHI_KILL_LONG_LOOPS', true); ")

        program.acceptChildren(this)
        sourceMapBuilder?.addLink()
    }
}



