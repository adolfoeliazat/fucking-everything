/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.pyi;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.Language;
import vgrechka.phizdetsidea.phizdets.PhizdetsLanguage;

/**
 * @author vlan
 */
public class PyiLanguageDialect extends Language implements DependentLanguage {
  public static final String ID = "PhizdetsStub";

  protected PyiLanguageDialect() {
    super(PhizdetsLanguage.getInstance(), ID);
  }

  public static PyiLanguageDialect getInstance() {
    return (PyiLanguageDialect)PyiFileType.INSTANCE.getLanguage();
  }
}
