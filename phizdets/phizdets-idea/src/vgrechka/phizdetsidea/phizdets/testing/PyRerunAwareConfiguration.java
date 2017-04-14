/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.testing;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@link AbstractPhizdetsTestRunConfiguration} that handles failed test rerun by itself
 *
 * @author Ilya.Kazakevich
 */
@FunctionalInterface
public interface PyRerunAwareConfiguration {

  /**
   * Lunched each time user clicks "rerun". Must return new test specs.
   */
  @NotNull
  List<String> getTestSpecsForRerun(@NotNull GlobalSearchScope scope, @NotNull List<Pair<Location<?>, AbstractTestProxy>> locations);
}
