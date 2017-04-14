package vgrechka.phizdetsidea.phizdets.debugger.pydev;

/**
 * @author traff
 */
public class SuspendCommand extends AbstractThreadCommand {
  protected SuspendCommand(final RemoteDebugger debugger, final String threadId) {
    super(debugger, SUSPEND_THREAD, threadId);
  }
}
