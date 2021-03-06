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
package vgrechka.phizdetsidea.phizdets.formatter;

import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import vgrechka.phizdetsidea.phizdets.PhizdetsLanguage;

/**
 * @author yole
 */
public class PyBlockContext {
  private final CommonCodeStyleSettings mySettings;
  private final PyCodeStyleSettings myPySettings;
  private final SpacingBuilder mySpacingBuilder;
  private final FormattingMode myMode;

  public PyBlockContext(CodeStyleSettings settings, SpacingBuilder builder, FormattingMode mode) {
    mySettings = settings.getCommonSettings(PhizdetsLanguage.getInstance());
    myPySettings = settings.getCustomSettings(PyCodeStyleSettings.class);
    mySpacingBuilder = builder;
    myMode = mode;
  }

  public CommonCodeStyleSettings getSettings() {
    return mySettings;
  }

  public PyCodeStyleSettings getPySettings() {
    return myPySettings;
  }

  public SpacingBuilder getSpacingBuilder() {
    return mySpacingBuilder;
  }

  public FormattingMode getMode() {
    return myMode;
  }
}
