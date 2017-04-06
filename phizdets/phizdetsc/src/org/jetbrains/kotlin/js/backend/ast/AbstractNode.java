/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.backend.ast;

import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor;
import org.jetbrains.kotlin.js.backend.PhizdetsToStringGenerationVisitor;
import org.jetbrains.kotlin.js.backend.ast.metadata.HasMetadata;
import org.jetbrains.kotlin.js.util.TextOutputImpl;
import phizdets.compiler.PhizdetscKt;

public abstract class AbstractNode extends HasMetadata implements JsNode {
    private static long nextDebugTag = 0L;
    public String debugTag = "" + nextDebugTag++;
    public boolean needsFuckingNewline = true;

    {
        if (debugTag.equals("191")) {
            "break on me".toString();
        }
    }

    @Override
    public String toString() {
        TextOutputImpl out = new TextOutputImpl();
        new PhizdetsToStringGenerationVisitor(out, new TextOutputImpl()).accept(this);
        return out.toString();
    }

    protected <T extends HasMetadata> T withMetadataFrom(T other) {
        this.copyMetadataFrom(other);
        if (other instanceof AbstractNode) {
            AbstractNode otherNode = (AbstractNode) other;
            debugTag = otherNode.debugTag;
            needsFuckingNewline = otherNode.needsFuckingNewline;
            PhizdetscKt.setDeclarationDescriptor(this, PhizdetscKt.getDeclarationDescriptor(otherNode));
        }
        //noinspection unchecked
        return (T) this;
    }
}
