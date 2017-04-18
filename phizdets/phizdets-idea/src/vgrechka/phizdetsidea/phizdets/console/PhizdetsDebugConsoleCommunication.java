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
package vgrechka.phizdetsidea.phizdets.console;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import com.intellij.util.Function;
import vgrechka.phizdetsidea.phizdets.console.pydev.AbstractConsoleCommunication;
import vgrechka.phizdetsidea.phizdets.console.pydev.InterpreterResponse;
import vgrechka.phizdetsidea.phizdets.console.pydev.PydevCompletionVariant;
import vgrechka.phizdetsidea.phizdets.debugger.PhiDebugProcess;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebuggerException;
import vgrechka.phizdetsidea.phizdets.debugger.pydev.PyDebugCallback;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author traff
 */
public class PhizdetsDebugConsoleCommunication extends AbstractConsoleCommunication {
  private static final Logger LOG = Logger.getInstance("#vgrechka.phizdetsidea.phizdets.console.pydev.PhizdetsDebugConsoleCommunication");
  private final PhiDebugProcess myDebugProcess;
  private boolean myNeedsMore = false;

  public PhizdetsDebugConsoleCommunication(Project project, PhiDebugProcess debugProcess) {
    super(project);
    myDebugProcess = debugProcess;
  }

  @NotNull
  @Override
  public List<PydevCompletionVariant> getCompletions(String text, String actualToken) throws Exception {
    return myDebugProcess.getCompletions(actualToken);
  }

  @Override
  public String getDescription(String refExpression) throws Exception {
    return myDebugProcess.getDescription(refExpression);
  }

  @Override
  public boolean isWaitingForInput() {
    return waitingForInput;
  }

  @Override
  public boolean needsMore() {
    return myNeedsMore;
  }

  @Override
  public boolean isExecuting() {
    return false;
  }

  protected void exec(final ConsoleCodeFragment command, final PyDebugCallback<Pair<String, Boolean>> callback) {
    myDebugProcess.consoleExec(command.getText(), new PyDebugCallback<String>() {
      @Override
      public void ok(String value) {
        callback.ok(parseExecResponseString(value));
      }

      @Override
      public void error(PyDebuggerException exception) {
        callback.error(exception);
      }
    });
  }

  public void execInterpreter(ConsoleCodeFragment code, final Function<InterpreterResponse, Object> callback) {
    if (waitingForInput) {
      final OutputStream processInput = myDebugProcess.getProcessHandler().getProcessInput();
      if (processInput != null) {
        try {
          final Charset defaultCharset = EncodingProjectManager.getInstance(myDebugProcess.getProject()).getDefaultCharset();
          processInput.write((code.getText()).getBytes(defaultCharset));
          processInput.flush();

        }
        catch (IOException e) {
          LOG.error(e.getMessage());
        }
      }
      myNeedsMore = false;
      waitingForInput = false;
      notifyCommandExecuted(waitingForInput);

    }
    else {

      exec(new ConsoleCodeFragment(code.getText(), false), new PyDebugCallback<Pair<String, Boolean>>() {
        @Override
        public void ok(Pair<String, Boolean> executed) {
          boolean more = executed.second;
          myNeedsMore = more;
          notifyCommandExecuted(more);
          callback.fun(new InterpreterResponse(more, isWaitingForInput()));

        }

        @Override
        public void error(PyDebuggerException exception) {
          myNeedsMore = false;
          notifyCommandExecuted(false);
          callback.fun(new InterpreterResponse(false, isWaitingForInput()));
        }
      });
    }
  }

  @Override
  public void notifyInputRequested() {
    waitingForInput = true;
    super.notifyInputRequested();
  }


  @Override
  public void interrupt() {
    throw new UnsupportedOperationException();
  }

  public boolean isSuspended() {
    return myDebugProcess.getSession().isSuspended();
  }
}
