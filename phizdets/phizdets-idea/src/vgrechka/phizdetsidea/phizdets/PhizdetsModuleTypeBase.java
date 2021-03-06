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
package vgrechka.phizdetsidea.phizdets;

import com.intellij.facet.FacetType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;

/**
 * @author yole
 */
public abstract class PhizdetsModuleTypeBase<T extends ModuleBuilder> extends ModuleType<T> {
  public static ModuleType getInstance() {
    return ModuleTypeManager.getInstance().findByID(PYTHON_MODULE);
  }

  @NonNls public static final String PYTHON_MODULE = "PYTHON_MODULE";

  protected PhizdetsModuleTypeBase() {
    super(PYTHON_MODULE);
  }

  @NotNull
  public String getName() {
    return "Phizdets Module";
  }

  @NotNull
  public String getDescription() {
    String basicDescription = "Phizdets modules are used for developing <b>Phizdets</b> applications.";
    FacetType[] facetTypes = Extensions.getExtensions(FacetType.EP_NAME);
    for (FacetType type : facetTypes) {
      if (type.getId().toString().equalsIgnoreCase("django")) {
        return basicDescription + " Supported technologies include <b>Django, Google App Engine, Mako, Jinja2</b> and others.";
      }
    }
    return basicDescription;
  }

  public Icon getBigIcon() {
    return PhizdetsIcons.Phizdets.Phizdets_24;
  }

  public Icon getNodeIcon(final boolean isOpened) {
    return PhizdetsIcons.Phizdets.PhizdetsClosed;
  }

  @Override
  public boolean isMarkInnerSupportedFor(JpsModuleSourceRootType type) {
    return type == JavaSourceRootType.SOURCE;
  }
}
