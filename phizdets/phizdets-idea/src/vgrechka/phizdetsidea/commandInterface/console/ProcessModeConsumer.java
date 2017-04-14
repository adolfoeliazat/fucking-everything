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
package vgrechka.phizdetsidea.commandInterface.console;

import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * Supports {@link CommandConsole} in "process-mode"
 * Delegates console streams to process
 *
 * @author Ilya.Kazakevich
 */
final class ProcessModeConsumer implements Consumer<String> {
  @NotNull
  private final ProcessBackedConsoleExecuteActionHandler myHandler;

  ProcessModeConsumer(@NotNull final ProcessHandler processHandler) {
    myHandler = new ProcessBackedConsoleExecuteActionHandler(processHandler, true);
  }

  @Override
  public void consume(final String t) {
    myHandler.processLine(t);
  }
}
