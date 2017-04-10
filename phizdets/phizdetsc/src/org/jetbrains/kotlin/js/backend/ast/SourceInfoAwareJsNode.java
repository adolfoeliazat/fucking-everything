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

import vgrechka.DebugKt;

public abstract class SourceInfoAwareJsNode extends AbstractNode {
    private Object source;

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object info) {
        source = info;
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
    }

    @Override
    public JsNode source(Object info) {
        source = info;
        if (source != null) {
            { // @debug-5
//                if (this instanceof JsInvocation) {
//                    JsInvocation node = (JsInvocation) this;
//                    "break on me".toString();
//                }
//
//                if (this.toString().contains("QUERY_STRING")) {
//                    "break on me".toString();
//                }
            }

            DebugKt.debug_attachShit(source, "jsNode", this);
        }
        return this;
    }
}