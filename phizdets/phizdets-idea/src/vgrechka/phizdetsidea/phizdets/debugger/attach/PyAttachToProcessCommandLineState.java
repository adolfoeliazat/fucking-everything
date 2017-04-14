package vgrechka.phizdetsidea.phizdets.debugger.attach;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import vgrechka.phizdetsidea.phizdets.PhizdetsHelper;
import vgrechka.phizdetsidea.phizdets.debugger.PyRemoteDebugProcess;
import vgrechka.phizdetsidea.phizdets.debugger.PyRemoteDebugProcessAware;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsConfigurationType;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsRunConfiguration;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsScriptCommandLineState;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * @author traff
 */
public class PyAttachToProcessCommandLineState extends PhizdetsScriptCommandLineState {


  private PyAttachToProcessCommandLineState(PhizdetsRunConfiguration runConfiguration,
                                            ExecutionEnvironment env) {
    super(runConfiguration, env);
  }

  public static PyAttachToProcessCommandLineState create(@NotNull Project project, @NotNull String sdkPath, int port, int pid)
    throws ExecutionException {
    PhizdetsRunConfiguration conf =
      (PhizdetsRunConfiguration)PhizdetsConfigurationType.getInstance().getFactory().createTemplateConfiguration(project);
    conf.setScriptName(PhizdetsHelper.ATTACH_DEBUGGER.asParamString());
    conf.setSdkHome(sdkPath);
    conf.setScriptParameters("--port " + port + " --pid " + pid);

    ExecutionEnvironment env =
      ExecutionEnvironmentBuilder.create(project, DefaultDebugExecutor.getDebugExecutorInstance(), conf).build();


    return new PyAttachToProcessCommandLineState(conf, env);
  }


  @Override
  protected ProcessHandler doCreateProcess(GeneralCommandLine commandLine) throws ExecutionException {
    ProcessHandler handler = super.doCreateProcess(commandLine);

    return new PyRemoteDebugProcessHandler(handler);
  }

  public static class PyRemoteDebugProcessHandler extends ProcessHandler implements PyRemoteDebugProcessAware {
    private final ProcessHandler myHandler;
    private PyRemoteDebugProcess myProcess = null;

    public PyRemoteDebugProcessHandler(ProcessHandler handler) {
      myHandler = handler;
      myHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          PyRemoteDebugProcessHandler.this.notifyTextAvailable(event.getText(), outputType);
        }
      });
    }

    @Override
    public void startNotify() {
      super.startNotify();
      myHandler.startNotify();
    }

    @Override
    protected void destroyProcessImpl() {
      if (myProcess != null) {
        myProcess.stop();
      }
      detachProcessImpl();
    }

    @Override
    protected void detachProcessImpl() {
      notifyProcessTerminated(0);
      notifyTextAvailable("Server stopped.\n", ProcessOutputTypes.SYSTEM);
    }

    @Override
    public boolean detachIsDefault() {
      return false;
    }

    @Override
    public OutputStream getProcessInput() {
      return null;
    }

    public void setRemoteDebugProcess(PyRemoteDebugProcess process) {
      myProcess = process;
    }
  }
}
