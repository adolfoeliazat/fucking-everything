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
package vgrechka.phizdetsidea.phizdets.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.JythonSdkFlavor;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.PhizdetsSdkFlavor;

/**
 * @author traff
 */
public class PhizdetsProcessRunner {
  private PhizdetsProcessRunner() {
  }

  public static ProcessHandler createProcess(GeneralCommandLine commandLine, boolean softKillOnWin) throws ExecutionException {
    if (PhizdetsSdkFlavor.getFlavor(commandLine.getExePath()) instanceof JythonSdkFlavor) {
      return JythonProcessHandler.createProcessHandler(commandLine);
    }
    else {

      return new PhizdetsProcessHandler(commandLine, softKillOnWin);
    }
  }

  public static ProcessHandler createProcess(GeneralCommandLine commandLine) throws ExecutionException {
    return createProcess(commandLine, PhizdetsProcessHandler.SOFT_KILL_ON_WIN);
  }

  public static ProcessHandler createProcessHandlingCtrlC(GeneralCommandLine commandLine) throws ExecutionException {
    return createProcess(commandLine, true);
  }
}
