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
package vgrechka.phizdetsidea.commandInterface.commandLine;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import org.jetbrains.annotations.NotNull;

/**
 * Manipulator to support reference injection. Will fail with out of it.
 *
 * @author Ilya.Kazakevich
 */
public final class CommandLineElementManipulator extends AbstractElementManipulator<CommandLineElement> {

  @Override
  public CommandLineElement handleContentChange(@NotNull final CommandLineElement element,
                                                @NotNull final TextRange range,
                                                final String newContent) {
    return null;
  }
}
