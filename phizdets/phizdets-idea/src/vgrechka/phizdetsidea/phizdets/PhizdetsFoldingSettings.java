/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "PhizdetsFoldingSettings",
  storages = @Storage("editor.codeinsight.xml")
)
public class PhizdetsFoldingSettings implements PersistentStateComponent<PhizdetsFoldingSettings> {
  public boolean COLLAPSE_LONG_STRINGS;
  public boolean COLLAPSE_LONG_COLLECTIONS;
  public boolean COLLAPSE_SEQUENTIAL_COMMENTS;

  @Nullable
  @Override
  public PhizdetsFoldingSettings getState() {
    return this;
  }

  @NotNull
  public static PhizdetsFoldingSettings getInstance() {
    return ServiceManager.getService(PhizdetsFoldingSettings.class);
  }

  @Override
  public void loadState(PhizdetsFoldingSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean isCollapseLongStrings() {
    return COLLAPSE_LONG_STRINGS;
  }

  public boolean isCollapseLongCollections() {
    return COLLAPSE_LONG_COLLECTIONS;
  }

  public boolean isCollapseSequentialComments() {
    return COLLAPSE_SEQUENTIAL_COMMENTS;
  }

}