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

import photlinc.AttachedShit;
import photlinc.PhotlincDebugGlobal;
import photlinc.DebugTagged;
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor;
import org.jetbrains.kotlin.js.backend.ast.metadata.HasMetadata;
import org.jetbrains.kotlin.js.facade.K2JSTranslator;
import org.jetbrains.kotlin.js.util.TextOutputImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class AbstractNode extends HasMetadata implements JsNode, DebugTagged {
    private String debugTag;
    public boolean commentedOut;
    public boolean suppressWarnings;
    public String mappedFromDebugTag;

    @Override
    public String toString() {
        TextOutputImpl out = new TextOutputImpl();
        new JsToStringGenerationVisitor(out).accept(this);
        return out.toString();
    }

    protected <T extends HasMetadata> T withMetadataFrom(T other) {
        this.copyMetadataFrom(other);
        if (other instanceof AbstractNode) {
            AbstractNode node = (AbstractNode) other;
            this.debugTag = node.getDebugTag();
            this.mappedFromDebugTag = node.mappedFromDebugTag;

            Map<Object, Object> shitToShit = K2JSTranslator.current.shitToShit;
            List<Object> keys = shitToShit.keySet().stream()
                    .filter((k) -> ((AttachedShit.Key) k).getObj() == node)
                    .collect(Collectors.toList());
            for (Object k : keys) {
                AttachedShit.Key key = (AttachedShit.Key) k;
                shitToShit.put(new AttachedShit.Key(this, key.getProp()), shitToShit.get(key));
            }
        }
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public final String getDebugTag() {
        if (debugTag == null) {
            throw new IllegalStateException("Would you be so fucking nice to call initDebugTag() in " + getClass());
        }
        return debugTag;
    }

    protected void initDebugTag() {
        debugTag = PhotlincDebugGlobal.INSTANCE.nextDebugTag();
        PhotlincDebugGlobal.INSTANCE.taggedShitCreated(this);
    }
}



