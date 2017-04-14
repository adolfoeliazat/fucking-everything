package vgrechka.phizdetsidea.phizdets.debugger.pydev;

import vgrechka.phizdetsidea.phizdets.debugger.PyDebuggerException;

/**
 * @author traff
 */
public interface PyDebugCallback<T> {
  void ok(T value);

  void error(PyDebuggerException exception);
}
