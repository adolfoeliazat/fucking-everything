/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;

/**
 * @author yole
 */
public class OverwriteEqualsInsertHandler implements InsertHandler<LookupElement> {
  public static OverwriteEqualsInsertHandler INSTANCE = new OverwriteEqualsInsertHandler();

  @Override
  public void handleInsert(InsertionContext context, LookupElement item) {
    if (context.getCompletionChar() != Lookup.REPLACE_SELECT_CHAR) {
      return;
    }
    Document doc = context.getDocument();
    int tailOffset = context.getTailOffset();
    if (tailOffset < doc.getCharsSequence().length() && doc.getCharsSequence().charAt(tailOffset) == '=') {
      doc.deleteString(tailOffset, tailOffset+1);
    }
  }
}