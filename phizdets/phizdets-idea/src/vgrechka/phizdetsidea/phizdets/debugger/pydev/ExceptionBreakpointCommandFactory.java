package vgrechka.phizdetsidea.phizdets.debugger.pydev;

/**
 * @author traff
 */
public interface ExceptionBreakpointCommandFactory {
  ExceptionBreakpointCommand createAddCommand(RemoteDebugger debugger);

  ExceptionBreakpointCommand createRemoveCommand(RemoteDebugger debugger);
}
