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

package org.jetbrains.kotlin.js.facade;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PairConsumer;
import org.jetbrains.kotlin.js.backend.ast.JsNode;
import org.jetbrains.kotlin.js.backend.ast.JsReturn;
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilder;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtConstantExpression;
import vgrechka.DebugKt;
import vgrechka.FileLineColumn;

import java.util.HashSet;

public class SourceMapBuilderConsumer implements PairConsumer<SourceMapBuilder, Object> {
    @Override
    public void consume(SourceMapBuilder builder, Object sourceInfo) {
        FileLineColumn flc = null;

        if (sourceInfo instanceof PsiElement) {
            PsiElement element = (PsiElement) sourceInfo;
            PsiFile file = element.getContainingFile();
            int offset = element.getNode().getStartOffset();
            Document document = file.getViewProvider().getDocument();
            assert document != null;
            int line = document.getLineNumber(offset);
            int column = offset - document.getLineStartOffset(line);

//        { // @debug-3
//            if ("dt-pizda".equals(DebugKt.getDebug_tag(sourceInfo))) {
//                "break on me".toString();
//            }
//        }

//        { // @debug-5
//            if (sourceInfo instanceof KtCallExpression) {
//                KtCallExpression ktCallExpression = (KtCallExpression) sourceInfo;
//                if (ktCallExpression.getText().contains("QUERY_STRING")) {
//                    "break on me".toString();
//                }
//            }
//        }

            { // @debug-5
                JsNode jsNode = (JsNode) DebugKt.debug_attachedShit(sourceInfo, "jsNode");
                if (jsNode != null) {
    //                if (jsNode instanceof JsReturn) {
    //                    "break on me".toString();
    //                }

    //                if (jsNode.toString().contains("QUERY_STRING")) {
    //                    "break on me".toString();
    //                }

    //                if (jsNode instanceof JsReturn) {
    //                    JsReturn jsReturn = (JsReturn) jsNode;
    //                    if (jsReturn.toString().contains("QUERY_STRING")) {
    //                        "break on me".toString();
    //                    }
    //                }
                }
            }

            { // @debug-source-map
    //            if (file.toString().contains("shared-back-php-impl.kt")) {
    //                if (line == 166 - 1) {
    //                    JsNode jsNode = (JsNode) DebugKt.debug_attachedShit(sourceInfo, "jsNode");
    //                    "break on me".toString();
    //                }
    //            }
            }

            flc = new FileLineColumn(file.getViewProvider().getVirtualFile().getPath(), line, column);
        }
        else if (sourceInfo instanceof FileLineColumn) {
            flc = (FileLineColumn) sourceInfo;
        }

        if (flc != null) {
            if (!addedLines.contains(flc.getLine())) {
                builder.addMapping(flc.getFile(), flc.getLine(), flc.getColumn());
                addedLines.add(flc.getLine());
            }
        }
    }

    private HashSet<Integer> addedLines = new HashSet<>();
}













